package com.cartouchoo.cartarocho.ui.common

import com.cartouchoo.cartarocho.domain.Contract

fun contractLabel(contract: Contract): String = when (contract) {
    Contract.PETITE -> "Petite"
    Contract.GARDE -> "Garde"
    Contract.GARDE_SANS -> "Garde sans"
    Contract.GARDE_CONTRE -> "Garde contre"
}
