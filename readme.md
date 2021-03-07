# Kotlinx-android
一些语法糖 包括

```kotlin
class Application:android.app.Application() {
    override fun onCreate {
        ......
        registerGlobalMapper("default_preferences")
    }
}
//simple mapper
class Mapper:PreferenceManager.Global() {
    val isBlackTheme:Boolean by Named("is_black_theme",defualt=false)
} 
class Activity..... {
    val mapper = Mapper()
    override fun onCreate(...) {
        mapper.owmer = this
    }
    // launch main and return unit
    fun anyway = main {
        // infix set value
        aMutableStringStateFlow set "value"
        // some js style
        view.onClick = {
            // dialog
            dialog(
                Btns.Cancel("cancel") {_,_-> 
                },Btns.Postivity("switch") {_,_-> 
                    blackTheme()
                }
                ,title = "black theme",
                content = "babababbababbabaa"
            )
        }
    }
    fun blackTheme() = defualt {
        ........
        mapper.isBlackTheme = true
    }
    
}
```