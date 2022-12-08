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


// Create a DataStore instance using the preferencesDataStore delegate, with the Context as
// receiver.
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "unscramble_datastore"
)

data class HighScoreData(
    val highscorePoints: Int = 0,
    val highscoreDateTime: String = ""
)

class SettingsDataStore(private val preference_datastore: DataStore<Preferences>) {
    private val HIGH_SCORE_VALUE = intPreferencesKey("saved_high_score_value")
    private val HIGH_SCORE_DATETIME = stringPreferencesKey("saved_high_score_datetime")

    val readFromDataStore: Flow<HighScoreData> = preference_datastore.data
        .catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            val highscorePoints = preferences[HIGH_SCORE_VALUE] ?: 0
            val highscoreDateTime = preferences[HIGH_SCORE_DATETIME] ?: ""
            HighScoreData(
                // On the first run of the app, we will return a high_score value of 0 by default
                highscorePoints,
                highscoreDateTime
            )
        }

    suspend fun saveToDataStore(newHighScore: Int, context: Context) {
        context.dataStore.edit { preferences ->
            preferences[HIGH_SCORE_DATETIME] = LocalDateTime.now().toString()
            preferences[HIGH_SCORE_VALUE] = newHighScore
        }
    }

    suspend fun clearDataStore(context: Context) {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

}