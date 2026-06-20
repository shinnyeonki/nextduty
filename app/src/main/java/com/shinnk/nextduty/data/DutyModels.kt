package com.shinnk.nextduty.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.LocalTime

@Serializable
data class DutySlot(
    val startTime: String,
    val endTime: String,
    val locations: List<String>,
    val alerts: List<Boolean>
)

@Serializable
enum class PtEffect {
    @SerialName("late_start") LATE_START,
    @SerialName("early_finish") EARLY_FINISH
}

@Serializable
data class DutyTable(
    val displayName: String,
    val capacity: Int,
    val ptEffect: PtEffect,
    val slots: List<DutySlot>,
    val alertOnFinish: Boolean = true
)

data class DutySettings(
    val tableName: String,
    val number: Int,
    val isPt: Boolean
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
    val location: String,
    val isFinish: Boolean = false
)

data class ProcessedSlot(
    val startTime: LocalTime,
    val endTime: LocalTime,
    val displayStartTime: String,
    val location: String,
    val alert: Boolean
)
