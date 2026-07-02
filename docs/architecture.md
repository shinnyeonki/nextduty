# NextDuty App Architecture (v2.1)

본 문서는 하단 바와 내비게이션 Drawer가 포함된 NextDuty 앱의 최신 아키텍처를 정의합니다.

## 1. UI 구조 (Navigation)

### A. Bottom Navigation (주요 탭)
- **Home (홈)**: 실시간 근무 현황 대시보드 및 근무 설정(편집/조회) 화면.
- **근무표 사진첩**: 사용자가 등록한 개인 근무표 이미지 관리.
- **편성표 사진첩**: 앱에 등록된 공식 편성표 이미지 열람.

### B. Navigation Drawer (고급 기능)
- **근무 설정**: 현재 적용 중인 근무 조와 번호를 수정.
- **순찰**: 순찰 지점 체크리스트 실행.
- **알림 설정**: 알람 리드 타임(분) 및 종료 알림 여부 설정.
- **편성표 데이터 수정**: JSON 기반의 근무 데이터(슬롯, 장소) 직접 편집.
- **서비스 알람 스위치**: 전체 알람 스케줄링 활성/비활성 토글.

## 2. 레이어 역할

- **UI Layer (Compose)**: `MainApp`을 중심으로 탭 전환 및 Drawer 이벤트를 처리합니다. `Material3` 기반의 UI 컴포넌트를 사용합니다.
- **Logic Layer (DutyCalculator)**: 근무 규칙(PT 적용, 시간 계산)을 처리하는 순수 로직 계층입니다.
- **Infrastructure Layer (AlarmProvider)**: `AlarmManager` 및 `NotificationManager`를 제어하여 시스템 레벨의 알람을 관리합니다.
- **Persistence Layer (DutyRepository)**: `Jetpack DataStore`를 사용하여 설정 및 커스텀 데이터를 JSON 형식으로 저장합니다.
- **Storage Layer (ImageStorage)**: 외부 이미지를 앱 내부 저장소로 복사하고 관리합니다.
