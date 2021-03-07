package me.heizi.kotlinx.android

import android.view.View

fun dontInvokeMe() = NoSuchMethodException("has no getter on listener")
var View.onClick: (View) -> Unit
    get() = { dontInvokeMe() }
    set(value) { setOnClickListener(value)}
