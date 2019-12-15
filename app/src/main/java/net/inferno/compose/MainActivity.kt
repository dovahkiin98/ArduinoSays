package net.inferno.compose

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.italic
import androidx.core.text.underline
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val bluetoothAdapter by lazy { BluetoothAdapter.getDefaultAdapter() }
    private var defaultBluetooth = false
    private var connectionSocket: BluetoothSocket? = null
    private var ledSequence = ByteArray(500)
    private var lastIndex = 0

    private var currentInputCount = 0
    private var currentLedSequence = ByteArray(500)

    private var timerJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        defaultBluetooth = bluetoothAdapter.isEnabled

        initBluetooth()

        red.setOnClickListener {
            if (connectionSocket == null) return@setOnClickListener

            addToSequence(4)
        }

        blue.setOnClickListener {
            if (connectionSocket == null) return@setOnClickListener


            addToSequence(5)
        }

        yellow.setOnClickListener {
            if (connectionSocket == null) return@setOnClickListener

            addToSequence(3)
        }

        green.setOnClickListener {
            if (connectionSocket == null) return@setOnClickListener

            addToSequence(2)
        }

        retryConnection.setOnClickListener { initBluetooth() }

        restart.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                connectionSocket!!.outputStream.write(SIGNAL_RESTART_SEQUENCE)

                withContext(Dispatchers.Main) {
                    startGame()
                }
            }
        }

        replay.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                connectionSocket!!.outputStream.write(SIGNAL_RESTART_SEQUENCE)

                withContext(Dispatchers.Main) {
                    startGame()
                }
            }
        }
    }

    private fun addToSequence(led: Byte) {
        currentLedSequence[currentInputCount++] = led

        if (currentInputCount == lastIndex) {
            standby.isVisible = true
            repeatMessage.isVisible = false

            connectionSocket!!.outputStream.write(SIGNAL_REPEATING_FINISHED)
            connectionSocket!!.outputStream.write(currentLedSequence)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initBluetooth() {
        if (!defaultBluetooth) bluetoothAdapter.enable()

        loading.isVisible = true
        error.isVisible = false

        var hc_05: BluetoothDevice? = null

        lifecycleScope.launch(Dispatchers.IO) {
            while (hc_05 == null) {
                val devices = bluetoothAdapter.bondedDevices

                hc_05 = devices.find { it.address == "00:13:EF:00:17:27" }
            }

            val uuids = hc_05!!.uuids
            connectionSocket = hc_05!!.createRfcommSocketToServiceRecord(uuids[0].uuid)

            bluetoothAdapter.cancelDiscovery()

            try {
                withTimeout(5000) {
                    withContext(Dispatchers.IO) {
                        try {
                            connectionSocket!!.connect()
                        } catch (e: Exception) {
                            connectionSocket = null

                            withContext(Dispatchers.Main) {
                                loading.isVisible = false
                                error.isVisible = true
                                errorText.text = "(" + e.message + ")"
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                connectionSocket = null

                withContext(Dispatchers.Main) {
                    loading.isVisible = false
                    error.isVisible = true
                    errorText.text = "(" + e.message + ")"
                }
            }

            if (connectionSocket != null) {
                listenToInput()

                connectionSocket!!.outputStream.write(SIGNAL_RESTART_SEQUENCE)

                withContext(Dispatchers.Main) {
                    startGame()
                }
            }
        }
    }

    private fun listenToInput() {
        lifecycleScope.launch(Dispatchers.IO) {
            while (connectionSocket != null && connectionSocket!!.isConnected) {
                val receivedSignal = connectionSocket!!.inputStream.read()

                when {
                    receivedSignal >= SIGNAL_WRONG_SEQUENCE -> {
                        withContext(Dispatchers.Main) {
                            gameOver.isVisible = true
                            score.text = buildSpannedString {
                                append("Your score was : ")
                                bold {
                                    italic {
                                        underline {
                                            append("${receivedSignal - SIGNAL_WRONG_SEQUENCE}")
                                        }
                                    }
                                }
                            }
                            standby.isVisible = false
                        }
                    }
                    receivedSignal == SIGNAL_GAME_WON -> {
                        withContext(Dispatchers.Main) {
                            gameWon.isVisible = true
                            standby.isVisible = false
                        }
                    }
                    receivedSignal > SIGNAL_SEQUENCE_FINISHED -> {
                        currentScore.text = "Your score : $lastIndex"
                        val led = (receivedSignal - SIGNAL_SEQUENCE_FINISHED).toByte()
                        ledSequence[lastIndex++] = led

                        currentInputCount = 0
                        currentLedSequence = ByteArray(500)

                        withContext(Dispatchers.Main) {
                            standby.isVisible = false
                            repeatMessage.isVisible = true
                        }
                    }
                }
            }
        }
    }

    private fun startGame() {
        loading.isVisible = false
        content.isVisible = true
        standby.isVisible = true
        gameOver.isVisible = false

        ledSequence = ByteArray(500)
        lastIndex = 0

        currentInputCount = 0
        currentLedSequence = ByteArray(500)
    }

    private fun endGame() {
        lifecycleScope.cancel()
        connectionSocket?.let {
            it.outputStream.write(SIGNAL_GAME_ENDED)
            while (it.isConnected) {
                try {
                    it.close()
                    connectionSocket = null
                } catch (e: Exception) {

                }
            }
        }
    }

    override fun onBackPressed() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Exit Game?")
            .setMessage("Are you sure you want to end the game?")
            .setPositiveButton("Yes") { _, _ ->
                endGame()
                super.onBackPressed()
            }
            .setNeutralButton("No") { _, _ -> }
            .show()
    }

    override fun onStop() {
        super.onStop()

        endGame()

        if (!defaultBluetooth) bluetoothAdapter.disable()
    }

    companion object {
        const val SIGNAL_REPEATING_FINISHED = 0xA1
        const val SIGNAL_RESTART_SEQUENCE = 0xA2
        const val SIGNAL_GAME_ENDED = 0xA0
        const val SIGNAL_GAME_WON = 0xA3

        const val SIGNAL_SEQUENCE_FINISHED = 0x50
        const val SIGNAL_WRONG_SEQUENCE = 0xC0
    }
}