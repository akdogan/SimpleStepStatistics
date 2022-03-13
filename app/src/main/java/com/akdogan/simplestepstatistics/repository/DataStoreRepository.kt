package com.akdogan.simplestepstatistics.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object DataStoreRepository {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    private val startOfWeekTypedKey: Preferences.Key<Int>
        get() = intPreferencesKey(KEY_START_DAY_OF_WEEK)

    fun getDataFLow(context: Context): Flow<Int> {
        val exampleCounterFlow: Flow<Int> = context.dataStore.data
            .map { preferences ->
                preferences[startOfWeekTypedKey] ?: 1
            }
        return exampleCounterFlow
    }

    suspend fun setData(context: Context, dayOfWeek: Int) {
        if (dayOfWeek < 1 || dayOfWeek > 7) {
            throw IllegalArgumentException("Specified Day is not in Range")
        }
        context.dataStore.edit { settings ->
            settings[startOfWeekTypedKey] = dayOfWeek
        }
    }


    private const val KEY_START_DAY_OF_WEEK = "start_day_of_week"

}