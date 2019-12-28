package net.inferno.compose.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import kotlinx.android.synthetic.main.activity_main.*
import net.inferno.compose.R
import net.inferno.compose.viewmodel.MainViewModel

class AwaitGameInputFragment : Fragment(R.layout.fragment_await_game_input) {
    private val gameViewModel by activityViewModels<MainViewModel>()

}