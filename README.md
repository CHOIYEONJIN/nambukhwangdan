
# 📌 1. 현재까지 구현된 기능

🔷 앱 구조

Jetpack Compose 기반 화면 구성

Room DB 사용 → Diary 저장 / 불러오기

Firestore 사용 → Diary 저장 (set().await()) 정상 작동

Firebase Functions + HuggingFace API 감정 분석 기능 완료

Hilt 기반 의존성 주입 구조 완료

Diary / Letter 도메인 모델, Mapper, Repository 구조 확립

ViewModel에서 UI 상태 관리 흐름 OK

🔷 현재 동작 확인된 흐름 (완료된 것)
[사용자 입력] → Compose UI → ViewModel → Room DB 저장 ✔  
                                    ↓
                             Firestore에도 저장 ✔  
                                    ↓
                  감정 분석 → Firestore 결과 업데이트도 가능 ✔

🔷 주요 화면
화면	역할	Firestore 관련 상태

DiaryWriteScreen	일기 작성 & DB 저장	Firestore 저장 완료

AnalyzeLoadingScreen	감정 분석 호출	성공

AnalyzeResultScreen	감정 결과 표시	UI 반영만 진행됨

EmotionCalendarScreen	글 목록 표시	Room 데이터만 사용

InboxScreen, LetterToTomorrowScreen	편지 저장까지는 가능	Firestore 읽기 기능 없음

# 📌 2. 팀원이 실행하기 위해 필요한 작업
🔧 반드시 필요한 것
항목	설명
google-services.json	각자 Firebase Console에서 다시 발급 → app/ 폴더에 직접 추가
.env / secrets	HuggingFace API Key는 절대 커밋 금지 → Cloud Functions에서 Secret으로 사용
Firebase 프로젝트 연동	firebase init → 기존 프로젝트 선택해서 Functions 연동 필요
Firestore 규칙 확인	로그인 여부 따라 read/write 제한 가능
🔧 팀원이 실행할 시 필요한 커맨드
# Firebase 연결
firebase login
firebase use --add   # 프로젝트 연결

# Functions 설치
cd functions
npm install

# Firebase functions deploy
firebase deploy --only functions


⚠ Room DB는 앱 내부 저장이므로 그대로 사용 가능.
Firestore 연결은 반드시 google-services.json + firebase init 필요.

# 📌 3. 앞으로 구현해야 할 기능

🚨 현재 부족한 부분 

구현 필요 항목	현재 상태	필요성

Firestore → 앱으로 불러오기(Read)	❌ 없음	앱 재실행 시 글이 안불러와짐

Firestore ↔ Room DB 동기화	❌ 없음	오프라인/온라인 모드 문제

감정 분석 결과 UI 반영	🔶 부분완료	Firestore 저장은 OK, UI 연결 필요

수정/삭제 Firestore 반영	❌ 없음	DB와 Firestore 불일치 가능

EmotionCalendar + Firestore 연동	❌ 없음	감정 분석 UI와 연동해야 함
