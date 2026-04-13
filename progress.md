# 갤러리 앱 구현 진행도

---

## 전체 진행도
█████████████░░░░░░░ 56% (9/16)

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
- [ ] `FavoriteEntity.kt` + `FavoriteDao.kt` + `GalleryDatabase.kt`
- [ ] `FavoriteRepository.kt`
- [ ] `DatabaseModule.kt`
- [ ] `FavoriteViewModel.kt`
- [ ] `FavoriteScreen.kt` — `MediaGridScreen` 공용 컴포넌트 재사용 (하단 내비게이션 없음, 스티키 헤더 없음, 단순 그리드)
- [ ] Photo View 하트 버튼 연동

### [P2-2] 휴지통 ⏳
- [ ] `TrashRepository.kt` (IS_TRASHED 쿼리)
- [ ] `TrashViewModel.kt`
- [ ] `TrashScreen.kt` — `MediaGridScreen` 공용 컴포넌트 재사용 (하단 내비게이션 없음, 스티키 헤더 없음, 단순 그리드)
- [ ] API 30 미만 안내 문구 분기

### [P2-3] 공유 기능 ⏳
- [ ] Android Sharesheet 연동 (단일/다중 선택 공유)

### [P2-4] 검색 스크린 ⏳
- [ ] `SearchRepository.kt` (파일명 / 도시명 쿼리)
- [ ] `SearchViewModel.kt` + `SearchScreen.kt`
- [ ] DockedSearchBar + FilterChipRow + 최근 검색어

### [P2-5] 위치 + 지도 스크린 ⏳
- [ ] EXIF GPS 파싱 유틸
- [ ] `LocationViewModel.kt` + `LocationScreen.kt` (국가/도시 StickyHeader)
- [ ] `MapViewModel.kt` + `MapScreen.kt` (GoogleMap + 클러스터 마커 + BottomSheet)
- [ ] secrets-gradle-plugin 설정 (`local.properties` → `MAPS_API_KEY`)

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
2026-04-13 | P1-9 완료 — VideoScreen·RecentsScreen·MediaGridScreen 공용 컴포넌트·SelectionActionBar 더보기·NavigationBar 숨김
2026-04-13 | P1-7 완료 — PhotoViewScreen (HorizontalPager + ZoomableImage + ThumbnailStrip + 상세정보 BottomSheet + BottomActionBar)
2026-04-13 | P1-8 완료 — VideoOverlay (ExoPlayer + 뮤트 버튼)
