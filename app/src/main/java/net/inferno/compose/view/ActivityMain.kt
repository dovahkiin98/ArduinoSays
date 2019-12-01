package net.inferno.compose.view

import android.widget.Toast
import androidx.compose.Composable
import androidx.compose.Context
import androidx.ui.core.Dp
import androidx.ui.core.Text
import androidx.ui.foundation.shape.border.Border
import androidx.ui.graphics.Color
import androidx.ui.graphics.SolidColor
import androidx.ui.layout.Center
import androidx.ui.layout.Column
import androidx.ui.material.Button
import androidx.ui.material.Colors
import androidx.ui.material.MaterialTheme
import androidx.ui.material.OutlinedButtonStyle
import androidx.ui.tooling.preview.Preview

@Composable
fun Greeting(context: Context? = null) {
    MaterialTheme {
        Center {
            Column(
                block = {
                    Button(
                        text = "Click Me",
                        onClick = {
                            Toast.makeText(context, "Hello world", Toast.LENGTH_LONG).show()
                        },
                        style = OutlinedButtonStyle(
                            color = Color.Transparent,
                            border = Border(
                                brush = SolidColor(Color.Blue),
                                width = Dp(2f)
                            )
                        )
                    )
                }
            )
        }
    }
}

@Preview
@Composable
fun DefaultPreview() {
    Greeting()
}
