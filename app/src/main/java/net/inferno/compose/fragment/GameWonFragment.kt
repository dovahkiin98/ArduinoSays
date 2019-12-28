package net.inferno.compose.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_game_won.*
import net.inferno.compose.R
import net.inferno.compose.viewmodel.MainViewModel

class GameWonFragment : Fragment(R.layout.fragment_game_won) {
    private val gameViewModel by activityViewModels<MainViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    }
}