package com.shinnk.nextduty

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

class PreferenceManager(private val context: Context) {

    companion object {
        val PT_STATUS = booleanPreferencesKey("pt_status")
        val DUTY_TABLE_NAME = stringPreferencesKey("duty_table_name") // e.g., "주1-1"
        val DUTY_NUMBER = intPreferencesKey("duty_number") // 1, 2, 3
        val LAST_SAVED_DATE = stringPreferencesKey("last_saved_date")
        val IS_APP_ACTIVE = booleanPreferencesKey("is_app_active")
        val WORK_SCHEDULE_IMAGES = stringPreferencesKey("work_schedule_images_list")
        val DUTY_TABLE_IMAGES = stringPreferencesKey("duty_table_images_list")
        val CUSTOM_DUTY_TABLES = stringPreferencesKey("custom_duty_tables")
        val ALARM_LEAD_TIME = intPreferencesKey("alarm_lead_time")
    }

    val alarmLeadTime: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[ALARM_LEAD_TIME] ?: 5
    }

    val customDutyTables: Flow<List<DutyTable>?> = context.dataStore.data.map { preferences ->
        val serialized = preferences[CUSTOM_DUTY_TABLES] ?: return@map null
        try {
            Json.decodeFromString<List<DutyTable>>(serialized)
        } catch (e: Exception) {
            null
        }
    }

    val workScheduleImages: Flow<List<String>> = context.dataStore.data.map { preferences ->
        val serialized = preferences[WORK_SCHEDULE_IMAGES] ?: ""
        val paths = if (serialized.isEmpty()) emptyList() else serialized.split("|")
        
        paths.filter { path ->
            if (path.startsWith("res:")) return@filter true
            val file = if (path.startsWith("/")) File(path) else File(context.filesDir, path)
            file.exists()
        }
    }

    val dutyTableImages: Flow<List<String>> = context.dataStore.data.map { preferences ->
        val serialized = preferences[DUTY_TABLE_IMAGES] ?: ""
        val paths = if (serialized.isEmpty()) {
            listOf("res:duty_ju1_12", "res:duty_ju1_34", "res:duty_ju2_1", "res:duty_ju2_23")
        } else {
            serialized.split("|")
        }

        paths.filter { path ->
            if (path.startsWith("res:")) return@filter true
            val file = if (path.startsWith("/")) File(path) else File(context.filesDir, path)
            file.exists()
        }
    }

    val isAppActive: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_APP_ACTIVE] ?: true
    }

    val ptStatus: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PT_STATUS] ?: false
    }

    val dutySettings: Flow<DutySettings?> = context.dataStore.data.map { preferences ->
        val lastDate = preferences[LAST_SAVED_DATE]
        val today = LocalDate.now().toString()
        
        if (lastDate == today) {
            val tableName = preferences[DUTY_TABLE_NAME] ?: return@map null
            val number = preferences[DUTY_NUMBER] ?: return@map null
            val isPt = preferences[PT_STATUS] ?: false
            DutySettings(tableName, number, isPt)
        } else {
            null
        }
    }

    suspend fun savePtStatus(status: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PT_STATUS] = status
        }
    }

    suspend fun saveAppActiveStatus(isActive: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_APP_ACTIVE] = isActive
        }
    }

    suspend fun saveWorkScheduleImages(images: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[WORK_SCHEDULE_IMAGES] = images.joinToString("|")
        }
    }

    suspend fun saveDutyTableImages(images: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[DUTY_TABLE_IMAGES] = images.joinToString("|")
        }
    }

    suspend fun saveDutySettings(tableName: String, number: Int, isPt: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DUTY_TABLE_NAME] = tableName
            preferences[DUTY_NUMBER] = number
            preferences[PT_STATUS] = isPt
            preferences[LAST_SAVED_DATE] = LocalDate.now().toString()
        }
    }

    suspend fun clearDailySettings() {
        context.dataStore.edit { preferences ->
            preferences.remove(DUTY_TABLE_NAME)
            preferences.remove(DUTY_NUMBER)
            preferences.remove(LAST_SAVED_DATE)
        }
    }

    suspend fun saveCustomDutyTables(tables: List<DutyTable>?) {
        context.dataStore.edit { preferences ->
            if (tables == null) {
                preferences.remove(CUSTOM_DUTY_TABLES)
            } else {
                preferences[CUSTOM_DUTY_TABLES] = Json.encodeToString(tables)
            }
        }
    }

    suspend fun saveAlarmLeadTime(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[ALARM_LEAD_TIME] = minutes
        }
    }
}
