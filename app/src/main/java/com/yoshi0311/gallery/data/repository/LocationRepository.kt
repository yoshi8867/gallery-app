package com.yoshi0311.gallery.data.repository

import android.content.ContentResolver
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.yoshi0311.gallery.data.model.CityGroup
import com.yoshi0311.gallery.data.model.MediaItem
import com.yoshi0311.gallery.util.ExifLocationUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.util.Collections
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class LocationRepository @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val contentResolver: ContentResolver,
    @ApplicationContext private val context: Context,
) {
    // Singleton 전용 스코프 — 앱 생명주기와 동일하게 유지
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // exifCache: null 값(GPS 없음)도 저장해야 해서 synchronizedMap 사용
    // (ConcurrentHashMap은 null 값 금지)
    private val exifCache: MutableMap<String, Pair<Double, Double>?> =
        Collections.synchronizedMap(HashMap())

    // geocodeCache: null 값 없음 → ConcurrentHashMap 유지
    private val geocodeCache = ConcurrentHashMap<String, Pair<String, String>>()

    // ── Hot StateFlow ─────────────────────────────────────────────
    // LocationScreen / MapScreen 어디서 구독하든 한 번만 계산하고 공유한다.
    // SharingStarted.Lazily: 첫 구독 시 시작, 이후 구독은 즉시 캐시 값 수신.

    private val mediaWithLocationState: StateFlow<List<MediaItem>> =
        mediaRepository.getAllMedia()
            .map { items -> readExifBatch(items.filter { !it.isVideo }) }
            .flowOn(Dispatchers.IO)
            .stateIn(scope, SharingStarted.Lazily, emptyList())

    private val locationGroupsState: StateFlow<Map<String, List<CityGroup>>> =
        mediaWithLocationState
            .map { items -> groupByCity(items) }
            .flowOn(Dispatchers.IO)
            .stateIn(scope, SharingStarted.Lazily, emptyMap())

    fun observeMediaWithLocation(): Flow<List<MediaItem>> = mediaWithLocationState
    fun observeLocationGroups(): Flow<Map<String, List<CityGroup>>> = locationGroupsState

    // ── EXIF 읽기 ────────────────────────────────────────────────

    private suspend fun readExifBatch(imageItems: List<MediaItem>): List<MediaItem> {
        val limited = imageItems.take(500)
        val semaphore = Semaphore(8)

        return coroutineScope {
            limited.map { item ->
                async(Dispatchers.IO) {
                    semaphore.withPermit {
                        val latLng = exifCache.getOrPut(item.uri.toString()) {
                            ExifLocationUtil.getLatLng(contentResolver, item.uri)
                        }
                        if (latLng != null) item.copy(latitude = latLng.first, longitude = latLng.second)
                        else null
                    }
                }
            }.awaitAll().filterNotNull()
        }
    }

    // ── 위치 그룹핑 ──────────────────────────────────────────────

    private suspend fun groupByCity(items: List<MediaItem>): Map<String, List<CityGroup>> {
        if (items.isEmpty()) return emptyMap()

        val keyToItems = items.groupBy { item ->
            val lat = Math.round(item.latitude!! * 100) / 100.0
            val lng = Math.round(item.longitude!! * 100) / 100.0
            "$lat,$lng"
        }

        val keyToName: Map<String, Pair<String, String>> = keyToItems.keys.associateWith { key ->
            geocodeCache.getOrPut(key) {
                val parts = key.split(",")
                reverseGeocode(parts[0].toDouble(), parts[1].toDouble())
            }
        }

        val grouped = mutableMapOf<Pair<String, String>, MutableList<MediaItem>>()
        for ((key, mediaItems) in keyToItems) {
            val nameKey = keyToName[key] ?: ("기타" to "알 수 없음")
            grouped.getOrPut(nameKey) { mutableListOf() }.addAll(mediaItems)
        }

        return grouped.entries
            .groupBy { it.key.first }
            .mapValues { (_, entries) ->
                entries.map { (key, mediaItems) ->
                    val sorted = mediaItems.sortedByDescending { it.dateTaken }
                    CityGroup(
                        country = key.first,
                        city = key.second,
                        coverUri = sorted.first().uri,
                        count = sorted.size,
                        items = sorted,
                    )
                }.sortedByDescending { it.count }
            }
    }

    // ── Reverse Geocoding ────────────────────────────────────────

    private suspend fun reverseGeocode(lat: Double, lng: Double): Pair<String, String> {
        if (!Geocoder.isPresent()) return "기타" to "%.2f, %.2f".format(lat, lng)

        val geocoder = Geocoder(context, Locale.getDefault())
        return withContext(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCoroutine { cont ->
                        geocoder.getFromLocation(lat, lng, 1) { addresses ->
                            cont.resume(parseAddress(addresses.firstOrNull()))
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(lat, lng, 1)
                    parseAddress(addresses?.firstOrNull())
                }
            } catch (_: Exception) {
                "기타" to "알 수 없음"
            }
        }
    }

    private fun parseAddress(address: Address?): Pair<String, String> {
        if (address == null) return "기타" to "알 수 없음"
        val country = address.countryName ?: "기타"
        val city = address.locality
            ?: address.subAdminArea
            ?: address.adminArea
            ?: "알 수 없음"
        return country to city
    }
}
