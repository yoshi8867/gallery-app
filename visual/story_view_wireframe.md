# Story View Screen — 와이어프레임 (Phase 3)
> **시안 B — Material You (Purple)** 기반  
> 슬라이드쇼 자동재생 + 재생속도 조절 + 배경음악

---

## 레이아웃 — 재생 중 상태

```
┌───────────────────────────────┐  ← 360dp 기준
│  봄 나들이(큰글씨)          * ⋮  │   StatusBar 숨김 (몰입형)  공유/더보기(* 표시에 공유 버튼 넣을 것.)
│  🎵음악제목                    │    ← Ambient Mode — YouTube가 영상 주변 배경에 영상 색상을 번지게 하는 것처럼 여백을 처리.
├───────────────────────────────┤
│                               │
│                               │
│                               │
│      [ 현재 슬라이드 이미지 ]  │  전체 화면 이미지 ← 이 영역의 우측 3분의1 정도 지점을 탭하면 다음 이미지로 넘김. 좌측 3분의1 정도 지점을 탭하면 이전 이미지로 넘김.
│         (페이드 인/아웃)       │
│                               │   ← 이 영역의 중앙 3분의1 지점을 탭하면 모든 메뉴 사라짐/생김 토글.
│                               │
│                               │
│                               │
├───────────────────────────────┤
│                               │   ← Ambient Mode — YouTube가 영상 주변 배경에 영상 색상을 번지게 하는 것처럼 여백을 처리.
│  ┌──┐    ┌───────────┐   ┌───┐ │  재생 컨트롤
│  │🎵│    │▶0:10/1:00│   │뮤트│ │  음악 토글 / 재생일시정지 / 뮤트
│  └──┘    └───────────┘   └────┘ │
│                               │
└───────────────────────────────┘
```

---

## 레이아웃 — UI 숨김 상태 (탭 시 전환)

```
┌───────────────────────────────┐
│                               │
│                               │
│                               │
│      [ 현재 슬라이드 이미지 ]  │  몰입형 뷰 (UI 전체 숨김)
│                               │
│                               │
│                               │
│                               │
│                               │
│                               │
└───────────────────────────────┘
```

---

## 레이아웃 — 재생속도 선택 (드롭다운)

```
┌───────────────────────────────┐
│ ←           봄 나들이     ⋮  │
├───────────────────────────────┤
│                               │
│      [ 현재 슬라이드 이미지 ]  │
│                               │
├───────────────────────────────┤
│  ●──────────────────────      │
│  3 / 12                       │
├───────────────────────────────┤
│  ┌──┐  ◀  ⏸  ▶  ┌────────┐ │
│  │🎵│              │ 0.5x   │ │  드롭다운 메뉴
│  └──┘              │ ● 1x   │ │  (현재 선택 = primary 표시)
│                    │ 1.5x   │ │
│                    │ 2x     │ │
│                    └────────┘ │
└───────────────────────────────┘
```

---

## 레이아웃 — 배경음악 선택 (바텀 시트)

```
┌───────────────────────────────┐
│ ←           봄 나들이     ⋮  │
├───────────────────────────────┤
│      [ 슬라이드 이미지 ]       │
│                               │
│            ────               │  드래그 핸들
│  배경음악 선택                 │  titleMedium
│                               │
│  ● 없음                       │  RadioButton + Text
│    봄 바람 (Acoustic)         │
│    잔잔한 피아노               │
│    신나는 팝                   │
│    여름 바다 (Lo-fi)          │
│                               │
│    [선택 완료]                 │  FilledButton
│                               │
└───────────────────────────────┘
```

---

## Composable 구조

```
StoryViewScreen(
    storyId: Long,
    viewModel: StoryViewModel
)
└── Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
    // 슬라이드 이미지 (CrossFade 전환)
    Crossfade(
        targetState = currentIndex,
        animationSpec = tween(durationMillis = 600)
    ) { index ->
        AsyncImage(
            model = story.mediaItems[index].uri,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }

    // UI 오버레이 (탭으로 토글)
    AnimatedVisibility(visible = isUiVisible) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TopAppBar
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = { IconButton(Back, tint = White) },
                title = { Text(story.title, color = White) },
                actions = { IconButton { Icon(MoreVert, tint = White) } }
            )

            Spacer(Modifier.weight(1f))

            // 진행 바
            LinearProgressIndicator(
                progress = currentIndex.toFloat() / totalCount,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                color = colorScheme.primary,
                trackColor = White.copy(alpha = 0.3f)
            )
            Text(
                "${currentIndex + 1} / $totalCount",
                color = White,
                style = labelMedium,
                modifier = Modifier.padding(start = 16.dp)
            )

            // 재생 컨트롤
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = SpaceBetween,
                verticalAlignment = CenterVertically
            ) {
                IconButton(onClick = viewModel::toggleMusic) {
                    Icon(if (isMusicOn) Icons.Default.MusicNote else Icons.Default.MusicOff, tint = White)
                }
                Row {
                    IconButton(onClick = viewModel::previous) { Icon(SkipPrevious, tint = White) }
                    IconButton(onClick = viewModel::togglePlayPause) {
                        Icon(if (isPlaying) Pause else PlayArrow, tint = White)
                    }
                    IconButton(onClick = viewModel::next) { Icon(SkipNext, tint = White) }
                }
                TextButton(onClick = { showSpeedMenu = true }) {
                    Text("${speed}x", color = White, style = labelLarge)
                    Icon(ArrowDropDown, tint = White)
                }
            }
        }
    }

    // 재생속도 드롭다운
    DropdownMenu(expanded = showSpeedMenu, onDismissRequest = { showSpeedMenu = false }) {
        listOf(0.5f, 1f, 1.5f, 2f).forEach { s ->
            DropdownMenuItem(
                text = { Text("${s}x") },
                onClick = { viewModel.setSpeed(s); showSpeedMenu = false },
                trailingIcon = { if (speed == s) Icon(Check, tint = colorScheme.primary) }
            )
        }
    }

    // 배경음악 ModalBottomSheet
    if (showMusicSheet) {
        ModalBottomSheet(onDismissRequest = { showMusicSheet = false }) {
            MusicSelectionSheet(
                selectedMusic = selectedMusic,
                onSelect = viewModel::selectMusic,
                onConfirm = { showMusicSheet = false }
            )
        }
    }
}
```

---

## 색상

| 요소 | Material3 토큰 | Hex |
|------|----------------|-----|
| 배경 | Black | #000000 |
| TopAppBar 배경 | 투명 | — |
| TopAppBar 텍스트·아이콘 | White | #FFFFFF |
| 진행 바 (진행) | colorScheme.primary | #6750A4 |
| 진행 바 (트랙) | White (α 30%) | — |
| 재생 컨트롤 아이콘 | White | #FFFFFF |
| 재생속도 텍스트 | White | #FFFFFF |
| 속도 드롭다운 배경 | colorScheme.surface | #FEF7FF |
| 선택된 속도 체크 | colorScheme.primary | #6750A4 |
| BottomSheet 배경 | colorScheme.surface | #FEF7FF |
| 음악 선택 RadioButton | colorScheme.primary | #6750A4 |

---

## 타이포

| 요소 | 스타일 | 크기 / 굵기 |
|------|--------|------------|
| TopAppBar 제목 (스토리명) | titleMedium | 16sp / 500 |
| 진행 숫자 | labelMedium | 12sp / 500 |
| 재생속도 텍스트 | labelLarge | 14sp / 500 |
| BottomSheet 제목 | titleMedium | 16sp / 500 |
| 음악 선택 항목 | bodyLarge | 16sp / 400 |

---

## 상호작용

| 동작 | 결과 |
|------|------|
| 화면 탭 | UI 오버레이 표시/숨김 토글 |
| ◀ 탭 | 이전 슬라이드 (CrossFade) |
| ▶ 탭 | 다음 슬라이드 (CrossFade) |
| ⏸/▶ 탭 | 자동재생 일시정지/재개 |
| 🎵 탭 | 배경음악 선택 BottomSheet 표시 |
| "1x" 탭 | 재생속도 드롭다운 표시 |
| 속도 선택 | 슬라이드 전환 간격 변경 |
| ← 탭 | StoryListScreen 복귀 |

> **P3 구현 전 확인 필요:**
> - 각 슬라이드 표시 시간 (기본: 3초)
> - 배경 음악 번들 음원 목록
> - 마지막 슬라이드 이후 동작 (반복 or 종료)
