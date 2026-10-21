package com.cartouchoo.cartarocho.ui.common

import java.util.Locale

/** Formate un score avec un signe explicite (+174, -58, 0). */
fun formatScore(value: Double): String {
    val isInteger = value == Math.floor(value)
    val magnitude = if (isInteger) {
        kotlin.math.abs(value).toLong().toString()
    } else {
        String.format(Locale.FRANCE, "%.1f", kotlin.math.abs(value))
    }
    return when {
        value > 0 -> "+$magnitude"
        value < 0 -> "-$magnitude"
        else -> "0"
    }
}

/** Formate un rang sous forme ordinale française (1er, 2e, 3e, ...). */
fun formatRank(rank: Int): String = when (rank) {
    1 -> "1er"
    else -> "${rank}e"
}
