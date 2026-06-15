# Data Structure & Rules (v2.1)

## 1. 인원수(Capacity) 기반 규칙
- 모든 `DutyTable`은 `displayName`을 고유 식별자(ID)로 사용합니다. 별도의 `id` 필드는 사용하지 않습니다.
- `DutyTable`은 `capacity`(2, 3, 4)를 가집니다.
- `DutySlot`의 `locations` 리스트 길이는 반드시 해당 테이블의 `capacity`와 일치해야 합니다.
- **UI 연동**: 홈 화면의 "근무 번호" 선택 버튼은 `1..capacity` 범위 내에서만 동적으로 생성됩니다.

## 2. 테이블 관리 (생성 및 삭제) 제언

현재 편집기는 기존 슬롯의 수정만 지원하나, 테이블 자체를 관리하기 위해 다음과 같은 로직을 제안합니다.

### A. 테이블 생성 (Add)
- **방식**: JSON 리스트에 새로운 `DutyTable` 객체를 추가합니다.
- **필수 값**: 새로운 고유 `id` (예: `CUSTOM_1`), `displayName`, `capacity`.
- **초기화**: 선택한 인원수(`capacity`)만큼 빈 `LocationType.Off` 슬롯을 기본으로 생성하여 데이터 무결성을 유지합니다.

### B. 테이블 삭제 (Delete)
- **방식**: `id`를 기준으로 리스트에서 제거합니다.
- **주의사항**: 현재 사용자가 선택하여 사용 중인 테이블을 삭제할 경우, 즉시 `defaultTables`의 첫 번째 항목으로 강제 전환하는 예외 처리가 필요합니다.

## 3. 데이터 무결성
- "근무없음"은 문자열 "근무없음" 대신 `LocationType.Off` 객체로 처리합니다.
- 데이터 저장 시 `prettyPrint = true` 옵션을 사용하여 사용자가 수동으로 JSON을 확인하거나 백업할 때의 가독성을 확보합니다.
