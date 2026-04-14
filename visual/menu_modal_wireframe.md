# Menu Modal (바텀 시트) — 와이어프레임
> **시안 B — Material You (Purple)** 기반  
> 하단 내비게이션 "메뉴" 탭 선택 시 표시되는 ModalBottomSheet

---

## 레이아웃 — 기본 상태

```
┌───────────────────────────────┐  ← 360dp 기준
│           ...                 │  배경 화면 (어둡게 스크림)
│                               │
│                               │
├───────────────────────────────┤
│            ────               │  드래그 핸들 (4×32dp)
├───────────────────────────────┤
│                               │
│  🎬  동영상                   │  MenuItem
│  ─────────────────────────    │  Divider
│  ♡  즐겨찾기                  │  MenuItem
│  ─────────────────────────    │
│  🕐  최근 항목                │  MenuItem
│  ─────────────────────────    │
│  📍  위치                     │  MenuItem
│  ─────────────────────────    │
│  👥  공유 앨범                │  MenuItem (미구현 → Toast)
│  ─────────────────────────    │
│  🧹  사진첩 정리              │  MenuItem (미구현 → Toast)
│  ─────────────────────────    │
│  🗑  휴지통                   │  MenuItem
│  ─────────────────────────    │
│  ⚙   설정                    │  MenuItem
│                               │
│  ┌─────────────────────────┐  │  안내 텍스트 (미구현 항목)
│  │ ⓘ "공유 앨범", "사진첩  │  │  (있을 경우 생략 가능)
│  │  정리"는 추후 구현 예정  │  │
│  └─────────────────────────┘  │
│                               │
└───────────────────────────────┘
│  ⊞사진   ⊟앨범   ✦스토리 ●메뉴│  NavigationBar (메뉴 탭 활성)
└───────────────────────────────┘
```

---

## 레이아웃 — 항목 세부 (스크롤 지원)

```
┌───────────────────────────────┐
│            ────               │  드래그 핸들
├───────────────────────────────┤
│ ┌──┐                          │
│ │🎬│  동영상                  │  MenuItem
│ └──┘                          │  아이콘(24dp) + 텍스트
│ ┌──┐                          │
│ │♡ │  즐겨찾기                │
│ └──┘                          │
│ ┌──┐                          │
│ │🕐│  최근 항목               │
│ └──┘                          │
│ ┌──┐                          │
│ │📍│  위치                    │
│ └──┘                          │
│ ┌──┐                          │
│ │👥│  공유 앨범               │  미구현 — 탭 시 Toast
│ └──┘                          │
│ ┌──┐                          │
│ │🧹│  사진첩 정리             │  미구현 — 탭 시 Toast
│ └──┘                          │
│ ┌──┐                          │
│ │🗑│  휴지통                  │
│ └──┘                          │
│ ┌──┐                          │
│ │⚙ │  설정                   │
│ └──┘                          │
└───────────────────────────────┘
```

---

## Composable 구조

```
MenuModalSheet(
    onDismiss: () -> Unit,
    navController: NavController
)
└── ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    dragHandle = { BottomSheetDefaults.DragHandle() }
) {
    Column(modifier = Modifier.navigationBarsPadding()) {
        MenuItemRow(
            icon = Icons.Outlined.Videocam,
            label = "동영상",
            onClick = { onDismiss(); navController.navigate(VideoScreen) }
        )
        Divider(color = colorScheme.outlineVariant)
        MenuItemRow(
            icon = Icons.Outlined.FavoriteBorder,
            label = "즐겨찾기",
            onClick = { onDismiss(); navController.navigate(FavoriteScreen) }
        )
        Divider(color = colorScheme.outlineVariant)
        MenuItemRow(
            icon = Icons.Outlined.AccessTime,
            label = "최근 항목",
            onClick = { onDismiss(); navController.navigate(RecentsScreen) }
        )
        Divider(color = colorScheme.outlineVariant)
        MenuItemRow(
            icon = Icons.Outlined.LocationOn,
            label = "위치",
            onClick = { onDismiss(); navController.navigate(LocationScreen) }
        )
        Divider(color = colorScheme.outlineVariant)
        MenuItemRow(
            icon = Icons.Outlined.PeopleOutline,
            label = "공유 앨범",
            onClick = { Toast.makeText(ctx, "추후 구현 예정입니다.", Toast.LENGTH_SHORT).show() }
        )
        Divider(color = colorScheme.outlineVariant)
        MenuItemRow(
            icon = Icons.Outlined.AutoFixHigh,
            label = "사진첩 정리",
            onClick = { Toast.makeText(ctx, "추후 구현 예정입니다.", Toast.LENGTH_SHORT).show() }
        )
        Divider(color = colorScheme.outlineVariant)
        MenuItemRow(
            icon = Icons.Outlined.DeleteOutline,
            label = "휴지통",
            onClick = { onDismiss(); navController.navigate(TrashScreen) }
        )
        Divider(color = colorScheme.outlineVariant)
        MenuItemRow(
            icon = Icons.Outlined.Settings,
            label = "설정",
            onClick = { onDismiss(); navController.navigate(SettingsScreen) }
        )
    }
}

@Composable
fun MenuItemRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(16.dp))
        Text(label, style = bodyLarge, color = colorScheme.onSurface)
    }
}
```

---

## 색상

| 요소 | Material3 토큰 | Hex |
|------|----------------|-----|
| BottomSheet 배경 | colorScheme.surface | #FEF7FF |
| 드래그 핸들 | colorScheme.outlineVariant | #CAC4D0 |
| 스크림 배경 | Black (α 32%) | — |
| 메뉴 아이콘 | colorScheme.onSurfaceVariant | #49454F |
| 메뉴 텍스트 | colorScheme.onSurface | #1D1B20 |
| 구분선 | colorScheme.outlineVariant | #CAC4D0 |
| Ripple 효과 | colorScheme.primary (α 12%) | — |
| NavigationBar 메뉴 탭 pill | colorScheme.secondaryContainer | #D0BCFF |
| NavigationBar 메뉴 탭 아이콘 | colorScheme.primary | #6750A4 |

---

## 타이포

| 요소 | 스타일 | 크기 / 굵기 |
|------|--------|------------|
| 메뉴 항목 텍스트 | bodyLarge | 16sp / 400 |
| Toast 메시지 | — | 시스템 기본 |

---

## 상호작용

| 동작 | 결과 |
|------|------|
| "동영상" 탭 | ModalBottomSheet 닫기 → VideoScreen 이동 |
| "즐겨찾기" 탭 | ModalBottomSheet 닫기 → FavoriteScreen 이동 |
| "최근 항목" 탭 | ModalBottomSheet 닫기 → RecentsScreen 이동 |
| "위치" 탭 | ModalBottomSheet 닫기 → LocationScreen 이동 |
| "공유 앨범" 탭 | Toast "추후 구현 예정입니다." |
| "사진첩 정리" 탭 | Toast "추후 구현 예정입니다." |
| "휴지통" 탭 | ModalBottomSheet 닫기 → TrashScreen 이동 |
| "설정" 탭 | ModalBottomSheet 닫기 → SettingsScreen 이동 |
| 스크림 탭 or 아래 스와이프 | ModalBottomSheet 닫기, 이전 탭 복귀 |
