package com.tertiaryinfotech.freefood.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/** User preferences. Mirrors the iOS `AppSettings` backed by UserDefaults. */
data class SettingsState(
    val onlyMyCountry: Boolean = true,
    val limitDistance: Boolean = true,
    val maxDistanceKm: Double = 10.0,
    val subscribedIds: Set<String> = emptySet(),
    val likedIds: Set<String> = emptySet(),
) {
    fun isSubscribed(id: String) = subscribedIds.contains(id)
    fun hasLiked(id: String) = likedIds.contains(id)
}

class AppSettings(private val context: Context) {

    /** The user's country, inferred from the device region (e.g. "SG"). */
    val myCountry: String = Locale.getDefault().country ?: ""

    val state: Flow<SettingsState> = context.dataStore.data.map { p ->
        SettingsState(
            onlyMyCountry = p[Keys.onlyMyCountry] ?: true,
            limitDistance = p[Keys.limitDistance] ?: true,
            maxDistanceKm = p[Keys.maxDistanceKm] ?: 10.0,
            subscribedIds = p[Keys.subscribed] ?: emptySet(),
            likedIds = p[Keys.liked] ?: emptySet(),
        )
    }

    suspend fun setOnlyMyCountry(value: Boolean) = edit { it[Keys.onlyMyCountry] = value }
    suspend fun setLimitDistance(value: Boolean) = edit { it[Keys.limitDistance] = value }
    suspend fun setMaxDistanceKm(value: Double) = edit { it[Keys.maxDistanceKm] = value }

    suspend fun toggleSubscribe(id: String) = edit { p ->
        val current = p[Keys.subscribed] ?: emptySet()
        p[Keys.subscribed] = if (id in current) current - id else current + id
    }

    suspend fun clearSubscriptions() = edit { it[Keys.subscribed] = emptySet() }

    suspend fun markLiked(id: String) = edit { p ->
        p[Keys.liked] = (p[Keys.liked] ?: emptySet()) + id
    }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.dataStore.edit(block)
    }

    private object Keys {
        val onlyMyCountry = booleanPreferencesKey("onlyMyCountry")
        val limitDistance = booleanPreferencesKey("limitDistance")
        val maxDistanceKm = doublePreferencesKey("maxDistanceKm")
        val subscribed = stringSetPreferencesKey("subscribedIDs")
        val liked = stringSetPreferencesKey("likedIDs")
    }
}
