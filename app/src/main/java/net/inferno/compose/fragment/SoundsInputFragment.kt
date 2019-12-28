package net.inferno.compose.fragment

import android.os.Bundle
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import kotlinx.android.synthetic.main.fragment_sounds_input.*
import net.inferno.compose.R
import net.inferno.compose.viewmodel.MainViewModel
import kotlin.math.abs

class SoundsInputFragment : Fragment(R.layout.fragment_sounds_input) {
    private val gameViewModel by activityViewModels<MainViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val listener = InputGestureDetector()
        val gestureDetector = GestureDetector(listener)

        inputView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
        }

        gameViewModel.gestureDetector = listener

        gameViewModel.currentScore.observe(this) {
            currentScore.text = "Your current score $it"
        }
    }

    inner class InputGestureDetector : SimpleOnGestureListener() {

        var onGesture: (Int) -> Unit = {}

        override fun onDown(e: MotionEvent?) = true

        override fun onFling(
            e1: MotionEvent, e2: MotionEvent, velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (
                e1.x - e2.x > SWIPE_MIN_DISTANCE
                && abs(velocityX) > SWIPE_THRESHOLD_VELOCITY
            ) {
                if (abs(e1.y - e2.y) > SWIPE_MAX_OFF_PATH) return false
                else onGesture(5)
            } else if (
                e2.x - e1.x > SWIPE_MIN_DISTANCE
                && abs(velocityX) > SWIPE_THRESHOLD_VELOCITY
            ) {
                if (abs(e1.y - e2.y) > SWIPE_MAX_OFF_PATH) return false
                else onGesture(3)
            }

            if (
                e1.y - e2.y > SWIPE_MIN_DISTANCE
                && abs(velocityY) > SWIPE_THRESHOLD_VELOCITY
            ) {
                if (abs(e1.x - e2.x) > SWIPE_MAX_OFF_PATH) return false
                else onGesture(2)
            } else if (
                e2.y - e1.y > SWIPE_MIN_DISTANCE
                && abs(velocityY) > SWIPE_THRESHOLD_VELOCITY
            ) {
                if (abs(e1.x - e2.x) > SWIPE_MAX_OFF_PATH) return false
                else onGesture(4)
            }
            return true
        }
    }

    companion object {
        private const val SWIPE_MIN_DISTANCE = 300
        private const val SWIPE_MAX_OFF_PATH = 250
        private const val SWIPE_THRESHOLD_VELOCITY = 1000
    }
}