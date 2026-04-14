# Trash Screen — 와이어프레임
> **시안 B — Material You (Purple)** 기반  
> OS `IS_TRASHED` MediaStore 플래그 기반 — 앱 자체 삭제 수행 없음

---

## 레이아웃 — API 30 이상, 항목 있음

```
┌───────────────────────────────┐  ← 360dp 기준
│ 9:41               ▲ ▶ ⬛    │  StatusBar
├───────────────────────────────┤
│ ← 휴지통                  ⋮  │  TopAppBar (Small)
├───────────────────────────────┤
│ ┌───────────────────────────┐ │
│ │ ⓘ  삭제 후 30일이 지나면  │ │  InfoBanner
│ │    자동으로 영구 삭제됩니다│ │  (surfaceVariant 배경)
│ └───────────────────────────┘ │
├───────────────────────────────┤
│ ┌───────┐ ┌───────┐ ┌───────┐ │
│ │       │ │ ▶     │ │       │ │  LazyVerticalGrid 3단
│ │       │ │ 0:42  │ │       │ │
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

## 레이아웃 — API 30 이상, 항목 없음 (Empty State)

```
┌───────────────────────────────┐
│ 9:41               ▲ ▶ ⬛    │
├───────────────────────────────┤
│ ← 휴지통                  ⋮  │
├───────────────────────────────┤
│ ┌───────────────────────────┐ │
│ │ ⓘ  삭제 후 30일이 지나면  │ │
│ │    자동으로 영구 삭제됩니다│ │
│ └───────────────────────────┘ │
│                               │
│                               │
│             🗑                │  아이콘 (64dp, onSurfaceVariant)
│                               │
│       휴지통이 비어 있어요     │  titleMedium
│                               │
│  삭제한 항목이 여기에 표시됩니다│  bodyMedium (onSurfaceVariant)
│                               │
│                               │
└───────────────────────────────┘
```

---

## 레이아웃 — API 29 이하 (미지원)

```
┌───────────────────────────────┐
│ 9:41               ▲ ▶ ⬛    │
├───────────────────────────────┤
│ ← 휴지통                  ⋮  │
├───────────────────────────────┤
│                               │
│                               │
│             ⚠                 │  경고 아이콘 (64dp, error 색상)
│                               │
│  이 Android 버전에서는        │  titleMedium
│  휴지통 기능을 지원하지 않습니다│
│                               │
│  삭제 시 바로 영구 삭제됩니다. │  bodyMedium (onSurfaceVariant)
│                               │
│                               │
└───────────────────────────────┘
```

---

## Composable 구조

```
TrashScreen(viewModel: TrashViewModel)
└── Scaffold(
    topBar = {
        TopAppBar(
            navigationIcon = { IconButton(Back) },
            title = { Text("휴지통") },
            actions = { IconButton { Icon(MoreVert) } }
        )
    }
) { padding ->
    Column {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // API 30 이상
            TrashInfoBanner(text = "삭제 후 30일이 지나면 자동으로 영구 삭제됩니다")

            val items = viewModel.trashedItems.collectAsState()
            if (items.isEmpty()) {
                TrashEmptyState()
            } else {
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
        } else {
            // API 29 이하
            TrashUnsupportedState()
        }
    }
}

// InfoBanner
TrashInfoBanner(text: String)
└── Surface(
    color = colorScheme.surfaceVariant,
    shape = RoundedCornerShape(12.dp),
    modifier = Modifier.padding(16.dp)
) {
    Row(verticalAlignment = CenterVertically, modifier = Modifier.padding(12.dp)) {
        Icon(Icons.Outlined.Info, tint = colorScheme.onSurfaceVariant)
        Spacer(width = 8.dp)
        Text(text, style = bodySmall, color = colorScheme.onSurfaceVariant)
    }
}
```

---

## 색상

| 요소 | Material3 토큰 | Hex |
|------|----------------|-----|
| 화면 배경 | colorScheme.background | #FEF7FF |
| TopAppBar | colorScheme.surface | #FEF7FF |
| InfoBanner 배경 | colorScheme.surfaceVariant | #E7E0EC |
| InfoBanner 텍스트 | colorScheme.onSurfaceVariant | #49454F |
| InfoBanner 아이콘 | colorScheme.onSurfaceVariant | #49454F |
| Empty State 아이콘 | colorScheme.onSurfaceVariant | #49454F |
| Unsupported 경고 아이콘 | colorScheme.error | #B3261E |
| Unsupported 설명 | colorScheme.onSurfaceVariant | #49454F |

---

## 타이포

| 요소 | 스타일 | 크기 / 굵기 |
|------|--------|------------|
| AppBar 제목 | titleLarge | 22sp / 400 |
| InfoBanner 텍스트 | bodySmall | 12sp / 400 |
| Empty / Unsupported 제목 | titleMedium | 16sp / 500 |
| Empty / Unsupported 설명 | bodyMedium | 14sp / 400 |

---

## 상호작용

| 동작 | 결과 |
|------|------|
| 아이템 탭 | PhotoViewScreen 이동 (읽기 전용 뷰) |
| 아이템 롱프레스 | 다중 선택 모드 진입 |
| ← 탭 | 이전 화면 복귀 |

> **주의:** 앱에서 직접 삭제·복구 수행 없음. OS 휴지통 항목 표시만 담당.
