package me.heizi.kotlinx.android.preferences

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.heizi.kotlinx.android.preferences.PreferencesManager.Global.Companion.INSTANT
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
/**
 * 偏好管理者
 *
 * 用[Named]的方式编辑Preferences
 */
sealed class PreferencesManager {
    companion object {
        private const val TAG = "PreferencesManager"
    }
    abstract var owner: LifecycleOwner?
    open fun <T:Any?> Named(key: String): Preference<T> {
        throw NotImplementedError()
    }

    /**
     * 依赖私有[PreferencesManager]
     */
    abstract class Private(context: Context, val name: String) : PreferencesManager() {
        private val mapper: PreferencesMapper =  DefaultPreferencesMapper(context.getSharedPreferences(name,
            Context.MODE_PRIVATE
        ))

        override var owner: LifecycleOwner? = null
            set(value) {

                value?.lifecycle?.addObserver(mapper)
                field = value
            }

        final override fun <T> Named(key: String): Preference<T> = PreferenceNullable(key)

        /** [Preference]实现 */
        private inner class PreferenceNullable<out T:Any?> constructor(override val key:String) : Preference<T> {
            override operator fun getValue(thisRef: PreferencesManager, property: KProperty<*>): T {
                return mapper.hashMap[key] as T
            }
            override operator fun setValue(thisRef: PreferencesManager, property: KProperty<*>, value: Any?) {
                mapper.updatePreference(key, value)
            }
        }
    }

    /**
     * 全局Mapper
     *
     * 依赖于[INSTANT],可以多次继承,但INSTANCE只有一个
     */
    abstract class Global : PreferencesManager() {
        final override fun <T> Named(key: String): Preference<T> = PreferenceNullable(key)
        override var owner: LifecycleOwner? = null
            set(value) {

                value?.lifecycle?.addObserver(INSTANT!!)

                field = value
            }

        companion object {
            private var INSTANT: PreferencesMapper? = null

            /**
             * 注册一个全局Mapper,建议在[android.app.Application.onCreate]的时候调用
             * 只能调用一次,否则直接
             * @throws IllegalStateException
             */
            fun Context.registerPreferencesMapper(name:String) {
                if (INSTANT != null) throw IllegalStateException("register twice global mapper !!!")
                INSTANT = DefaultPreferencesMapper(getSharedPreferences(name, Context.MODE_PRIVATE))
            }
            /**
             * [INSTANT] 扩展用法
             */
            fun <T> preference(key: String, default: T?=null) = GlobalPreference(key,default)

        }
        class GlobalPreference <T> (private val key: String, private val default:T?) {
            operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
                return (INSTANT!!.hashMap[key] ?:default) as T
            }
            operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Any?) {
                INSTANT!!.updatePreference(key, value)
            }
        }

        /** [Preference]实现 */
        private class PreferenceNullable<out T:Any?> (override val key:String) : Preference<T> {
            override operator fun getValue(thisRef: PreferencesManager, property: KProperty<*>): T {
                Log.i(TAG, "getValue: ${INSTANT!!.hashMap[key]},$key")
                return INSTANT!!.hashMap[key] as T
            }
            override operator fun setValue(thisRef: PreferencesManager, property: KProperty<*>, value: Any?) {
                INSTANT!!.updatePreference(key, value)
            }
        }
    }

    /**
     * if you wanna put a string to [SharedPreferences] at updateAnyway method, here is example
     * ```
     * fun example():Preference
     * var anyway:String? by example()
     *
     * fun updateAnyway() {
     *     anyway = "Anyway"
     * }
     * ```
     * and the [SharedPreferences] will actually update after you called filed setter
     */
    interface Preference <out T:Any?> {
        val key:String
        operator fun getValue(thisRef: PreferencesManager, property: KProperty<*>): T
        operator fun setValue(thisRef: PreferencesManager, property: KProperty<*>, value: Any?)
    }

    /** [PreferencesMapper]的实现 */
    private class DefaultPreferencesMapper(
        private val sp: SharedPreferences
    ): PreferencesMapper {
        var scope: CoroutineScope = GlobalScope
        /** 使用HashMap存储 */
        private var _hashMap:HashMap<String,Any?>? = HashMap()
        /** 监听改变事件 */
        private val onChange = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, _ ->
            hashMap.putAll(sharedPreferences.all)
        }

        override val hashMap get() = _hashMap!!
        override fun <T> updatePreference(key: String, value: T) { scope.launch(Dispatchers.Default) {
            sp.edit(commit = true) {
                when (value) {
                    is Int -> putInt(key, value)
                    is String -> putString(key, value)
                    is Boolean -> putBoolean(key, value)
                    is Float -> putFloat(key, value)
                    is Long -> putLong(key, value)
                    is MutableSet<*> -> putStringSet(key, value as MutableSet<String>)
                    else -> putString(key, value.toString())
                }
            }
            onChange.onSharedPreferenceChanged(sp,key)
        } }

        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        fun onCreate() {
            Log.i(TAG, "onCreate: called")
            if (_hashMap==null) _hashMap = HashMap()
            hashMap.putAll(sp.all)
            sp.registerOnSharedPreferenceChangeListener(onChange)
        }
        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun onStart() {
            hashMap.putAll(sp.all)
        }
        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        fun onResume() {
            Log.i(TAG, "onResume: ${hashMap.size}")

        }
        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onStop() {
            Log.i(TAG, "onStop: called")
            hashMap.clear()
        }
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            Log.i(TAG, "onDestroy: called")
            sp.unregisterOnSharedPreferenceChangeListener(onChange)
            _hashMap = null
        }
    }
}