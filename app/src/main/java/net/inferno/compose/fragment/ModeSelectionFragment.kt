package net.inferno.compose.fragment

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import kotlinx.android.synthetic.main.fragment_mode_selection.*
import net.inferno.compose.R
import net.inferno.compose.game.GameState
import net.inferno.compose.viewmodel.MainViewModel

class ModeSelectionFragment : Fragment(R.layout.fragment_mode_selection) {

    private val gameViewModel by activityViewModels<MainViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        colorsMode.setOnClickListener {
            gameViewModel.gameState.postValue(GameState.UserInput(mode = MainViewModel.GAME_MODE_COLORS))
        }

        soundsMode.setOnClickListener {
            gameViewModel.gameState.postValue(GameState.UserInput(mode = MainViewModel.GAME_MODE_SOUNDS))
        }

        gameViewModel.maxScore.observe(this) {
            maxScore.text = buildSpannedString {
                append("Your max score is : ")
                bold { append("$it") }
            }
        }

    }
}