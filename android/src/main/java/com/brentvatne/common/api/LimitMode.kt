package com.brentvatne.common.api

import androidx.annotation.StringDef

internal object LimitMode {
    /**
     * Don't limit video resolution
     */
    const val DISABLED = "disabled"

    /**
     * Limit video resolution to screen resolution
     */
    const val SCREEN_SIZE = "screen"

    /**
     * Limit video resolution to video surface size
     */
    const val VIDEO_SURFACE = "videoSurface"

    @JvmStatic
    @Mode
    fun toLimitMode(ordinal: String): String =
        when (ordinal) {
            SCREEN_SIZE -> SCREEN_SIZE
            VIDEO_SURFACE -> VIDEO_SURFACE
            else -> DISABLED
        }

    @Retention(AnnotationRetention.SOURCE)
    @StringDef(
        SCREEN_SIZE,
        VIDEO_SURFACE,
        DISABLED
    )
    annotation class Mode
}
