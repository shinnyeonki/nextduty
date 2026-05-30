package com.shinnk.nextduty

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.time.LocalDate

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferenceManager(private val context: Context) {

    companion object {
        val PT_STATUS = booleanPreferencesKey("pt_status")
        val DUTY_TIME = stringPreferencesKey("duty_time") // JU1, JU2
        val DUTY_TABLE = intPreferencesKey("duty_table") // 1, 2, 3, 4
        val DUTY_NUMBER = intPreferencesKey("duty_number") // 1, 2, 3, 4
        val LAST_SAVED_DATE = stringPreferencesKey("last_saved_date")
        val IS_APP_ACTIVE = booleanPreferencesKey("is_app_active")
        val WORK_SCHEDULE_IMAGES = stringSetPreferencesKey("work_schedule_images")
        val SCHEDULED_ALARM_COUNT = intPreferencesKey("scheduled_alarm_count")
    }

    val workScheduleImages: Flow<List<String>> = context.dataStore.data.map { preferences ->
        val paths = preferences[WORK_SCHEDULE_IMAGES]?.toList() ?: emptyList()
        // 실제 파일이 존재하는 것들만 필터링 (엣지케이스 방지)
        paths.filter { File(it).exists() }
    }

    val isAppActive: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_APP_ACTIVE] ?: true
    }

    val ptStatus: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PT_STATUS] ?: false
    }

    val scheduledAlarmCount: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[SCHEDULED_ALARM_COUNT] ?: 0
    }

    val dutySettings: Flow<DutySettings?> = context.dataStore.data.map { preferences ->
        val lastDate = preferences[LAST_SAVED_DATE]
        val today = LocalDate.now().toString()
        
        if (lastDate == today) {
            val time = preferences[DUTY_TIME] ?: return@map null
            val table = preferences[DUTY_TABLE] ?: return@map null
            val number = preferences[DUTY_NUMBER] ?: return@map null
            val isPt = preferences[PT_STATUS] ?: false
            DutySettings(time, table, number, isPt)
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
            preferences[WORK_SCHEDULE_IMAGES] = images.toSet()
        }
    }

    suspend fun saveScheduledAlarmCount(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[SCHEDULED_ALARM_COUNT] = count
        }
    }

    suspend fun saveDutySettings(time: String, table: Int, number: Int, isPt: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DUTY_TIME] = time
            preferences[DUTY_TABLE] = table
            preferences[DUTY_NUMBER] = number
            preferences[PT_STATUS] = isPt
            preferences[LAST_SAVED_DATE] = LocalDate.now().toString()
        }
    }

    suspend fun clearDailySettings() {
        context.dataStore.edit { preferences ->
            preferences.remove(DUTY_TIME)
            preferences.remove(DUTY_TABLE)
            preferences.remove(DUTY_NUMBER)
            preferences.remove(LAST_SAVED_DATE)
        }
    }
}

data class DutySettings(
    val time: String,
    val table: Int,
    val number: Int,
    val isPt: Boolean
)
