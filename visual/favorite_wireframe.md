# Favorite Screen — 와이어프레임
> **시안 B — Material You (Purple)** 기반  
> Room DB에 URI로 즐겨찾기 마킹된 미디어 목록 표시

---

## 레이아웃 — 즐겨찾기 항목 있음 (3단 그리드)

```
┌───────────────────────────────┐  ← 360dp 기준
│ 9:41               ▲ ▶ ⬛    │  StatusBar
├───────────────────────────────┤
│ ← 즐겨찾기               ⋮   │  TopAppBar (Small)
├───────────────────────────────┤
│ ┌───────┐ ┌───────┐ ┌───────┐ │
│ │     ♥ │ │  ▶  ♥ │ │     ♥ │ │  LazyVerticalGrid 3단
│ │       │ │ 0:42  │ │       │ │  (즐겨찾기 아이콘 오버레이)
│ └───────┘ └───────┘ └───────┘ │
│ ┌───────┐ ┌───────┐ ┌───────┐ │
│ │     ♥ │ │    ♥  │ │     ♥ │ │
│ │       │ │       │ │       │ │
│ └───────┘ └───────┘ └───────┘ │
│ ┌───────┐ ┌───────┐ ┌───────┐ │
│ │     ♥ │ │    ♥  │ │     ♥ │ │
│ │       │ │       │ │       │ │
│ └───────┘ └───────┘ └───────┘ │
│              ...               │
└───────────────────────────────┘
```

> 즐겨찾기 아이콘(♥)은 우상단에 작게 오버레이 표시 (primary 색상)

---

## 레이아웃 — 즐겨찾기 항목 없음 (Empty State)

```
┌───────────────────────────────┐
│ 9:41               ▲ ▶ ⬛    │
├───────────────────────────────┤
│ ← 즐겨찾기               ⋮   │
├───────────────────────────────┤
│                               │
│                               │
│                               │
│             ♡                 │  아이콘 (64dp, onSurfaceVariant)
│                               │
│       즐겨찾기가 없어요         │  titleMedium
│                               │
│  사진이나 동영상을 길게 눌러   │  bodyMedium (onSurfaceVariant)
│  즐겨찾기에 추가해 보세요.     │
│                               │
│                               │
│                               │
└───────────────────────────────┘
```

---

## 레이아웃 — 다중 선택 모드

```
┌───────────────────────────────┐
│ 9:41               ▲ ▶ ⬛    │
├───────────────────────────────┤
│ ✕   2개 선택됨                │  SelectionTopBar
├───────────────────────────────┤
│ ┌───────┐ ┌───────┐ ┌───────┐ │
│ │ ✓ 🟣  │ │ ♥     │ │ ✓ 🟣  │ │
│ │       │ │       │ │       │ │
│ └───────┘ └───────┘ └───────┘ │
├───────────────────────────────┤
│  ♡ 즐겨찾기 해제  공유   🗑 삭제│  SelectionActionBar
└───────────────────────────────┘
```

> 즐겨찾기 화면에서는 액션 바의 즐겨찾기 버튼이 "즐겨찾기 해제"로 표시됨

---

## Composable 구조

```
FavoriteScreen(viewModel: FavoriteViewModel)
└── Scaffold(
    topBar = {
        TopAppBar(
            navigationIcon = { IconButton(Back) },
            title = { Text("즐겨찾기") },
            actions = { IconButton { Icon(MoreVert) } }
        )
    }
) { padding ->
    val mediaItems = viewModel.favoriteItems.collectAsState()

    if (mediaItems.isEmpty()) {
        FavoriteEmptyState()
    } else {
        if (isSelectionMode) {
            SelectionTopBar(count = selectedCount, onClose = ::exitSelection)
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(gridColumnCount)
        ) {
            items(mediaItems, key = { it.uri }) { media ->
                MediaThumbnail(
                    media = media,
                    showFavoriteBadge = true,     // ♥ 오버레이 항상 표시
                    isSelected = media in selectedSet,
                    onTap = { handleTap(media) },
                    onLongPress = { enterSelection(media) }
                )
            }
        }
        if (isSelectionMode) {
            SelectionActionBar(
                showRemoveFavorite = true,        // "즐겨찾기 해제"
                onRemoveFavorite = viewModel::removeFromFavorite,
                onShare = viewModel::share,
                onDelete = viewModel::delete
            )
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
| 즐겨찾기 배지 (♥) | colorScheme.primary | #6750A4 |
| Empty State 아이콘 | colorScheme.onSurfaceVariant | #49454F |
| Empty State 제목 | colorScheme.onSurface | #1D1B20 |
| Empty State 설명 | colorScheme.onSurfaceVariant | #49454F |

---

## 타이포

| 요소 | 스타일 | 크기 / 굵기 |
|------|--------|------------|
| AppBar 제목 | titleLarge | 22sp / 400 |
| Empty State 제목 | titleMedium | 16sp / 500 |
| Empty State 설명 | bodyMedium | 14sp / 400 |

---

## 상호작용

| 동작 | 결과 |
|------|------|
| 아이템 탭 (일반 모드) | PhotoViewScreen 이동 |
| 아이템 롱프레스 | 다중 선택 모드 진입 |
| 선택 후 "즐겨찾기 해제" | Room DB에서 URI 삭제 → 목록에서 즉시 제거 |
| 선택 후 삭제 | MediaStore.createDeleteRequest() → 목록에서 제거 |
| ← 탭 | 이전 화면 복귀 |
