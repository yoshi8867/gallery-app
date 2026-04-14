# 갤러리 앱 구현 진행도

---

## 전체 진행도
█████████████████░░░ 85% (14/16) + 테마 개선

---

## Phase 1 — 기본 기능

### [P1-1] 프로젝트 초기 세팅 ✅
- [x] architecture-templates base → customizer.sh 적용 (`com.yoshi0311.gallery`, `Gallery`)
- [x] `build.gradle.kts` 의존성 추가: Hilt, Navigation, Room + KSP, Coil, ExoPlayer(Media3), secrets-gradle-plugin
- [x] `GalleryApplication.kt` (Hilt @HiltAndroidApp)
- [x] `MainActivity.kt` (NavHost 호스팅)
- [x] `ui/theme/` Color.kt / Type.kt / Theme.kt (Material You Purple 시안 B)
- [x] `ui/navigation/` GalleryNavHost.kt
- [x] `ui/component/` GalleryNavigationBar.kt

### [P1-2] 권한 요청 흐름 ✅
- [x] `READ_MEDIA_IMAGES` / `READ_MEDIA_VIDEO` (API 33+) / `READ_EXTERNAL_STORAGE` (API 29~32)
- [x] `ACCESS_MEDIA_LOCATION` (EXIF GPS 읽기)
- [x] PermissionScreen Composable (권한 요청 UI)

### [P1-3] MediaStore 데이터 소스 ✅
- [x] `MediaItem.kt` 도메인 모델 (사진/동영상 공통)
- [x] `Album.kt` 도메인 모델
- [x] `MediaStoreDataSource.kt` — 전체 미디어 쿼리 (날짜 내림차순)
- [x] `MediaRepository.kt`
- [x] `AlbumRepository.kt`
- [x] Hilt 모듈: `MediaStoreModule.kt`, `RepositoryModule.kt`

### [P1-4] Photo Main Screen ✅
- [x] `PhotoMainViewModel.kt` — 날짜별 그룹핑 StateFlow
- [x] `PhotoMainScreen.kt` — LargeTopAppBar(Collapsing) + LazyColumn + StickyHeader
- [x] `MediaThumbnail.kt` — 동영상 배지 오버레이 포함
- [x] 핀치 줌: 1.5 / 3 / 4 / 7 / 11 / 20단 전환 (11·20단은 저해상도 썸네일)
- [x] 다중 선택 모드 (롱프레스) + `SelectionTopBar.kt` + SelectionActionBar
- [x] SearchBar 탭 → SearchScreen 이동

### [P1-5] Album List Screen ✅
- [x] `AlbumListViewModel.kt`
- [x] `AlbumListScreen.kt` — LazyVerticalGrid 1·2·3단 + 정렬 칩 (이름순/최신순/항목 수순)
- [x] `AlbumCard.kt` Composable

### [P1-6] Album View Screen ✅
- [x] `AlbumViewViewModel.kt`
- [x] `AlbumViewScreen.kt` — LazyVerticalGrid + AnimatedVisibility 서랍 패널
- [x] 서랍 열림/닫힘 제스처 (우측 스와이프 열림, 좌측 스와이프 닫힘)
- [x] 열 수 5단계 (닫힘: 2/3/4/7/12단, 열림: 2/3/5/9단)
- [x] 다중 선택 모드 (롱프레스)

### [P1-7] Photo View Screen ✅
- [x] `PhotoViewViewModel.kt`
- [x] `PhotoViewScreen.kt` — HorizontalPager + ZoomableImage
- [x] 하단 ThumbnailStrip (LazyRow) — 구현 완료, 현재 주석 처리 상태
- [x] 상세정보 패널 (ModalBottomSheet — 파일명·날짜·크기·해상도·위치)
- [x] BottomActionBar (즐겨찾기/편집Toast/AIToast/공유/삭제)

### [P1-8] 동영상 재생 (ExoPlayer) ✅
- [x] Photo View 내 동영상 분기: ExoPlayer 컨트롤러 + 뮤트 버튼
- [x] `VideoOverlay.kt` Composable

### [P1-9] 특수 앨범: 동영상 / 최근 항목 ✅
- [x] `VideoViewModel.kt` + `VideoScreen.kt` — 동영상 필터, 단순 그리드, 핀치 줌 3단(3/4/7), 다중 선택
- [x] `RecentsViewModel.kt` + `RecentsScreen.kt` — 30일/전체 기간 필터, 단순 그리드, 다중 선택
- [x] `MediaGridScreen.kt` — 동영상·최근·즐겨찾기·휴지통 공용 Composable
- [x] `SelectionActionBar` 더보기(···) 버튼 추가
- [x] 메뉴 2차 화면 NavigationBar 숨김 처리

---

## Phase 2 — 부가 기능

### [P2-1] 즐겨찾기 ✅
- [x] `FavoriteEntity.kt` + `FavoriteDao.kt` + `GalleryDatabase.kt`
- [x] `FavoriteRepository.kt`
- [x] `DatabaseModule.kt`
- [x] `FavoriteViewModel.kt`
- [x] `FavoriteScreen.kt` — `MediaGridScreen` 공용 컴포넌트 재사용 (하단 내비게이션 없음, 스티키 헤더 없음, 단순 그리드)
- [x] Photo View 하트 버튼 연동
- [x] 즐겨찾기 스크린 썸네일 우상단 빨간 하트 표시 (columnCount ≤ 3 단계에서만, 선택 모드 시 숨김)

### [P2-2] 휴지통 ✅
- [x] `TrashRepository.kt` (IS_TRASHED 쿼리)
- [x] `TrashViewModel.kt`
- [x] `TrashScreen.kt` — `MediaGridScreen` 공용 컴포넌트 재사용 (하단 내비게이션 없음, 스티키 헤더 없음, 단순 그리드)
- [x] 실제로 삭제를 수행하진 않고 그런 흉내만 냄.
- [x] 방금 삭제한 파일 기준 30일 후에 삭제되는 것처럼 썸네일 하단에 '30일'이라고 표시함. 삭제한 지 하루 지난 건 29일로. (하지만 실제로 30일이 지난다고 해서 파일이 삭제되지는 않음.)
- [x] 휴지통 포토뷰 하단 메뉴 → 복원 + 삭제 버튼만 표시 (복원만 동작, 삭제는 Toast)

### [P2-3] 공유 기능 ✅
- [x] Android Sharesheet 연동 (단일/다중 선택 공유)
- [x] `util/ShareUtil.kt` — 공통 공유 유틸 (단일: ACTION_SEND, 다중: ACTION_SEND_MULTIPLE)
- [x] PhotoMainScreen, AlbumViewScreen, VideoScreen, RecentsScreen, FavoriteScreen 연동

### [P2-4] 검색 스크린 ⏳
- [x] 구현 계획 취소됨.

### [P2-5] 위치 + 지도 스크린 ✅
- [x] EXIF GPS 파싱 유틸 (`util/ExifLocationUtil.kt`)
- [x] `LocationRepository.kt` (EXIF 읽기 + Geocoder 캐싱 + 국가/도시 그룹핑)
- [x] `LocationViewModel.kt` + `LocationScreen.kt` (국가 섹션 헤더 + 도시 카드 + 빈 상태)
- [x] `MapViewModel.kt` + `MapScreen.kt` (GoogleMap + 클러스터 마커 + BottomSheet)
- [x] secrets-gradle-plugin 설정 (`local.properties` → `MAPS_API_KEY`)
- ⚠️ **지도 화면(MapScreen)은 Google Maps API 유료 결제 필요로 인해 추후 개발로 미룸.**
  LocationScreen(위치 목록)까지는 정상 동작. 지도 화면은 API 키 발급 후 재개 예정.

### [P2-6] `PhotoMainScreen.kt`에서 다중 선택 시 
- [x] 다중 선택 후 하단 메뉴에서 즐겨찾기 버튼 눌렀을 때 한꺼번에 즐겨찾기하는 기능.
- [x] 다중 선택 후 하단 메뉴에서 삭제(휴지통) 버튼 눌렀을 때 한꺼번에 휴지통에 들어가는 기능.

### [P2-7] `PhotoMainScreen.kt`, `MediaGridScreen.kt`의 썸네일 패딩
- [x] 썸네일 padding 값이 아주 작게 들어가도록(그래서 썸네일끼리 붙어있을 때 흰색 틈이 보이게) 수정하기.

### [P2-8] `MediaGridScreen.kt`을 사용하는 화면들에서 핀치인/아웃 기능 확인 ✅
- [x] `scaleAccumulator`를 Compose State → `pointerInput` 블록 내부 로컬 변수로 이동해 recompose 유발 방지
- [x] `pointerInput(Unit)` → `pointerInput(onPinchIn, onPinchOut)` key 변경으로 람다 최신화 보장
- [x] `PhotoMainScreen.kt`의 핀치 핸들러도 동일하게 수정 + 디버그 오버레이 제거

### [P2-9] `PhotoMainScreen.kt`과 `AlbumViewScreen.kt`에서 다중 선택 시 기능 ✅
- [x] 다중 선택 후 뒤로가기 버튼(시스템 버튼)을 눌렀을 때 선택 취소 (`BackHandler` 적용)
- [x] 다중 선택 시 하단 메뉴(사진/앨범/스토리/메뉴) 사라짐 — `onSelectionModeChange` 콜백으로 GalleryNavHost에 상태 전달
- [x] `MediaGridScreen.kt` 사용 화면(즐겨찾기·휴지통·최근·동영상)에서도 다중 선택 중 뒤로가기 시 선택 취소 (`BackHandler` 추가)
- [x] `AlbumViewScreen.kt` 다중 선택 즐겨찾기/삭제 버튼 동작 구현 (`AlbumViewViewModel`에 `FavoriteRepository` 주입)

### [P2-10] `AlbumViewScreen.kt`에서 손가락 두 개가 닿았을 때 ✅
- [x] 스와이프 핸들러에 `isMultiTouch` 플래그 추가 — 두 손가락 감지 시 드로어 열기/닫기 비활성화

### [P2-11] 화면 간 이동 ✅
- [x] `NavDisplay`에 `transitionSpec`/`popTransitionSpec` 설정 — 진입 180ms fade, 뒤로가기 100ms fade

---

## Phase 3 — 선택 기능 (최후순위)

### [P3-1] 스토리 리스트 스크린 ✅
- [x] `Story.kt` 도메인 모델 (bucketId 기반, count 프로퍼티)
- [x] `StoryViewModel.kt` — bucketId 그룹핑, 3장 이상 버킷만 스토리 생성
- [x] `StoryListScreen.kt` — HorizontalPager 캐러셀(peek), LazyRow 최근, 전체너비 정사각형 다양한 스토리

### [P3-2] 스토리 뷰 스크린 ✅
- [x] `StoryViewScreen.kt` — Ambient Mode 블러 배경 + CrossFade 슬라이드쇼
- [x] `StoryViewViewModel.kt` — 200ms 타이머, 일시정지 연속성(elapsedInCurrentSlideMs)
- [x] 재생 컨트롤: 좌/중/우 탭존 + 하단 🎵|▶시간|뮤트
- [x] 재생속도 DropdownMenu (0.5x/1x/1.5x/2x)
- [x] 배경음악 ModalBottomSheet + `res/raw/autumn_day.mp3` 번들 (ExoPlayer 연결은 추후)

---

## 마지막 업데이트
2026-04-14 | P3-1·P3-2 완료 — 스토리 리스트(HorizontalPager 캐러셀·LazyRow·전체너비카드) + 스토리 뷰(Ambient Mode·CrossFade·200ms 타이머·음악 바텀시트)
2026-04-14 | 테마 개선 — `Theme.kt` surface·background·surfaceContainer 계열 흰색 고정 (다이나믹 컬러 회색 틴트 제거)
2026-04-14 | P2-1·P2-2 마무리 — 즐겨찾기 하트배지·휴지통포토뷰하단메뉴·MediaGridScreen BackHandler·AlbumView 즐겨찾기/삭제 구현
2026-04-14 | P2-3·P2-8·P2-9·P2-10·P2-11 완료 — 공유·핀치줌버그·뒤로가기선택취소·내비바숨김·두손가락드로어방지·화면전환속도
2026-04-13 | P2-5 완료(부분) — LocationScreen 동작 확인. MapScreen은 Google Maps API 키 필요로 추후 개발 미룸
2026-04-13 | P1-9 완료 — VideoScreen·RecentsScreen·MediaGridScreen 공용 컴포넌트·SelectionActionBar 더보기·NavigationBar 숨김
2026-04-13 | P1-7 완료 — PhotoViewScreen (HorizontalPager + ZoomableImage + ThumbnailStrip + 상세정보 BottomSheet + BottomActionBar)
2026-04-13 | P1-8 완료 — VideoOverlay (ExoPlayer + 뮤트 버튼)
