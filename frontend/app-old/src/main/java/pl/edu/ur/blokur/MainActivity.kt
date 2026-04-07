package pl.edu.ur.blokur

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import pl.edu.ur.blokur.ui.navigation.MainScaffold
import pl.edu.ur.blokur.ui.theme.BlokurTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BlokurTheme {
                MainScaffold()
            }
        }
    }
}
