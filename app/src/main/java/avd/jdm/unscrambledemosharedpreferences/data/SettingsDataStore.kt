package avd.jdm.unscrambledemosharedpreferences.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.LocalDateTime

private const val HIGH_SCORE_PREFERENCES_NAME = "high_score_preferences"

// Create a DataStore instance using the preferencesDataStore delegate, with the Context as
// receiver.
val Context.dataStore : DataStore<Preferences> by preferencesDataStore(
    name = HIGH_SCORE_PREFERENCES_NAME
)

class SettingsDataStore(preference_datastore: DataStore<Preferences>) {
    private val HIGH_SCORE_VALUE = intPreferencesKey("saved_high_score_value")
    private val HIGH_SCORE_DATETIME = stringPreferencesKey("saved_high_score_datetime")

    val preferenceFlow: Flow<Int> = preference_datastore.data
        .catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[HIGH_SCORE_DATETIME] ?: ""
            // On the first run of the app, we will return a high_score value of 0 by default
            preferences[HIGH_SCORE_VALUE] ?: 0
        }

    suspend fun saveHighScoreToPreferencesStore(newHighScore: Int, context: Context) {
        context.dataStore.edit { preferences ->
            preferences[HIGH_SCORE_DATETIME] = LocalDateTime.now().toString()
            preferences[HIGH_SCORE_VALUE] = newHighScore
        }
    }
}