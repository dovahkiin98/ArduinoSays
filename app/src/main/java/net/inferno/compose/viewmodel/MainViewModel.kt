package net.inferno.compose.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import net.inferno.compose.fragment.SoundsInputFragment
import net.inferno.compose.game.GameManager
import net.inferno.compose.game.GameState

class MainViewModel : ViewModel() {

    lateinit var gameManager: GameManager

    val gameState = MutableLiveData<GameState>()
    val currentScore = MutableLiveData(0)
    val maxScore = MutableLiveData<Int>()
    val gameStarted = MutableLiveData<Boolean>(false)

    lateinit var preference: SharedPreferences

    var colors
        get() = gameManager.colors
        set(colors) {
            gameManager.colors = colors
        }

    var gestureDetector: SoundsInputFragment.InputGestureDetector?
        get() = null
        set(gestureDetector) {
            gameManager.setGestureDetector(gestureDetector!!)
        }

    fun init(lifecycle: Lifecycle, context: Context) {
        gameManager = GameManager(lifecycle, this)

        lifecycle.addObserver(gameManager)

        preference = PreferenceManager.getDefaultSharedPreferences(context)

        maxScore.postValue(preference.getInt("maxscore", 0))
    }

    companion object {
        const val GAME_MODE_SOUNDS = 0xA
        const val GAME_MODE_COLORS = 0xB
    }
}