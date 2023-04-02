package top.riverelder.android.aimpfix

import android.R.attr.classLoader
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers


const val AIMP_PACKAGE_NAME = "com.aimp.player"

class AIMPFix : IXposedHookLoadPackage {

    @Throws(Throwable::class)
    override fun handleLoadPackage(lpparam: LoadPackageParam?) {
        if (lpparam == null) return

        val packageName: String = lpparam.packageName
        val classLoader: ClassLoader = lpparam.classLoader

        if (packageName != AIMP_PACKAGE_NAME) return

        XposedBridge.log("Start hooking $AIMP_PACKAGE_NAME")

        XposedHelpers.findAndHookMethod("com.aimp.player.core.meta.TrackInfoProvider",
            classLoader,
            "load",
            "com.aimp.player.core.player.AudioStream",
            "com.aimp.player.core.meta.TrackInfo",
            Int::class.javaPrimitiveType,
            object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun afterHookedMethod(param: MethodHookParam) {
                    val trackInfo = param.args[1]
                    val fileNameWithoutExtension: String = XposedHelpers.callMethod(XposedHelpers.getObjectField(trackInfo, "fileName"), "getDisplayNameWOExt") as String? ?: return
                    val parts = fileNameWithoutExtension.split('-', ignoreCase = true, limit = 2)
                    var title: String = fileNameWithoutExtension
                    var artist: String = ""
                    if (parts.size == 2) {
                        title = parts[1].trim()
                        artist = parts[0].trim()
                    } else if (parts.size == 1) {
                        title = parts[0].trim()
                        artist = ""
                    }
                    XposedHelpers.setObjectField(trackInfo, "title", title)
                    XposedHelpers.setObjectField(trackInfo, "artist", artist)
                }
            })

    }
}
