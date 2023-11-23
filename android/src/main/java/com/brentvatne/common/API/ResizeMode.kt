package com.brentvatne.common.API

import androidx.annotation.IntDef

internal object ResizeMode {
    /**
     * Either the width or height is decreased to obtain the desired aspect ratio.
     */
    const val RESIZE_MODE_FIT = 0

    /**
     * The width is fixed and the height is increased or decreased to obtain the desired aspect ratio.
     */
    const val RESIZE_MODE_FIXED_WIDTH = 1

    /**
     * The height is fixed and the width is increased or decreased to obtain the desired aspect ratio.
     */
    const val RESIZE_MODE_FIXED_HEIGHT = 2

    /**
     * The height and the width is increased or decreased to fit the size of the view.
     */
    const val RESIZE_MODE_FILL = 3

    /**
     * Keeps the aspect ratio but takes up the view's size.
     */
    const val RESIZE_MODE_CENTER_CROP = 4

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(
        RESIZE_MODE_FIT,
        RESIZE_MODE_FIXED_WIDTH,
        RESIZE_MODE_FIXED_HEIGHT,
        RESIZE_MODE_FILL,
        RESIZE_MODE_CENTER_CROP
    )
    annotation class Mode
}
