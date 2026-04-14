# Photo View Screen — 와이어프레임
> **시안 B — Material You (Purple)** 기반

---

## 레이아웃 — 사진 보기 (UI 표시 상태)

```
┌───────────────────────────────┐  ← 360dp 기준
│ 9:41               ▲ ▶ ⬛    │  StatusBar
├───────────────────────────────┤
│ ←   2026년 4월 10일     ⋮    │  TopAppBar (투명, 오버레이)
├───────────────────────────────┤
│                               │
│                               │
│                               │
│                               │
│       [ 미디어 이미지 영역 ]   │  HorizontalPager (핀치 줌·팬 가능)
│       (가로 스와이프로 전환)   │
│                               │
│                               │
│                               │
│                               │
├───────────────────────────────┤
│  ◀ ┌──┐ ┌──┐ ┌──┐ ┌──┐ ▶  │  LazyRow 썸네일 스트립 (현재 선택 강조)
│    └──┘ └──┘[└──┘]└──┘      │
├───────────────────────────────┤
│    ♡    ✏    ✨    ⇑    🗑   │  BottomActionBar
└───────────────────────────────┘
```

> **위로 드래그** → 상세정보 패널 표시 (AnchoredDraggable)

---

## 레이아웃 — UI 숨김 상태 (탭 시 전환)

```
┌───────────────────────────────┐
│                               │  StatusBar 투명
│                               │
│                               │
│                               │
│       [ 미디어 이미지 영역 ]   │
│       (몰입형 뷰)              │
│                               │
│                               │
│                               │
│                               │
│                               │
│                               │
└───────────────────────────────┘
```

---

## 레이아웃 — 상세정보 패널 (위로 드래그)

```
┌───────────────────────────────┐
│ ←   2026년 4월 10일     ⋮    │
├───────────────────────────────┤
│                               │
│     [ 미디어 (축소 표시) ]     │
│                               │
├───────────────────────────────┤
│         ─────────             │  드래그 핸들
│ 📄 파일명                     │
│   IMG_20260410_152300.jpg     │
│                               │
│ 📅 날짜                       │
│   2026년 4월 10일 오후 3:23   │
│                               │
│ 📏 크기 및 해상도              │
│   4.2 MB  /  4032 × 3024     │
│                               │
│ 📍 위치                       │
│   대한민국 서울특별시 강남구   │
│                               │
├───────────────────────────────┤
│    ♡    ✏    ✨    ⇑    🗑   │
└───────────────────────────────┘
```

---

## 레이아웃 — 동영상 재생 (ExoPlayer)

```
┌───────────────────────────────┐
│ ←   2026년 4월 10일     🔇   │  TopAppBar (뮤트 버튼으로 대체)
├───────────────────────────────┤
│                               │
│                               │
│       [ 동영상 재생 영역 ]     │  ExoPlayer Surface
│         ▶  (재생 중)          │
│    ──────●──────────────      │  SeekBar
│    0:12          1:23         │
│                               │
│                               │
├───────────────────────────────┤
│  ◀ ┌──┐ ┌──┐[┌──┐]┌──┐ ▶  │  썸네일 스트립
├───────────────────────────────┤
│    ♡    ✏    ✨    ⇑    🗑   │
└───────────────────────────────┘
```

---

## Composable 구조

```
PhotoViewScreen(
    mediaList: List<MediaItem>,
    initialIndex: Int,
    viewModel: PhotoViewViewModel
)
└── Scaffold(
    topBar = {
        AnimatedVisibility(visible = isUiVisible) {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.4f)
                ),
                navigationIcon = { IconButton(Back) },
                title = { Text(formattedDate, color = Color.White) },
                actions = { IconButton { Icon(MoreVert, tint = Color.White) } }
            )
        }
    }
) { padding ->
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // 메인 뷰어
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .clickable { toggleUiVisibility() }
        ) { page ->
            val media = mediaList[page]
            if (media.isVideo) {
                ExoPlayerView(
                    uri = media.uri,
                    isMuted = isMuted
                )
            } else {
                ZoomableImage(
                    model = media.uri,
                    onDragUp = { expandInfoPanel() }
                )
            }
        }

        // 썸네일 스트립 + 액션 바
        AnimatedVisibility(
            visible = isUiVisible,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column {
                ThumbnailStrip(
                    items = mediaList,
                    currentIndex = pagerState.currentPage,
                    onThumbnailClick = { pagerState.scrollToPage(it) }
                )
                BottomActionBar(
                    isFavorite = isFavorite,
                    onFavorite = viewModel::toggleFavorite,
                    onShare    = viewModel::share,
                    onDelete   = viewModel::delete
                )
            }
        }

        // 상세정보 패널 (AnchoredDraggable)
        InfoPanel(
            anchoredDraggableState = infoState,
            media = currentMedia
        )
    }
}
```

---

## 색상

| 요소 | Material3 토큰 | Hex |
|------|----------------|-----|
| 배경 | Black | #000000 |
| TopAppBar 배경 | Black (α 40%) | — |
| TopAppBar 텍스트·아이콘 | White | #FFFFFF |
| 썸네일 스트립 배경 | Black (α 60%) | — |
| 현재 썸네일 테두리 | colorScheme.primary | #6750A4 |
| BottomActionBar 배경 | Black (α 60%) | — |
| 액션 아이콘 (기본) | White | #FFFFFF |
| 즐겨찾기 아이콘 (활성) | colorScheme.primary | #6750A4 |
| 상세정보 패널 배경 | colorScheme.surfaceContainerHigh | #ECE6F0 |
| 상세정보 텍스트 | colorScheme.onSurface | #1D1B20 |
| 상세정보 보조 텍스트 | colorScheme.onSurfaceVariant | #49454F |
| 드래그 핸들 | colorScheme.outlineVariant | #CAC4D0 |

---

## 타이포

| 요소 | 스타일 | 크기 / 굵기 |
|------|--------|------------|
| TopAppBar 날짜 | titleMedium | 16sp / 500 |
| 상세정보 섹션 라벨 | labelLarge | 14sp / 500 |
| 상세정보 값 | bodyMedium | 14sp / 400 |
| 위치 주소 | bodySmall | 12sp / 400 |

---

## 상호작용

| 동작 | 결과 |
|------|------|
| 화면 탭 | TopAppBar·썸네일·액션바 표시/숨김 토글 |
| 좌우 스와이프 | 이전/다음 미디어로 전환 (HorizontalPager) |
| 핀치 줌·팬 | 이미지 확대/이동 (TransformableState) |
| 위로 드래그 | 상세정보 패널 확장 (AnchoredDraggable) |
| 아래로 드래그 | 상세정보 패널 축소 |
| ← 탭 | 이전 화면 복귀 |
| ♡ 탭 | 즐겨찾기 토글 (Room DB) |
| ✏ 탭 | Toast "추후 구현 예정입니다." |
| ✨ 탭 | Toast "추후 구현 예정입니다." |
| ⇑ 탭 | Android Sharesheet 호출 |
| 🗑 탭 | 삭제 확인 다이얼로그 → MediaStore.createDeleteRequest() |
| 🔇 탭 (동영상) | ExoPlayer 음소거 토글 |
