# Album List Screen — 와이어프레임
> **시안 B — Material You (Purple)** 기반

---

## 레이아웃 — 기본 상태 (2단 그리드)

```
┌───────────────────────────────┐  ← 360dp 기준
│ 9:41               ▲ ▶ ⬛    │  StatusBar
├───────────────────────────────┤
│ 앨범                    ⋮    │  TopAppBar (Medium)
├───────────────────────────────┤
│ [이름순 ▾] [최신순] [항목 수순]│  SortChipRow (SingleChoiceSegmented)
├───────────────────────────────┤
│  ┌─────────────┐ ┌───────────┐│
│  │             │ │           ││
│  │  (커버 이미지)│ │(커버 이미지)││  AlbumCard
│  │             │ │           ││
│  ├─────────────┤ ├───────────┤│
│  │ DCIM        │ │ Downloads ││
│  │ 352장       │ │ 12장      ││
│  └─────────────┘ └───────────┘│
│  ┌─────────────┐ ┌───────────┐│
│  │             │ │           ││
│  │             │ │           ││
│  │             │ │           ││
│  ├─────────────┤ ├───────────┤│
│  │ Screenshots │ │ Camera    ││
│  │ 24장        │ │ 88장      ││
│  └─────────────┘ └───────────┘│
│  ┌─────────────┐ ┌───────────┐│
│  │             │ │           ││
│  │             │ │           ││
│  │             │ │           ││
│  ├─────────────┤ ├───────────┤│
│  │ Instagram   │ │ KakaoTalk ││
│  │ 67장        │ │ 124장     ││
│  └─────────────┘ └───────────┘│
│              ...               │
├───────────────────────────────┤
│  ⊞사진   ⊟앨범   ✦스토리  ≡메뉴│  NavigationBar
└───────────────────────────────┘
```

---

## 레이아웃 — 3단 그리드 (핀치 인)

```
┌───────────────────────────────┐
│ 9:41               ▲ ▶ ⬛    │
├───────────────────────────────┤
│ 앨범                    ⋮    │
├───────────────────────────────┤
│ [이름순 ▾] [최신순] [항목 수순]│
├───────────────────────────────┤
│ ┌────────┐ ┌────────┐ ┌──────┐│
│ │        │ │        │ │      ││
│ │        │ │        │ │      ││
│ ├────────┤ ├────────┤ ├──────┤│
│ │DCIM    │ │Downld..│ │Scrsht││
│ │352장   │ │12장    │ │24장  ││
│ └────────┘ └────────┘ └──────┘│
│ ┌────────┐ ┌────────┐ ┌──────┐│
│ │        │ │        │ │      ││
│ ├────────┤ ├────────┤ ├──────┤│
│ │Camera  │ │Insta.. │ │Kakao.││
│ │88장    │ │67장    │ │124장 ││
│ └────────┘ └────────┘ └──────┘│
├───────────────────────────────┤
│  ⊞사진   ⊟앨범   ✦스토리  ≡메뉴│
└───────────────────────────────┘
```

---

## Composable 구조

```
AlbumListScreen(viewModel: AlbumListViewModel)
└── Scaffold(
    topBar = {
        TopAppBar(
            title = { Text("앨범") },
            actions = { IconButton { Icon(MoreVert) } }
        )
    },
    bottomBar = { GalleryNavigationBar(selected = Tab.Album) }
) { padding ->
    Column {
        SortChipRow(
            options = [이름순, 최신순, 항목 수순],
            selected = sortOrder,
            onSelect = viewModel::setSortOrder
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(columnCount),  // 1~3단
            modifier = Modifier.pinchToZoom(
                onZoomIn  = { columnCount = max(1, columnCount - 1) },
                onZoomOut = { columnCount = min(3, columnCount + 1) }
            )
        ) {
            items(albums) { album ->
                AlbumCard(
                    album = album,
                    onClick = { navTo(AlbumViewScreen(albumId = album.id)) }
                )
            }
        }
    }
}

AlbumCard(album: Album)
├── Card(shape = RoundedCornerShape(12.dp), elevation = 0.dp)
│   ├── AsyncImage(model = album.coverUri, contentScale = Crop)
│   └── Column(modifier = Modifier.padding(8.dp))
│       ├── Text(album.name, style = titleSmall, maxLines = 1, overflow = Ellipsis)
│       └── Text("${album.count}장", style = bodySmall)
```

---

## 색상

| 요소 | Material3 토큰 | Hex |
|------|----------------|-----|
| 화면 배경 | colorScheme.background | #FEF7FF |
| TopAppBar | colorScheme.surface | #FEF7FF |
| 앨범 카드 배경 | colorScheme.surfaceVariant | #E7E0EC |
| 앨범 카드 모서리 | RoundedCornerShape(12.dp) | — |
| 앨범명 텍스트 | colorScheme.onSurface | #1D1B20 |
| 미디어 개수 텍스트 | colorScheme.onSurfaceVariant | #49454F |
| 정렬 칩 (선택) 배경 | colorScheme.secondaryContainer | #E8DEF8 |
| 정렬 칩 (선택) 텍스트 | colorScheme.onSecondaryContainer | #1D192B |
| 정렬 칩 (미선택) 배경 | colorScheme.surface | #FEF7FF |
| NavigationBar 배경 | colorScheme.surfaceVariant | #E7E0EC |
| 활성 탭 pill | colorScheme.secondaryContainer | #D0BCFF |
| 활성 탭 아이콘 | colorScheme.primary | #6750A4 |

---

## 타이포

| 요소 | 스타일 | 크기 / 굵기 |
|------|--------|------------|
| AppBar 제목 | titleLarge | 22sp / 400 |
| 앨범명 | titleSmall | 14sp / 600 |
| 미디어 개수 | bodySmall | 12sp / 400 |
| 정렬 칩 라벨 | labelLarge | 14sp / 500 |

---

## 상호작용

| 동작 | 결과 |
|------|------|
| 앨범 카드 탭 | AlbumViewScreen(albumId) 이동 |
| 핀치 아웃 | 열 수 증가: 1→2→3 |
| 핀치 인 | 열 수 감소: 3→2→1 |
| 정렬 칩 탭 | 앨범 목록 정렬 변경 (즉시 반영) |
| ⋮ 탭 | 드롭다운: 숨김 폴더 표시 토글 |
