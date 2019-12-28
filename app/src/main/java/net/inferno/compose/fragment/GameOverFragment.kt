package net.inferno.compose.fragment

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import kotlinx.android.synthetic.main.fragment_game_over.*
import net.inferno.compose.R
import net.inferno.compose.viewmodel.MainViewModel

class GameOverFragment : Fragment(R.layout.fragment_game_over) {
    private val gameViewModel by activityViewModels<MainViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        score.text = requireArguments().getCharSequence("message") ?: ""
    }

    companion object {
        fun newInstance(message: CharSequence) = GameOverFragment().apply {
            arguments = bundleOf(
                "message" to message
            )
        }
    }
}