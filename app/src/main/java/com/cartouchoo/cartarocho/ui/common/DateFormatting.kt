package com.cartouchoo.cartarocho.ui.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDate(epochMillis: Long): String =
    SimpleDateFormat("d MMMM yyyy", Locale.FRANCE).format(Date(epochMillis))

fun formatDateTime(epochMillis: Long): String =
    SimpleDateFormat("d MMMM yyyy 'à' HH:mm", Locale.FRANCE).format(Date(epochMillis))
