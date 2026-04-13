# 갤러리 앱 구현 진행도

---

## 전체 진행도
██████████████░░░░░░ 62% (10/16)

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

### [P2-1] 즐겨찾기 ⏳
- [x] `FavoriteEntity.kt` + `FavoriteDao.kt` + `GalleryDatabase.kt`
- [x] `FavoriteRepository.kt`
- [x] `DatabaseModule.kt`
- [x] `FavoriteViewModel.kt`
- [x] `FavoriteScreen.kt` — `MediaGridScreen` 공용 컴포넌트 재사용 (하단 내비게이션 없음, 스티키 헤더 없음, 단순 그리드)
- [x] Photo View 하트 버튼 연동

### [P2-2] 휴지통 ⏳
- [x] `TrashRepository.kt` (IS_TRASHED 쿼리)
- [x] `TrashViewModel.kt`
- [x] `TrashScreen.kt` — `MediaGridScreen` 공용 컴포넌트 재사용 (하단 내비게이션 없음, 스티키 헤더 없음, 단순 그리드)
- [x] 실제로 삭제를 수행하진 않고 그런 흉내만 냄.
- [x] 방금 삭제한 파일 기준 30일 후에 삭제되는 것처럼 썸네일 하단에 '30일'이라고 표시함. 삭제한 지 하루 지난 건 29일로. (하지만 실제로 30일이 지난다고 해서 파일이 삭제되지는 않음.) 

### [P2-3] 공유 기능 ⏳
- [ ] Android Sharesheet 연동 (단일/다중 선택 공유)

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

### [P2-8] `MediaGridScreen.kt`을 사용하는 화면들에서 핀치인/아웃 기능 확인
- [ ] `PhotoMainScreen.kt`처럼 핀치 줌: 1.5 / 3 / 4 / 7 / 11 / 20단 전환(11·20단은 저해상도 썸네일)이 되어야 하는데 안 되고 있음. 구현은 이미 되어있는데 버그 때문에 기능하지 못 하는 것 같음.

### [P2-9] `PhotoMainScreen.kt`과 `AlbumViewScreen.kt`에서 다중 선택 시 기능 
- [ ] 다중 선택 후 뒤로가기 버튼(시스템 버튼)을 눌렀을 때 선택 취소 되어야 함(지금은 앱이 종료되어 버림)
- [ ] `PhotoMainScreen.kt`과 `AlbumViewScreen.kt`에서 다중 선택 시 하단 메뉴(사진/앨범/스토리/메뉴)가 사라져야 함. (다중 선택이 취소 되면 다시 생김)

### [P2-10] `AlbumViewScreen.kt`에서 손가락 두 개가 닿았을 때
- [ ] 손가락 2개가 닿았다는 것은 핀치 줌을 시도하는 것임. 즉, 손가락 2개 닿은 상태에서는 드로어가 열리거나 닫히면 안 되도록 수정해야 함.

---

## Phase 3 — 선택 기능 (최후순위)

### [P3-1] 스토리 리스트 스크린 ⏳
- [ ] `Story.kt` 도메인 모델
- [ ] `StoryViewModel.kt` + `StoryListScreen.kt`
- [ ] 자동 생성 로직 (P3 구현 전 기준 확정 필요)

### [P3-2] 스토리 뷰 스크린 ⏳
- [ ] `StoryViewScreen.kt` — CrossFade 슬라이드쇼 + LinearProgressIndicator
- [ ] 재생 컨트롤 (이전/재생·일시정지/다음)
- [ ] 재생속도 DropdownMenu (0.5x/1x/1.5x/2x)
- [ ] 배경음악 ModalBottomSheet

---

## 마지막 업데이트
2026-04-13 | P2-5 완료(부분) — LocationScreen 동작 확인. MapScreen은 Google Maps API 키 필요로 추후 개발 미룸
2026-04-13 | P1-9 완료 — VideoScreen·RecentsScreen·MediaGridScreen 공용 컴포넌트·SelectionActionBar 더보기·NavigationBar 숨김
2026-04-13 | P1-7 완료 — PhotoViewScreen (HorizontalPager + ZoomableImage + ThumbnailStrip + 상세정보 BottomSheet + BottomActionBar)
2026-04-13 | P1-8 완료 — VideoOverlay (ExoPlayer + 뮤트 버튼)
