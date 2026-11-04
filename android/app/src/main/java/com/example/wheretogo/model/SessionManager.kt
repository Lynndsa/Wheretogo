
package com.example.wheretogo.model

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_session")

object SessionManager {

    private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    private val USER_LOGIN = stringPreferencesKey("user_login")
    private val USER_ID = longPreferencesKey("user_id")

    fun isLoggedIn(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[IS_LOGGED_IN] ?: false }

    fun getUserLogin(context: Context): Flow<String?> =
        context.dataStore.data.map { it[USER_LOGIN] }

    fun getUserId(context: Context): Flow<Long?> =
        context.dataStore.data.map { it[USER_ID] }

    suspend fun saveSession(context: Context, idUser: Long, login: String) {
        context.dataStore.edit {
            it[IS_LOGGED_IN] = true
            it[USER_ID] = idUser
            it[USER_LOGIN] = login
        }
    }

    suspend fun clearSession(context: Context) {
        context.dataStore.edit {
            it[IS_LOGGED_IN] = false
            it.remove(USER_LOGIN)
            it.remove(USER_ID)
        }
    }
}
