package net.inferno.compose.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import kotlinx.android.synthetic.main.fragment_colors_input.*
import net.inferno.compose.R
import net.inferno.compose.viewmodel.MainViewModel

class ColorsInputFragment : Fragment(R.layout.fragment_colors_input) {
    private val gameViewModel by activityViewModels<MainViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        gameViewModel.colors = listOf(
            green,
            yellow,
            red,
            blue
        )

        gameViewModel.currentScore.observe(this) {
            currentScore.text = "Your current score $it"
        }
    }
}