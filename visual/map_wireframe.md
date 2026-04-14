# Map Screen — 와이어프레임
> **시안 B — Material You (Purple)** 기반  
> Google Maps SDK for Compose, 미디어 위치 마커 클러스터링

---

## 레이아웃 — 기본 상태 (지도 + 클러스터 마커)

```
┌───────────────────────────────┐  ← 360dp 기준
│ 9:41               ▲ ▶ ⬛    │  StatusBar (투명/반투명)
├───────────────────────────────┤
│ ←                        ⋮   │  TopAppBar (투명 오버레이)
├───────────────────────────────┤
│                               │
│         ┌──┐   ┌───┐          │
│         │📷│   │ 8 │          │  Google Maps Composable
│         └──┘   └───┘          │  (단일 마커: 썸네일 / 클러스터: 숫자 배지)
│   ┌──┐                        │
│   │📷│        ┌──┐            │
│   └──┘        │📷│            │
│               └──┘            │
│        ┌──┐                   │
│        │12│                   │
│        └──┘                   │
│                               │
│                           [+] │  줌 인
│                           [-] │  줌 아웃
└───────────────────────────────┘
```

---

## 레이아웃 — 마커 탭 시 미리보기 표시

```
┌───────────────────────────────┐
│ 9:41               ▲ ▶ ⬛    │
├───────────────────────────────┤
│ ←                        ⋮   │
├───────────────────────────────┤
│                               │
│         ┌──┐   ┌───┐          │
│         │📷│   │ 8 │          │
│         └──┘   └───┘          │
│   ┌──┐                        │
│   │📷│◀                       │  선택된 마커 (강조 표시)
│   └──┘                        │
│                               │
│                               │
│                               │
├───────────────────────────────┤
│ ┌──────────────────────────┐  │  미디어 미리보기 카드 (BottomSheet)
│ │                          │  │
│ │   [썸네일 이미지]         │  │  ModalBottomSheet
│ │                          │  │  (드래그 핸들 포함)
│ ├──────────────────────────┤  │
│ │ IMG_20260410_152300.jpg   │  │
│ │ 2026년 4월 10일 · 서울 강남│ │
│ │               [열기 →]   │  │
│ └──────────────────────────┘  │
└───────────────────────────────┘
```

---

## 레이아웃 — 클러스터 탭 시 미리보기

```
┌───────────────────────────────┐
│ ←                        ⋮   │
├───────────────────────────────┤
│                               │
│        [지도 배경]            │
│                               │
│        ┌───┐ ← 탭됨          │
│        │ 8 │                  │
│        └───┘                  │
│                               │
├───────────────────────────────┤
│ ┌──────────────────────────┐  │  클러스터 미리보기 (ModalBottomSheet)
│ │   ─────────              │  │  드래그 핸들
│ │ 이 위치의 사진 (8장)      │  │
│ │                          │  │
│ │ ┌──┐ ┌──┐ ┌──┐ ┌──┐     │  │  썸네일 LazyRow
│ │ │  │ │  │ │  │ │  │ ... │  │
│ │ └──┘ └──┘ └──┘ └──┘     │  │
│ │               [모두 보기]│  │
│ └──────────────────────────┘  │
└───────────────────────────────┘
```

---

## Composable 구조

```
MapScreen(
    cityFilter: String? = null,   // null = 전체
    viewModel: MapViewModel
)
└── Box(modifier = Modifier.fillMaxSize()) {
    // Google Maps
    GoogleMap(
        cameraPositionState = cameraPositionState,
        modifier = Modifier.fillMaxSize()
    ) {
        clusters.forEach { cluster ->
            if (cluster.size == 1) {
                // 단일 마커: 썸네일 이미지 마커
                MarkerComposable(
                    state = MarkerState(position = cluster.position),
                    onClick = { showSinglePreview(cluster.items.first()) }
                ) {
                    ThumbnailMarker(uri = cluster.items.first().uri)
                }
            } else {
                // 클러스터 마커: 숫자 배지
                MarkerComposable(
                    state = MarkerState(position = cluster.position),
                    onClick = { showClusterPreview(cluster.items) }
                ) {
                    ClusterBadgeMarker(count = cluster.size)
                }
            }
        }
    }

    // TopAppBar (투명 오버레이)
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        navigationIcon = { IconButton(Back) },
        actions = { IconButton { Icon(MoreVert, tint = Color.Black) } },
        modifier = Modifier.align(Alignment.TopCenter)
    )

    // 미리보기 BottomSheet
    if (selectedMedia != null) {
        ModalBottomSheet(onDismissRequest = { clearSelection() }) {
            MediaPreviewCard(
                media = selectedMedia,
                onOpenClick = { navTo(PhotoViewScreen(selectedMedia)) }
            )
        }
    }

    if (selectedCluster != null) {
        ModalBottomSheet(onDismissRequest = { clearSelection() }) {
            ClusterPreviewSheet(
                items = selectedCluster,
                onItemClick = { navTo(PhotoViewScreen(it)) },
                onViewAllClick = { /* PhotoMain with location filter */ }
            )
        }
    }
}
```

---

## 색상

| 요소 | Material3 토큰 | Hex |
|------|----------------|-----|
| TopAppBar 배경 | 투명 | — |
| 단일 마커 테두리 | colorScheme.primary | #6750A4 |
| 클러스터 배지 배경 | colorScheme.primary | #6750A4 |
| 클러스터 배지 텍스트 | colorScheme.onPrimary | #FFFFFF |
| BottomSheet 배경 | colorScheme.surface | #FEF7FF |
| 드래그 핸들 | colorScheme.outlineVariant | #CAC4D0 |
| 미리보기 제목 | colorScheme.onSurface | #1D1B20 |
| 미리보기 설명 | colorScheme.onSurfaceVariant | #49454F |
| "열기" 버튼 | colorScheme.primary | #6750A4 |

---

## 타이포

| 요소 | 스타일 | 크기 / 굵기 |
|------|--------|------------|
| 클러스터 숫자 | labelMedium | 12sp / 700 |
| 미리보기 파일명 | titleSmall | 14sp / 600 |
| 미리보기 날짜·위치 | bodySmall | 12sp / 400 |
| "모두 보기" 버튼 | labelLarge | 14sp / 500 |

---

## 상호작용

| 동작 | 결과 |
|------|------|
| 단일 마커 탭 | ModalBottomSheet — 단일 미디어 미리보기 |
| 클러스터 마커 탭 | ModalBottomSheet — 해당 위치 미디어 LazyRow |
| 미리보기 "열기" 탭 | PhotoViewScreen 이동 |
| "모두 보기" 탭 | (위치 필터 적용) 미디어 목록 화면 이동 |
| 지도 핀치 줌·팬 | 지도 확대/축소·이동 |
| ← 탭 | LocationScreen 복귀 |
| BottomSheet 아래로 드래그 | 미리보기 닫힘 |
