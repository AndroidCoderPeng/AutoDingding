package com.pengxh.autodingding.extensions

fun Int.appendZero(): String {
    return if (this < 10) {
        "0$this"
    } else {
        this.toString()
    }
}