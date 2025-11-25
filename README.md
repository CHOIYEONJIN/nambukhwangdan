**구현해야하는 기능들**

*알림 기능 구현

홈 화면 편지 랜덤 위치 변경 분기점 설정

ㄴ 홈 화면 편지 실제 일기 데이터가 적용되게 변경

랜덤 편지 전송 시스템 구현

미래 편지 받는 시스템 구현

Sentiment Clova API 적용

랜덤 편지 내용 필터링 API 적용

FireStore 및 firebase 구현 완료

감정 캘린더 날짜 클릭 시 일기 내용 보이게

설정화면에서 닉네임 변경 및 로그아웃 구현

diary/letter 구분 필요 (data 나눌것인지? 어쩔 것인지)**


**현재 진행 상황**	

RoomData, Hilt 설치 및 사용 준비 완료

Diary -> DiaryEntity 변환을 위한 diaryMapping 파일 작성 완료

감정캘린더 달력 구현 완료


**진행 중인 사항**

실제 일기 앱 내부 데이터를 DB에 저장해 사용할 수 있도록 연결 필요

연결된 데이터 기반 감정캘린더 화면 연결

각 날짜 클릭 시 하단에 일기 표시 UI 제작 필요

FireBase 연결


**새로 추가된 파일별 기능 설명**

DiaryEntity -> diary를 DB에 저장 가능하게 만든 버전
=> 실제 DB에 저장되는 Data 형식이라고 볼 수 있음

AppDataBase.kt -> RoomDB로서 DB 구조 정의 및 DAO를 통해 data 꺼냄

DiaryDao -> DB로부터 Data를 가져오는 함수를 가짐 
(약간 data class에 접근하는 역할을 하는 viewmodel 같은 느낌)

DiaryRepository-> Dao 를 통해 DB에게서 Data를 전달받음

AppModule -> Hlit 설정해주는 파일

Converters-> RoomDB가 읽을 수 있는 자료형으로 변환해 줌
