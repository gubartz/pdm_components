package br.edu.ifsp.hto.htoipdm.ui_components.extensions

import kotlin.math.pow
import kotlin.math.round

fun Double.roundTo(digits: Int): Double {
    val factor = 10.0.pow(digits)
    return round(this * factor) / factor
}