# Data Structure & Rules (v2.2)

## 1. 데이터 모델 (Data Models)

### A. DutyTable
- `displayName`: 테이블의 고유 식별자 및 표시 이름.
- `capacity`: 해당 근무에 투입되는 총 인원 (2, 3, 4명 등).
- `ptEffect`: PT 근무 시 적용되는 효과 (`LATE_START`, `EARLY_FINISH`).
- `slots`: 시간대별 근무 정보를 담은 `DutySlot` 리스트.
- `alertOnFinish`: 해당 테이블에서 종료 알람을 기본으로 사용할지 여부.

### B. DutySlot
- `startTime`, `endTime`: "HH:mm" 형식의 문자열.
- `locations`: `capacity` 크기와 동일한 장소 리스트. (인덱스 = 근무 번호 - 1)
- `alerts`: 각 번호별 알람 활성화 여부 리스트.

## 2. 계산 규칙 (Calculation Rules)

- **PT 시간 조정**:
    - `LATE_START`: 전체 근무 시작 시점을 30분 뒤로 미룹니다.
    - `EARLY_FINISH`: 전체 근무 종료 시점을 30분 앞으로 당깁니다.
- **슬롯 필터링**: PT 적용 후의 유효 근무 시간대와 겹치지 않는 슬롯은 자동으로 제거됩니다.
- **알람 생성**: `DutyCalculator.getAlarmSchedules`를 통해 시작 알람과 종료 알람(선택)을 생성합니다.

## 3. 데이터 퍼시스턴스
- **DataStore**: 모든 설정은 `androidx.datastore`를 통해 비동기적으로 관리됩니다.
- **JSON 직렬화**: 커스텀 편성표 리스트는 `kotlinx-serialization`을 사용하여 JSON 문자열로 변환되어 저장됩니다.
- **이미지 경로**: `res:` 프리픽스(리소스 이미지) 또는 절대 경로(내부 저장소 이미지)를 파이프(`|`)로 연결하여 관리합니다.
