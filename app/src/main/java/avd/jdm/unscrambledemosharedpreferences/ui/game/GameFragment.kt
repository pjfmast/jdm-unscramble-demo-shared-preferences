package avd.jdm.unscrambledemosharedpreferences.ui.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import avd.jdm.unscrambledemosharedpreferences.R
import avd.jdm.unscrambledemosharedpreferences.data.SettingsDataStore
import avd.jdm.unscrambledemosharedpreferences.data.dataStore
import avd.jdm.unscrambledemosharedpreferences.databinding.GameFragmentBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.time.LocalDateTime

private const val TAG = "GameFragment"

/**
 * Fragment where the game is played, contains the game logic.
 */
class GameFragment : Fragment() {

    // Binding object instance with access to the views in the game_fragment.xml layout
    private lateinit var binding: GameFragmentBinding
    private lateinit var gameSettingsDataStore: SettingsDataStore

    // Keeps track of which LayoutManager is in use for the [RecyclerView]
    private var highScoreValue = 0

    // Create a ViewModel the first time the fragment is created.
    // If the fragment is re-created, it receives the same GameViewModel instance created by the
    // first fragment.
    private val viewModel: GameViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout XML file and return a binding object instance
        binding = DataBindingUtil.inflate(inflater, R.layout.game_fragment, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the viewModel for data binding - this allows the bound layout access
        // to all the data in the VieWModel
        binding.gameViewModel = viewModel
        binding.maxNoOfWords = MAX_NO_OF_WORDS
        // Specify the fragment view as the lifecycle owner of the binding.
        // This is used so that the binding can observe LiveData updates
        binding.lifecycleOwner = viewLifecycleOwner

        // Setup a click listener for the Submit and Skip buttons.
        binding.submit.setOnClickListener { onSubmitWord() }
        binding.skip.setOnClickListener { onSkipWord() }

        // Initialize SettingsDataStore
        gameSettingsDataStore = SettingsDataStore(requireContext().dataStore)
        gameSettingsDataStore.preferenceFlow.asLiveData().observe(viewLifecycleOwner, { })
        gameSettingsDataStore.preferenceFlow.asLiveData().observe(viewLifecycleOwner, { value ->
            highScoreValue = value
        })


    }

    /*
    * Checks the user's word, and updates the score accordingly.
    * Displays the next scrambled word.
    * After the last word, the user is shown a Dialog with the final score.
    */
    private fun onSubmitWord() {
        val playerWord = binding.textInputEditText.text.toString()

        if (viewModel.isUserWordCorrect(playerWord)) {
            setErrorTextField(false)
            if (!viewModel.nextWord()) {
                showFinalScoreDialog()
            }
        } else {
            setErrorTextField(true)
        }
    }

    /*
     * Skips the current word without changing the score.
     * Increases the word count.
     * After the last word, the user is shown a Dialog with the final score.
     */
    private fun onSkipWord() {
        if (viewModel.nextWord()) {
            setErrorTextField(false)
        } else {
            showFinalScoreDialog()
        }
    }

    /*
     * Creates and shows an AlertDialog with final score.
     */
    private fun showFinalScoreDialog() {
        val newScore = viewModel.score.value ?: 0
        val title = if (newScore < highScoreValue) (
                getString((R.string.congratulations))
                ) else (getString(R.string.new_high_score, highScoreValue, newScore))

        if (newScore > highScoreValue) {
            lifecycleScope.launch {
                gameSettingsDataStore.saveHighScoreToPreferencesStore(newScore, requireContext())
            }
        }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(getString(R.string.you_scored, viewModel.score.value))
                .setCancelable(false)
                .setNegativeButton(getString(R.string.exit)) { _, _ ->
                    exitGame()
                }
                .setPositiveButton(getString(R.string.play_again)) { _, _ ->
                    restartGame()
                }
                .show()

// not needed anymore, SharedPreferences replaced wih Preferences DataStore:
    // updateHighScore()
    }

/*// not needed anymore, SharedPreferences replaced wih Preferences DataStore:
    private fun getHighScore(): Int {
        val sharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE) ?: return -1
        Log.d(TAG, "getHighScore(): stored preferences " + sharedPreferences.all)

        return sharedPreferences.getInt(getString(R.string.saved_high_score_value), -1)
    }*/

    // not needed anymore, SharedPreferences replaced wih Preferences DataStore:
/*    private fun updateHighScore() {
        val sharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE) ?: return

        with(sharedPreferences.edit()) {
            val newHighScore = viewModel.score.value ?: 0
            putInt(getString(R.string.saved_high_score_value), newHighScore)
            putString(getString(R.string.saved_high_score_date), LocalDateTime.now().toString())
            apply()
        }
    }*/

    /*
     * Re-initializes the data in the ViewModel and updates the views with the new data, to
     * restart the game.
     */
    private fun restartGame() {
        viewModel.reinitializeData()
        setErrorTextField(false)
    }

    /*
     * Exits the game.
     */
    private fun exitGame() {
        activity?.finish()
    }

    /*
    * Sets and resets the text field error status.
    */
    private fun setErrorTextField(error: Boolean) {
        if (error) {
            binding.textField.isErrorEnabled = true
            binding.textField.error = getString(R.string.try_again)
        } else {
            binding.textField.isErrorEnabled = false
            binding.textInputEditText.text = null
        }
    }
}
