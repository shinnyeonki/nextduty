package com.shinnk.nextduty

import java.time.Duration
import java.time.LocalTime
import java.util.Locale

data class TimeSlot(
    val startTime: String,
    val endTime: String,
    val locations: List<String>
)

data class DutyInfo(
    val currentLoc: String,
    val currentRange: String,
    val nextLoc: String,
    val nextStart: String,
    val remaining: Duration
)

data class DutyAlarm(
    val triggerTime: LocalTime,
    val displayStartTime: String,
    val location: String
)

data class ProcessedSlot(
    val startTime: LocalTime,
    val endTime: LocalTime,
    val displayStartTime: String,
    val location: String
)

object DutyCore {

    // [주1 근무 조] 편성표 데이터 수정본
    private val ju1_table1 = listOf(
        TimeSlot("08:00", "09:00", listOf("대형버스주차장", "2층로비", "1층로비", "근무없음")),
        TimeSlot("09:00", "10:00", listOf("제1버스주차장/나래울입구", "1층로비", "대형버스주차장", "근무없음")),
        TimeSlot("10:00", "11:00", listOf("2층로비", "대형버스주차장", "제1버스주차장/나래울입구", "근무없음")),
        TimeSlot("11:00", "11:20", listOf("제1버스주차장/나래울입구", "식당앞 E/S", "어체앞 E/S", "근무없음")),
        TimeSlot("11:20", "12:40", listOf("점심시간", "점심시간", "점심시간", "점심시간")),
        TimeSlot("12:40", "13:00", listOf("제1버스주차장/나래울입구", "식당앞 E/S", "어체앞 E/S", "근무없음")),
        TimeSlot("13:00", "14:00", listOf("1층로비", "대형버스주차장", "2층로비", "근무없음")),
        TimeSlot("14:00", "15:00", listOf("어체앞 E/S", "제1버스주차장/나래울입구", "식당앞 E/S", "근무없음")),
        TimeSlot("15:00", "16:00", listOf("식당앞 E/S", "어체앞 E/S", "제1버스주차장/나래울입구", "근무없음")),
        TimeSlot("16:00", "17:00", listOf("제1버스주차장/나래울입구", "식당앞 E/S", "어체앞 E/S", "근무없음"))
    )

    private val ju1_table2 = listOf(
        TimeSlot("08:00", "09:00", listOf("나래울입구(초소)", "2층로비", "1층로비", "근무없음")),
        TimeSlot("09:00", "10:00", listOf("2층로비", "1층로비", "나래울입구(초소)", "근무없음")),
        TimeSlot("10:00", "11:00", listOf("1층로비", "나래울입구(초소)", "2층로비", "근무없음")),
        TimeSlot("11:00", "11:20", listOf("나래울입구(초소)", "어체앞 E/S", "식당앞 E/S", "근무없음")),
        TimeSlot("11:20", "12:40", listOf("점심시간", "점심시간", "점심시간", "점심시간")),
        TimeSlot("12:40", "13:00", listOf("나래울입구(초소)", "어체앞 E/S", "식당앞 E/S", "근무없음")),
        TimeSlot("13:00", "14:00", listOf("1층로비", "나래울입구(초소)", "2층로비", "근무없음")),
        TimeSlot("14:00", "15:00", listOf("어체앞 E/S", "순찰(본관/숙련관)", "식당앞 E/S", "근무없음")),
        TimeSlot("15:00", "16:00", listOf("식당앞 E/S", "어체앞 E/S", "순찰(본관/숙련관)", "근무없음")),
        TimeSlot("16:00", "17:00", listOf("순찰(본관/숙련관)", "식당앞 E/S", "어체앞 E/S", "근무없음"))
    )

    private val ju1_table3 = listOf(
        TimeSlot("08:00", "09:00", listOf("나래울입구(초소)", "2층로비", "1층로비", "근무없음")),
        TimeSlot("09:00", "10:00", listOf("2층로비", "1층로비", "나래울입구(초소)", "근무없음")),
        TimeSlot("10:00", "11:00", listOf("1층로비", "나래울입구(초소)", "2층로비", "근무없음")),
        TimeSlot("11:00", "11:20", listOf("나래울입구(초소)", "어체앞 E/S", "식당앞 E/S", "근무없음")),
        TimeSlot("11:20", "12:40", listOf("점심시간", "점심시간", "점심시간", "점심시간")),
        TimeSlot("12:40", "13:00", listOf("나래울입구(초소)", "어체앞 E/S", "식당앞 E/S", "근무없음")),
        TimeSlot("13:00", "14:00", listOf("1층로비", "나래울입구(초소)", "2층로비", "근무없음")),
        TimeSlot("14:00", "15:00", listOf("식당앞 E/S", "어체앞 E/S", "나래울입구(초소)", "근무없음")),
        TimeSlot("15:00", "16:00", listOf("나래울입구(초소)", "식당앞 E/S", "어체앞 E/S", "근무없음")),
        TimeSlot("16:00", "17:00", listOf("어체앞 E/S", "나래울입구(초소)", "식당앞 E/S", "근무없음"))
    )

    private val ju1_table4 = listOf(
        TimeSlot("08:00", "09:00", listOf("대형버스주차장", "제1버스주차장/나래울입구", "2층로비", "1층로비")),
        TimeSlot("09:00", "10:00", listOf("제1버스주차장/나래울입구", "2층로비", "1층로비", "대형버스주차장")),
        TimeSlot("10:00", "11:00", listOf("2층로비", "1층로비", "대형버스주차장", "제1버스주차장/나래울입구")),
        TimeSlot("11:00", "11:20", listOf("순찰(본관/숙련관)", "어체앞 E/S", "제1버스주차장/나래울입구", "식당앞 E/S")),
        TimeSlot("11:20", "12:40", listOf("점심시간", "점심시간", "점심시간", "점심시간")),
        TimeSlot("12:40", "13:00", listOf("순찰(본관/숙련관)", "어체앞 E/S", "제1버스주차장/나래울입구", "식당앞 E/S")),
        TimeSlot("13:00", "14:00", listOf("대형버스주차장", "제1버스주차장/나래울입구", "2층로비", "1층로비")),
        TimeSlot("14:00", "15:00", listOf("제1버스주차장/나래울입구", "어체앞 E/S", "식당앞 E/S", "순찰(본관/숙련관)")),
        TimeSlot("15:00", "16:00", listOf("어체앞 E/S", "식당앞 E/S", "순찰(본관/숙련관)", "제1버스주차장/나래울입구")),
        TimeSlot("16:00", "17:00", listOf("식당앞 E/S", "순찰(본관/숙련관)", "제1버스주차장/나래울입구", "어체앞 E/S"))
    )

    // [주2 근무 조] 편성표 데이터 수정본
    private val ju2_table1 = listOf(
        TimeSlot("11:00", "12:00", listOf("대형버스주차장", "2층로비", "1층로비", "근무없음")),
        TimeSlot("12:00", "12:40", listOf("2층로비", "1층로비", "대형버스주차장", "근무없음")),
        TimeSlot("12:40", "14:00", listOf("점심시간", "점심시간", "점심시간", "점심시간")),
        TimeSlot("14:00", "15:00", listOf("1층로비", "대형버스주차장", "2층로비", "근무없음")),
        TimeSlot("15:00", "16:00", listOf("대형버스주차장", "2층로비", "1층로비", "근무없음")),
        TimeSlot("16:00", "17:00", listOf("2층로비", "1층로비", "대형버스주차장", "근무없음")),
        TimeSlot("17:00", "18:00", listOf("1층로비", "대형버스주차장", "2층로비", "근무없음")),
        TimeSlot("18:00", "19:00", listOf("2층로비", "근무없음", "1층로비", "근무없음")),
        TimeSlot("19:00", "20:00", listOf("1층로비", "근무없음", "2층로비", "근무없음"))
    )

    private val ju2_table2 = listOf(
        TimeSlot("11:00", "12:00", listOf("나래울입구(초소)", "2층로비", "1층로비", "근무없음")),
        TimeSlot("12:00", "12:40", listOf("2층로비", "1층로비", "나래울입구(초소)", "근무없음")),
        TimeSlot("12:40", "14:00", listOf("점심시간", "점심시간", "점심시간", "점심시간")),
        TimeSlot("14:00", "15:00", listOf("1층로비", "나래울입구(초소)", "2층로비", "근무없음")),
        TimeSlot("15:00", "16:00", listOf("나래울입구(초소)", "2층로비", "1층로비", "근무없음")),
        TimeSlot("16:00", "17:00", listOf("2층로비", "1층로비", "나래울입구(초소)", "근무없음")),
        TimeSlot("17:00", "18:00", listOf("1층로비", "나래울입구(초소)", "2층로비", "근무없음")),
        TimeSlot("18:00", "19:00", listOf("2층로비", "근무없음", "1층로비", "근무없음")),
        TimeSlot("19:00", "20:00", listOf("1층로비", "근무없음", "2층로비", "근무없음"))
    )

    private val ju2_table3 = listOf(
        TimeSlot("11:00", "12:00", listOf("2층로비", "1층로비", "근무없음")),
        TimeSlot("12:00", "12:40", listOf("1층로비", "2층로비", "근무없음")),
        TimeSlot("12:40", "14:00", listOf("점심시간", "점심시간", "점심시간")),
        TimeSlot("14:00", "15:00", listOf("1층로비", "2층로비", "근무없음")),
        TimeSlot("15:00", "16:00", listOf("2층로비", "1층로비", "근무없음")),
        TimeSlot("16:00", "17:00", listOf("1층로비", "2층로비", "근무없음")),
        TimeSlot("17:00", "18:00", listOf("2층로비", "1층로비", "근무없음")),
        TimeSlot("18:00", "19:00", listOf("2층로비", "근무없음", "근무없음")),
        TimeSlot("19:00", "20:00", listOf("2층로비", "근무없음", "근무없음"))
    )

    val totalMap = mapOf(
        "JU1_1" to ju1_table1,
        "JU1_2" to ju1_table2,
        "JU1_3" to ju1_table3,
        "JU1_4" to ju1_table4,
        "JU2_1" to ju2_table1,
        "JU2_2" to ju2_table2,
        "JU2_3" to ju2_table3
    )

    /**
     * 비즈니스 로직(근무 규칙)에 따른 출근/퇴근 시간 반환
     */
    fun getShiftTimes(time: String, isPt: Boolean): Pair<LocalTime, LocalTime> {
        val start = if (isPt && time == "JU2") LocalTime.of(11, 30) 
                    else if (time == "JU1") LocalTime.of(8, 0) 
                    else LocalTime.of(11, 0)
        
        val end = if (isPt && time == "JU1") LocalTime.of(16, 30) 
                  else if (time == "JU1") LocalTime.of(17, 0) 
                  else LocalTime.of(20, 0)
        
        return start to end
    }

    /**
     * 비즈니스 로직(근무 규칙)이 적용된 가공된 타임슬롯 리스트 반환
     */
    private fun getProcessedSlots(time: String, table: Int, number: Int, isPt: Boolean): List<ProcessedSlot> {
        val tableKey = "${time}_$table"
        // [가드] 존재하지 않는 테이블 요청 시 기본값(JU1_1) 반환
        val timeSlots = totalMap[tableKey] ?: totalMap["JU1_1"]!!
        val (shiftStart, shiftEnd) = getShiftTimes(time, isPt)

        return timeSlots.mapNotNull { slot ->
            // [가드] 범위를 벗어난 번호 요청 시 1번(첫 번째 위치)으로 안전하게 대응
            val location = slot.locations.getOrNull(number - 1) ?: slot.locations.firstOrNull() ?: "알 수 없는 장소"
            val originalStart = LocalTime.parse(slot.startTime)
            val originalEnd = LocalTime.parse(slot.endTime)

            // 1. [공통 규칙] 특정 장소 제외
            if (location == "근무없음") return@mapNotNull null

            // 2. [PT 규칙] 적용
            var finalStart = originalStart
            val finalEnd = originalEnd
            var displayStart = slot.startTime

            if (isPt) {
                // [JU1 + PT] 16:30 이후 근무 제외
                if (time == "JU1" && !originalStart.isBefore(shiftEnd)) {
                    return@mapNotNull null
                }
                // [JU2 + PT] 11:00 근무는 11:30 시작으로 변경
                if (time == "JU2" && slot.startTime == "11:00") {
                    finalStart = shiftStart
                    displayStart = "11:30"
                }
            }

            ProcessedSlot(finalStart, finalEnd, displayStart, location)
        }
    }

    /**
     * 비즈니스 로직(근무 규칙)에 따른 알람 스케줄 계산
     */
    fun getAlarmSchedules(time: String, table: Int, number: Int, isPt: Boolean): List<DutyAlarm> {
        val slots = getProcessedSlots(time, table, number, isPt)
        if (slots.isEmpty()) return emptyList()

        val alarms = mutableListOf<DutyAlarm>()

        // 1. 첫 근무 시작 5분 전 알람
        alarms.add(DutyAlarm(
            triggerTime = slots.first().startTime.minusMinutes(5),
            displayStartTime = slots.first().displayStartTime,
            location = slots.first().location
        ))

        // 2. 각 슬롯의 종료 5분 전 알람 (다음 이동지 알림)
        slots.forEachIndexed { index, slot ->
            val nextSlot = slots.getOrNull(index + 1)
            val nextLoc = nextSlot?.location ?: "업무 종료"
            val nextTime = nextSlot?.displayStartTime ?: slot.endTime.toString()

            alarms.add(DutyAlarm(
                triggerTime = slot.endTime.minusMinutes(5),
                displayStartTime = nextTime,
                location = nextLoc
            ))
        }

        // 중복 시간 제거 및 정렬
        return alarms.distinctBy { it.triggerTime }.sortedBy { it.triggerTime }
    }

    fun calculateDutyInfo(currentTime: LocalTime, settings: DutySettings): DutyInfo {
        val processedSlots = getProcessedSlots(settings.time, settings.table, settings.number, settings.isPt)
        val (shiftStart, shiftEnd) = getShiftTimes(settings.time, settings.isPt)

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

    fun formatDuration(duration: Duration): String = String.format(
        Locale.getDefault(), "%02d:%02d:%02d",
        duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()
    )
}