# 📱 마이 라이프 메니저 (My Life Manager)

> 가계부와 일정, 용돈 계획을 한곳에서 관리하는 Android 앱입니다.  
> 잔액과 오늘의 지출을 확인하고, 일정 알림과 날씨 정보를 대시보드에서 확인할 수 있습니다.

## 🌟 주요 기능

### 🏠 대시보드
- 사용자 이름과 오늘 날짜 표시
- 현재 잔액 및 오늘 지출 금액 표시
- 설정한 용돈 지급 주기에 따른 지급일까지 남은 날짜 계산
- 현재 잔액과 남은 날짜를 바탕으로 오늘 권장 지출액 계산
- 오늘의 일정과 가계부 내역 요약
- GPS 또는 설정한 도시를 기준으로 날씨 정보 표시

### 💰 가계부
- 지출·수입 내역 추가, 수정, 스와이프로 삭제
- 내역 제목을 기준으로 실시간 검색
- 지출 내역에 '낭비' 표시
- 이번 달 낭비 지출 건수와 금액 집계
- 지출 및 수입 내역의 날짜와 금액 표시
- 오늘 지출액과 현재 잔액 계산

### 📅 계획표와 알림
- 날짜별 일정 확인 및 달력에서 일정이 있는 날짜 표시
- 일정 추가, 수정, 완료 표시, 스와이프로 삭제
- 일정 제목 검색
- 반복 주기 설정: 반복 없음, 매주, 매월, 매년
- 일정 시각의 알림 또는 사용자 지정 시간 전 알림 설정
- 일정 알림은 Android `AlarmManager`와 `BroadcastReceiver`를 사용합니다.
- 매일 오후 9시에 오늘 지출이 없는 경우 지출 기록 리마인더를 표시합니다.

> 반복 일정은 달력과 일정 목록에 반복 표시되도록 구현되어 있습니다. 일정 알림은 일정의 날짜와 시각을 기준으로 예약됩니다. Android 버전과 알람 권한 설정에 따라 알림 시각이 달라질 수 있습니다.

### ⚙️ 설정
- 사용자 이름과 초기 잔액 설정
- 용돈 금액, 지급 주기(주간·월간), 지급 요일 또는 날짜 설정
- GPS 위치 사용 여부와 날씨 표시 도시 설정
- 저장된 앱 데이터 초기화

## 🛠️ 기술 스택

| 구분 | 기술 |
| --- | --- |
| 언어 | Java |
| Android 최소 SDK | API 24 (Android 7.0) |
| Compile SDK | API 37 |
| Target SDK | API 37 |
| Java 소스 호환성 | Java 11 |
| UI | Android Views, View Binding, Material Components, ConstraintLayout |
| 로컬 데이터 저장 | SharedPreferences, Gson |
| 일정 달력 | Material CalendarView |
| 날씨 네트워크 통신 | `HttpURLConnection`, OpenWeatherMap API |
| 알림 | `AlarmManager`, `BroadcastReceiver`, `NotificationManager` |

## 📂 프로젝트 구조

```text
app/
├── src/main/
│   ├── java/com/mlm/
│   │   ├── MainActivity.java                 # 대시보드
│   │   ├── SplashActivity.java               # 스플래시 화면
│   │   ├── ExpenseActivity.java               # 가계부
│   │   ├── ScheduleActivity.java              # 계획표와 달력
│   │   ├── SettingsActivity.java              # 앱 설정
│   │   ├── Expense.java                       # 가계부 데이터 모델
│   │   ├── Schedule.java                      # 일정 데이터 모델
│   │   ├── ExpenseAdapter.java                # 가계부 목록 어댑터
│   │   ├── ScheduleAdapter.java               # 일정 목록 어댑터
│   │   ├── MyDataStore.java                   # SharedPreferences/Gson 데이터 관리
│   │   ├── WeatherClient.java                 # 날씨 API 클라이언트
│   │   ├── WeatherResponse.java               # 날씨 API 응답 모델
│   │   ├── ScheduleAlarmReceiver.java         # 일정 알림 처리
│   │   └── DailyExpenseAlarmReceiver.java     # 지출 리마인더 처리
│   └── res/
│       ├── layout/                            # 화면 및 목록 레이아웃
│       ├── menu/                              # 하단 내비게이션 메뉴
│       └── values/                            # 문자열, 테마, 색상 등
├── src/test/                                  # 로컬 단위 테스트
└── src/androidTest/                           # Android 기기 테스트
```

## 🚀 빌드 및 실행

1. Android Studio에서 프로젝트를 엽니다.
2. Gradle Sync를 수행합니다.
3. Android 7.0(API 24) 이상 에뮬레이터 또는 기기에서 `app` 모듈을 실행합니다.

명령줄에서 빌드하려면 다음을 실행합니다.

```bash
./gradlew assembleDebug
```

로컬 단위 테스트 실행:

```bash
./gradlew test
```

## 🌦️ 날씨 API 설정

날씨 기능을 사용하려면 OpenWeatherMap API 키를 프로젝트 루트의 `local.properties`에 설정합니다.

```properties
weatherApiKey=YOUR_OPENWEATHERMAP_API_KEY
```

Gradle은 이 값을 앱의 `BuildConfig`에 전달합니다. API 키는 저장소에 커밋하지 마세요. 키가 설정되지 않은 경우 앱 빌드는 가능하지만 날씨 조회는 정상적으로 동작하지 않을 수 있습니다.

GPS 기반 날씨를 사용하려면 앱에서 위치 권한을 허용해야 합니다. 권한이 없거나 GPS 위치를 사용할 수 없으면 설정한 도시의 날씨를 조회합니다.
