# Location Screen — 와이어프레임
> **시안 B — Material You (Purple)** 기반  
> GPS 메타데이터 기반 국가/도시별 분류 — GPS 없는 미디어 제외

---

## 레이아웃 — 기본 상태 (지역 카드 목록)

```
┌───────────────────────────────┐  ← 360dp 기준
│ 9:41               ▲ ▶ ⬛    │  StatusBar
├───────────────────────────────┤
│ ← 위치                   ⋮   │  TopAppBar (Small)
├───────────────────────────────┤
│  📍 지도에서 보기 →           │  MapEntryButton (TextButton)
├───────────────────────────────┤
│ 대한민국                       │  CountryHeader (섹션 헤더)
├───────────────────────────────┤
│  ┌───────────────────────────┐│
│  │                           ││
│  │     [커버 이미지]          ││  LocationCard
│  │                           ││
│  ├───────────────────────────┤│
│  │ 서울특별시 강남구     24장 ││
│  └───────────────────────────┘│
│  ┌───────────────────────────┐│
│  │                           ││
│  │     [커버 이미지]          ││
│  │                           ││
│  ├───────────────────────────┤│
│  │ 부산광역시 해운대구    8장 ││
│  └───────────────────────────┘│
├───────────────────────────────┤
│ 일본                           │  CountryHeader
├───────────────────────────────┤
│  ┌───────────────────────────┐│
│  │                           ││
│  │     [커버 이미지]          ││
│  │                           ││
│  ├───────────────────────────┤│
│  │ 도쿄 신주쿠              5장││
│  └───────────────────────────┘│
└───────────────────────────────┘
```

---

## 레이아웃 — GPS 없는 미디어 안내

```
┌───────────────────────────────┐
│ 9:41               ▲ ▶ ⬛    │
├───────────────────────────────┤
│ ← 위치                   ⋮   │
├───────────────────────────────┤
│  📍 지도에서 보기 →           │
├───────────────────────────────┤
│ ┌───────────────────────────┐ │
│ │ ⓘ  위치 정보가 없는       │ │  InfoBanner
│ │    사진은 표시되지 않습니다│ │
│ └───────────────────────────┘ │
├───────────────────────────────┤
│ 대한민국                       │
│  ┌────────────────────────┐   │
│  │                        │   │
│  │    [커버 이미지]        │   │
│  ├────────────────────────┤   │
│  │ 서울 강남구       24장  │   │
│  └────────────────────────┘   │
└───────────────────────────────┘
```

---

## 레이아웃 — GPS 항목 없음 (Empty State)

```
┌───────────────────────────────┐
│ 9:41               ▲ ▶ ⬛    │
├───────────────────────────────┤
│ ← 위치                   ⋮   │
├───────────────────────────────┤
│                               │
│                               │
│             📍                │  아이콘 (64dp, onSurfaceVariant)
│                               │
│     위치 정보가 있는 사진이    │  titleMedium
│         없어요                │
│                               │
│  카메라 앱에서 위치 정보를     │  bodyMedium (onSurfaceVariant)
│  활성화하고 사진을 찍어보세요. │
│                               │
└───────────────────────────────┘
```

---

## Composable 구조

```
LocationScreen(viewModel: LocationViewModel)
└── Scaffold(
    topBar = {
        TopAppBar(
            navigationIcon = { IconButton(Back) },
            title = { Text("위치") },
            actions = { IconButton { Icon(MoreVert) } }
        )
    }
) { padding ->
    val locationGroups = viewModel.locationGroups.collectAsState()

    if (locationGroups.isEmpty()) {
        LocationEmptyState()
    } else {
        LazyColumn {
            // 지도 진입 버튼
            item {
                TextButton(
                    onClick = { navTo(MapScreen) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Icon(Icons.Outlined.Map)
                    Spacer(4.dp)
                    Text("지도에서 보기")
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.ArrowForward)
                }
                Divider()
            }

            locationGroups.forEach { (country, cities) ->
                stickyHeader {
                    Text(
                        country,
                        style = titleSmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(cities) { cityGroup ->
                    LocationCard(
                        cityGroup = cityGroup,
                        onClick = { navTo(MapScreen(filter = cityGroup.city)) }
                    )
                }
            }
        }
    }
}

LocationCard(cityGroup: CityGroup)
└── Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
    AsyncImage(model = cityGroup.coverUri, contentScale = Crop, modifier = Modifier.height(160.dp))
    Row(modifier = Modifier.padding(12.dp)) {
        Text(cityGroup.cityName, style = titleSmall)
        Spacer(Modifier.weight(1f))
        Text("${cityGroup.count}장", style = bodySmall, color = onSurfaceVariant)
    }
}
```

---

## 색상

| 요소 | Material3 토큰 | Hex |
|------|----------------|-----|
| 화면 배경 | colorScheme.background | #FEF7FF |
| TopAppBar | colorScheme.surface | #FEF7FF |
| 지역 카드 배경 | colorScheme.surface | #FEF7FF |
| 카드 모서리 | RoundedCornerShape(16.dp) | — |
| 카드 테두리 | colorScheme.outlineVariant | #CAC4D0 |
| 도시명 텍스트 | colorScheme.onSurface | #1D1B20 |
| 미디어 개수 | colorScheme.onSurfaceVariant | #49454F |
| 국가 섹션 헤더 | colorScheme.onBackground | #1D1B20 |
| 지도 버튼 텍스트 | colorScheme.primary | #6750A4 |
| InfoBanner 배경 | colorScheme.surfaceVariant | #E7E0EC |

---

## 타이포

| 요소 | 스타일 | 크기 / 굵기 |
|------|--------|------------|
| AppBar 제목 | titleLarge | 22sp / 400 |
| 국가 섹션 헤더 | titleSmall | 14sp / 600 |
| 도시명 | titleSmall | 14sp / 600 |
| 미디어 개수 | bodySmall | 12sp / 400 |
| 지도 버튼 | labelLarge | 14sp / 500 |

---

## 상호작용

| 동작 | 결과 |
|------|------|
| "지도에서 보기" 탭 | MapScreen 이동 (전체 마커) |
| 지역 카드 탭 | MapScreen 이동 (해당 도시 필터) |
| ← 탭 | 이전 화면 복귀 |
