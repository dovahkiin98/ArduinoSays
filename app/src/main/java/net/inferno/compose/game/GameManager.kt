package net.inferno.compose.game

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.view.View
import androidx.core.content.edit
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.italic
import androidx.core.text.underline
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.*
import net.inferno.compose.fragment.SoundsInputFragment
import net.inferno.compose.viewmodel.MainViewModel

class GameManager(val lifecycle: Lifecycle, private val viewModel: MainViewModel) :
    LifecycleObserver {

    private val lifecycleScope = lifecycle.coroutineScope
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private var defaultBluetooth = bluetoothAdapter.isEnabled
    private var connectionSocket: BluetoothSocket? = null

    private var ledSequence = ByteArray(500)
    private var lastIndex = 0

    private var currentInputCount = 0
    private var currentLedSequence = ByteArray(500)

    private var gameState
        get() = viewModel.gameState.value
        set(gameState) {
            viewModel.gameState.postValue(gameState)
        }

    private var currentScore
        get() = viewModel.currentScore.value ?: 0
        set(currentScore) {
            viewModel.currentScore.postValue(currentScore)
        }

    var colors = emptyList<View>()
        set(colors) {
            colors.forEach {
                it.setOnClickListener { _ ->
                    if (connectionSocket == null) return@setOnClickListener

                    addToSequence(it.tag.toString().toByte())
                }
            }
            field = colors
        }

    fun setGestureDetector(value: SoundsInputFragment.InputGestureDetector) {
        value.onGesture = { direction ->
            if (connectionSocket != null) {
                addToSequence(direction.toByte())
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun initBluetooth() {
        if (!defaultBluetooth) bluetoothAdapter.enable()

        gameState = GameState.Connecting()

        var hc_05: BluetoothDevice? = bluetoothAdapter.bondedDevices.find {
            it.address == "00:13:EF:00:17:27"
        }

        lifecycleScope.launch(Dispatchers.IO) {
            while (hc_05 == null) {
                hc_05 = bluetoothAdapter.bondedDevices.find { it.address == "00:13:EF:00:17:27" }
            }

            val uuids = hc_05!!.uuids
            connectionSocket = hc_05!!.createRfcommSocketToServiceRecord(uuids[0].uuid)

            bluetoothAdapter.cancelDiscovery()

            try {
                withTimeout(5000) {
                    withContext(Dispatchers.IO) {
                        if (connectionSocket != null && connectionSocket!!.isConnected) connectionSocket!!.close()

                        try {
                            connectionSocket!!.connect()
                        } catch (e: Exception) {
                            connectionSocket = null


                            gameState = GameState.ConnectionError("(${e.message})")
                        }
                    }
                }
            } catch (e: Exception) {
                connectionSocket = null

                gameState = GameState.ConnectionError("(${e.message})")
            }

            if (connectionSocket != null) {
                listenToGameInput()

                gameState = GameState.ConnectionSuccessful()
            }
        }
    }

    private fun addToSequence(led: Byte) {
        currentLedSequence[currentInputCount++] = led

        if (currentInputCount == lastIndex) {
            gameState = GameState.AwaitGameInput()

            connectionSocket!!.outputStream.write(SIGNAL_REPEATING_FINISHED)
            connectionSocket!!.outputStream.write(currentLedSequence)
        }
    }

    private fun listenToGameInput() {
        lifecycleScope.launch(Dispatchers.IO) {
            while (connectionSocket != null && connectionSocket!!.isConnected) {
                try {
                    val receivedSignal = connectionSocket!!.inputStream.read()

                    when {
                        receivedSignal == SIGNAL_WRONG_SEQUENCE -> {
                            gameState = GameState.GameOver(buildSpannedString {
                                append("Your score was : ")
                                bold {
                                    italic {
                                        underline {
                                            append("$currentScore")
                                        }
                                    }
                                }
                            })

                            if (currentScore > viewModel.preference.getInt("maxscore", 0)) {
                                viewModel.preference.edit {
                                    putInt("maxscore", currentScore)
                                }

                                viewModel.maxScore.postValue(currentScore)
                            }
                        }
                        receivedSignal == SIGNAL_GAME_WON -> {
                            gameState = GameState.GameWon()

                            viewModel.preference.edit {
                                putInt("maxscore", 25)
                            }

                            viewModel.maxScore.postValue(currentScore)
                        }
                        receivedSignal > SIGNAL_SEQUENCE_FINISHED -> {
                            currentScore = lastIndex
                            val led = (receivedSignal - SIGNAL_SEQUENCE_FINISHED).toByte()
                            ledSequence[lastIndex++] = led

                            currentInputCount = 0
                            currentLedSequence.fill(0)

                            gameState = GameState.UserInput()

                            if (currentScore > viewModel.preference.getInt("maxscore", 0)) {
                                viewModel.preference.edit {
                                    putInt("maxscore", currentScore)
                                }

                                viewModel.maxScore.postValue(currentScore)
                            }
                        }
                    }
                } catch (e: Exception) {

                }
            }

            gameState = GameState.Disconnected("Disconnected")
        }
    }

    fun startGame() {
        lifecycleScope.launch(Dispatchers.IO) {
            connectionSocket!!.outputStream.write(SIGNAL_RESTART_SEQUENCE)

            gameState = GameState.AwaitGameInput()

            ledSequence = ByteArray(500)
            lastIndex = 0

            currentInputCount = 0
            currentLedSequence.fill(0)
        }
    }

    fun endGame() {
        lifecycleScope.launch(Dispatchers.IO) {
            connectionSocket?.outputStream?.write(SIGNAL_GAME_ENDED)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onStop() {
        GlobalScope.launch(Dispatchers.IO) {
            if (viewModel.gameStarted.value == true) {
                connectionSocket?.outputStream?.write(SIGNAL_GAME_ENDED)
            }

            connectionSocket?.let {
                while (it.isConnected) {
                    try {
                        it.close()
                        connectionSocket = null
                    } catch (e: Exception) {

                    }
                }
            }


            gameState = GameState.Disconnected("Disconnected")

            if (!defaultBluetooth) bluetoothAdapter.disable()
        }
    }

    companion object {
        const val SIGNAL_REPEATING_FINISHED = 0xA1
        const val SIGNAL_RESTART_SEQUENCE = 0xA2
        const val SIGNAL_GAME_ENDED = 0xA0
        const val SIGNAL_GAME_WON = 0xA3

        const val SIGNAL_SEQUENCE_FINISHED = 0xD0
        const val SIGNAL_WRONG_SEQUENCE = 0xC0
    }
}