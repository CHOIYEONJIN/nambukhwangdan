# 📌 앱 개발 진행 상황

## 📌 1. 현재까지 구현된 기능


| 기술 | 구현 상태 |
|------|------------|
| Jetpack Compose UI | ✔ 전체화면 구현 |
| MVVM + Repository | ✔ 상태 관리 및 데이터 흐름 |
| Hilt DI | ✔ 의존성 주입 완료 |
| Room DB | ✔ Diary / Letter 저장 & 조회 |
| Firestore | ✔ 저장(set()) 가능 |
| Firebase Functions | ✔ 호출 성공 |
| HuggingFace API (Secret 방식) | ✔ 연동 완료 |
| Mapper 구조 | ✔ Domain ↔ Entity 변환 완료 |
| ViewModel | ✔ UI State & 비즈니스 로직 처리 |

---

### 🔄 데이터 흐름 (현재 정상 동작)

    [사용자 입력]
        ↓
    Compose UI
        ↓
    ViewModel (상태 관리)
        ↓
    Room DB 저장 (local)
        ↓
    Firestore 저장 (remote)
        ↓
    Firebase Functions → 감정 분석 호출
        ↓
    Firestore sentiment 업데이트 완료

---

### 📱 주요 화면 & 상태

| 화면 / 기능                    | 현재 구현 상태                                   | 필요한 추가 작업                                      |
|------------------------------|--------------------------------------------------|-------------------------------------------------------|
| DiaryWriteScreen             | Room + Firestore 저장 기능 ✔                    | UI 안정화                                              |
| AnalyzeLoadingScreen         | 감정 분석 대기 화면 ✔                            | 필요 시 로딩시간 변경 (현재 7초로 설정됨)                 |
| AnalyzeResultScreen          | 감정 결과 표시 및 Firestore 저장 ✔              | UI 개선 / 감정 결과 확인 흐름 정리 필요               |
| EmotionCalendarScreen        | Room 기반 감정 표시 ✔                            | Firestore → Room sync 후 UI 반영 구조 설계 필요        |
| JournalListScreen            | 목록 조회 및 감정 스티커 표시 ✔                  |  UI Dialog 등 개선 필요           |
| InboxScreen                  | 임시 UI 구현됨                              | 실제 편지 DB 구조 연결 필요                            |
| LetterToTomorrowScreen       | 작성 화면 UI 일부 존재                            | 미래 날짜 설정 및 받기 기능 구현 필요              |
| Firestore → Room 동기화      | ❌ 미구현                                         | 앱 실행 시 sync 처리(ViewModel 또는 Repository) 필요  |
| 로그인 기반 UID 구조화       | ✔ 구현 완료                                       | Firestore 컬렉션 구조를 UID 기반으로 정리 필요         |
| 편지 기능 전체               | Room 기준 일부 UI만 존재                         | Firestore 연동 여부 판단 후 로직 필요 여부 결정        |


---

## 📌 2. 팀원이 실행하기 위해 필요한 설정

### ⚠️ 반드시 필요한 항목

| 항목 | 설명 |
|------|------|
| `google-services.json` | Firebase Console → 직접 발급 → `app/` 폴더에 추가 |
| Firebase Secrets | HuggingFace API Key는 절대 커밋 금지 |
| Firebase 프로젝트 연동 | `firebase init` 후 기존 프로젝트 선택 |
| Firestore 보안 규칙 | 인증에 따라 read/write 제한 가능 |

---

### 🔧 실행 시 필요한 커맨드 모음

```bash
# Firebase 로그인 및 프로젝트 연결
firebase login
firebase use --add           # 기존 프로젝트 선택

# Functions 설치
cd functions
npm install

# Secret 등록 (API Key 입력 모드가 나옴)
firebase functions:secrets:set huggingface_key

# Functions 배포
firebase deploy --only functions

🔐 API Key 보안 처리 방식

현재 프로젝트는 API Key를 하드코딩하지 않습니다.
Firebase Secret Manager를 통해 암호화된 방식으로 함수 내에서만 접근합니다.

// index.ts 내부 코드
const HF_API_KEY = defineSecret("huggingface_key");
const apiKey = HF_API_KEY.value();

따라서 json 파일이나 env 파일은 GitHub에 포함되지 않으며,
매 실행마다 Secret Manager에서 key를 로드하는 방식입니다.

