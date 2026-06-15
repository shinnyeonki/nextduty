# NextDuty App Architecture (v2.0)

본 문서는 하단 바와 내비게이션 Drawer가 도입된 NextDuty 앱의 아키텍처를 정의합니다.

## 1. UI 구조 (Navigation)

### A. Bottom Navigation (주요 기능)
- **Home (홈)**: 실시간 근무 현황(Dashboard) 및 근무 설정(Setup) 화면.
- **Work Gallery (근무표)**: 사용자가 직접 촬영/등록한 근무표 이미지 관리.
- **Table Gallery (편성표)**: 앱에서 기본 제공하는 공식 편성표 이미지 열람.

### B. Navigation Drawer (관리 기능)
- **편성표 수정**: 근무 데이터(시간, 장소)를 직접 편집.
- **순찰**: 순찰 지점 체크리스트 실행.
- **앱 설정**: 현재 설정된 근무(조, 번호)를 초기화하고 재설정.

## 2. 레이어 역할

- **UI Layer (Compose)**: 하단 바의 탭 상태와 Drawer의 이벤트를 처리하며, `AnimatedContent`를 통해 화면 전환을 수행합니다.
- **Data Layer (DutyCore)**: 모든 데이터 계산의 원천입니다. UI 레이어는 `DutyCore`에서 계산된 `DutyInfo` 객체만을 바라봅니다.
- **Persistence Layer (DataStore)**: 수정된 편성표(`List<DutyTable>`)를 JSON으로 직렬화하여 영구 저장합니다.
