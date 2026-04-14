# Photo Main Screen — 와이어프레임
> **시안 B — Material You (Purple)** 기반

---

## 레이아웃 — 기본 상태 (3단 그리드, TopAppBar 펼침)

```
┌───────────────────────────────┐  ← 360dp 기준
│ 9:41               ▲ ▶ ⬛    │  StatusBar
├───────────────────────────────┤
│                      🔍   ⋮  │
│ 갤러리                        │  LargeTopAppBar (Expanded)
│ ┌───────────────────────────┐ │
│ │ 🔍  사진 검색...          │ │  SearchBar (secondaryContainer)
│ └───────────────────────────┘ │
├───────────────────────────────┤
│ 2026년 4월 10일         12장  │  StickyHeader
├───────────────────────────────┤
│ ┌───────┐ ┌───────┐ ┌───────┐ │
│ │       │ │ ▶     │ │       │ │
│ │       │ │ 0:42  │ │       │ │  LazyVerticalGrid 3단
│ └───────┘ └───────┘ └───────┘ │
│ ┌───────┐ ┌───────┐ ┌───────┐ │
│ │       │ │       │ │       │ │
│ └───────┘ └───────┘ └───────┘ │
│ ┌───────┐ ┌───────┐ ┌───────┐ │
│ │       │ │ ▶     │ │       │ │
│ │       │ │ 1:23  │ │       │ │
│ └───────┘ └───────┘ └───────┘ │
├───────────────────────────────┤
│ 2026년 4월 8일           6장  │  StickyHeader
├───────────────────────────────┤
│ ┌───────┐ ┌───────┐ ┌───────┐ │
│ │       │ │       │ │       │ │
│ └───────┘ └───────┘ └───────┘ │
├───────────────────────────────┤
│  ⊞사진    ⊟앨범   ✦스토리 ≡메뉴 │  NavigationBar
└───────────────────────────────┘
```

---

## 레이아웃 — 스크롤 후 (TopAppBar 축소)

```
┌───────────────────────────────┐
│ 9:41               ▲ ▶ ⬛    │
├───────────────────────────────┤
│ 갤러리               🔍   ⋮  │  SmallTopAppBar (collapsed)
├───────────────────────────────┤
│ 2026년 4월 10일         12장  │  StickyHeader (고정)
├───────────────────────────────┤
│ ┌───────┐ ┌───────┐ ┌───────┐ │
│ │       │ │       │ │       │ │
│ └───────┘ └───────┘ └───────┘ │
│           ...                  │
```

---

## 레이아웃 — 다중 선택 모드 (롱프레스)

```
┌───────────────────────────────┐
│ 9:41               ▲ ▶ ⬛    │
├───────────────────────────────┤
│ ✕   3개 선택됨                │  SelectionTopBar (surface)
├───────────────────────────────┤
│ 2026년 4월 10일         12장  │
├───────────────────────────────┤
│ ┌───────┐ ┌───────┐ ┌───────┐ │
│ │ ✓ 🟣  │ │       │ │ ✓ 🟣  │ │  선택: primary 체크 + dim 오버레이
│ └───────┘ └───────┘ └───────┘ │
│ ┌───────┐ ┌───────┐ ┌───────┐ │
│ │       │ │ ✓ 🟣  │ │       │ │
│ └───────┘ └───────┘ └───────┘ │
├───────────────────────────────┤
│    ♡ 즐겨찾기  공유   🗑 삭제  │  SelectionActionBar
└───────────────────────────────┘
```

---

## Composable 구조

```
PhotoMainScreen(viewModel: PhotoMainViewModel)
└── Scaffold(
    topBar = {
        LargeTopAppBar(
            title = { Text("갤러리") },
            scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
            actions = {
                IconButton(onClick = { navTo(SearchScreen) }) { Icon(Search) }
                IconButton(onClick = { showDropdown() })   { Icon(MoreVert) }
            }
        )
    },
    bottomBar = { GalleryNavigationBar(selected = Tab.Photo) }
) { padding ->
    if (isSelectionMode) {
        SelectionTopBar(count = selectedCount, onClose = ::exitSelection)
        SelectionActionBar(onFavorite, onShare, onDelete)
    }
    LazyColumn(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        // SearchBar (TopAppBar 하단에 고정)
        item { SearchBar(onClick = { navTo(SearchScreen) }) }

        mediaGroups.forEach { (date, items) ->
            stickyHeader { DateSectionHeader(date = date, count = items.size) }
            item {
                LazyVerticalGrid(columns = GridCells.Fixed(gridColumnCount)) {
                    items(items) { media ->
                        MediaThumbnail(
                            media = media,
                            isSelected = media in selectedSet,
                            isSelectionMode = isSelectionMode,
                            onTap = { handleTap(media) },      // 단수 탭: PhotoView or 핀치 아웃
                            onLongPress = { enterSelection(media) }
                        )
                    }
                }
            }
        }
    }
}
```

---

## 색상

| 요소 | Material3 토큰 | Hex |
|------|----------------|-----|
| 화면 배경 | colorScheme.background | #FEF7FF |
| TopAppBar | colorScheme.surface | #FEF7FF |
| 검색 바 배경 | colorScheme.secondaryContainer | #E8DEF8 |
| 검색 바 텍스트 | colorScheme.onSecondaryContainer | #1D192B |
| StickyHeader 배경 | colorScheme.background (α 88%) | — |
| StickyHeader 날짜 텍스트 | colorScheme.onSurfaceVariant | #49454F |
| StickyHeader 개수 텍스트 | colorScheme.onSurfaceVariant | #49454F |
| 동영상 배지 배경 | rgba(0,0,0,0.65) | — |
| 동영상 배지 텍스트 | White | #FFFFFF |
| 그리드 gap | — | 2dp |
| NavigationBar 배경 | colorScheme.surfaceVariant | #E7E0EC |
| 활성 탭 아이콘 | colorScheme.primary | #6750A4 |
| 활성 탭 pill | colorScheme.secondaryContainer | #D0BCFF |
| 비활성 탭 아이콘 | colorScheme.onSurfaceVariant | #49454F |
| SelectionTopBar | colorScheme.surface | #FEF7FF |
| 체크 오버레이 | colorScheme.primary (α 40%) | — |
| 체크 아이콘 | colorScheme.onPrimary | #FFFFFF |

---

## 타이포

| 요소 | 스타일 | 크기 / 굵기 |
|------|--------|------------|
| AppBar 제목 (expanded) | headlineMedium | 28sp / 400 |
| AppBar 제목 (collapsed) | titleLarge | 22sp / 400 |
| 날짜 섹션 헤더 | titleSmall | 14sp / 600 |
| 미디어 개수 | labelMedium | 12sp / 500 |
| 동영상 배지 | labelSmall | 11sp / 500 |
| NavigationBar 탭 라벨 | labelSmall | 12sp / 400 |
| SelectionTopBar 개수 | titleMedium | 16sp / 500 |

---

## 상호작용

| 동작 | 결과 |
|------|------|
| 아이템 탭 (7단 이하, 일반 모드) | PhotoViewScreen 이동 |
| 아이템 탭 (11단) | 7단으로 핀치 아웃 효과 (animateIntAsState) |
| 아이템 탭 (20단) | 11단으로 핀치 아웃 효과 |
| 아이템 롱프레스 | 다중 선택 모드 진입 (scale 0.9 + 체크 오버레이 fadeIn) |
| 핀치 아웃 | 열 수 단계 감소: 20→11→7→4→3→1.5 |
| 핀치 인 | 열 수 단계 증가: 1.5→3→4→7→11→20 |
| 검색 바 탭 | SearchScreen 이동 |
| 검색 아이콘 탭 | SearchScreen 이동 |
| 스크롤 다운 | LargeTopAppBar → SmallTopAppBar 축소 |
| 스크롤 업 | SmallTopAppBar → LargeTopAppBar 확장 |
| 선택 모드: 즐겨찾기 | 선택 항목 Room DB 즐겨찾기 추가/제거 |
| 선택 모드: 공유 | Android Sharesheet 호출 |
| 선택 모드: 삭제 | MediaStore.createDeleteRequest() 다이얼로그 |
| 선택 모드: ✕ | 선택 모드 종료, NavigationBar 복원 |
