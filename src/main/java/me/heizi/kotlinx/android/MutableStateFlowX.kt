package me.heizi.kotlinx.android

import kotlinx.coroutines.flow.MutableStateFlow


inline infix fun <T> MutableStateFlow<T>.set(value:T) { this.value = value }

