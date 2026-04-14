# Album View Screen — 와이어프레임
> **시안 B — Material You (Purple)** 기반

---

## 레이아웃 — 서랍 닫힘, 3단 그리드

```
┌───────────────────────────────┐  ← 360dp 기준
│ 9:41               ▲ ▶ ⬛    │  StatusBar
├───────────────────────────────┤
│ ← DCIM                  ⋮   │  TopAppBar (Small)
├───────────────────────────────┤
│ ┌───────┐ ┌───────┐ ┌───────┐ │
│ │       │ │ ▶     │ │       │ │
│ │       │ │ 0:42  │ │       │ │  LazyVerticalGrid 3단
│ └───────┘ └───────┘ └───────┘ │
│ ┌───────┐ ┌───────┐ ┌───────┐ │
│ │       │ │       │ │       │ │
│ └───────┘ └───────┘ └───────┘ │
│ ┌───────┐ ┌───────┐ ┌───────┐ │
│ │       │ │       │ │       │ │
│ └───────┘ └───────┘ └───────┘ │
│ ┌───────┐ ┌───────┐ ┌───────┐ │
│ │       │ │       │ │ ▶     │ │
│ │       │ │       │ │ 1:05  │ │
│ └───────┘ └───────┘ └───────┘ │
│              ...               │
├───────────────────────────────┤
│  ⊞사진   ⊟앨범   ✦스토리  ≡메뉴│  NavigationBar
└───────────────────────────────┘
```

> **서랍 열기:** 우측 엣지에서 왼쪽으로 스와이프

---

## 레이아웃 — 서랍 열림 (AnimatedVisibility)

```
┌───────────────────────────────┐
│ 9:41               ▲ ▶ ⬛    │
├───────────────────────────────┤
│ ← DCIM                  ⋮   │
├─────────────────────┬─────────┤
│ ████▌  │┌────┐ ┌────┐       │  ← 사이드 드로어(좌측에서 나옴)
│        ││    │ │ ▶  │       │    (AnimatedVisibility,
│ ┌────┐ │ └────┘ └────┘       │    slideInHorizontally)
│ │    │ │ ┌────┐ ┌────┐       │
│ ├────┤ │ │    │ │    │       │
│ │    │ │ └────┘ └────┘       │
│ ├────┤ │ ┌────┐ ┌────┐       │
│ │    │ │ │    │ │    │       │
│ └────┘ │ └────┘ └────┘       │
│  전체  │                     │
│  88장  │                     │
│       │        ...          │
├───────┴──────────────────────┤
│  ⊞사진   ⊟앨범   ✦스토리  ≡메뉴│
└───────────────────────────────┘
```

---

## 레이아웃 — 다중 선택 모드 (롱프레스)

```
┌───────────────────────────────┐
│ 9:41               ▲ ▶ ⬛    │
├───────────────────────────────┤
│ ✕   2개 선택됨                │  SelectionTopBar
├───────────────────────────────┤
│ ┌───────┐ ┌───────┐ ┌───────┐ │
│ │ ✓ 🟣  │ │       │ │ ✓ 🟣  │ │  선택 항목: primary 오버레이
│ └───────┘ └───────┘ └───────┘ │
│ ┌───────┐ ┌───────┐ ┌───────┐ │
│ │       │ │       │ │       │ │
│ └───────┘ └───────┘ └───────┘ │
├───────────────────────────────┤
│    ♡ 즐겨찾기  공유   🗑 삭제  │  SelectionActionBar
└───────────────────────────────┘
```

---

## 열 수 단계 (서랍 닫힘 / 열림)

| 상태 | 1단계 | 2단계 | 3단계 | 4단계 | 5단계 |
|------|-------|-------|-------|-------|-------|
| 서랍 닫힘 | 1.5단 | 3단 | 4단 | 7단 | 12단 |    -> 기기 화면 크기에 따라 단수 유동적으로 조정
| 서랍 열림 | 1.5단 | 2단 | 3단 | 5단 | 9단 |     -> 기기 화면 크기에 따라 단수 유동적으로 조정

> 마지막 단계(최소 축소)에서 아이템 탭 → 핀치 아웃 효과만 적용 (PhotoView 이동 없음)

---

## Composable 구조

```
AlbumViewScreen(albumId: Long, viewModel: AlbumViewViewModel)
└── Scaffold(
    topBar = {
        TopAppBar(
            navigationIcon = { IconButton(Back) },
            title = { Text(albumName) },
            actions = { IconButton { Icon(MoreVert) } }
        )
    },
    bottomBar = { GalleryNavigationBar(selected = Tab.Album) }
) { padding ->
    if (isSelectionMode) {
        SelectionTopBar(count = selectedCount, onClose = ::exitSelection)
    }
    Box {
        LazyVerticalGrid(
            columns = GridCells.Fixed(effectiveColumnCount),
            modifier = Modifier
                .fillMaxSize()
                .pinchToZoom(onZoomIn = ::decreaseColumn, onZoomOut = ::increaseColumn)
        ) {
            items(mediaItems) { media ->
                MediaThumbnail(
                    media = media,
                    isSelected = media in selectedSet,
                    onTap = { handleTap(media) },
                    onLongPress = { enterSelection(media) }
                )
            }
        }

        // 서랍 (우측 끝에서 스와이프로 열림/닫힘)
        AnimatedVisibility(
            visible = isDrawerOpen,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            AlbumDrawerPanel(
                miniThumbs = mediaItems.take(30),
                totalCount = totalCount
            )
        }
    }
    if (isSelectionMode) {
        SelectionActionBar(onFavorite, onShare, onDelete)
    }
}
```

---

## 색상

| 요소 | Material3 토큰 | Hex |
|------|----------------|-----|
| 화면 배경 | colorScheme.background | #FEF7FF |
| TopAppBar | colorScheme.surface | #FEF7FF |
| 서랍 패널 배경 | colorScheme.surfaceContainerHigh | #ECE6F0 |
| 서랍 패널 섬네일 테두리 | colorScheme.outline | #CAC4D0 |
| 서랍 개수 텍스트 | colorScheme.onSurfaceVariant | #49454F |
| 동영상 배지 배경 | rgba(0,0,0,0.65) | — |
| 선택 오버레이 | colorScheme.primary (α 40%) | — |
| NavigationBar | colorScheme.surfaceVariant | #E7E0EC |

---

## 타이포

| 요소 | 스타일 | 크기 / 굵기 |
|------|--------|------------|
| AppBar 제목 (앨범명) | titleLarge | 22sp / 400 |
| 서랍 총 개수 | bodyMedium | 14sp / 400 |
| 동영상 배지 | labelSmall | 11sp / 500 |
| SelectionTopBar 개수 | titleMedium | 16sp / 500 |

---

## 상호작용

| 동작 | 결과 |
|------|------|
| 아이템 탭 (서랍 닫힘, 최대 단수 이외) | PhotoViewScreen 이동 |
| 아이템 탭 (최소 축소 단계) | 핀치 아웃 효과만 (열 수 감소) |
| 아이템 롱프레스 | 다중 선택 모드 진입 |
| 우측 엣지 스와이프 (좌향) | 서랍 열림 (slideInHorizontally) |
| 서랍 열린 상태 우측 탭 or 스와이프 (우향) | 서랍 닫힘 |
| 핀치 아웃 | 열 수 증가 (서랍 상태에 따라 단계 상이) |
| 핀치 인 | 열 수 감소 |
| ← 탭 | AlbumListScreen 복귀 |
