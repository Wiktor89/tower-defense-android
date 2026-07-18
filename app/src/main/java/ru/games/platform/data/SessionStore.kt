package ru.games.platform.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import ru.games.platform.data.api.User

private val Context.dataStore by preferencesDataStore("games_session")

class SessionStore(private val context: Context) {
    private val gson = Gson()
    private val keyBaseUrl = stringPreferencesKey("base_url")
    private val keyUser = stringPreferencesKey("user_json")

    val baseUrl: Flow<String> = context.dataStore.data.map { it[keyBaseUrl].orEmpty() }

    val user: Flow<User?> = context.dataStore.data.map { prefs ->
        prefs[keyUser]?.let { runCatching { gson.fromJson(it, User::class.java) }.getOrNull() }
    }

    suspend fun getBaseUrl(): String = baseUrl.first()

    suspend fun getUser(): User? = user.first()

    suspend fun setBaseUrl(url: String) {
        context.dataStore.edit { it[keyBaseUrl] = url.trim().trimEnd('/') }
    }

    suspend fun setUser(user: User) {
        context.dataStore.edit { it[keyUser] = gson.toJson(user) }
    }

    suspend fun clearUser() {
        context.dataStore.edit { it.remove(keyUser) }
    }
}
