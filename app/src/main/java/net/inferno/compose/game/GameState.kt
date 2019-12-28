package net.inferno.compose.game

open class GameState(val message: CharSequence) {
    class Connecting(message: CharSequence = "Loading") : GameState(message)

    class ConnectionSuccessful(message: CharSequence = "Connected") : GameState(message)

    class ConnectionError(message: CharSequence) : GameState(message)

    class Disconnected(message: CharSequence) : GameState(message)

    class GameOver(message: CharSequence) : GameState(message)

    class GameWon(message: CharSequence = "") : GameState(message)

    class AwaitGameInput(message: CharSequence = "") : GameState(message)

    class UserInput(message: CharSequence = "", val mode: Int = 0) : GameState(message)
}