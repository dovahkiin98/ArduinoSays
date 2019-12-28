package net.inferno.compose.fragment

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import kotlinx.android.synthetic.main.fragment_connection_error.*
import net.inferno.compose.R
import net.inferno.compose.viewmodel.MainViewModel

class ConnectionErrorFragment : Fragment(R.layout.fragment_connection_error) {
    private val gameViewModel by activityViewModels<MainViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        errorText.text = requireArguments().getCharSequence("message") ?: ""

        retryConnection.setOnClickListener { gameViewModel.gameManager.initBluetooth() }
    }

    companion object {
        fun newInstance(message: CharSequence) = ConnectionErrorFragment().apply {
            arguments = bundleOf(
                "message" to message
            )
        }
    }
}