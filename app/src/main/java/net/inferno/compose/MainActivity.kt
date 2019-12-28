package net.inferno.compose

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.observe
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Job
import net.inferno.compose.fragment.*
import net.inferno.compose.game.GameState
import net.inferno.compose.viewmodel.MainViewModel

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val gameViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gameViewModel.init(lifecycle, this)

        toolbar.setNavigationOnClickListener { onBackPressed() }

        toolbar.setOnMenuItemClickListener {
            gameViewModel.gameManager.startGame()
            true
        }

        gameViewModel.gameState.observe(this) {
            when (it) {
                is GameState.UserInput -> {
                    messageLayer.isVisible = false

                    if (it.mode != 0) {
                        when (it.mode) {
                            MainViewModel.GAME_MODE_COLORS -> setInputFragment(ColorsInputFragment())
                            MainViewModel.GAME_MODE_SOUNDS -> setInputFragment(SoundsInputFragment())
                        }

                        gameViewModel.gameManager.setGameMode(it.mode)

                        toolbar.setNavigationIcon(R.drawable.ic_arrow)
                        toolbar.menu.findItem(R.id.action_restart).isVisible = true

                        gameViewModel.gameManager.startGame()

                        gameViewModel.gameStarted.postValue(true)
                    }

                    toolbar.menu.findItem(R.id.action_restart).isEnabled = true
                }

                is GameState.Connecting -> {
                    setMessageFragment(ConnectingFragment())
                }

                is GameState.ConnectionSuccessful -> {
                    inputLayer.isVisible = true
                    messageLayer.isVisible = false

                    if(gameViewModel.gameStarted.value == false) {
                        toolbar.navigationIcon = null
                        toolbar.menu.findItem(R.id.action_restart).isVisible = false

                        setInputFragment(ModeSelectionFragment())
                    } else gameViewModel.gameManager.startGame()
                }

                is GameState.ConnectionError -> {
                    setMessageFragment(ConnectionErrorFragment.newInstance(it.message))
                }

                is GameState.AwaitGameInput -> {
                    inputLayer.isVisible = true
                    messageLayer.isVisible = true

                    setMessageFragment(AwaitGameInputFragment())

                    toolbar.menu.findItem(R.id.action_restart).isEnabled = false
                }

                is GameState.GameOver -> {
                    messageLayer.isVisible = true

                    toolbar.menu.findItem(R.id.action_restart).isEnabled = true

                    setMessageFragment(GameOverFragment.newInstance(it.message))
                }

                is GameState.GameWon -> {
                    messageLayer.isVisible = true

                    setMessageFragment(GameWonFragment())
                }

                is GameState.Disconnected -> {
                    inputLayer.isVisible = false
                    messageLayer.isVisible = true

                    toolbar.menu.findItem(R.id.action_restart).isEnabled = false

                    setMessageFragment(ConnectingFragment())
                }
            }
        }
    }

    private fun setMessageFragment(fragment: Fragment) {
        supportFragmentManager.commit {
            replace(R.id.messageLayer, fragment)
        }
    }

    private fun setInputFragment(fragment: Fragment) {
        supportFragmentManager.commit {
            replace(R.id.inputLayer, fragment)
        }
    }

    override fun onBackPressed() {
        if (gameViewModel.gameStarted.value == true) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Exit Game?")
                .setMessage("Are you sure you want to end the game?")
                .setPositiveButton("Yes") { _, _ ->
                    gameViewModel.gameStarted.value = false
                    gameViewModel.gameState.postValue(GameState.ConnectionSuccessful())
                    gameViewModel.gameManager.endGame()
                }
                .setNeutralButton("No") { _, _ -> }
                .show()
        } else super.onBackPressed()
    }
}