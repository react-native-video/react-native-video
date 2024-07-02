package com.brentvatne.react

import com.brentvatne.common.toolbox.ReactBridgeUtils
import com.brentvatne.exoplayer.ReactExoplayerView
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.UiThreadUtil
import com.facebook.react.uimanager.UIManagerHelper
import com.facebook.react.uimanager.common.UIManagerType
import kotlin.math.roundToInt

class VideoManagerModule(reactContext: ReactApplicationContext?) : ReactContextBaseJavaModule(reactContext) {
    override fun getName(): String = REACT_CLASS

    private fun performOnPlayerView(reactTag: Int, callback: (ReactExoplayerView?) -> Unit) {
        UiThreadUtil.runOnUiThread {
            try {
                val uiManager = UIManagerHelper.getUIManager(
                    reactApplicationContext,
                    if (BuildConfig.IS_NEW_ARCHITECTURE_ENABLED) UIManagerType.FABRIC else UIManagerType.DEFAULT
                )

                val view = uiManager?.resolveView(reactTag)

                if (view is ReactExoplayerView) {
                    callback(view)
                } else {
                    callback(null)
                }
            } catch (e: Exception) {
                callback(null)
            }
        }
    }

    @ReactMethod
    fun setPlayerPauseState(paused: Boolean?, reactTag: Int) {
        performOnPlayerView(reactTag) {
            it?.setPausedModifier(paused!!)
        }
    }

    @ReactMethod
    fun seek(info: ReadableMap, reactTag: Int) {
        if (!info.hasKey("time")) {
            return
        }

        val time = ReactBridgeUtils.safeGetInt(info, "time")
        performOnPlayerView(reactTag) {
            it?.seekTo((time * 1000f).roundToInt().toLong())
        }
    }

    @ReactMethod
    fun setVolume(volume: Float, reactTag: Int) {
        performOnPlayerView(reactTag) {
            it?.setVolumeModifier(volume)
        }
    }

    @ReactMethod
    fun getCurrentPosition(reactTag: Int, promise: Promise) {
        performOnPlayerView(reactTag) {
            it?.getCurrentPosition(promise)
        }
    }

    @ReactMethod
    fun setFullScreen(fullScreen: Boolean, reactTag: Int) {
        performOnPlayerView(reactTag) {
            it?.handleFullScreenMode(fullScreen)
        }
    }

    companion object {
        private const val REACT_CLASS = "VideoManager"
    }
}
