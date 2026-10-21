package com.cartouchoo.cartarocho

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cartouchoo.cartarocho.ui.navigation.CartarochoNavHost
import com.cartouchoo.cartarocho.ui.theme.CartarochoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as CartarochoApplication).container

        setContent {
            CartarochoTheme {
                CartarochoNavHost(container = container)
            }
        }
    }
}
