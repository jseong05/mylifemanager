# 📱 마이 라이프 메니저 (My Life Manager)

> **스마트한 일상 관리 & 자산 관리 안드로이드 애플리케이션**  
> 용돈 관리, 오늘 권장 지출액 계산, 일정 관리 및 정교한 알람 기능, 실시간 날씨 정보 연동까지 제공하는 Material3 기반 라이프 스타일 매니저 앱입니다.

---

## 🌟 주요 기능 (Key Features)

### 🏠 1. 메인 대시보드 (Dashboard)
- **개인화 인사말 & 날짜**: 사용자 이름 및 당일 날짜 자동 표시
- **자산 요약 카드**: 현재 총 잔액 및 오늘 지출 금액 실시간 연동
- **🌤️ 실시간 날씨 연동**: OpenWeatherMap API 기반 위치/날씨 정보 수신 및 대시보드 표시
- **💡 D-Day 및 권장 지출액 계산**:
  - 다음 용돈 받는 날까지의 D-Day 자동 계산
  - `현재 잔액 / 남은 D-Day` 기반으로 **오늘 권장 지출액** 자동 계산
  - 잔액 마이너스 시 위트 있는 위기 경고 메시지 출력
- **오늘의 일정 & 최근 내역 요약**: 메인 화면에서 바로 확인 가능한 카드형 리스트

### 💰 2. 가계부 (Expense Tracker)
- **지출 및 수입 CRUD**: 내역 추가, 수정, 스와이프 삭제 기능
- **낭비 챌린지**: 지출 내역 등록 시 '낭비' 여부 체크 및 관리
- **스마트 오늘 지출 측정**: 타임스탬프(`timestamp`) 기반 당일 지출만 정확히 합산
- **Material3 다이얼로그**: `MaterialAlertDialogBuilder` 및 `TextInputLayout`(OutlinedBox) 기반의 깔끔한 입력 폼

### 📅 3. 계획표 & 알람 (Schedule & Alarm)
- **일정 관리 CRUD**: 일정 등록, 완료 체크, 수정, 스와이프 삭제
- **몇 분 전 알람 보내기**: 일정 시간 10분 전 / 30분 전 / 1시간 전 맞춤형 알림 설정
- **시스템 알람 연동**: `AlarmManager` + `BroadcastReceiver` (`ScheduleAlarmReceiver`) 기반 정확한 시간에 노티피케이션 전송
- **자동 알람 취소**: 일정 스와이프 삭제 시 해당 등록 알람 자동 취소 처리

### ⚙️ 4. 설정 (Settings)
- **프로필 & 잔액 설정**: 사용자 이름, 초기 잔액 설정
- **용돈 설정**: 용돈 금액, 지급 주기(월간/주간) 및 지급 날짜/요일 지정
- **안전 데이터 초기화**: 실수 방지를 위한 `MaterialAlertDialogBuilder` 2차 확인 다이얼로그 적용

---

## 🎨 디자인 시스템 & UX (Design System)

- **Material Design 3 (M3)**: Teal Color Palette 기반의 감각적인 디자이너 테마
- **Dark Mode (다크 모드 지원)**: `values` / `values-night` 컬러 팔레트를 통한 완성도 높은 야간 모드 지원
- **Edge-to-Edge**: 상/하단 투명 시스템 바 적용으로 넓은 시야 확보
- **즉시 화면 전환 (NoAnimation)**: 테마 수준 및 코드 레벨에서 애니메이션을 비활성화하여 빠른 응답 속도 제공
- **입력 힌트 (Ex 가이드)**: 사용자가 입력을 시작하면 자동으로 사라지는 직관적인 하드코딩 예시 힌트 지원

---

## 🛠️ 기술 스택 (Tech Stack)

| 구분 | 기술 / 라이브러리 |
| :--- | :--- |
| **언어 (Language)** | Java |
| **최소 SDK (Min SDK)** | API 24 (Android 7.0) |
| **타겟 SDK (Target SDK)** | API 34 (Android 14) |
| **UI Toolkit** | Material Components for Android (Material3), ConstraintLayout |
| **View Binding** | Android ViewBinding |
| **데이터 저장 (Persistence)** | SharedPreferences + Google Gson (`MyDataStore`) |
| **시스템 서비스 (System)** | AlarmManager, BroadcastReceiver, NotificationManager |
| **네트워킹 (Networking)** | HttpURLConnection (`WeatherClient`), OpenWeatherMap API |

---

## 📂 프로젝트 구조 (Project Structure)

```
app/src/main/
├── java/com/mlm/
│   ├── MainActivity.java           # 메인 대시보드 화면
│   ├── ExpenseActivity.java        # 가계부 화면
│   ├── ScheduleActivity.java       # 계획표 화면
│   ├── SettingsActivity.java       # 설정 화면
│   ├── Expense.java                # 가계부 데이터 모델 (Timestamp 지원)
│   ├── Schedule.java               # 일정 데이터 모델
│   ├── ExpenseAdapter.java         # 가계부 리사이클러뷰 어댑터
│   ├── ScheduleAdapter.java        # 계획표 리사이클러뷰 어댑터
│   ├── MyDataStore.java            # SharedPreferences & Gson 데이터 매니저
│   ├── ScheduleAlarmReceiver.java  # 일정 알림 브로드캐스트 리시버
│   ├── WeatherClient.java          # OpenWeatherMap 날씨 API 클라이언트
│   └── WeatherResponse.java        # 날씨 API 응답 모델
└── res/
    ├── drawable/                   # 벡터 아이콘 (ic_home, ic_wallet, ic_calendar, ic_add 등)
    ├── layout/                     # 화면 및 아이템, 다이얼로그 레이아웃
    ├── menu/                       # BottomNavigationView 메뉴 (bottom_nav_menu.xml)
    └── values/                     # Material3 테마, colors, strings
```

---

## 🚀 빌드 및 실행 (Build & Run)

1. Android Studio에서 프로젝트를 오픈합니다.
2. Gradle Sync를 수행합니다.
3. 연결된 Android 에뮬레이터 또는 실기기(Android 7.0 이상)에서 `app` 모듈을 실행합니다.

```bash
# 커맨드라인 빌드
./gradlew assembleDebug
```
