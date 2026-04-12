package com.yoshi0311.gallery.ui.screen.albumlist

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yoshi0311.gallery.viewmodel.AlbumListViewModel
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumListScreen(
    onNavigateToAlbum: (albumId: Long, albumName: String) -> Unit,
    onNavigateToSearch: () -> Unit,
    viewModel: AlbumListViewModel = hiltViewModel(),
) {
    val albums by viewModel.albums.collectAsStateWithLifecycle()
    val sortOrder = viewModel.sortOrder
    val columnCount = viewModel.columnCount

    var scaleAccumulator by remember { mutableStateOf(1f) }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "앨범",
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.padding(vertical = 40.dp),
                    )
                },
                actions = {
//                    IconButton(onClick = { /* TODO: 숨김 폴더 토글 드롭다운 */ }) {
//                        Icon(Icons.Default.MoreVert, contentDescription = "더보기")
//                    }
                },
                scrollBehavior = scrollBehavior,
                modifier = Modifier.padding(vertical = 40.dp),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            AlbumListHeroHeader(
                onSearch = onNavigateToSearch,
                onMore = { /* TODO: 숨김 폴더 토글 드롭다운 */ },
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(columnCount),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            var prevDistance = 0f
                            var isPinching = false

                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                val active = event.changes.filter { it.pressed }

                                if (active.size >= 2) {
                                    val p1 = active[0].position
                                    val p2 = active[1].position
                                    val dx = p2.x - p1.x
                                    val dy = p2.y - p1.y
                                    val distance = sqrt(dx * dx + dy * dy)

                                    if (!isPinching) {
                                        prevDistance = distance
                                        isPinching = true
                                    } else if (prevDistance > 0f) {
                                        val zoom = distance / prevDistance
                                        scaleAccumulator *= zoom

                                        when {
                                            scaleAccumulator > 1.3f -> {
                                                viewModel.zoomIn()
                                                scaleAccumulator = 1f
                                            }
                                            scaleAccumulator < 0.77f -> {
                                                viewModel.zoomOut()
                                                scaleAccumulator = 1f
                                            }
                                        }
                                        prevDistance = distance
                                    }
                                    event.changes.forEach { it.consume() }
                                } else {
                                    if (isPinching) {
                                        isPinching = false
                                        prevDistance = 0f
                                        scaleAccumulator = 1f
                                    }
                                    if (event.changes.all { !it.pressed }) break
                                }
                            }
                        }
                    },
            ) {
                items(albums, key = { it.id }) { album ->
                    AlbumCard(
                        album = album,
                        onClick = { onNavigateToAlbum(album.id, album.name) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AlbumListHeroHeader(
    onSearch: () -> Unit,
    onMore: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 4.dp, top = 4.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        IconButton(onClick = onSearch) {
            Icon(Icons.Default.Search, contentDescription = "검색")
        }
        IconButton(onClick = onMore) {
            Icon(Icons.Default.MoreVert, contentDescription = "더보기")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortChipRow(
    sortOrder: AlbumListViewModel.SortOrder,
    onSortChange: (AlbumListViewModel.SortOrder) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = AlbumListViewModel.SortOrder.entries
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        options.forEachIndexed { index, order ->
            SegmentedButton(
                selected = sortOrder == order,
                onClick = { onSortChange(order) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size,
                ),
                label = {
                    Text(
                        text = when (order) {
                            AlbumListViewModel.SortOrder.BY_NAME  -> "이름순"
                            AlbumListViewModel.SortOrder.BY_DATE  -> "최신순"
                            AlbumListViewModel.SortOrder.BY_COUNT -> "항목 수순"
                        },
                    )
                },
            )
        }
    }
}
