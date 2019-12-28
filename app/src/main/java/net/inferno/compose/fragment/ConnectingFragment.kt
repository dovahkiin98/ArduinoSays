package net.inferno.compose.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import net.inferno.compose.R
import net.inferno.compose.viewmodel.MainViewModel

class ConnectingFragment : Fragment(R.layout.fragment_connecting) {
    private val gameViewModel by activityViewModels<MainViewModel>()
}