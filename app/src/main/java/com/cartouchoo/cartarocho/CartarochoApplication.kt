package com.cartouchoo.cartarocho

import android.app.Application

class CartarochoApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
