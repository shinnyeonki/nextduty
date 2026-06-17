package com.shinnk.nextduty.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDate

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DutyRepository(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true; prettyPrint = true }

    companion object {
        private val IS_PT = booleanPreferencesKey("is_pt")
        private val DUTY_TABLE_NAME = stringPreferencesKey("duty_table_name")
        private val DUTY_NUMBER = intPreferencesKey("duty_number")
        private val LAST_SAVED_DATE = stringPreferencesKey("last_saved_date")
        private val IS_APP_ACTIVE = booleanPreferencesKey("is_app_active")
        private val WORK_SCHEDULE_IMAGES = stringPreferencesKey("work_schedule_images_list")
        private val DUTY_TABLE_IMAGES = stringPreferencesKey("duty_table_images_list")
        private val CUSTOM_DUTY_TABLES = stringPreferencesKey("custom_duty_tables")
        private val ALARM_LEAD_TIME = intPreferencesKey("alarm_lead_time")
    }

    // --- 기본 데이터 ---
    private fun String.toLoc(): LocationType = when (this) {
        "근무없음" -> LocationType.Off
        "점심시간" -> LocationType.Lunch
        else -> LocationType.Active(this)
    }

    private val defaultTables = listOf(
        DutyTable("주1-1", 3, PtEffect.EARLY_FINISH, listOf(
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
        DutyTable("주1-2", 3, PtEffect.EARLY_FINISH, listOf(
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
        DutyTable("주1-3", 3, PtEffect.EARLY_FINISH, listOf(
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
        DutyTable("주1-4", 4, PtEffect.EARLY_FINISH, listOf(
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
        DutyTable("주2-1", 3, PtEffect.LATE_START, listOf(
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
        DutyTable("주2-2", 3, PtEffect.LATE_START, listOf(
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
        DutyTable("주2-3", 2, PtEffect.LATE_START, listOf(
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

    fun getDefaultTables(): List<DutyTable> = defaultTables

    // --- 흐름 데이터 (Flows) ---
    val customDutyTables: Flow<List<DutyTable>?> = context.dataStore.data.map { preferences ->
        preferences[CUSTOM_DUTY_TABLES]?.let { json.decodeFromString<List<DutyTable>>(it) }
    }

    val allTables: Flow<List<DutyTable>> = customDutyTables.map { it ?: defaultTables }

    val dutySettings: Flow<DutySettings?> = context.dataStore.data.map { preferences ->
        val lastDate = preferences[LAST_SAVED_DATE]
        if (lastDate == LocalDate.now().toString()) {
            val tableName = preferences[DUTY_TABLE_NAME] ?: return@map null
            val number = preferences[DUTY_NUMBER] ?: return@map null
            val isPt = preferences[IS_PT] ?: false
            DutySettings(tableName, number, isPt)
        } else null
    }

    val isPt: Flow<Boolean> = context.dataStore.data.map { it[IS_PT] ?: false }
    val isAppActive: Flow<Boolean> = context.dataStore.data.map { it[IS_APP_ACTIVE] ?: true }
    val alarmLeadTime: Flow<Int> = context.dataStore.data.map { it[ALARM_LEAD_TIME] ?: 5 }

    val workScheduleImages: Flow<List<String>> = context.dataStore.data.map { preferences ->
        val serialized = preferences[WORK_SCHEDULE_IMAGES] ?: ""
        serialized.split("|").filter { it.isNotEmpty() && (it.startsWith("res:") || File(it).exists()) }
    }

    val dutyTableImages: Flow<List<String>> = context.dataStore.data.map { preferences ->
        val serialized = preferences[DUTY_TABLE_IMAGES] ?: ""
        if (serialized.isEmpty()) listOf("res:duty_ju1_12", "res:duty_ju1_34", "res:duty_ju2_1", "res:duty_ju2_23")
        else serialized.split("|").filter { it.isNotEmpty() && (it.startsWith("res:") || File(it).exists()) }
    }

    // --- 저장 함수 ---
    suspend fun saveDutySettings(tableName: String, number: Int, isPt: Boolean) {
        context.dataStore.edit {
            it[DUTY_TABLE_NAME] = tableName; it[DUTY_NUMBER] = number
            it[IS_PT] = isPt; it[LAST_SAVED_DATE] = LocalDate.now().toString()
        }
    }

    suspend fun savePtStatus(isPt: Boolean) = context.dataStore.edit { it[IS_PT] = isPt }
    suspend fun saveAppActiveStatus(isActive: Boolean) = context.dataStore.edit { it[IS_APP_ACTIVE] = isActive }
    suspend fun saveAlarmLeadTime(minutes: Int) = context.dataStore.edit { it[ALARM_LEAD_TIME] = minutes }
    suspend fun saveCustomDutyTables(tables: List<DutyTable>?) = context.dataStore.edit { 
        if (tables == null) it.remove(CUSTOM_DUTY_TABLES) else it[CUSTOM_DUTY_TABLES] = json.encodeToString(tables)
    }
    suspend fun saveWorkScheduleImages(images: List<String>) = context.dataStore.edit { it[WORK_SCHEDULE_IMAGES] = images.joinToString("|") }
    suspend fun saveDutyTableImages(images: List<String>) = context.dataStore.edit { it[DUTY_TABLE_IMAGES] = images.joinToString("|") }
}
