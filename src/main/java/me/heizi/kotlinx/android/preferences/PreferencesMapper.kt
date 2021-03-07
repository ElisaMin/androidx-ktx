package me.heizi.kotlinx.android.preferences

import androidx.lifecycle.LifecycleObserver

/**
 * Preferences mapper
 *
 * 包含`get`和`update`这个请求
 */
interface PreferencesMapper:LifecycleObserver {
    val hashMap:HashMap<String,Any?>
    fun <T> updatePreference(key: String,value:T)
}