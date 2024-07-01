package com.brentvatne.exoplayer;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import androidx.media3.exoplayer.ExoPlayer;

public class ExoPlayerPhoneStateListener extends PhoneStateListener {
    private final ExoPlayer exoPlayer;

    public ExoPlayerPhoneStateListener(ExoPlayer exoPlayer) {
        this.exoPlayer = exoPlayer;
    }

    @Override
    public void onCallStateChanged(int state, String phoneNumber) {
        super.onCallStateChanged(state, phoneNumber);
        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                if (exoPlayer != null && !exoPlayer.isPlaying()) {
                    exoPlayer.play();
                }
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
            case TelephonyManager.CALL_STATE_RINGING:
                if (exoPlayer != null && exoPlayer.isPlaying()) {
                    exoPlayer.pause();
                }
                break;
        }
    }
}
