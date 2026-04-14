# Recents Screen — 와이어프레임
> **시안 B — Material You (Purple)** 기반  
> 최근 추가된 미디어 필터 (30일 / 전체 기간 토글)

---

## 레이아웃 — 기본 상태 (30일, 3단 그리드)

```
┌───────────────────────────────┐  ← 360dp 기준
│ 9:41               ▲ ▶ ⬛    │  StatusBar
├───────────────────────────────┤
│ ← 최근 항목              ⋮   │  TopAppBar (Small)
├───────────────────────────────┤
│  ┌──────────────┐ ┌──────────┐│  FilterChipRow
│  │ ● 최근 30일  │ │ 전체 기간 ││  (SingleChoice)
│  └──────────────┘ └──────────┘│
├───────────────────────────────┤
│ 오늘                    6장   │  StickyHeader
├───────────────────────────────┤
│ ┌───────┐ ┌───────┐ ┌───────┐ │
│ │       │ │ ▶     │ │       │ │  LazyVerticalGrid 3단
│ │       │ │ 0:42  │ │       │ │  DATE_ADDED 내림차순 정렬
│ └───────┘ └───────┘ └───────┘ │
│ ┌───────┐ ┌───────┐ ┌───────┐ │
│ │       │ │       │ │       │ │
│ └───────┘ └───────┘ └───────┘ │
├───────────────────────────────┤
│ 어제                    3장   │  StickyHeader
├───────────────────────────────┤
│ ┌───────┐ ┌───────┐ ┌───────┐ │
│ │       │ │       │ │       │ │
│ └───────┘ └───────┘ └───────┘ │
│              ...               │
└───────────────────────────────┘
```

---

## 레이아웃 — 전체 기간 필터

```
┌───────────────────────────────┐
│ 9:41               ▲ ▶ ⬛    │
├───────────────────────────────┤
│ ← 최근 항목              ⋮   │
├───────────────────────────────┤
│  ┌──────────────┐ ┌──────────┐│
│  │  최근 30일   │ │● 전체 기간││
│  └──────────────┘ └──────────┘│
├───────────────────────────────┤
│ 2026년 4월 10일        6장   │
├───────────────────────────────┤
│ ┌───────┐ ┌───────┐ ┌───────┐ │
│ │       │ │       │ │       │ │
│ └───────┘ └───────┘ └───────┘ │
├───────────────────────────────┤
│ 2026년 4월 8일         3장   │
├───────────────────────────────┤
│ ┌───────┐ ┌───────┐ ┌───────┐ │
│ │       │ │       │ │       │ │
│ └───────┘ └───────┘ └───────┘ │
│              ...               │
└───────────────────────────────┘
```

---

## Composable 구조

```
RecentsScreen(viewModel: RecentsViewModel)
└── Scaffold(
    topBar = {
        TopAppBar(
            navigationIcon = { IconButton(Back) },
            title = { Text("최근 항목") },
            actions = { IconButton { Icon(MoreVert) } }
        )
    }
) { padding ->
    Column {
        // 필터 칩
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            SegmentedButton(
                selected = filter == Filter.Recent30,
                onClick = { viewModel.setFilter(Filter.Recent30) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) { Text("최근 30일") }
            SegmentedButton(
                selected = filter == Filter.All,
                onClick = { viewModel.setFilter(Filter.All) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) { Text("전체 기간") }
        }

        LazyColumn {
            mediaGroups.forEach { (date, items) ->
                stickyHeader { DateSectionHeader(date = date, count = items.size) }
                item {
                    LazyVerticalGrid(columns = GridCells.Fixed(gridColumnCount)) {
                        items(items) { media ->
                            MediaThumbnail(
                                media = media,
                                onTap = { navTo(PhotoViewScreen(media)) },
                                onLongPress = { enterSelection(media) }
                            )
                        }
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
| 필터 칩 (선택) 배경 | colorScheme.secondaryContainer | #E8DEF8 |
| 필터 칩 (선택) 텍스트 | colorScheme.onSecondaryContainer | #1D192B |
| 필터 칩 (미선택) 배경 | colorScheme.surface | #FEF7FF |
| 필터 칩 테두리 | colorScheme.outline | #CAC4D0 |
| StickyHeader 배경 | colorScheme.background (α 88%) | — |
| StickyHeader 날짜 | colorScheme.onSurfaceVariant | #49454F |

---

## 타이포

| 요소 | 스타일 | 크기 / 굵기 |
|------|--------|------------|
| AppBar 제목 | titleLarge | 22sp / 400 |
| 필터 칩 라벨 | labelLarge | 14sp / 500 |
| 날짜 섹션 헤더 | titleSmall | 14sp / 600 |
| 미디어 개수 | labelMedium | 12sp / 500 |

---

## 상호작용

| 동작 | 결과 |
|------|------|
| "최근 30일" 칩 탭 | 30일 이내 DATE_ADDED 필터로 재쿼리 |
| "전체 기간" 칩 탭 | 전체 DATE_ADDED 기준 표시 |
| 아이템 탭 | PhotoViewScreen 이동 |
| 아이템 롱프레스 | 다중 선택 모드 진입 |
| ← 탭 | 이전 화면 복귀 |
