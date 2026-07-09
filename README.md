# 트립포켓 TripPocket
- Android Native App, 1인 제작

## 개요
### 국내의 여행지를 한 눈에 쉽게 볼 수 있는 여행 관광지 소개 앱입니다.
- 국내 지역 기반으로 검색하여 [관광지, 문화시설, 지역행사, 레포츠, 여행코스] 탭 별로 여행지 정보를 제공합니다.
- 관심있는 여행지는 북마크 기능을 통해 한 눈에 모아 볼 수 있습니다.

## 기술 스택
- 개발언어 : Java
- 네트워크 : ExcutorService, XmlPullParser
- DB : RoomDB
- 개발환경 : Android Studio
- 오픈소스 : 한국관광공사 TourAPI

## 사용기술
- Room Database
- ExecutorService 기반 멀티스레드 처리
- XmlPullParser를 이용항 XML Parsing
- RecyclerView
- Shimmer Effect
- 무한 스크롤 페이징 구현

## 와이어프레임 / 시스템 구조도
<img src="images/wireframe_structure.png" width="300">

## UI Flow
<img src="images/ui_flow.png" width="300">

## 주요기능
### 1) 지역 기반 검색
- 지역명, 시군구명을 선택하여 여행지를 검색할 수 있다.
- 탭 카테고리 별로 여행지를 분류하여 검색할 수 있다.
<img src="images/main.jpg" width="200">

### 2) 지역 상세 조회
- 선택한 여행지의 상세 정보를 볼 수 있다.
(1) 관광지/문화시설/축제공연행사/레포츠
- 여행지의 소개정보(이용시간, 이용요금, 주차시설, 주차요금, 행사기간 등)를 조회할 수 있다.
- 주소를 클릭하면 카카오맵이 연동된다.
<img src="images/detail01_1.jpg" width="200"> <img src="images/detail01_2.jpg" width="200"> <img src="images/kakaomap.jpg" width="200">
(2) 여행코스
- 여행코스 목록을 볼 수 있다.
<img src="images/recommend01_1.jpg" width="200"> <img src="images/recommend01_2.jgp" width="200">

### 3) 북마크 등록/해지
- 관심있는 여행지를 북마크 할 수 있으며, 북마크 목록을 조회/관리 할 수 있다.
<img src="images/bookmark_list.jpg" width="200">

