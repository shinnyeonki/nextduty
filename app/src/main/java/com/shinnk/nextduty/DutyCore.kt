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

    fun calculateDutyInfo(currentTime: LocalTime, settings: DutySettings): DutyInfo {
        val tableKey = "${settings.time}_${settings.table}"
        val timeSlots = totalMap[tableKey] ?: emptyList()

        val isPt = settings.isPt
        val shiftStart = if (isPt && settings.time == "JU2") LocalTime.of(11, 30) else (if (settings.time == "JU1") LocalTime.of(8, 0) else LocalTime.of(11, 0))
        val shiftEnd = if (isPt && settings.time == "JU1") LocalTime.of(16, 30) else (if (settings.time == "JU1") LocalTime.of(17, 0) else LocalTime.of(20, 0))

        val (currLoc, currRange) = when {
            currentTime.isBefore(shiftStart) -> "출근 전" to "시작 시간: $shiftStart"
            !currentTime.isBefore(shiftEnd) -> "업무 종료" to "퇴근 완료"
            else -> {
                val slot = timeSlots.find { s ->
                    val start = LocalTime.parse(s.startTime)
                    val end = LocalTime.parse(s.endTime)
                    !currentTime.isBefore(start) && currentTime.isBefore(end)
                }
                (slot?.locations?.getOrNull(settings.number - 1) ?: "근무 외 시간") to (slot?.let { "${it.startTime} ~ ${it.endTime}" } ?: "")
            }
        }

        val nextSlot = timeSlots.find { currentTime.isBefore(LocalTime.parse(it.startTime)) }
        val (nLoc, nStart) = if (nextSlot != null && LocalTime.parse(nextSlot.startTime).isBefore(shiftEnd)) {
            (nextSlot.locations.getOrNull(settings.number - 1) ?: "없음") to "시작 예정: ${nextSlot.startTime}"
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