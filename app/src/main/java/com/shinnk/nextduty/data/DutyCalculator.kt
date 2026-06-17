package com.shinnk.nextduty.data

import java.time.Duration
import java.time.LocalTime
import java.util.Locale

object DutyCalculator {

    fun getShiftTimes(table: DutyTable, isPt: Boolean): Pair<LocalTime, LocalTime> {
        if (table.slots.isEmpty()) return LocalTime.of(0, 0) to LocalTime.of(0, 0)

        var start = table.slots.minOf { LocalTime.parse(it.startTime) }
        var end = table.slots.maxOf { LocalTime.parse(it.endTime) }

        if (isPt) {
            when (table.ptEffect) {
                PtEffect.LATE_START -> start = start.plusMinutes(30)
                PtEffect.EARLY_FINISH -> end = end.minusMinutes(30)
            }
        }
        
        return start to end
    }

    fun getProcessedSlots(table: DutyTable, number: Int, isPt: Boolean): List<ProcessedSlot> {
        val (shiftStart, shiftEnd) = getShiftTimes(table, isPt)

        return table.slots.mapNotNull { slot ->
            val locationType = slot.locations.getOrNull(number - 1) ?: LocationType.Off
            
            // "근무없음"만 제외하고, "점심시간"과 "일반근무"는 포함
            if (locationType is LocationType.Off) return@mapNotNull null

            val originalStart = LocalTime.parse(slot.startTime)
            val originalEnd = LocalTime.parse(slot.endTime)

            var finalStart = originalStart
            var finalEnd = originalEnd
            var displayStart = slot.startTime

            // PT 처리: 근무 시간이 시프트 범위 내에 있도록 조정
            if (isPt) {
                // 시작 시간이 시프트 시작 전이면 시프트 시작으로 조정
                if (finalStart.isBefore(shiftStart)) {
                    finalStart = shiftStart
                    displayStart = shiftStart.toString()
                }
                // 종료 시간이 시프트 종료 후이면 시프트 종료로 조정
                if (finalEnd.isAfter(shiftEnd)) {
                    finalEnd = shiftEnd
                }
            }

            if (!finalStart.isBefore(finalEnd) || !finalStart.isBefore(shiftEnd)) return@mapNotNull null

            ProcessedSlot(finalStart, finalEnd, displayStart, locationType.getDisplayName())
        }
    }

    fun calculateDutyInfo(currentTime: LocalTime, table: DutyTable, settings: DutySettings): DutyInfo {
        val processedSlots = getProcessedSlots(table, settings.number, settings.isPt)
        val (shiftStart, shiftEnd) = getShiftTimes(table, settings.isPt)

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
