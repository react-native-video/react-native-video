package com.brentvatne.exoplayer;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.activity.OnBackPressedCallback;
import androidx.media3.ui.LegacyPlayerControlView;

import com.brentvatne.common.toolbox.DebugLog;

import java.lang.ref.WeakReference;

@SuppressLint("PrivateResource")
public class FullScreenPlayerView extends Dialog {
    private final LegacyPlayerControlView playerControlView;
    private final ExoPlayerView exoPlayerView;
    private final ReactExoplayerView reactExoplayerView;
    private ViewGroup parent;
    private final FrameLayout containerView;
    private final OnBackPressedCallback onBackPressedCallback;
    private final Handler mKeepScreenOnHandler;
    private final Runnable mKeepScreenOnUpdater;

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

    public FullScreenPlayerView(Context context, ExoPlayerView exoPlayerView, ReactExoplayerView reactExoplayerView, LegacyPlayerControlView playerControlView, OnBackPressedCallback onBackPressedCallback) {
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
        super.onBackPressed();
        onBackPressedCallback.handleOnBackPressed();
    }

    @Override
    protected void onStart() {
        parent = (FrameLayout)(exoPlayerView.getParent());

        parent.removeView(exoPlayerView);
        containerView.addView(exoPlayerView, generateDefaultLayoutParams());

        if (playerControlView != null) {
            ImageButton imageButton = playerControlView.findViewById(com.brentvatne.react.R.id.exo_fullscreen);
            imageButton.setImageResource(androidx.media3.ui.R.drawable.exo_icon_fullscreen_exit);
            imageButton.setContentDescription(getContext().getString(androidx.media3.ui.R.string.exo_controls_fullscreen_exit_description));
            parent.removeView(playerControlView);
            containerView.addView(playerControlView, generateDefaultLayoutParams());
        }

        super.onStart();
    }

    @Override
    protected void onStop() {
        mKeepScreenOnHandler.removeCallbacks(mKeepScreenOnUpdater);
        containerView.removeView(exoPlayerView);
        parent.addView(exoPlayerView, generateDefaultLayoutParams());

        if (playerControlView != null) {
            ImageButton imageButton = playerControlView.findViewById(com.brentvatne.react.R.id.exo_fullscreen);
            imageButton.setImageResource(androidx.media3.ui.R.drawable.exo_icon_fullscreen_enter);
            imageButton.setContentDescription(getContext().getString(androidx.media3.ui.R.string.exo_controls_fullscreen_enter_description));
            containerView.removeView(playerControlView);
            parent.addView(playerControlView, generateDefaultLayoutParams());
        }

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

    public void hideWithoutPlayer() {
        for (int i = 0; i < containerView.getChildCount(); i++) {
            if (containerView.getChildAt(i) != exoPlayerView) {
                containerView.getChildAt(i).setVisibility(View.GONE);
            }
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
}
