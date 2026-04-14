# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 빌드 및 실행

```bash
# 디버그 빌드
./gradlew assembleDebug

# 기기에 설치
./gradlew installDebug

# 전체 테스트 (단위)
./gradlew test

# 단일 테스트 클래스 실행
./gradlew test --tests "com.yoshi0311.gallery.ExampleTest"

# 린트
./gradlew lint
```

## 아키텍처 개요

MVVM + Repository 패턴. 단일 Activity (`MainActivity`).

```
UI Layer        → ui/screen/**  +  ui/component/**
ViewModel Layer → viewmodel/**
Repository Layer→ data/repository/**
Data Sources    → data/local/db (Room)
                  data/local/mediastore (MediaStore API)
```

### 데이터 흐름

**미디어 읽기**: `MediaStoreDataSource` → `callbackFlow` → `ContentObserver`로 실시간 감지 → `MediaRepository` → ViewModel의 `StateFlow` → Compose `collectAsStateWithLifecycle()`

**즐겨찾기 / 휴지통**: Room DB (`FavoriteDao`, `TrashDao`)에 `mediaId`만 저장. 실제 미디어 목록은 `combine(MediaRepository.getAllMedia(), Dao.observeIds())`로 조인. 휴지통은 soft delete(DB 기록)이며 실제 파일 삭제 없음.

### 네비게이션

`Navigation3` 라이브러리 사용 (`androidx.navigation3`). `GalleryNavHost.kt`가 앱 전체 진입점.

- `Screen.kt`에 `@Serializable` NavKey들 정의 (data object / data class)
- `rememberNavBackStack` + `NavDisplay` + `entryProvider` 조합으로 백스택 관리
- 바텀 탭(사진/앨범/스토리/메뉴) 전환 시 `backStack.clear()` 후 `add()`로 교체
- `MenuModalSheet`는 Scaffold 외부 오버레이로 배치 (`showMenuSheet` 상태로 제어)

### 화면 목록

| NavKey | 설명 |
|---|---|
| `PhotosScreen` | 사진 그리드 메인 (핀치 줌, 다중 선택) |
| `AlbumsScreen` | 앨범 목록 |
| `StoryListScreen` | 스토리 목록 |
| `PhotoViewScreen(mediaId, albumId?, fromTrash)` | 사진/동영상 뷰어 |
| `AlbumViewScreen(albumId, albumName)` | 앨범 내 사진 그리드 |
| `FavoritesScreen`, `TrashScreen`, `RecentsScreen` | 기능성 화면 |
| `LocationScreen` → `MapScreen(cityFilter?)` | 위치 기반 탐색 |
| `VideosScreen` | 동영상 목록 |

## 주요 패턴 및 규칙

### 상단 바 패턴 (사진/앨범/스토리 탭)
세 메인 탭은 동일한 구조:
1. `CenterAlignedTopAppBar` + `enterAlwaysScrollBehavior` (스크롤 시 숨김)
2. `Scaffold(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))`
3. TopAppBar 아래 `HeroHeader` 컴포저블 (검색 아이콘 + 더보기 드롭다운)
4. 더보기(`DropdownMenu`) 항목: 선택 / 만들기 / 비슷한 이미지 모아보기 / 슬라이드쇼

### 다중 선택 모드 (PhotoMain, AlbumView)
- `PhotoMainViewModel.selectionMode`, `selectedIds`로 상태 관리
- `enterSelectionMode(id)` — 롱 프레스 진입, `enterSelectionMode()` — 더보기 메뉴 진입
- `GalleryNavHost`에서 `isSelectionActive` 상태를 올려받아 바텀 내비게이션 숨김 처리

### 화면 회전 (PhotoViewScreen)
- `AndroidManifest.xml`에 `configChanges="orientation|screenSize|..."` 설정됨 → Activity 재생성 없이 회전
- `requestedOrientation`으로 가로/세로 전환, `DisposableEffect`에서 화면 이탈 시 초기화

### MediaStore 주의사항
- 미디어 읽기는 `IO Dispatcher`에서 실행 (`flowOn(Dispatchers.IO)`)
- API 29+ / 이하 분기 존재 (`MediaStore.VOLUME_EXTERNAL` vs `"external"`)
- `latitude`, `longitude`는 `ACCESS_MEDIA_LOCATION` 권한 필요, `LocationRepository`에서만 읽음

### 메뉴 모달
`MenuModalSheet`는 `ModalBottomSheet`가 아닌 `Dialog(usePlatformDefaultWidth = false)` + `Surface`로 구현 (좌우·하단 여백이 있는 floating 카드 형태). 외부 터치 닫힘은 투명 배경 Box의 `clickable(onDismiss)`로 수동 처리.

## DI 구조

- `DatabaseModule` — Room DB, DAO 제공
- `MediaStoreModule` — `ContentResolver` 제공
- `RepositoryModule` — Repository 인터페이스 → Impl 바인딩

## 미구현 / 플레이스홀더

- `SearchScreen` — 플레이스홀더
- `SettingsScreen` — 플레이스홀더
- 더보기 메뉴 "만들기", "비슷한 이미지 모아보기", "슬라이드쇼" — Toast만 표시
- 공유 앨범, 사진첩 정리 — Toast만 표시
- Samsung Studio 연동 — `com.sec.android.gallery3d` / `com.samsung.android.app.gallery` 패키지 시도, 없으면 Toast
