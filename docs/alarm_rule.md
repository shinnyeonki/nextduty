# 알람 및 전체 화면 알림 규칙 (Alarm & Full Screen Notification Rules)

본 문서는 Android 14(API 34) 이상의 최신 정책을 반영하여, 앱이 백그라운드 상태이거나 화면이 켜져 있는 상황에서도 안정적으로 전체 화면 알람을 표시하기 위한 구현 규칙을 정의합니다.

## 1. 기본 원칙
1. **사용자 경험 (UX)**: 알람은 사용자가 즉각적인 조치를 취해야 하는 고순위 작업이므로, 화면 잠금 여부와 상관없이 전체 화면(`AlarmActivity`)으로 표시되어야 합니다.
2. **백그라운드 제한 준수**: Android 10(API 29)부터 적용된 백그라운드 Activity 시작 제한을 `FullScreenIntent`와 `Overlay` 권한을 통해 해결합니다.

## 2. 필수 권한 및 설정
- **`USE_FULL_SCREEN_INTENT`**: 알림과 함께 Activity를 즉시 실행하기 위해 필요합니다. Android 14부터는 알람/전화 앱에 대해서만 기본 허용되므로, `NotificationManager.canUseFullScreenIntent()`로 확인이 필요합니다.
- **`SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`**: 정확한 시간에 알람을 발생시키기 위해 필요합니다.
- **`SYSTEM_ALERT_WINDOW` (다른 앱 위에 표시)**: 화면이 켜져 있을 때 heads-up 알림(상단 바) 대신 즉시 전체 화면 Activity를 띄우기 위해 사용됩니다.

## 3. 구현 규칙

### A. Notification Channel 설정
- 알림 채널의 중요도는 반드시 `IMPORTANCE_HIGH` 이상이어야 합니다.
- `setBypassDnd(true)`를 통해 방해 금지 모드에서도 알람이 울리도록 설정합니다.

### B. AlarmReceiver (BroadcastReceiver)
1. **Activity 시작 시도**: 화면이 켜져 있는 경우 사용자에게 즉시 전체 화면을 보여주기 위해 `context.startActivity()`를 시도합니다. 이때 `SYSTEM_ALERT_WINDOW` 권한이 있어야 백그라운드에서도 성공합니다.
2. **Full Screen Intent 설정**: `NotificationCompat.Builder.setFullScreenIntent()`를 사용하여 시스템이 잠금 화면 위에서 Activity를 실행하도록 합니다.
3. **우선순위 힌트**: `setCategory(CATEGORY_ALARM)` 및 `setPriority(PRIORITY_MAX)`를 설정하여 시스템이 이를 알람으로 인식하게 합니다.

### C. AlarmActivity (Activity)
- **화면 깨우기**: `setShowWhenLocked(true)`, `setTurnScreenOn(true)`를 호출하여 꺼진 화면을 깨우고 잠금을 우회하여 표시합니다.
- **화면 유지**: `FLAG_KEEP_SCREEN_ON`을 사용하여 사용자가 확인하기 전까지 화면이 꺼지지 않게 합니다.
- **실행 모드**: `singleInstance` 또는 `singleTop`을 사용하여 동일한 알람이 여러 번 뜰 때 하나만 유지되도록 합니다.

## 4. 문제 해결 (Troubleshooting)
- **현상**: 화면이 켜져 있을 때 상단 알림 바만 뜨고 전체 화면이 안 나옴.
    - **원인**: Android 시스템은 사용자의 방해를 최소화하기 위해 화면 사용 중에는 heads-up 알림을 우선시합니다.
    - **해결**: `SYSTEM_ALERT_WINDOW` 권한을 획득하고, Receiver에서 직접 `startActivity()`를 호출하여 강제로 화면을 전환합니다.
- **현상**: Android 14 기기에서 알람이 동작하지 않음.
    - **원인**: `USE_FULL_SCREEN_INTENT` 권한이 시스템 설정에서 거부되었을 수 있습니다.
    - **해결**: `Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT` 액션을 통해 설정 화면으로 유도해야 합니다.
