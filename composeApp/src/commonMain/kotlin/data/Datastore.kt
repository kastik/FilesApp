package data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import getDataDir
import getDeviceName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath
import java.io.IOException


data class UserPreferences(
    val deviceName: String,
    val deleteAfterZipping: Boolean,
    val saveDestination: String,
    val runBackgroundService: Boolean,
    val backgroundServiceTypeIsUploader: Boolean,
    val savedDeviceNames: Set<String>,
)

class UserPreferencesRepo {

    private val dataStore: DataStore<Preferences> =
        PreferenceDataStoreFactory.createWithPath(
            produceFile = { "${getDataDir()}${dataStoreFileName}".toPath() }
        )

    private object UserPreferencesKeys {
        val DEVICE_NAME = stringPreferencesKey("device_name")
        val DELETE_AFTER_ZIP = booleanPreferencesKey("delete_after_zip")
        val SAVE_DESTINATION = stringPreferencesKey("save_destination")
        val RUN_BACKGROUND_SERVICE = booleanPreferencesKey("run_background_service")
        val BACKGROUND_SERVICE_TYPE_IS_UPLOADER = booleanPreferencesKey("background_service_type")
        val SAVED_DEVICE_NAMES = stringSetPreferencesKey("saved_device_names")
    }

    companion object {
        internal const val dataStoreFileName = "\\filesApp.preferences_pb"
    }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val deviceName = preferences[UserPreferencesKeys.DEVICE_NAME] ?: getDeviceName()
            val deleteAfterZipping = preferences[UserPreferencesKeys.DELETE_AFTER_ZIP] ?: false
            val saveDestination = preferences[UserPreferencesKeys.SAVE_DESTINATION] ?: getDataDir()
            val runBackgroundService =
                preferences[UserPreferencesKeys.RUN_BACKGROUND_SERVICE] ?: false
            val backgroundServiceTypeIsUploader =
                preferences[UserPreferencesKeys.BACKGROUND_SERVICE_TYPE_IS_UPLOADER] ?: false
            val savedDeviceNames = preferences[UserPreferencesKeys.SAVED_DEVICE_NAMES] ?: emptySet()

            UserPreferences(
                deviceName,
                deleteAfterZipping,
                saveDestination,
                runBackgroundService,
                backgroundServiceTypeIsUploader,
                savedDeviceNames
            )
        }

    suspend fun updateDeviceName(deviceName: String) {
        dataStore.edit { preferences ->
            preferences[UserPreferencesKeys.DEVICE_NAME] = deviceName
        }
    }

    suspend fun updateDeleteAfterZipping(deleteAfterZipping: Boolean) {
        dataStore.edit { preferences ->
            preferences[UserPreferencesKeys.DELETE_AFTER_ZIP] = deleteAfterZipping
        }
    }

    suspend fun updateSaveDestination(destination: String) {
        dataStore.edit { preferences ->
            preferences[UserPreferencesKeys.SAVE_DESTINATION] = destination
        }
    }

    suspend fun updateRunBackgroundService(runBackgroundService: Boolean) {
        dataStore.edit { preferences ->
            preferences[UserPreferencesKeys.RUN_BACKGROUND_SERVICE] = runBackgroundService
        }
    }

    suspend fun updateBackgroundServiceTypeIsUploader(backgroundServiceTypeIsUploader: Boolean) {
        dataStore.edit { preferences ->
            preferences[UserPreferencesKeys.BACKGROUND_SERVICE_TYPE_IS_UPLOADER] =
                backgroundServiceTypeIsUploader
        }
    }

    suspend fun updateSavedDeviceNames(savedDeviceNames: Set<String>) {
        dataStore.edit { preferences ->
            preferences[UserPreferencesKeys.SAVED_DEVICE_NAMES] = savedDeviceNames
        }
    }

}

