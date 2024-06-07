package com.brentvatne.exoplayer

import android.os.Build
import androidx.fragment.app.Fragment
import java.util.UUID

class ReactExoplayerFragment(private val view: ReactExoplayerView) : Fragment() {
    val id = "${ReactExoplayerFragment::class.java.simpleName}_${UUID.randomUUID()}"
    private var mIsOnStopCalled = false

    override fun onStart() {
        super.onStart()
        mIsOnStopCalled = false
    }

    override fun onStop() {
        // On entering Picture-in-Picture mode, onPause is called, but not onStop.
        // For this reason, this is the place where we should pause the video playback.
        super.onStop()
        if (!view.playInBackground) view.setPausedModifier(true)
        mIsOnStopCalled = true

        // Handling when onStop is called while in multi-window mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
            activity?.isInMultiWindowMode == true &&
            activity?.isInPictureInPictureMode != true &&
            !view.playInBackground
        ) {
            view.setPausedModifier(true)
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        view.setIsInPictureInPicture(isInPictureInPictureMode)
        // To pause player when closing PIP window.
        if (!isInPictureInPictureMode && mIsOnStopCalled && !view.playInBackground) {
            view.setPausedModifier(true)
        }
    }
}
