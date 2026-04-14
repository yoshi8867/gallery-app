# Video Screen — 와이어프레임
> **시안 B — Material You (Purple)** 기반  
> 동영상 특수 앨범 — 기기 내 전체 동영상 필터링 표시

---

## 레이아웃 — 기본 상태 (3단 그리드)

```
┌───────────────────────────────┐  ← 360dp 기준
│ 9:41               ▲ ▶ ⬛    │  StatusBar
├───────────────────────────────┤
│ ← 동영상                 ⋮   │  TopAppBar (Small)
├───────────────────────────────┤
│ 2026년 4월 10일         8개   │  StickyHeader (날짜 섹션)
├───────────────────────────────┤
│ ┌───────┐ ┌───────┐ ┌───────┐ │
│ │ ▶     │ │ ▶     │ │ ▶     │ │
│ │ 0:42  │ │ 1:23  │ │ 0:08  │ │  LazyVerticalGrid 3단
│ └───────┘ └───────┘ └───────┘ │  (전체 아이템이 동영상)
│ ┌───────┐ ┌───────┐ ┌───────┐ │
│ │ ▶     │ │ ▶     │ │ ▶     │ │
│ │ 2:11  │ │ 0:31  │ │ 3:04  │ │
│ └───────┘ └───────┘ └───────┘ │
├───────────────────────────────┤
│ 2026년 4월 8일          3개   │
├───────────────────────────────┤
│ ┌───────┐ ┌───────┐ ┌───────┐ │
│ │ ▶     │ │ ▶     │ │ ▶     │ │
│ │ 0:55  │ │ 1:47  │ │ 0:22  │ │
│ └───────┘ └───────┘ └───────┘ │
│              ...               │
└───────────────────────────────┘
```

> NavigationBar 없음 — 메뉴 모달에서 진입하는 화면 (TopAppBar 뒤로가기로 복귀)

---

## 레이아웃 — 다중 선택 모드

```
┌───────────────────────────────┐
│ 9:41               ▲ ▶ ⬛    │
├───────────────────────────────┤
│ ✕   3개 선택됨                │  SelectionTopBar
├───────────────────────────────┤
│ ┌───────┐ ┌───────┐ ┌───────┐ │
│ │✓ ▶    │ │ ▶     │ │✓ ▶    │ │  선택 항목: primary 오버레이
│ │ 0:42  │ │ 1:23  │ │ 0:08  │ │
│ └───────┘ └───────┘ └───────┘ │
│ ┌───────┐ ┌───────┐ ┌───────┐ │
│ │ ▶     │ │✓ ▶    │ │ ▶     │ │
│ │ 2:11  │ │ 0:31  │ │ 3:04  │ │
│ └───────┘ └───────┘ └───────┘ │
├───────────────────────────────┤
│    ♡ 즐겨찾기  공유   🗑 삭제  │
└───────────────────────────────┘
```

---

## Composable 구조

```
VideoScreen(viewModel: VideoViewModel)
└── Scaffold(
    topBar = {
        TopAppBar(
            navigationIcon = { IconButton(Back) },
            title = { Text("동영상") },
            actions = { IconButton { Icon(MoreVert) } }
        )
    }
) { padding ->
    if (isSelectionMode) {
        SelectionTopBar(count = selectedCount, onClose = ::exitSelection)
    }
    LazyColumn {
        videoGroups.forEach { (date, items) ->
            stickyHeader { DateSectionHeader(date = date, count = items.size) }
            item {
                LazyVerticalGrid(columns = GridCells.Fixed(gridColumnCount)) {
                    items(items) { video ->
                        MediaThumbnail(
                            media = video,              // isVideo = true → 배지 항상 표시
                            isSelected = video in selectedSet,
                            onTap = { handleTap(video) },
                            onLongPress = { enterSelection(video) }
                        )
                    }
                }
            }
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
| StickyHeader 배경 | colorScheme.background (α 88%) | — |
| StickyHeader 날짜 | colorScheme.onSurfaceVariant | #49454F |
| 동영상 배지 배경 | rgba(0,0,0,0.65) | — |
| 동영상 배지 텍스트 | White | #FFFFFF |
| 선택 오버레이 | colorScheme.primary (α 40%) | — |
| 체크 아이콘 | colorScheme.onPrimary | #FFFFFF |

---

## 타이포

| 요소 | 스타일 | 크기 / 굵기 |
|------|--------|------------|
| AppBar 제목 | titleLarge | 22sp / 400 |
| 날짜 섹션 헤더 | titleSmall | 14sp / 600 |
| 동영상 개수 | labelMedium | 12sp / 500 |
| 동영상 배지 (재생시간) | labelSmall | 11sp / 500 |

---

## 상호작용

| 동작 | 결과 |
|------|------|
| 아이템 탭 (일반 모드) | PhotoViewScreen 이동 (동영상 재생) |
| 아이템 롱프레스 | 다중 선택 모드 진입 |
| 핀치 아웃 | 열 수 증가 (3→4→7) |
| 핀치 인 | 열 수 감소 (7→4→3) |
| ← 탭 | MenuModal / 이전 화면 복귀 |
