package com.example.travelassistant

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class DataStoreManager(private val context: Context) {

    companion object {
        private val Context.dataStore by preferencesDataStore(name = "settings")
        private val USER_ID_DS_KEY = stringPreferencesKey("user_id_key")
    }

    suspend fun storeUserId(id: String) {
        CoroutineScope(Dispatchers.IO).launch {
            context.dataStore.edit { settings ->
                settings[USER_ID_DS_KEY] = id
            }
        }
    }

    val userIdFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[USER_ID_DS_KEY] ?: ""
        }
}