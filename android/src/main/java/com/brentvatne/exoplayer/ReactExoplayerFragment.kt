package com.brentvatne.exoplayer

import android.os.Build
import androidx.fragment.app.Fragment

class ReactExoplayerFragment(private val view: ReactExoplayerView) : Fragment() {

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
        if (Build.VERSION.SDK_INT >= 24 && activity?.isInMultiWindowMode == true && activity?.isInPictureInPictureMode != true && !view.playInBackground) {
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
