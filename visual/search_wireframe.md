# Search Screen — 와이어프레임
> **시안 B — Material You (Purple)** 기반  
> 파일명 텍스트 검색 / 위치 기반 검색 / 미디어 타입 필터

---

## 레이아웃 — 검색 전 (초기 상태)

```
┌───────────────────────────────┐  ← 360dp 기준
│ 9:41               ▲ ▶ ⬛    │  StatusBar
├───────────────────────────────┤
│ ┌───────────────────────────┐ │
│ │ ←  🔍  사진 검색...    ✕ │ │  SearchBar (활성화, 포커스)
│ └───────────────────────────┘ │
├───────────────────────────────┤
│  [전체 ▾]  [사진]  [동영상]   │  FilterChipRow
├───────────────────────────────┤
│                               │
│        최근 검색어             │  섹션 제목
│                               │
│  🕐  강남                    ✕│  최근 검색어 항목
│  🕐  도쿄                    ✕│
│  🕐  IMG_2025                ✕│
│                               │
│        [최근 검색어 모두 삭제] │  TextButton
│                               │
└───────────────────────────────┘
```

---

## 레이아웃 — 검색 중 (결과 표시)

```
┌───────────────────────────────┐
│ 9:41               ▲ ▶ ⬛    │
├───────────────────────────────┤
│ ┌───────────────────────────┐ │
│ │ ←  🔍  강남            ✕  │ │  SearchBar (입력 중)
│ └───────────────────────────┘ │
├───────────────────────────────┤
│  [전체 ▾]  [사진]  [동영상]   │  FilterChipRow
├───────────────────────────────┤
│ 검색 결과 24장                 │  결과 개수 (labelMedium)
├───────────────────────────────┤
│ ┌───────┐ ┌───────┐ ┌───────┐ │
│ │       │ │       │ │ ▶     │ │  LazyVerticalGrid 3단
│ │       │ │       │ │ 0:42  │ │
│ └───────┘ └───────┘ └───────┘ │
│ ┌───────┐ ┌───────┐ ┌───────┐ │
│ │       │ │       │ │       │ │
│ └───────┘ └───────┘ └───────┘ │
│ ┌───────┐ ┌───────┐ ┌───────┐ │
│ │       │ │       │ │       │ │
│ └───────┘ └───────┘ └───────┘ │
│              ...               │
└───────────────────────────────┘
```

---

## 레이아웃 — 검색 결과 없음

```
┌───────────────────────────────┐
│ 9:41               ▲ ▶ ⬛    │
├───────────────────────────────┤
│ ┌───────────────────────────┐ │
│ │ ←  🔍  없는검색어      ✕  │ │
│ └───────────────────────────┘ │
├───────────────────────────────┤
│  [전체 ▾]  [사진]  [동영상]   │
├───────────────────────────────┤
│                               │
│                               │
│             🔍                │  아이콘 (64dp, onSurfaceVariant)
│                               │
│      검색 결과가 없어요        │  titleMedium
│                               │
│  다른 검색어나 필터를          │  bodyMedium (onSurfaceVariant)
│  사용해 보세요.                │
│                               │
└───────────────────────────────┘
```

---

## Composable 구조

```
SearchScreen(viewModel: SearchViewModel)
└── Scaffold { padding ->
    Column {
        // SearchBar
        DockedSearchBar(
            query = query,
            onQueryChange = viewModel::onQueryChange,
            onSearch = viewModel::search,
            active = isSearchActive,
            onActiveChange = { isSearchActive = it },
            leadingIcon = {
                if (isSearchActive) IconButton(onClick = ::navigateBack) { Icon(ArrowBack) }
                else Icon(Search)
            },
            trailingIcon = {
                if (query.isNotEmpty()) IconButton(onClick = viewModel::clearQuery) { Icon(Close) }
            },
            placeholder = { Text("사진 검색...") }
        ) {
            // 최근 검색어 (active = true, query 비어있을 때)
            if (query.isEmpty()) {
                RecentSearchList(
                    items = recentSearches,
                    onItemClick = viewModel::searchFromRecent,
                    onRemove = viewModel::removeRecent,
                    onClearAll = viewModel::clearAllRecent
                )
            }
        }

        // 필터 칩 (사진 / 동영상 / 전체)
        FilterChipRow(
            selectedFilter = mediaTypeFilter,
            onFilterChange = viewModel::setMediaTypeFilter
        )

        // 결과
        when (val state = searchState) {
            is SearchState.Idle    -> { /* 초기 상태 — 최근 검색어만 표시 */ }
            is SearchState.Loading -> CircularProgressIndicator()
            is SearchState.Empty   -> SearchEmptyState()
            is SearchState.Results -> {
                Text("검색 결과 ${state.items.size}장", style = labelMedium)
                LazyVerticalGrid(columns = GridCells.Fixed(3)) {
                    items(state.items) { media ->
                        MediaThumbnail(
                            media = media,
                            onTap = { navTo(PhotoViewScreen(media)) }
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
| SearchBar 배경 | colorScheme.surfaceContainerHigh | #E8DEF8 |
| SearchBar 텍스트 | colorScheme.onSurface | #1D1B20 |
| SearchBar 플레이스홀더 | colorScheme.onSurfaceVariant | #49454F |
| 필터 칩 (선택) 배경 | colorScheme.secondaryContainer | #E8DEF8 |
| 필터 칩 (선택) 텍스트 | colorScheme.onSecondaryContainer | #1D192B |
| 필터 칩 (미선택) 테두리 | colorScheme.outline | #CAC4D0 |
| 최근 검색어 아이콘 | colorScheme.onSurfaceVariant | #49454F |
| 최근 검색어 텍스트 | colorScheme.onSurface | #1D1B20 |
| 결과 개수 텍스트 | colorScheme.onSurfaceVariant | #49454F |
| Empty State 아이콘 | colorScheme.onSurfaceVariant | #49454F |

---

## 타이포

| 요소 | 스타일 | 크기 / 굵기 |
|------|--------|------------|
| SearchBar 입력 / 플레이스홀더 | bodyLarge | 16sp / 400 |
| 필터 칩 라벨 | labelLarge | 14sp / 500 |
| 최근 검색어 섹션 제목 | titleSmall | 14sp / 600 |
| 최근 검색어 항목 | bodyMedium | 14sp / 400 |
| 결과 개수 | labelMedium | 12sp / 500 |
| Empty State 제목 | titleMedium | 16sp / 500 |
| Empty State 설명 | bodyMedium | 14sp / 400 |

---

## 상호작용

| 동작 | 결과 |
|------|------|
| SearchBar 탭 | SearchBar 활성화, 최근 검색어 표시 |
| 텍스트 입력 후 검색 | SearchRepository 쿼리 실행, 결과 표시 |
| ✕ (검색 초기화) | 쿼리 초기화, 최근 검색어로 복귀 |
| ← (뒤로가기) | SearchBar 비활성화 또는 이전 화면 복귀 |
| 최근 검색어 탭 | 해당 검색어로 즉시 검색 |
| 최근 검색어 ✕ | 해당 항목 삭제 |
| "모두 삭제" 탭 | 최근 검색어 전체 삭제 확인 다이얼로그 |
| 필터 칩 탭 | 미디어 타입 필터 변경, 결과 갱신 |
| 결과 아이템 탭 | PhotoViewScreen 이동 |
