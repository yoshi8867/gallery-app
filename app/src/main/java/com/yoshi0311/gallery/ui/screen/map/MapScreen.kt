package com.yoshi0311.gallery.ui.screen.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.allowHardware
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
import com.yoshi0311.gallery.data.model.MediaItem
import com.yoshi0311.gallery.viewmodel.MapViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, MapsComposeExperimentalApi::class)
@Composable
fun MapScreen(
    cityFilter: String?,
    onBack: () -> Unit,
    onNavigateToPhoto: (Long) -> Unit,
    viewModel: MapViewModel = hiltViewModel(),
) {
    LaunchedEffect(cityFilter) {
        viewModel.setCityFilter(cityFilter)
    }

    val items by viewModel.displayItems.collectAsStateWithLifecycle()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(36.5, 127.5), 6f)
    }

    LaunchedEffect(items) {
        if (items.isNotEmpty()) {
            val bounds = LatLngBounds.Builder().apply {
                items.forEach { include(LatLng(it.latitude!!, it.longitude!!)) }
            }.build()
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 120))
        }
    }

    val clusterItems = remember(items) { items.map { MediaClusterItem(it) } }

    var selectedItem by remember { mutableStateOf<MediaItem?>(null) }
    var selectedCluster by remember { mutableStateOf<List<MediaItem>?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
        ) {
            Clustering(
                items = clusterItems,
                onClusterClick = { cluster ->
                    selectedCluster = cluster.items.map { it.mediaItem }
                    true
                },
                onClusterItemClick = { item ->
                    selectedItem = item.mediaItem
                    true
                },
                clusterContent = { cluster -> ClusterBadgeMarker(count = cluster.size) },
                clusterItemContent = { item -> ThumbnailMarker(uri = item.mediaItem.uri.toString()) },
            )
        }

        TopAppBar(
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "뒤로",
                        tint = Color.Black,
                    )
                }
            },
            title = {},
            actions = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "더보기",
                        tint = Color.Black,
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White.copy(alpha = 0.7f),
            ),
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }

    selectedItem?.let { item ->
        ModalBottomSheet(
            onDismissRequest = { selectedItem = null },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
        ) {
            SingleMediaPreview(
                item = item,
                onOpenClick = {
                    val id = item.id
                    selectedItem = null
                    onNavigateToPhoto(id)
                },
                modifier = Modifier.padding(bottom = 24.dp),
            )
        }
    }

    selectedCluster?.let { cluster ->
        ModalBottomSheet(
            onDismissRequest = { selectedCluster = null },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
        ) {
            ClusterPreview(
                items = cluster,
                onItemClick = { item ->
                    selectedCluster = null
                    onNavigateToPhoto(item.id)
                },
                modifier = Modifier.padding(bottom = 24.dp),
            )
        }
    }
}

// ── 마커 ─────────────────────────────────────────────────────────────────────

@Composable
private fun ThumbnailMarker(uri: String) {
    // 구글맵 마커는 소프트웨어 렌더링 → 하드웨어 비트맵 사용 불가
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(uri)
                .allowHardware(false)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
        )
    }
}

@Composable
private fun ClusterBadgeMarker(count: Int) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

// ── BottomSheet ───────────────────────────────────────────────────────────────

@Composable
private fun SingleMediaPreview(
    item: MediaItem,
    onOpenClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        AsyncImage(
            model = item.uri,
            contentDescription = item.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = SimpleDateFormat("yyyy년 M월 d일", Locale.KOREAN).format(Date(item.dateTaken)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = onOpenClick) {
                Text("열기")
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Outlined.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun ClusterPreview(
    items: List<MediaItem>,
    onItemClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "이 위치의 사진 (${items.size}장)",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            items(items = items.take(20)) { item ->
                AsyncImage(
                    model = item.uri,
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .padding(end = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onItemClick(item) },
                )
            }
        }
    }
}

// ── ClusterItem 래퍼 ──────────────────────────────────────────────────────────

private class MediaClusterItem(val mediaItem: MediaItem) : ClusterItem {
    override fun getPosition(): LatLng = LatLng(mediaItem.latitude!!, mediaItem.longitude!!)
    override fun getTitle(): String = mediaItem.name
    override fun getSnippet(): String? = null
    override fun getZIndex(): Float = 0f
}
