package com.brentvatne.exoplayer;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.media3.ui.PlayerControlView;
import androidx.media3.ui.PlayerView;

import com.brentvatne.common.toolbox.DebugLog;

import java.lang.ref.WeakReference;

public class FullScreenPlayerView extends Dialog {
    private final PlayerControlView playerControlView;
    private final ExoPlayerView exoPlayerView;
    private final ReactExoplayerView reactExoplayerView;
    private ViewGroup parent;
    private final FrameLayout containerView;
    private final Handler mKeepScreenOnHandler;
    private final Runnable mKeepScreenOnUpdater;
    private final OnBackPressedCallback onBackPressedCallback;
    private static class KeepScreenOnUpdater implements Runnable {
        private final static long UPDATE_KEEP_SCREEN_ON_FLAG_MS = 200;
        private final WeakReference<FullScreenPlayerView> mFullscreenPlayer;

        KeepScreenOnUpdater(FullScreenPlayerView player) {
            mFullscreenPlayer = new WeakReference<>(player);
        }

        @Override
        public void run() {
            try {
                FullScreenPlayerView fullscreenVideoPlayer = mFullscreenPlayer.get();
                if (fullscreenVideoPlayer != null) {
                    final Window window = fullscreenVideoPlayer.getWindow();
                    if (window != null) {
                        boolean isPlaying = fullscreenVideoPlayer.exoPlayerView.isPlaying();
                        if (isPlaying) {
                            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        } else {
                            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        }
                    }
                    fullscreenVideoPlayer.mKeepScreenOnHandler.postDelayed(this, UPDATE_KEEP_SCREEN_ON_FLAG_MS);
                }
            } catch (Exception ex) {
                DebugLog.e("ExoPlayer Exception", "Failed to flag FLAG_KEEP_SCREEN_ON on fullscreeen.");
                DebugLog.e("ExoPlayer Exception", ex.toString());
            }
        }
    }

    public FullScreenPlayerView(Context context, ExoPlayerView exoPlayerView, ReactExoplayerView reactExoplayerView, PlayerControlView playerControlView, OnBackPressedCallback onBackPressedCallback) {
        super(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        this.playerControlView = playerControlView;
        this.exoPlayerView = exoPlayerView;
        this.reactExoplayerView = reactExoplayerView;
        this.onBackPressedCallback = onBackPressedCallback;
        containerView = new FrameLayout(context);
        setContentView(containerView, generateDefaultLayoutParams());
        mKeepScreenOnUpdater = new KeepScreenOnUpdater(this);
        mKeepScreenOnHandler = new Handler();
    }

    @Override
    public void onBackPressed() {
        ImageView exoFullScreen = findViewById(androidx.media3.ui.R.id.exo_fullscreen);
        if(exoFullScreen != null){
            exoFullScreen.performClick();
        }
        onBackPressedCallback.handleOnBackPressed();
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        parent = (FrameLayout)(exoPlayerView.getParent());
        parent.removeView(exoPlayerView);
        containerView.addView(exoPlayerView, generateDefaultLayoutParams());
        setupFullscreenButtonListener();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mKeepScreenOnHandler.removeCallbacks(mKeepScreenOnUpdater);
        containerView.removeView(exoPlayerView);
        parent.addView(exoPlayerView, generateDefaultLayoutParams());
        parent.requestLayout();
        parent = null;
        super.onStop();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (reactExoplayerView.getPreventsDisplaySleepDuringVideoPlayback()) {
            mKeepScreenOnHandler.post(mKeepScreenOnUpdater);
        }
    }

    private FrameLayout.LayoutParams generateDefaultLayoutParams() {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        layoutParams.setMargins(0, 0, 0, 0);
        return layoutParams;
    }

    private void setupFullscreenButtonListener() {
        if (playerControlView != null) {
            exoPlayerView.setFullscreenButtonClickListener(new PlayerView.FullscreenButtonClickListener() {
                @Override
                public void onFullscreenButtonClick(boolean isFullScreen) {
                    reactExoplayerView.setFullscreen(isFullScreen);
                }
            });
        }
    }
}
