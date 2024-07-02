package com.brentvatne.react

import android.media.MediaCodecList
import android.media.MediaDrm
import android.media.MediaFormat
import android.media.UnsupportedSchemeException
import android.os.Build
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import java.util.UUID

class VideoDecoderPropertiesModule(reactContext: ReactApplicationContext?) : ReactContextBaseJavaModule(reactContext) {
    override fun getName(): String = REACT_CLASS

    @ReactMethod
    fun getWidevineLevel(p: Promise) {
        var widevineLevel = 0
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            p.resolve(widevineLevel)
            return
        }
        try {
            val mediaDrm = MediaDrm(WIDEVINE_UUID)
            val securityProperty = mediaDrm.getPropertyString(SECURITY_LEVEL_PROPERTY)
            widevineLevel = when (securityProperty) {
                "L1" -> 1
                "L2" -> 2
                "L3" -> 3
                else -> 0
            }
        } catch (e: UnsupportedSchemeException) {
            e.printStackTrace()
        }
        p.resolve(widevineLevel)
    }

    @ReactMethod
    fun isCodecSupported(mimeType: String?, width: Int, height: Int, p: Promise) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            p.resolve("unsupported")
            return
        }
        val mRegularCodecs = MediaCodecList(MediaCodecList.REGULAR_CODECS)
        val format = MediaFormat.createVideoFormat(mimeType!!, width, height)
        val codecName = mRegularCodecs.findDecoderForFormat(format)
        if (codecName == null) {
            p.resolve("unsupported")
            return
        }

        // Fallback for android < 10
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            p.resolve("software")
            return
        }
        val isHardwareAccelerated = mRegularCodecs.codecInfos.any {
            it.name.equals(codecName, ignoreCase = true) && it.isHardwareAccelerated
        }
        p.resolve(if (isHardwareAccelerated) "software" else "hardware")
    }

    @ReactMethod
    fun isHEVCSupported(p: Promise) = isCodecSupported("video/hevc", 1920, 1080, p)

    companion object {
        private val WIDEVINE_UUID = UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L)
        private const val SECURITY_LEVEL_PROPERTY = "securityLevel"
        private const val REACT_CLASS = "RNVDecoderPropertiesModule"
    }
}
