package com.shinnk.nextduty

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Duration
import java.time.LocalTime
import java.util.Locale

object DutyCore {
    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    private fun String.toLoc(): LocationType = when (this) {
        "근무없음" -> LocationType.Off
        "점심시간" -> LocationType.Lunch
        else -> LocationType.Active(this)
    }

    private val defaultTables = listOf(
        // 주1-1 (Capacity 3)
        DutyTable("주1-1", 3, listOf(
            DutySlot("08:00", "09:00", listOf("대형버스주차장", "2층로비", "1층로비").map { it.toLoc() }),
            DutySlot("09:00", "10:00", listOf("제1버스주차장/나래울입구", "1층로비", "대형버스주차장").map { it.toLoc() }),
            DutySlot("10:00", "11:00", listOf("1층로비", "대형버스주차장", "제1버스주차장/나래울입구").map { it.toLoc() }),
            DutySlot("11:00", "11:20", listOf("제1버스주차장/나래울입구", "식당앞 E/S", "어체앞 E/S").map { it.toLoc() }),
            DutySlot("11:20", "12:40", listOf("점심시간", "점심시간", "점심시간").map { it.toLoc() }),
            DutySlot("12:40", "14:00", listOf("1층로비", "대형버스주차장", "2층로비").map { it.toLoc() }),
            DutySlot("14:00", "15:00", listOf("어체앞 E/S", "제1버스주차장/나래울입구", "식당앞 E/S").map { it.toLoc() }),
            DutySlot("15:00", "16:00", listOf("식당앞 E/S", "어체앞 E/S", "제1버스주차장/나래울입구").map { it.toLoc() }),
            DutySlot("16:00", "17:00", listOf("제1버스주차장/나래울입구", "식당앞 E/S", "어체앞 E/S").map { it.toLoc() })
        )),
        // 주1-2 (Capacity 3)
        DutyTable("주1-2", 3, listOf(
            DutySlot("08:00", "09:00", listOf("나래울입구(초소)", "2층로비", "1층로비").map { it.toLoc() }),
            DutySlot("09:00", "10:00", listOf("2층로비", "1층로비", "나래울입구(초소)").map { it.toLoc() }),
            DutySlot("10:00", "11:00", listOf("1층로비", "나래울입구(초소)", "2층로비").map { it.toLoc() }),
            DutySlot("11:00", "11:20", listOf("나래울입구(초소)", "어체앞 E/S", "식당앞 E/S").map { it.toLoc() }),
            DutySlot("11:20", "12:40", listOf("점심시간", "점심시간", "점심시간").map { it.toLoc() }),
            DutySlot("12:40", "14:00", listOf("1층로비", "나래울입구(초소)", "2층로비").map { it.toLoc() }),
            DutySlot("14:00", "15:00", listOf("어체앞 E/S", "순찰(본관/숙련관)", "식당앞 E/S").map { it.toLoc() }),
            DutySlot("15:00", "16:00", listOf("식당앞 E/S", "어체앞 E/S", "순찰(본관/숙련관)").map { it.toLoc() }),
            DutySlot("16:00", "17:00", listOf("순찰(본관/숙련관)", "식당앞 E/S", "어체앞 E/S").map { it.toLoc() })
        )),
        // 주1-3 (Capacity 3)
        DutyTable("주1-3", 3, listOf(
            DutySlot("08:00", "09:00", listOf("나래울입구(초소)", "2층로비", "1층로비").map { it.toLoc() }),
            DutySlot("09:00", "10:00", listOf("2층로비", "1층로비", "나래울입구(초소)").map { it.toLoc() }),
            DutySlot("10:00", "11:00", listOf("1층로비", "나래울입구(초소)", "2층로비").map { it.toLoc() }),
            DutySlot("11:00", "11:20", listOf("나래울입구(초소)", "어체앞 E/S", "식당앞 E/S").map { it.toLoc() }),
            DutySlot("11:20", "12:40", listOf("점심시간", "점심시간", "점심시간").map { it.toLoc() }),
            DutySlot("12:40", "14:00", listOf("1층로비", "나래울입구(초소)", "2층로비").map { it.toLoc() }),
            DutySlot("14:00", "15:00", listOf("식당앞 E/S", "어체앞 E/S", "나래울입구(초소)").map { it.toLoc() }),
            DutySlot("15:00", "16:00", listOf("나래울입구(초소)", "식당앞 E/S", "어체앞 E/S").map { it.toLoc() }),
            DutySlot("16:00", "17:00", listOf("어체앞 E/S", "나래울입구(초소)", "식당앞 E/S").map { it.toLoc() })
        )),
        // 주1-4 (Capacity 4 - User didn't specify to change this, but usually 1-4 is 4 people)
        DutyTable("주1-4", 4, listOf(
            DutySlot("08:00", "09:00", listOf("대형버스주차장", "제1버스주차장/나래울입구", "2층로비", "1층로비").map { it.toLoc() }),
            DutySlot("09:00", "10:00", listOf("제1버스주차장/나래울입구", "2층로비", "1층로비", "대형버스주차장").map { it.toLoc() }),
            DutySlot("10:00", "11:00", listOf("2층로비", "1층로비", "대형버스주차장", "제1버스주차장/나래울입구").map { it.toLoc() }),
            DutySlot("11:00", "11:20", listOf("순찰(본관/숙련관)", "어체앞 E/S", "제1버스주차장/나래울입구", "식당앞 E/S").map { it.toLoc() }),
            DutySlot("11:20", "12:40", listOf("점심시간", "점심시간", "점심시간", "점심시간").map { it.toLoc() }),
            DutySlot("12:40", "14:00", listOf("대형버스주차장", "제1버스주차장/나래울입구", "2층로비", "1층로비").map { it.toLoc() }),
            DutySlot("14:00", "15:00", listOf("제1버스주차장/나래울입구", "어체앞 E/S", "식당앞 E/S", "순찰(본관/숙련관)").map { it.toLoc() }),
            DutySlot("15:00", "16:00", listOf("어체앞 E/S", "식당앞 E/S", "순찰(본관/숙련관)", "제1버스주차장/나래울입구").map { it.toLoc() }),
            DutySlot("16:00", "17:00", listOf("식당앞 E/S", "순찰(본관/숙련관)", "제1버스주차장/나래울입구", "어체앞 E/S").map { it.toLoc() })
        )),
        // 주2-1 (Capacity 3)
        DutyTable("주2-1", 3, listOf(
            DutySlot("11:00", "12:00", listOf("대형버스주차장", "2층로비", "1층로비").map { it.toLoc() }),
            DutySlot("12:00", "12:40", listOf("2층로비", "1층로비", "대형버스주차장").map { it.toLoc() }),
            DutySlot("12:40", "14:00", listOf("점심시간", "점심시간", "점심시간").map { it.toLoc() }),
            DutySlot("14:00", "15:00", listOf("1층로비", "대형버스주차장", "2층로비").map { it.toLoc() }),
            DutySlot("15:00", "16:00", listOf("대형버스주차장", "2층로비", "1층로비").map { it.toLoc() }),
            DutySlot("16:00", "17:00", listOf("2층로비", "1층로비", "대형버스주차장").map { it.toLoc() }),
            DutySlot("17:00", "18:00", listOf("1층로비", "대형버스주차장", "2층로비").map { it.toLoc() }),
            DutySlot("18:00", "19:00", listOf("2층로비", "근무없음", "1층로비").map { it.toLoc() }),
            DutySlot("19:00", "20:00", listOf("1층로비", "근무없음", "2층로비").map { it.toLoc() })
        )),
        // 주2-2 (Capacity 3)
        DutyTable("주2-2", 3, listOf(
            DutySlot("11:00", "12:00", listOf("나래울입구(초소)", "2층로비", "1층로비").map { it.toLoc() }),
            DutySlot("12:00", "12:40", listOf("2층로비", "1층로비", "나래울입구(초소)").map { it.toLoc() }),
            DutySlot("12:40", "14:00", listOf("점심시간", "점심시간", "점심시간").map { it.toLoc() }),
            DutySlot("14:00", "15:00", listOf("1층로비", "나래울입구(초소)", "2층로비").map { it.toLoc() }),
            DutySlot("15:00", "16:00", listOf("나래울입구(초소)", "2층로비", "1층로비").map { it.toLoc() }),
            DutySlot("16:00", "17:00", listOf("2층로비", "1층로비", "나래울입구(초소)").map { it.toLoc() }),
            DutySlot("17:00", "18:00", listOf("1층로비", "나래울입구(초소)", "2층로비").map { it.toLoc() }),
            DutySlot("18:00", "19:00", listOf("2층로비", "근무없음", "1층로비").map { it.toLoc() }),
            DutySlot("19:00", "20:00", listOf("1층로비", "근무없음", "2층로비").map { it.toLoc() })
        )),
        // 주2-3 (Capacity 3)
        DutyTable("주2-3", 2, listOf(
            DutySlot("11:00", "12:00", listOf("2층로비", "1층로비").map { it.toLoc() }),
            DutySlot("12:00", "12:40", listOf("1층로비", "2층로비").map { it.toLoc() }),
            DutySlot("12:40", "14:00", listOf("점심시간", "점심시간").map { it.toLoc() }),
            DutySlot("14:00", "15:00", listOf("2층로비", "1층로비").map { it.toLoc() }),
            DutySlot("15:00", "16:00", listOf("1층로비", "2층로비").map { it.toLoc() }),
            DutySlot("16:00", "17:00", listOf("2층로비", "1층로비").map { it.toLoc() }),
            DutySlot("17:00", "18:00", listOf("1층로비", "2층로비").map { it.toLoc() }),
            DutySlot("18:00", "19:00", listOf("1층로비", "근무없음").map { it.toLoc() }),
            DutySlot("19:00", "20:00", listOf("1층로비", "근무없음").map { it.toLoc() })
        ))
    )

    private var tablesMap: Map<String, DutyTable> = defaultTables.associateBy { it.displayName }

    fun setCustomTables(tables: List<DutyTable>?) {
        tablesMap = if (tables.isNullOrEmpty()) {
            defaultTables.associateBy { it.displayName }
        } else {
            tables.associateBy { it.displayName }
        }
    }

    fun loadTablesFromJson(jsonString: String?) {
        if (jsonString.isNullOrBlank()) {
            tablesMap = defaultTables.associateBy { it.displayName }
            return
        }
        try {
            val customTables = json.decodeFromString<List<DutyTable>>(jsonString)
            tablesMap = customTables.associateBy { it.displayName }
        } catch (e: Exception) {
            e.printStackTrace()
            tablesMap = defaultTables.associateBy { it.displayName }
        }
    }

    fun getTablesAsJson(): String = json.encodeToString(tablesMap.values.toList())

    fun getAllTables(): List<DutyTable> = tablesMap.values.toList()

    fun getDefaultTables(): List<DutyTable> = defaultTables

    fun getTable(name: String): DutyTable? = tablesMap[name]

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

    private fun getProcessedSlots(tableName: String, number: Int, isPt: Boolean): List<ProcessedSlot> {
        val table = tablesMap[tableName] ?: return emptyList()
        val (shiftStart, shiftEnd) = getShiftTimes(tableName, isPt)
        val isJu1 = tableName.contains("주1")
        val isJu2 = tableName.contains("주2")

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

    fun getAlarmSchedules(tableName: String, number: Int, isPt: Boolean, leadTime: Int = 5): List<DutyAlarm> {
        val slots = getProcessedSlots(tableName, number, isPt)
        if (slots.isEmpty()) return emptyList()

        val alarms = mutableListOf<DutyAlarm>()
        slots.forEach { slot ->
            alarms.add(DutyAlarm(slot.startTime.minusMinutes(leadTime.toLong()), slot.displayStartTime, slot.location))
        }

        val lastSlot = slots.last()
        alarms.add(DutyAlarm(lastSlot.endTime.minusMinutes(leadTime.toLong()), lastSlot.endTime.toString(), "업무 종료"))

        return alarms.asSequence().distinctBy { it.triggerTime }.sortedBy { it.triggerTime }.toList()
    }

    fun calculateDutyInfo(currentTime: LocalTime, settings: DutySettings): DutyInfo {
        val processedSlots = getProcessedSlots(settings.tableName, settings.number, settings.isPt)
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

    fun formatDuration(duration: Duration): String = String.format(
        Locale.getDefault(), "%02d:%02d:%02d",
        duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart()
    )
}
