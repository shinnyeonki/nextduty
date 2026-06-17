package com.shinnk.nextduty.data

import java.time.Duration
import java.time.LocalTime
import java.util.Locale

object DutyCalculator {

    fun getShiftTimes(tableName: String, isPt: Boolean): Pair<LocalTime, LocalTime> {
        val isJu1 = tableName.contains("주1")
        val isJu2 = tableName.contains("주2")
        
        val start = if (isPt && isJu2) LocalTime.of(11, 30) 
                    else if (isJu1) LocalTime.of(8, 0) 
                    else LocalTime.of(11, 0)
        
        val end = if (isPt && isJu1) LocalTime.of(16, 30) 
                  else if (isJu1) LocalTime.of(17, 0) 
                  else LocalTime.of(20, 0)
        
        return start to end
    }

    fun getProcessedSlots(table: DutyTable, number: Int, isPt: Boolean): List<ProcessedSlot> {
        val (shiftStart, shiftEnd) = getShiftTimes(table.displayName, isPt)
        val isJu1 = table.displayName.contains("주1")
        val isJu2 = table.displayName.contains("주2")

        return table.slots.mapNotNull { slot ->
            val locationType = slot.locations.getOrNull(number - 1) ?: LocationType.Off
            if (locationType is LocationType.Off) return@mapNotNull null

            val originalStart = LocalTime.parse(slot.startTime)
            val originalEnd = LocalTime.parse(slot.endTime)

            var finalStart = originalStart
            var finalEnd = originalEnd
            var displayStart = slot.startTime

            if (isPt) {
                if (isJu1 && !originalStart.isBefore(shiftEnd)) return@mapNotNull null
                if (isJu2 && slot.startTime == "11:00") {
                    finalStart = shiftStart
                    displayStart = "11:30"
                }
            }

            if (isJu2 && number == 2 && slot.startTime == "17:00") {
                finalEnd = LocalTime.of(17, 30)
            }

            if (!finalStart.isBefore(finalEnd) || !finalStart.isBefore(shiftEnd)) return@mapNotNull null

            ProcessedSlot(finalStart, finalEnd, displayStart, locationType.getDisplayName())
        }
    }

    fun calculateDutyInfo(currentTime: LocalTime, table: DutyTable, settings: DutySettings): DutyInfo {
        val processedSlots = getProcessedSlots(table, settings.number, settings.isPt)
        val (shiftStart, shiftEnd) = getShiftTimes(settings.tableName, settings.isPt)

        val (currLoc, currRange) = when {
            currentTime.isBefore(shiftStart) -> "출근 전" to "시작 예정: $shiftStart"
            !currentTime.isBefore(shiftEnd) -> "업무 종료" to "퇴근 완료"
            else -> {
                val slot = processedSlots.find { s ->
                    !currentTime.isBefore(s.startTime) && currentTime.isBefore(s.endTime)
                }
                (slot?.location ?: "근무 외 시간") to (slot?.let { "${it.displayStartTime} ~ ${it.endTime}" } ?: "현재 정보 없음")
            }
        }

        val nextSlot = processedSlots.find { currentTime.isBefore(it.startTime) }
        val (nLoc, nStart) = if (nextSlot != null && nextSlot.startTime.isBefore(shiftEnd)) {
            nextSlot.location to "시작 예정: ${nextSlot.displayStartTime}"
        } else {
            "없음 (퇴근 예정)" to "수고하셨습니다"
        }

        val remaining = if (currentTime.isBefore(shiftEnd)) Duration.between(currentTime, shiftEnd) else Duration.ZERO

        return DutyInfo(currLoc, currRange, nLoc, nStart, remaining)
    }

    fun getAlarmSchedules(table: DutyTable, number: Int, isPt: Boolean, leadTime: Int = 5): List<DutyAlarm> {
        val slots = getProcessedSlots(table, number, isPt)
        if (slots.isEmpty()) return emptyList()

        val alarms = mutableListOf<DutyAlarm>()
        slots.forEach { slot ->
            alarms.add(DutyAlarm(slot.startTime.minusMinutes(leadTime.toLong()), slot.displayStartTime, slot.location))
        }

        val lastSlot = slots.last()
        alarms.add(DutyAlarm(lastSlot.endTime.minusMinutes(leadTime.toLong()), lastSlot.endTime.toString(), "업무 종료"))

        return alarms.asSequence().distinctBy { it.triggerTime }.sortedBy { it.triggerTime }.toList()
    }

    fun formatDuration(duration: Duration): String = String.format(
        Locale.getDefault(), "%02d:%02d:%02d",
        duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()
    )
}
