package com.brentvatne.exoplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.media3.common.AdViewProvider;
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.Player;
import androidx.media3.common.Tracks;
import androidx.media3.common.VideoSize;
import androidx.media3.common.text.Cue;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.media3.ui.SubtitleView;

import com.brentvatne.common.api.SubtitleStyle;
import com.brentvatne.common.api.ViewType;
import com.brentvatne.common.toolbox.DebugLog;
import com.google.common.collect.ImmutableList;

import java.util.List;

public final class ExoPlayerView extends PlayerView implements AdViewProvider {
    private final static String TAG = "ExoPlayerView";
    private View surfaceView;
    private final View shutterView;
    private final ComponentListener componentListener;
    private ExoPlayer player;
    private final Context context;
    private final ViewGroup.LayoutParams layoutParams;
    private final AspectRatioFrameLayout layout;

    private @ViewType.ViewType int viewType = ViewType.VIEW_TYPE_SURFACE;
    private boolean hideShutterView = false;

    public ExoPlayerView(Context context) {
        this(context, null);
    }

    public ExoPlayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExoPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.context = context;

        shutterView = findViewById(androidx.media3.ui.R.id.exo_shutter);

        layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        componentListener = new ComponentListener();
        FrameLayout.LayoutParams aspectRatioParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        aspectRatioParams.gravity = Gravity.CENTER;
        layout = new AspectRatioFrameLayout(context);
        layout.setLayoutParams(aspectRatioParams);

        updateSurfaceView(viewType);
        requestLayout();
    }

    private void clearVideoView() {
        View surfaceView = this.getVideoSurfaceView();
        if (surfaceView instanceof TextureView) {
            player.clearVideoTextureView((TextureView) surfaceView);
        } else if (surfaceView instanceof SurfaceView) {
            player.clearVideoSurfaceView((SurfaceView) surfaceView);
        }
    }

    public boolean isPlaying() {
        return player != null && player.isPlaying();
    }

    public void setSubtitleStyle(SubtitleStyle style) {
        // ensure we reset subtile style before reapplying it
        SubtitleView subtitleLayout = getSubtitleView();
        subtitleLayout.setUserDefaultStyle();
        subtitleLayout.setUserDefaultTextSize();

        if (style.getFontSize() > 0) {
            subtitleLayout.setFixedTextSize(TypedValue.COMPLEX_UNIT_SP, style.getFontSize());
        }
        subtitleLayout.setPadding(style.getPaddingLeft(), style.getPaddingTop(), style.getPaddingRight(), style.getPaddingBottom());
        if (style.getOpacity() != 0) {
            subtitleLayout.setAlpha(style.getOpacity());
            subtitleLayout.setVisibility(View.VISIBLE);
        } else {
            subtitleLayout.setVisibility(View.GONE);
        }

    }

    public void updateSurfaceView(@ViewType.ViewType int viewType) {
        this.viewType = viewType;
        boolean viewNeedRefresh = false;
        if (viewType == ViewType.VIEW_TYPE_SURFACE || viewType == ViewType.VIEW_TYPE_SURFACE_SECURE) {
            if (!(surfaceView instanceof SurfaceView)) {
                surfaceView = new SurfaceView(context);
                viewNeedRefresh = true;
            }
            ((SurfaceView) surfaceView).setSecure(viewType == ViewType.VIEW_TYPE_SURFACE_SECURE);
        } else if (viewType == ViewType.VIEW_TYPE_TEXTURE) {
            if (!(surfaceView instanceof TextureView)) {
                surfaceView = new TextureView(context);
                viewNeedRefresh = true;
                // Support opacity properly:
                ((TextureView) surfaceView).setOpaque(false);
            } else {
                DebugLog.wtf(TAG, "wtf is this texture " + viewType);
            }
        }
            if (viewNeedRefresh) {
                surfaceView.setLayoutParams(layoutParams);

                if (layout.getChildAt(0) != null) {
                    layout.removeViewAt(0);
                }
                addView(surfaceView, 0, layoutParams);
            }
        }

    private void updateShutterViewVisibility() {
        if(shutterView != null) {
            shutterView.setVisibility(this.hideShutterView ? INVISIBLE : VISIBLE);
        }
    }

    public void closeShutterView() {
        if(shutterView != null) {
            shutterView.setVisibility(INVISIBLE);
        }
    }

    @Override
    public void requestLayout() {
        super.requestLayout();
        post(measureAndLayout);
    }

    // AdsLoader.AdViewProvider implementation.

    /**
     * Set the {@link ExoPlayer} to use. The {@link ExoPlayer#addListener} method of the
     * player will be called and previous
     * assignments are overridden.
     *
     * @param player The {@link ExoPlayer} to use.
     */
    public void setPlayer(ExoPlayer player) {
        if (this.player == player) {
            return;
        }
        if (this.player != null) {
            this.player.removeListener(componentListener);
            clearVideoView();
        }
        this.player = player;
        ((PlayerView)this).setPlayer(player);
        setHideShutterView(this.hideShutterView);
        if (player != null) {
            player.addListener(componentListener);
        }
        requestLayout();
    }

    public void setHideShutterView(boolean hideShutterView) {
        this.hideShutterView = hideShutterView;
        updateShutterViewVisibility();
    }

    private final Runnable measureAndLayout = () -> {
        measure(
                MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.EXACTLY));
        layout(getLeft(), getTop(), getRight(), getBottom());
    };

    private void updateForCurrentTrackSelections(Tracks tracks) {
        if (tracks == null) {
            return;
        }
        ImmutableList<Tracks.Group> groups = tracks.getGroups();
        for (Tracks.Group group: groups) {
            if (group.getType() == C.TRACK_TYPE_VIDEO && group.length > 0) {
                // get the first track of the group to identify aspect ratio
                Format format = group.getTrackFormat(0);

                // update aspect ratio !
                // layout.setAspectRatio(format.height == 0 ? 1 : (format.width * format.pixelWidthHeightRatio) / format.height);
                return;
            }
        }
        // no video tracks, in that case refresh shutterView visibility
        setHideShutterView(hideShutterView);
    }

    public void invalidateAspectRatio() {
        // Resetting aspect ratio will force layout refresh on next video size changed
        //layout.invalidateAspectRatio();
    }

    private final class ComponentListener implements Player.Listener {

        @Override
        public void onCues(@NonNull List<Cue> cues) {
            SubtitleView subtitleView = getSubtitleView();
            if (subtitleView != null) {
                subtitleView.setCues(cues);
            } else {
                DebugLog.w("SubtitleHandler", "Subtitle view is null, cannot set cues");
            }
        }

        @Override
        public void onVideoSizeChanged(VideoSize videoSize) {
            /*
            boolean isInitialRatio = layout.getAspectRatio() == 0;
            if (videoSize.height == 0 || videoSize.width == 0) {
                // When changing video track we receive an ghost state with height / width = 0
                // No need to resize the view in that case
                return;
            }
            layout.setAspectRatio((videoSize.width * videoSize.pixelWidthHeightRatio) / videoSize.height);

            // React native workaround for measuring and layout on initial load.
            if (isInitialRatio) {
                post(measureAndLayout);
            }*/
        }

        @Override
        public void onRenderedFirstFrame() {
            setHideShutterView(hideShutterView);
        }

        @Override
        public void onTracksChanged(@NonNull Tracks tracks) {
            updateForCurrentTrackSelections(tracks);
        }
    }
}
