package com.example.gestiondetareasdsm.storage

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.gestiondetareasdsm.model.Session
import com.example.gestiondetareasdsm.model.Task
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore by preferencesDataStore(name = "pomodoro_data")

class DataStoreManager(private val context: Context) {

    companion object {

        val TASKS = stringPreferencesKey("tasks")

        val SESSIONS = stringPreferencesKey("sessions")

        val REMAINING_TIME = longPreferencesKey("remaining_time")

        val FINISH_TIME = longPreferencesKey("finish_time")
    }

    private val gson = Gson()

    suspend fun saveTasks(tasks: List<Task>) {

        val json = gson.toJson(tasks)

        context.dataStore.edit {

            it[TASKS] = json

        }
    }

    fun getTasks(): Flow<List<Task>> {

        return context.dataStore.data

            .catch {

                if (it is IOException) {

                    emit(emptyPreferences())

                } else {

                    throw it

                }

            }

            .map {

                val json = it[TASKS] ?: "[]"

                val type = object : TypeToken<List<Task>>() {}.type

                gson.fromJson(json, type)

            }

    }

    suspend fun saveSessions(sessions: List<Session>) {

        val json = gson.toJson(sessions)

        context.dataStore.edit {

            it[SESSIONS] = json

        }

    }

    fun getSessions(): Flow<List<Session>> {

        return context.dataStore.data

            .catch {

                if (it is IOException) {

                    emit(emptyPreferences())

                } else {

                    throw it

                }

            }

            .map {

                val json = it[SESSIONS] ?: "[]"

                val type = object : TypeToken<List<Session>>() {}.type

                gson.fromJson(json, type)

            }

    }

    suspend fun saveRemainingTime(time: Long) {

        context.dataStore.edit {

            it[REMAINING_TIME] = time

        }

    }

    fun getRemainingTime(): Flow<Long> {

        return context.dataStore.data.map {

            it[REMAINING_TIME] ?: 0L

        }

    }

    suspend fun saveFinishTime(time: Long) {

        context.dataStore.edit {

            it[FINISH_TIME] = time

        }

    }

    fun getFinishTime(): Flow<Long> {

        return context.dataStore.data.map {

            it[FINISH_TIME] ?: 0L

        }

    }

}