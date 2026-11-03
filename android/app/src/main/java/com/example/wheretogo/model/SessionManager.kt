package com.example.wheretogo.model


import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(
    name = "user_session"
)

object SessionManager {

    private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")

    fun isLoggedIn(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[IS_LOGGED_IN] ?: false
        }
    }

    suspend fun saveSession(context: Context) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = true
        }
    }

    suspend fun clearSession(context: Context) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = false
        }
    }
}
