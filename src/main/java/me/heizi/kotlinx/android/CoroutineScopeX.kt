package me.heizi.kotlinx.android

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch



inline fun CoroutineScope.main(crossinline block: suspend CoroutineScope.()->Unit) = launch(Dispatchers.Main) {block()}
inline fun CoroutineScope.io(crossinline block: suspend CoroutineScope.()->Unit) = launch(Dispatchers.IO) {block()}
inline fun CoroutineScope.default(crossinline block: suspend CoroutineScope.()->Unit) = launch(Dispatchers.Default) {block()}
inline fun CoroutineScope.unconfined(crossinline block: suspend CoroutineScope.()->Unit) = launch(Dispatchers.Unconfined) {block()}

inline fun ViewModel.main(crossinline block: suspend CoroutineScope.()->Unit) {viewModelScope.launch(
    Dispatchers.Main) {block()}}
inline fun ViewModel.io(crossinline block: suspend CoroutineScope.()->Unit) {viewModelScope.launch(
    Dispatchers.IO) {block()}}
inline fun ViewModel.default(crossinline block: suspend CoroutineScope.()->Unit) {viewModelScope.launch(
    Dispatchers.Default) {block()}}
inline fun ViewModel.unconfined(crossinline block: suspend CoroutineScope.()->Unit) {viewModelScope.launch(
    Dispatchers.Unconfined) {block()}}

inline fun LifecycleOwner.main(crossinline block: suspend CoroutineScope.()->Unit) {lifecycleScope.launch(
    Dispatchers.Main) {block()}}
inline fun LifecycleOwner.io(crossinline block: suspend CoroutineScope.()->Unit) {lifecycleScope.launch(
    Dispatchers.IO) {block()}}
inline fun LifecycleOwner.default(crossinline block: suspend CoroutineScope.()->Unit) {lifecycleScope.launch(
    Dispatchers.Default) {block()}}
inline fun LifecycleOwner.unconfined(crossinline block: suspend CoroutineScope.()->Unit) {lifecycleScope.launch(
    Dispatchers.Unconfined) {block()}}