package com.brentvatne.exoplayer;

import static androidx.media3.common.C.CONTENT_TYPE_DASH;
import static androidx.media3.common.C.CONTENT_TYPE_HLS;
import static androidx.media3.common.C.CONTENT_TYPE_OTHER;
import static androidx.media3.common.C.CONTENT_TYPE_RTSP;
import static androidx.media3.common.C.CONTENT_TYPE_SS;
import static androidx.media3.common.C.TIME_END_OF_SOURCE;

import static com.brentvatne.exoplayer.DataSourceUtil.buildAssetDataSourceFactory;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.accessibility.CaptioningManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Metadata;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.common.StreamKey;
import androidx.media3.common.Timeline;
import androidx.media3.common.TrackGroup;
import androidx.media3.common.TrackSelectionOverride;
import androidx.media3.common.Tracks;
import androidx.media3.common.text.CueGroup;
import androidx.media3.common.util.Util;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DataSpec;
import androidx.media3.datasource.HttpDataSource;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.dash.DashMediaSource;
import androidx.media3.exoplayer.dash.DashUtil;
import androidx.media3.exoplayer.dash.DefaultDashChunkSource;
import androidx.media3.exoplayer.dash.manifest.AdaptationSet;
import androidx.media3.exoplayer.dash.manifest.DashManifest;
import androidx.media3.exoplayer.dash.manifest.Period;
import androidx.media3.exoplayer.dash.manifest.Representation;
import androidx.media3.exoplayer.drm.DefaultDrmSessionManager;
import androidx.media3.exoplayer.drm.DefaultDrmSessionManagerProvider;
import androidx.media3.exoplayer.drm.DrmSessionEventListener;
import androidx.media3.exoplayer.drm.DrmSessionManager;
import androidx.media3.exoplayer.drm.DrmSessionManagerProvider;
import androidx.media3.exoplayer.drm.FrameworkMediaDrm;
import androidx.media3.exoplayer.drm.HttpMediaDrmCallback;
import androidx.media3.exoplayer.drm.UnsupportedDrmException;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.ima.ImaAdsLoader;
import androidx.media3.exoplayer.mediacodec.MediaCodecInfo;
import androidx.media3.exoplayer.mediacodec.MediaCodecUtil;
import androidx.media3.exoplayer.rtsp.RtspMediaSource;
import androidx.media3.exoplayer.smoothstreaming.DefaultSsChunkSource;
import androidx.media3.exoplayer.smoothstreaming.SsMediaSource;
import androidx.media3.exoplayer.source.ClippingMediaSource;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.MergingMediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.exoplayer.source.SingleSampleMediaSource;
import androidx.media3.exoplayer.source.TrackGroupArray;
import androidx.media3.exoplayer.source.ads.AdsMediaSource;
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.exoplayer.trackselection.ExoTrackSelection;
import androidx.media3.exoplayer.trackselection.MappingTrackSelector;
import androidx.media3.exoplayer.trackselection.TrackSelection;
import androidx.media3.exoplayer.trackselection.TrackSelectionArray;
import androidx.media3.exoplayer.upstream.BandwidthMeter;
import androidx.media3.exoplayer.upstream.DefaultAllocator;
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter;
import androidx.media3.exoplayer.util.EventLogger;
import androidx.media3.extractor.metadata.emsg.EventMessage;
import androidx.media3.extractor.metadata.id3.Id3Frame;
import androidx.media3.extractor.metadata.id3.TextInformationFrame;
import androidx.media3.session.MediaSessionService;
import androidx.media3.ui.DefaultTimeBar;
import androidx.media3.ui.LegacyPlayerControlView;

import com.brentvatne.common.api.BufferConfig;
import com.brentvatne.common.api.BufferingStrategy;
import com.brentvatne.common.api.ControlsConfig;
import com.brentvatne.common.api.DRMProps;
import com.brentvatne.common.api.ResizeMode;
import com.brentvatne.common.api.SideLoadedTextTrack;
import com.brentvatne.common.api.SideLoadedTextTrackList;
import com.brentvatne.common.api.Source;
import com.brentvatne.common.api.SubtitleStyle;
import com.brentvatne.common.api.TimedMetadata;
import com.brentvatne.common.api.Track;
import com.brentvatne.common.api.VideoTrack;
import com.brentvatne.common.react.VideoEventEmitter;
import com.brentvatne.common.toolbox.DebugLog;
import com.brentvatne.react.BuildConfig;
import com.brentvatne.react.R;
import com.brentvatne.react.ReactNativeVideoManager;
import com.brentvatne.receiver.AudioBecomingNoisyReceiver;
import com.brentvatne.receiver.BecomingNoisyListener;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.uimanager.ThemedReactContext;
import com.google.ads.interactivemedia.v3.api.AdError;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent;
import com.google.common.collect.ImmutableList;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.lang.Math;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@SuppressLint("ViewConstructor")
public class ReactExoplayerView extends FrameLayout implements
        LifecycleEventListener,
        Player.Listener,
        BandwidthMeter.EventListener,
        BecomingNoisyListener,
        DrmSessionEventListener,
        AdEvent.AdEventListener,
        AdErrorEvent.AdErrorListener {

    public static final double DEFAULT_MAX_HEAP_ALLOCATION_PERCENT = 1;
    public static final double DEFAULT_MIN_BUFFER_MEMORY_RESERVE = 0;

    private static final String TAG = "ReactExoplayerView";

    private static final CookieManager DEFAULT_COOKIE_MANAGER;
    private static final int SHOW_PROGRESS = 1;

    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private final VideoEventEmitter eventEmitter;
    private final ReactExoplayerConfig config;
    private final DefaultBandwidthMeter bandwidthMeter;
    private LegacyPlayerControlView playerControlView;
    private View playPauseControlContainer;
    private Player.Listener eventListener;

    private ExoPlayerView exoPlayerView;
    private FullScreenPlayerView fullScreenPlayerView;
    private ImaAdsLoader adsLoader;

    private DataSource.Factory mediaDataSourceFactory;
    private ExoPlayer player;
    private DefaultTrackSelector trackSelector;
    private boolean playerNeedsSource;
    private MediaMetadata customMetadata;

    private ServiceConnection playbackServiceConnection;
    private PlaybackServiceBinder playbackServiceBinder;

    // logger to be enable by props
    private EventLogger debugEventLogger = null;
    private boolean enableDebug = false;
    private static final String TAG_EVENT_LOGGER = "RNVExoplayer";

    private int resumeWindow;
    private long resumePosition;
    private boolean loadVideoStarted;
    private boolean isFullscreen;
    private boolean isInBackground;
    private boolean isPaused;
    private boolean isBuffering;
    private boolean muted = false;
    private boolean hasAudioFocus = false;
    private float rate = 1f;
    private AudioOutput audioOutput = AudioOutput.SPEAKER;
    private float audioVolume = 1f;
    private int minLoadRetryCount = 3;
    private BufferConfig bufferConfig = new BufferConfig();
    private int maxBitRate = 0;
    private boolean hasDrmFailed = false;
    private boolean isUsingContentResolution = false;
    private boolean selectTrackWhenReady = false;
    private final Handler mainHandler;
    private Runnable mainRunnable;
    private boolean useCache = false;
    private ControlsConfig controlsConfig = new ControlsConfig();

    // Props from React
    private Source source = new Source();
    private boolean repeat;
    private String audioTrackType;
    private String audioTrackValue;
    private String videoTrackType;
    private String videoTrackValue;
    private String textTrackType;
    private String textTrackValue;
    private SideLoadedTextTrackList textTracks;
    private boolean disableFocus;
    private boolean focusable = true;
    private BufferingStrategy.BufferingStrategyEnum bufferingStrategy;
    private long contentStartTime = -1L;
    private boolean disableDisconnectError;
    private boolean preventsDisplaySleepDuringVideoPlayback = true;
    private float mProgressUpdateInterval = 250.0f;
    private boolean playInBackground = false;
    private boolean mReportBandwidth = false;
    private boolean controls;
    private Uri adTagUrl;

    private boolean showNotificationControls = false;
    // \ End props

    // React
    private final ThemedReactContext themedReactContext;
    private final AudioManager audioManager;
    private final AudioBecomingNoisyReceiver audioBecomingNoisyReceiver;
    private final AudioManager.OnAudioFocusChangeListener audioFocusChangeListener;

    // store last progress event values to avoid sending unnecessary messages
    private long lastPos = -1;
    private long lastBufferDuration = -1;
    private long lastDuration = -1;

    private boolean viewHasDropped = false;

    private String instanceId = String.valueOf(UUID.randomUUID());

    private void updateProgress() {
        if (player != null) {
            if (playerControlView != null && isPlayingAd() && controls) {
                playerControlView.hide();
            }
            long bufferedDuration = player.getBufferedPercentage() * player.getDuration() / 100;
            long duration = player.getDuration();
            long pos = player.getCurrentPosition();
            if (pos > duration) {
                pos = duration;
            }

            if (lastPos != pos
                    || lastBufferDuration != bufferedDuration
                    || lastDuration != duration) {
                lastPos = pos;
                lastBufferDuration = bufferedDuration;
                lastDuration = duration;
                eventEmitter.progressChanged(pos, bufferedDuration, player.getDuration(), getPositionInFirstPeriodMsForCurrentWindow(pos));
            }
        }
    }

    private final Handler progressHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == SHOW_PROGRESS) {
                updateProgress();
                msg = obtainMessage(SHOW_PROGRESS);
                sendMessageDelayed(msg, Math.round(mProgressUpdateInterval));
            }
        }
    };

    public double getPositionInFirstPeriodMsForCurrentWindow(long currentPosition) {
        Timeline.Window window = new Timeline.Window();
        if(!player.getCurrentTimeline().isEmpty()) {
            player.getCurrentTimeline().getWindow(player.getCurrentMediaItemIndex(), window);
        }
        return window.windowStartTimeMs + currentPosition;
    }

    public ReactExoplayerView(ThemedReactContext context, ReactExoplayerConfig config) {
        super(context);
        this.themedReactContext = context;
        this.eventEmitter = new VideoEventEmitter(context);
        this.config = config;
        this.bandwidthMeter = config.getBandwidthMeter();
        mainHandler = new Handler();
        createViews();

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        themedReactContext.addLifecycleEventListener(this);
        audioBecomingNoisyReceiver = new AudioBecomingNoisyReceiver(themedReactContext);
        audioFocusChangeListener = new OnAudioFocusChangedListener(this, themedReactContext);
    }

    private boolean isPlayingAd() {
        return player != null && player.isPlayingAd();
    }

    @Override
    public void setId(int id) {
        super.setId(id);
        eventEmitter.setViewId(id);
    }

    private void createViews() {
        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }

        LayoutParams layoutParams = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        exoPlayerView = new ExoPlayerView(getContext());
        exoPlayerView.setLayoutParams(layoutParams);
        addView(exoPlayerView, 0, layoutParams);

        exoPlayerView.setFocusable(this.focusable);
    }

    // LifecycleEventListener implementation
    @Override
    public void onHostResume() {
        if (!playInBackground || !isInBackground) {
            setPlayWhenReady(!isPaused);
        }
        isInBackground = false;
    }

    @Override
    public void onHostPause() {
        isInBackground = true;
        if (playInBackground) {
            return;
        }
        setPlayWhenReady(false);
    }

    @Override
    public void onHostDestroy() {
        cleanUpResources();
    }

    @Override
    protected void onDetachedFromWindow() {
        cleanupPlaybackService();
        super.onDetachedFromWindow();
    }

    public void cleanUpResources() {
        stopPlayback();
        themedReactContext.removeLifecycleEventListener(this);
        releasePlayer();
        viewHasDropped = true;
    }

    //BandwidthMeter.EventListener implementation
    @Override
    public void onBandwidthSample(int elapsedMs, long bytes, long bitrate) {
        if (mReportBandwidth) {
            if (player == null) {
                eventEmitter.bandwidthReport(bitrate, 0, 0, "-1");
            } else {
                Format videoFormat = player.getVideoFormat();
                int width = videoFormat != null ? videoFormat.width : 0;
                int height = videoFormat != null ? videoFormat.height : 0;
                String trackId = videoFormat != null ? videoFormat.id : "-1";
                eventEmitter.bandwidthReport(bitrate, height, width, trackId);
            }
        }
    }

    // Internal methods

    /**
     * Toggling the visibility of the player control view
     */
    private void togglePlayerControlVisibility() {
        if (player == null) return;
        reLayoutControls();
        if (playerControlView.isVisible()) {
            playerControlView.hide();
        } else {
            playerControlView.show();
        }
    }

    /**
     * Initializing Player control
     */
    private void initializePlayerControl() {
        if (playerControlView == null) {
            playerControlView = new LegacyPlayerControlView(getContext());
            playerControlView.addVisibilityListener(new LegacyPlayerControlView.VisibilityListener() {
                @Override
                public void onVisibilityChange(int visibility) {
                    eventEmitter.controlsVisibilityChanged(visibility == View.VISIBLE);
                }
            });
        }

        if (fullScreenPlayerView == null) {
            fullScreenPlayerView = new FullScreenPlayerView(getContext(), exoPlayerView, this, playerControlView, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    setFullscreen(false);
                }
            });
        }

        // Setting the player for the playerControlView
        playerControlView.setPlayer(player);
        playPauseControlContainer = playerControlView.findViewById(R.id.exo_play_pause_container);

        // Invoking onClick event for exoplayerView
        exoPlayerView.setOnClickListener((View v) -> {
            if (!isPlayingAd()) {
                togglePlayerControlVisibility();
            }
        });

        //Handling the playButton click event
        ImageButton playButton = playerControlView.findViewById(R.id.exo_play);
        playButton.setOnClickListener((View v) -> {
            if (player != null && player.getPlaybackState() == Player.STATE_ENDED) {
                player.seekTo(0);
            }
            setPausedModifier(false);
        });

        //Handling the rewind and forward button click events
        ImageButton exoRewind = playerControlView.findViewById(R.id.exo_rew);
        ImageButton exoForward = playerControlView.findViewById(R.id.exo_ffwd);
        exoRewind.setOnClickListener((View v) -> {
            seekTo(player.getCurrentPosition() - controlsConfig.getSeekIncrementMS());
        });

        exoForward.setOnClickListener((View v) -> {
            seekTo(player.getCurrentPosition() + controlsConfig.getSeekIncrementMS());
        });

        //Handling the pauseButton click event
        ImageButton pauseButton = playerControlView.findViewById(R.id.exo_pause);
        pauseButton.setOnClickListener((View v) ->
            setPausedModifier(true)
        );

        //Handling the fullScreenButton click event
        final ImageButton fullScreenButton = playerControlView.findViewById(R.id.exo_fullscreen);
        fullScreenButton.setOnClickListener(v -> setFullscreen(!isFullscreen));
        updateFullScreenButtonVisibility();
        refreshProgressBarVisibility();

        // Invoking onPlaybackStateChanged and onPlayWhenReadyChanged events for Player
        eventListener = new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                View playButton = playerControlView.findViewById(R.id.exo_play);
                View pauseButton = playerControlView.findViewById(R.id.exo_pause);
                if (playButton != null && playButton.getVisibility() == GONE) {
                    playButton.setVisibility(INVISIBLE);
                }
                if (pauseButton != null && pauseButton.getVisibility() == GONE) {
                    pauseButton.setVisibility(INVISIBLE);
                }
                reLayout(playPauseControlContainer);
                //Remove this eventListener once its executed. since UI will work fine once after the reLayout is done
                player.removeListener(eventListener);
            }

            @Override
            public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
                reLayout(playPauseControlContainer);
                //Remove this eventListener once its executed. since UI will work fine once after the reLayout is done
                player.removeListener(eventListener);
            }
        };
        player.addListener(eventListener);
    }

    /**
     * Adding Player control to the frame layout
     */
    private void addPlayerControl() {
        if (playerControlView == null) return;
        LayoutParams layoutParams = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        playerControlView.setLayoutParams(layoutParams);
        int indexOfPC = indexOfChild(playerControlView);
        if (indexOfPC != -1) {
            removeViewAt(indexOfPC);
        }
        addView(playerControlView, 1, layoutParams);
        reLayout(playerControlView);
    }

    /**
     * Update the layout
     * @param view  view needs to update layout
     *
     * This is a workaround for the open bug in react-native: <a href="https://github.com/facebook/react-native/issues/17968">...</a>
     */
    private void reLayout(View view) {
        if (view == null) return;
        view.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY));
        view.layout(view.getLeft(), view.getTop(), view.getMeasuredWidth(), view.getMeasuredHeight());
    }

    private void refreshProgressBarVisibility (){
        if(playerControlView == null) return;
        DefaultTimeBar exoProgress;
        TextView exoDuration;
        TextView exoPosition;
        exoProgress = playerControlView.findViewById(R.id.exo_progress);
        exoDuration = playerControlView.findViewById(R.id.exo_duration);
        exoPosition = playerControlView.findViewById(R.id.exo_position);
        if(controlsConfig.getHideSeekBar()){
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT,
                    1.0f
            );
            exoProgress.setVisibility(GONE);
            exoDuration.setVisibility(GONE);
            exoPosition.setLayoutParams(param);
        }else{
            exoProgress.setVisibility(VISIBLE);
            exoDuration.setVisibility(VISIBLE);
            // Reset the layout parameters of exoPosition to their default state
            LinearLayout.LayoutParams defaultParam = new LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
            );
            exoPosition.setLayoutParams(defaultParam);
        }
    }

    private void reLayoutControls() {
        reLayout(exoPlayerView);
        reLayout(playerControlView);
    }

    public void setDebug(boolean enableDebug) {
        this.enableDebug = enableDebug;
        refreshDebugState();
    }

    private void refreshDebugState() {
        if (player == null) {
            return;
        }
        if (enableDebug) {
            debugEventLogger = new EventLogger(TAG_EVENT_LOGGER);
            player.addAnalyticsListener(debugEventLogger);
        } else if (debugEventLogger != null) {
            player.removeAnalyticsListener(debugEventLogger);
            debugEventLogger = null;
        }
    }

    public void setViewType(int viewType) {
        exoPlayerView.updateSurfaceView(viewType);
    }

    private class RNVLoadControl extends DefaultLoadControl {
        private final int availableHeapInBytes;
        private final Runtime runtime;
        public RNVLoadControl(DefaultAllocator allocator, BufferConfig config) {
            super(allocator,
                    config.getMinBufferMs() != BufferConfig.Companion.getBufferConfigPropUnsetInt()
                            ? config.getMinBufferMs()
                            : DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
                    config.getMaxBufferMs() != BufferConfig.Companion.getBufferConfigPropUnsetInt()
                            ? config.getMaxBufferMs()
                            : DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
                    config.getBufferForPlaybackMs() != BufferConfig.Companion.getBufferConfigPropUnsetInt()
                            ? config.getBufferForPlaybackMs()
                            : DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS ,
                    config.getBufferForPlaybackAfterRebufferMs() != BufferConfig.Companion.getBufferConfigPropUnsetInt()
                            ? config.getBufferForPlaybackAfterRebufferMs()
                            : DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS,
                    -1,
                    true,
                    config.getBackBufferDurationMs() != BufferConfig.Companion.getBufferConfigPropUnsetInt()
                            ? config.getBackBufferDurationMs()
                            : DefaultLoadControl.DEFAULT_BACK_BUFFER_DURATION_MS,
                    DefaultLoadControl.DEFAULT_RETAIN_BACK_BUFFER_FROM_KEYFRAME);
            runtime = Runtime.getRuntime();
            ActivityManager activityManager = (ActivityManager) themedReactContext.getSystemService(ThemedReactContext.ACTIVITY_SERVICE);
            double maxHeap = config.getMaxHeapAllocationPercent() != BufferConfig.Companion.getBufferConfigPropUnsetDouble()
                    ? bufferConfig.getMaxHeapAllocationPercent()
                    : DEFAULT_MAX_HEAP_ALLOCATION_PERCENT;
            availableHeapInBytes = (int) Math.floor(activityManager.getMemoryClass() * maxHeap * 1024 * 1024);
        }

        @Override
        public boolean shouldContinueLoading(long playbackPositionUs, long bufferedDurationUs, float playbackSpeed) {
            if (bufferingStrategy == BufferingStrategy.BufferingStrategyEnum.DisableBuffering) {
                return false;
            } else if (bufferingStrategy == BufferingStrategy.BufferingStrategyEnum.DependingOnMemory) {
                // The goal of this algorithm is to pause video loading (increasing the buffer)
                // when available memory on device become low.
                int loadedBytes = getAllocator().getTotalBytesAllocated();
                boolean isHeapReached = availableHeapInBytes > 0 && loadedBytes >= availableHeapInBytes;
                if (isHeapReached) {
                    return false;
                }
                long usedMemory = runtime.totalMemory() - runtime.freeMemory();
                long freeMemory = runtime.maxMemory() - usedMemory;
                double minBufferMemoryReservePercent = bufferConfig.getMinBufferMemoryReservePercent() != BufferConfig.Companion.getBufferConfigPropUnsetDouble()
                        ? bufferConfig.getMinBufferMemoryReservePercent()
                        : ReactExoplayerView.DEFAULT_MIN_BUFFER_MEMORY_RESERVE;
                long reserveMemory = (long) minBufferMemoryReservePercent * runtime.maxMemory();
                long bufferedMs = bufferedDurationUs / (long) 1000;
                if (reserveMemory > freeMemory && bufferedMs > 2000) {
                    // We don't have enough memory in reserve so we stop buffering to allow other components to use it instead
                    return false;
                }
                if (runtime.freeMemory() == 0) {
                    DebugLog.w(TAG, "Free memory reached 0, forcing garbage collection");
                    runtime.gc();
                    return false;
                }
            }
            // "default" case or normal case for "DependingOnMemory"
            return super.shouldContinueLoading(playbackPositionUs, bufferedDurationUs, playbackSpeed);
        }
    }

    private void initializePlayer() {
        ReactExoplayerView self = this;
        Activity activity = themedReactContext.getCurrentActivity();
        // This ensures all props have been settled, to avoid async racing conditions.
        mainRunnable = () -> {
            if (viewHasDropped) {
                return;
            }
            try {
                if (player == null) {
                    // Initialize core configuration and listeners
                    initializePlayerCore(self);
                }
                if (playerNeedsSource && source.getUri() != null) {
                    exoPlayerView.invalidateAspectRatio();
                    // DRM session manager creation must be done on a different thread to prevent crashes so we start a new thread
                    ExecutorService es = Executors.newSingleThreadExecutor();
                    es.execute(() -> {
                        // DRM initialization must run on a different thread
                        if (viewHasDropped) {
                            return;
                        }
                        if (activity == null) {
                            DebugLog.e(TAG, "Failed to initialize Player!, null activity");
                            eventEmitter.error("Failed to initialize Player!", new Exception("Current Activity is null!"), "1001");
                            return;
                        }

                        // Initialize handler to run on the main thread
                        activity.runOnUiThread(() -> {
                            if (viewHasDropped) {
                                return;
                            }
                            try {
                                // Source initialization must run on the main thread
                                initializePlayerSource();
                            } catch (Exception ex) {
                                self.playerNeedsSource = true;
                                DebugLog.e(TAG, "Failed to initialize Player! 1");
                                DebugLog.e(TAG, ex.toString());
                                ex.printStackTrace();
                                self.eventEmitter.error(ex.toString(), ex, "1001");
                            }
                        });
                    });
                } else if (source.getUri() != null) {
                    initializePlayerSource();
                }
            } catch (Exception ex) {
                self.playerNeedsSource = true;
                DebugLog.e(TAG, "Failed to initialize Player! 2");
                DebugLog.e(TAG, ex.toString());
                ex.printStackTrace();
                eventEmitter.error(ex.toString(), ex, "1001");
            }
        };
        mainHandler.postDelayed(mainRunnable, 1);
    }

    public void getCurrentPosition(Promise promise) {
        if (player != null) {
            double currentPosition = player.getCurrentPosition() / 1000;
            promise.resolve(currentPosition);
        } else {
            promise.reject("PLAYER_NOT_AVAILABLE", "Player is not initialized.");
        }
    }

    private void initializePlayerCore(ReactExoplayerView self) {
        ExoTrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
        self.trackSelector = new DefaultTrackSelector(getContext(), videoTrackSelectionFactory);
        self.trackSelector.setParameters(trackSelector.buildUponParameters()
                .setMaxVideoBitrate(maxBitRate == 0 ? Integer.MAX_VALUE : maxBitRate));

        DefaultAllocator allocator = new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE);
        RNVLoadControl loadControl = new RNVLoadControl(
                allocator,
                bufferConfig
        );
        DefaultRenderersFactory renderersFactory =
                new DefaultRenderersFactory(getContext())
                        .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
                        .setEnableDecoderFallback(true)
                        .forceEnableMediaCodecAsynchronousQueueing();

        // Create an AdsLoader.
        adsLoader = new ImaAdsLoader
                .Builder(themedReactContext)
                .setAdEventListener(this)
                .setAdErrorListener(this)
                .build();
        DefaultMediaSourceFactory mediaSourceFactory = new DefaultMediaSourceFactory(mediaDataSourceFactory);
        if (useCache) {
            mediaSourceFactory.setDataSourceFactory(RNVSimpleCache.INSTANCE.getCacheFactory(buildHttpDataSourceFactory(true)));
        }

        if (adsLoader != null) {
            mediaSourceFactory.setLocalAdInsertionComponents(unusedAdTagUri -> adsLoader, exoPlayerView);
        }

        player = new ExoPlayer.Builder(getContext(), renderersFactory)
                .setTrackSelector(self.trackSelector)
                .setBandwidthMeter(bandwidthMeter)
                .setLoadControl(loadControl)
                .setMediaSourceFactory(mediaSourceFactory)
                .build();
        ReactNativeVideoManager.Companion.getInstance().onInstanceCreated(instanceId, player);
        refreshDebugState();
        player.addListener(self);
        player.setVolume(muted ? 0.f : audioVolume * 1);
        exoPlayerView.setPlayer(player);
        if (adsLoader != null) {
            adsLoader.setPlayer(player);
        }
        audioBecomingNoisyReceiver.setListener(self);
        bandwidthMeter.addEventListener(new Handler(), self);
        setPlayWhenReady(!isPaused);
        playerNeedsSource = true;

        PlaybackParameters params = new PlaybackParameters(rate, 1f);
        player.setPlaybackParameters(params);
        changeAudioOutput(this.audioOutput);

        if(showNotificationControls) {
            setupPlaybackService();
        }
    }

    private DrmSessionManager initializePlayerDrm() {
        DrmSessionManager drmSessionManager = null;
        DRMProps drmProps = source.getDrmProps();
        // need to realign UUID in DRM Props from source
        if (drmProps != null && drmProps.getDrmType() != null) {
            UUID uuid = Util.getDrmUuid(drmProps.getDrmType());
            if (uuid != null) {
                try {
                    DebugLog.w(TAG, "drm buildDrmSessionManager");
                    drmSessionManager = buildDrmSessionManager(uuid, drmProps);
                } catch (UnsupportedDrmException e) {
                    int errorStringId = Util.SDK_INT < 18 ? R.string.error_drm_not_supported
                            : (e.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
                            ? R.string.error_drm_unsupported_scheme : R.string.error_drm_unknown);
                    eventEmitter.error(getResources().getString(errorStringId), e, "3003");
                }
            }
        }
        return drmSessionManager;
    }

    private void initializePlayerSource() {
        if (source.getUri() == null) {
            return;
        }
        /// init DRM
        DrmSessionManager drmSessionManager = initializePlayerDrm();
        if (drmSessionManager == null && source.getDrmProps() != null && source.getDrmProps().getDrmType() != null) {
            // Failed to initialize DRM session manager - cannot continue
            DebugLog.e(TAG, "Failed to initialize DRM Session Manager Framework!");
            return;
        }
        // init source to manage ads and external text tracks
        ArrayList<MediaSource> mediaSourceList = buildTextSources();
        MediaSource videoSource = buildMediaSource(source.getUri(), source.getExtension(), drmSessionManager, source.getCropStartMs(), source.getCropEndMs());
        MediaSource mediaSourceWithAds = null;
        if (adTagUrl != null && adsLoader != null) {
            DefaultMediaSourceFactory mediaSourceFactory = new DefaultMediaSourceFactory(mediaDataSourceFactory)
                    .setLocalAdInsertionComponents(unusedAdTagUri -> adsLoader, exoPlayerView);
            DataSpec adTagDataSpec = new DataSpec(adTagUrl);
            mediaSourceWithAds = new AdsMediaSource(videoSource, adTagDataSpec, ImmutableList.of(source.getUri(), adTagUrl), mediaSourceFactory, adsLoader, exoPlayerView);
        } else {
            if (adTagUrl == null && adsLoader != null) {
                adsLoader.release();
                adsLoader = null;
            }
        }
        MediaSource mediaSource;
        if (mediaSourceList.isEmpty()) {
            if (mediaSourceWithAds != null) {
                mediaSource = mediaSourceWithAds;
            } else {
                mediaSource = videoSource;
            }
        } else {
            if (mediaSourceWithAds != null) {
                mediaSourceList.add(0, mediaSourceWithAds);
            } else {
                mediaSourceList.add(0, videoSource);
            }
            MediaSource[] textSourceArray = mediaSourceList.toArray(
                    new MediaSource[mediaSourceList.size()]
            );
            mediaSource = new MergingMediaSource(textSourceArray);
        }

        // wait for player to be set
        while (player == null) {
            try {
                wait();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                DebugLog.e(TAG, ex.toString());
            }
        }

        boolean haveResumePosition = resumeWindow != C.INDEX_UNSET;
        if (haveResumePosition) {
            player.seekTo(resumeWindow, resumePosition);
            player.setMediaSource(mediaSource, false);
        } else if (source.getStartPositionMs() > 0) {
            player.setMediaSource(mediaSource, source.getStartPositionMs());
        } else {
            player.setMediaSource(mediaSource, true);
        }
        player.prepare();
        playerNeedsSource = false;

        reLayoutControls();

        eventEmitter.loadStart();
        loadVideoStarted = true;

        finishPlayerInitialization();
    }

    private void finishPlayerInitialization() {
        // Initializing the playerControlView
        initializePlayerControl();
        setControls(controls);
        applyModifiers();
    }

    private void setupPlaybackService() {
        if (!showNotificationControls || player == null) {
            return;
        }

        playbackServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                playbackServiceBinder = (PlaybackServiceBinder) service;

                try {
                    playbackServiceBinder.getService().registerPlayer(player,
                            Objects.requireNonNull((Class<Activity>) (themedReactContext.getCurrentActivity()).getClass()));
                } catch (Exception e) {
                    DebugLog.e(TAG, "Cloud not register ExoPlayer");
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                try {
                    playbackServiceBinder.getService().unregisterPlayer(player);
                } catch (Exception ignored) {}

                playbackServiceBinder = null;
            }

            @Override
            public void onNullBinding(ComponentName name) {
                DebugLog.e(TAG, "Cloud not register ExoPlayer");
            }
        };

        Intent intent = new Intent(themedReactContext, VideoPlaybackService.class);
        intent.setAction(MediaSessionService.SERVICE_INTERFACE);

        themedReactContext.startService(intent);

        int flags;
        if (Build.VERSION.SDK_INT >= 29) {
            flags = Context.BIND_AUTO_CREATE | Context.BIND_INCLUDE_CAPABILITIES;
        } else {
            flags = Context.BIND_AUTO_CREATE;
        }

        themedReactContext.bindService(intent, playbackServiceConnection, flags);
    }

    private void cleanupPlaybackService() {
        try {
            if(player != null && playbackServiceBinder != null) {
                playbackServiceBinder.getService().unregisterPlayer(player);
            }

            playbackServiceBinder = null;

            if(playbackServiceConnection != null) {
                themedReactContext.unbindService(playbackServiceConnection);
            }
        } catch(Exception e) {
            DebugLog.w(TAG, "Cloud not cleanup playback service");
        }
    }

    private DrmSessionManager buildDrmSessionManager(UUID uuid, DRMProps drmProps) throws UnsupportedDrmException {
        return buildDrmSessionManager(uuid, drmProps, 0);
    }

    private DrmSessionManager buildDrmSessionManager(UUID uuid, DRMProps drmProps, int retryCount) throws UnsupportedDrmException {
        if (Util.SDK_INT < 18) {
            return null;
        }
        try {
            HttpMediaDrmCallback drmCallback = new HttpMediaDrmCallback(drmProps.getDrmLicenseServer(),
                    buildHttpDataSourceFactory(false));

            String[] keyRequestPropertiesArray = drmProps.getDrmLicenseHeader();
            for (int i = 0; i < keyRequestPropertiesArray.length - 1; i += 2) {
                drmCallback.setKeyRequestProperty(keyRequestPropertiesArray[i], keyRequestPropertiesArray[i + 1]);
            }
            FrameworkMediaDrm mediaDrm = FrameworkMediaDrm.newInstance(uuid);
            if (hasDrmFailed) {
                // When DRM fails using L1 we want to switch to L3
                mediaDrm.setPropertyString("securityLevel", "L3");
            }
            return new DefaultDrmSessionManager.Builder()
                    .setUuidAndExoMediaDrmProvider(uuid, (_uuid) -> mediaDrm)
                    .setKeyRequestParameters(null)
                    .setMultiSession(drmProps.getMultiDrm())
                    .build(drmCallback);
        } catch (UnsupportedDrmException ex) {
            // Unsupported DRM exceptions are handled by the calling method
            throw ex;
        } catch (Exception ex) {
            if (retryCount < 3) {
                // Attempt retry 3 times in case where the OS Media DRM Framework fails for whatever reason
                return buildDrmSessionManager(uuid, drmProps, ++retryCount);
            }
            // Handle the unknow exception and emit to JS
            eventEmitter.error(ex.toString(), ex, "3006");
            return null;
        }
    }

    private MediaSource buildMediaSource(Uri uri, String overrideExtension, DrmSessionManager drmSessionManager, long cropStartMs, long cropEndMs) {
        if (uri == null) {
            throw new IllegalStateException("Invalid video uri");
        }
        int type;
        if ("rtsp".equals(overrideExtension)) {
            type = CONTENT_TYPE_RTSP;
        } else {
            type = Util.inferContentType(!TextUtils.isEmpty(overrideExtension) ? "." + overrideExtension
                    : uri.getLastPathSegment());
        }
        config.setDisableDisconnectError(this.disableDisconnectError);

        MediaItem.Builder mediaItemBuilder = new MediaItem.Builder()
                .setUri(uri);

        // refresh custom Metadata
        customMetadata = ConfigurationUtils.buildCustomMetadata(source.getMetadata());
        if (customMetadata != null) {
            mediaItemBuilder.setMediaMetadata(customMetadata);
        }

        if (adTagUrl != null) {
            mediaItemBuilder.setAdsConfiguration(
                    new MediaItem.AdsConfiguration.Builder(adTagUrl).build()
            );
        }

        MediaItem.LiveConfiguration.Builder liveConfiguration = ConfigurationUtils.getLiveConfiguration(bufferConfig);
        mediaItemBuilder.setLiveConfiguration(liveConfiguration.build());

        MediaSource.Factory mediaSourceFactory;
        DrmSessionManagerProvider drmProvider;
        List<StreamKey> streamKeys = new ArrayList<>();
        if (drmSessionManager != null) {
            drmProvider = ((_mediaItem) -> drmSessionManager);
        } else {
            drmProvider = new DefaultDrmSessionManagerProvider();
        }

        switch (type) {
            case CONTENT_TYPE_SS:
                if(!BuildConfig.USE_EXOPLAYER_SMOOTH_STREAMING) {
                    DebugLog.e("Exo Player Exception", "Smooth Streaming is not enabled!");
                    throw new IllegalStateException("Smooth Streaming is not enabled!");
                }

                mediaSourceFactory = new SsMediaSource.Factory(
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory),
                        buildDataSourceFactory(false)
                );
                break;
            case CONTENT_TYPE_DASH:
                if(!BuildConfig.USE_EXOPLAYER_DASH) {
                    DebugLog.e("Exo Player Exception", "DASH is not enabled!");
                    throw new IllegalStateException("DASH is not enabled!");
                }

                mediaSourceFactory = new DashMediaSource.Factory(
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory),
                        buildDataSourceFactory(false)
                );
                break;
            case CONTENT_TYPE_HLS:
                if (!BuildConfig.USE_EXOPLAYER_HLS) {
                    DebugLog.e("Exo Player Exception", "HLS is not enabled!");
                    throw new IllegalStateException("HLS is not enabled!");
                }

                mediaSourceFactory = new HlsMediaSource.Factory(
                        mediaDataSourceFactory
                ).setAllowChunklessPreparation(source.getTextTracksAllowChunklessPreparation());
                break;
            case CONTENT_TYPE_OTHER:
                if ("asset".equals(uri.getScheme())) {
                    try {
                        DataSource.Factory assetDataSourceFactory = buildAssetDataSourceFactory(themedReactContext, uri);
                        mediaSourceFactory = new ProgressiveMediaSource.Factory(assetDataSourceFactory);
                    } catch (Exception e) {
                        throw new IllegalStateException("cannot open input file" + uri);
                    }
                } else if ("file".equals(uri.getScheme()) ||
                        !useCache) {
                    mediaSourceFactory = new ProgressiveMediaSource.Factory(
                            mediaDataSourceFactory
                    );
                } else {
                    mediaSourceFactory = new ProgressiveMediaSource.Factory(
                            RNVSimpleCache.INSTANCE.getCacheFactory(buildHttpDataSourceFactory(true))
                    );

                }
                break;
            case CONTENT_TYPE_RTSP:
                if (!BuildConfig.USE_EXOPLAYER_RTSP) {
                    DebugLog.e("Exo Player Exception", "RTSP is not enabled!");
                    throw new IllegalStateException("RTSP is not enabled!");
                }

                mediaSourceFactory = new RtspMediaSource.Factory();
                break;
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }

        MediaItem mediaItem = mediaItemBuilder.setStreamKeys(streamKeys).build();
        MediaSource mediaSource = mediaSourceFactory
                .setDrmSessionManagerProvider(drmProvider)
                .setLoadErrorHandlingPolicy(
                        config.buildLoadErrorHandlingPolicy(minLoadRetryCount)
                )
                .createMediaSource(mediaItem);

        if (cropStartMs >= 0 && cropEndMs >= 0) {
            return new ClippingMediaSource(mediaSource, cropStartMs * 1000, cropEndMs * 1000);
        } else if (cropStartMs >= 0) {
            return new ClippingMediaSource(mediaSource, cropStartMs * 1000, TIME_END_OF_SOURCE);
        } else if (cropEndMs >= 0) {
            return new ClippingMediaSource(mediaSource, 0, cropEndMs * 1000);
        }

        return mediaSource;
    }

    private ArrayList<MediaSource> buildTextSources() {
        ArrayList<MediaSource> textSources = new ArrayList<>();
        if (textTracks == null) {
            return textSources;
        }

        for (SideLoadedTextTrack track : textTracks.getTracks()) {
            MediaSource textSource = buildTextSource(track.getTitle(),
                    track.getUri(),
                    track.getType(),
                    track.getLanguage());
            textSources.add(textSource);
        }
        return textSources;
    }

    private MediaSource buildTextSource(String title, Uri uri, String mimeType, String language) {
        MediaItem.SubtitleConfiguration subtitleConfiguration = new MediaItem.SubtitleConfiguration.Builder(uri)
                .setMimeType(mimeType)
                .setLanguage(language)
                .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                .setRoleFlags(C.ROLE_FLAG_SUBTITLE)
                .setLabel(title)
                .build();
        return new SingleSampleMediaSource.Factory(mediaDataSourceFactory)
                .createMediaSource(subtitleConfiguration, C.TIME_UNSET);
    }

    private void releasePlayer() {
        if (player != null) {
            if (adsLoader != null) {
                adsLoader.setPlayer(null);
            }

            if(playbackServiceBinder != null) {
                playbackServiceBinder.getService().unregisterPlayer(player);
                themedReactContext.unbindService(playbackServiceConnection);
            }

            updateResumePosition();
            player.release();
            player.removeListener(this);
            trackSelector = null;

            ReactNativeVideoManager.Companion.getInstance().onInstanceRemoved(instanceId, player);
            player = null;
        }

        if (adsLoader != null) {
            adsLoader.release();
        }
        adsLoader = null;
        progressHandler.removeMessages(SHOW_PROGRESS);
        audioBecomingNoisyReceiver.removeListener();
        bandwidthMeter.removeEventListener(this);

        if (mainHandler != null && mainRunnable != null) {
            mainHandler.removeCallbacks(mainRunnable);
            mainRunnable = null;
        }
    }

    private static class OnAudioFocusChangedListener implements AudioManager.OnAudioFocusChangeListener {
        private final ReactExoplayerView view;
        private final ThemedReactContext themedReactContext;

        private OnAudioFocusChangedListener(ReactExoplayerView view, ThemedReactContext themedReactContext) {
            this.view = view;
            this.themedReactContext = themedReactContext;
        }

        @Override
        public void onAudioFocusChange(int focusChange) {
            Activity activity = themedReactContext.getCurrentActivity();

            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    view.hasAudioFocus = false;
                    view.eventEmitter.audioFocusChanged(false);
                    // FIXME this pause can cause issue if content doesn't have pause capability (can happen on live channel)
                    if (activity != null) {
                        activity.runOnUiThread(view::pausePlayback);
                    }
                    view.audioManager.abandonAudioFocus(this);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    view.eventEmitter.audioFocusChanged(false);
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    view.hasAudioFocus = true;
                    view.eventEmitter.audioFocusChanged(true);
                    break;
                default:
                    break;
            }

            if (view.player != null && activity != null) {
                if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                    // Lower the volume
                    if (!view.muted) {
                        activity.runOnUiThread(() ->
                            view.player.setVolume(view.audioVolume * 0.8f)
                        );
                    }
                } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                    // Raise it back to normal
                    if (!view.muted) {
                        activity.runOnUiThread(() ->
                            view.player.setVolume(view.audioVolume * 1)
                        );
                    }
                }
            }
        }
    }

    private boolean requestAudioFocus() {
        if (disableFocus || source.getUri() == null || this.hasAudioFocus) {
            return true;
        }
        int result = audioManager.requestAudioFocus(audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private void setPlayWhenReady(boolean playWhenReady) {
        if (player == null) {
            return;
        }

        if (playWhenReady) {
            this.hasAudioFocus = requestAudioFocus();
            if (this.hasAudioFocus) {
                player.setPlayWhenReady(true);
            }
        } else {
            // ensure playback is not ENDED, else it will trigger another ended event
            if (player.getPlaybackState() != Player.STATE_ENDED) {
                player.setPlayWhenReady(false);
            }
        }
    }

    private void resumePlayback() {
        if (player != null) {
            if (!player.getPlayWhenReady()) {
                setPlayWhenReady(true);
            }
            setKeepScreenOn(preventsDisplaySleepDuringVideoPlayback);
        }
    }

    private void pausePlayback() {
        if (player != null) {
            if (player.getPlayWhenReady()) {
                setPlayWhenReady(false);
            }
        }
        setKeepScreenOn(false);
    }

    private void stopPlayback() {
        onStopPlayback();
        releasePlayer();
    }

    private void onStopPlayback() {
        if (isFullscreen) {
            setFullscreen(false);
        }
        audioManager.abandonAudioFocus(audioFocusChangeListener);
    }

    private void updateResumePosition() {
        resumeWindow = player.getCurrentMediaItemIndex();
        resumePosition = player.isCurrentMediaItemSeekable() ? Math.max(0, player.getCurrentPosition())
                : C.TIME_UNSET;
    }

    private void clearResumePosition() {
        resumeWindow = C.INDEX_UNSET;
        resumePosition = C.TIME_UNSET;
    }

    /**
     * Returns a new DataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #bandwidthMeter} as a listener to the new
     *                          DataSource factory.
     * @return A new DataSource factory.
     */
    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return DataSourceUtil.getDefaultDataSourceFactory(this.themedReactContext,
                useBandwidthMeter ? bandwidthMeter : null, source.getHeaders());
    }

    /**
     * Returns a new HttpDataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #bandwidthMeter} as a listener to the new
     *     DataSource factory.
     * @return A new HttpDataSource factory.
     */
    private HttpDataSource.Factory buildHttpDataSourceFactory(boolean useBandwidthMeter) {
        return DataSourceUtil.getDefaultHttpDataSourceFactory(this.themedReactContext, useBandwidthMeter ? bandwidthMeter : null, source.getHeaders());
    }

    // AudioBecomingNoisyListener implementation
    @Override
    public void onAudioBecomingNoisy() {
        eventEmitter.audioBecomingNoisy();
    }

    // Player.Listener implementation
    @Override
    public void onIsLoadingChanged(boolean isLoading) {
        // Do nothing.
    }

    @Override
    public void onEvents(@NonNull Player player, Player.Events events) {
        if (events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED) || events.contains(Player.EVENT_PLAY_WHEN_READY_CHANGED)) {
            int playbackState = player.getPlaybackState();
            boolean playWhenReady = player.getPlayWhenReady();
            String text = "onStateChanged: playWhenReady=" + playWhenReady + ", playbackState=";
            eventEmitter.playbackRateChange(playWhenReady && playbackState == ExoPlayer.STATE_READY ? 1.0f : 0.0f);
            switch (playbackState) {
                case Player.STATE_IDLE:
                    text += "idle";
                    eventEmitter.idle();
                    clearProgressMessageHandler();
                    if (!player.getPlayWhenReady()) {
                        setKeepScreenOn(false);
                    }
                    break;
                case Player.STATE_BUFFERING:
                    text += "buffering";
                    onBuffering(true);
                    clearProgressMessageHandler();
                    setKeepScreenOn(preventsDisplaySleepDuringVideoPlayback);
                    break;
                case Player.STATE_READY:
                    text += "ready";
                    eventEmitter.ready();
                    onBuffering(false);
                    clearProgressMessageHandler(); // ensure there is no other message
                    startProgressHandler();
                    videoLoaded();
                    if (selectTrackWhenReady && isUsingContentResolution) {
                        selectTrackWhenReady = false;
                        setSelectedTrack(C.TRACK_TYPE_VIDEO, videoTrackType, videoTrackValue);
                    }
                    // Setting the visibility for the playerControlView
                    if (playerControlView != null) {
                        playerControlView.show();
                    }
                    setKeepScreenOn(preventsDisplaySleepDuringVideoPlayback);
                    break;
                case Player.STATE_ENDED:
                    text += "ended";
                    updateProgress();
                    eventEmitter.end();
                    onStopPlayback();
                    setKeepScreenOn(false);
                    break;
                default:
                    text += "unknown";
                    break;
            }
            DebugLog.d(TAG, text);
        }
    }

    private void startProgressHandler() {
        progressHandler.sendEmptyMessage(SHOW_PROGRESS);
    }

    /**
     *  The progress message handler will duplicate recursions of the onProgressMessage handler
     *  on change of player state from any state to STATE_READY with playWhenReady is true (when
     *  the video is not paused). This clears all existing messages.
     */
    private void clearProgressMessageHandler() {
        progressHandler.removeMessages(SHOW_PROGRESS);
    }

    private void videoLoaded() {
        if (!player.isPlayingAd() && loadVideoStarted) {
            loadVideoStarted = false;
            if (audioTrackType != null) {
                setSelectedAudioTrack(audioTrackType, audioTrackValue);
            }
            if (videoTrackType != null) {
                setSelectedVideoTrack(videoTrackType, videoTrackValue);
            }
            if (textTrackType != null) {
                setSelectedTextTrack(textTrackType, textTrackValue);
            }
            Format videoFormat = player.getVideoFormat();
            boolean isRotatedContent = videoFormat != null && (videoFormat.rotationDegrees == 90 || videoFormat.rotationDegrees == 270);
            int width = videoFormat != null ? (isRotatedContent ? videoFormat.height : videoFormat.width) : 0;
            int height = videoFormat != null ? (isRotatedContent ? videoFormat.width : videoFormat.height) : 0;
            String trackId = videoFormat != null ? videoFormat.id : "-1";

            // Properties that must be accessed on the main thread
            long duration = player.getDuration();
            long currentPosition = player.getCurrentPosition();
            ArrayList<Track> audioTracks = getAudioTrackInfo();
            ArrayList<Track> textTracks  = getTextTrackInfo();

            if (this.contentStartTime != -1L) {
                ExecutorService es = Executors.newSingleThreadExecutor();
                es.execute(() -> {
                    // To prevent ANRs caused by getVideoTrackInfo we run this on a different thread and notify the player only when we're done
                    ArrayList<VideoTrack> videoTracks = getVideoTrackInfoFromManifest();
                    if (videoTracks != null) {
                        isUsingContentResolution = true;
                    }
                    eventEmitter.load(duration, currentPosition, width, height,
                            audioTracks, textTracks, videoTracks, trackId );

                });
                return;
            }

            ArrayList<VideoTrack> videoTracks = getVideoTrackInfo();

            eventEmitter.load(duration, currentPosition, width, height,
                    audioTracks, textTracks, videoTracks, trackId);
        }
    }

    private static boolean isTrackSelected(TrackSelection selection, TrackGroup group,
                                           int trackIndex){
        return selection != null && selection.getTrackGroup() == group
                && selection.indexOf( trackIndex ) != C.INDEX_UNSET;
    }

    private ArrayList<Track> getAudioTrackInfo() {
        ArrayList<Track> audioTracks = new ArrayList<>();
        if (trackSelector == null) {
            // Likely player is unmounting so no audio tracks are available anymore
            return audioTracks;
        }

        MappingTrackSelector.MappedTrackInfo info = trackSelector.getCurrentMappedTrackInfo();
        int index = getTrackRendererIndex(C.TRACK_TYPE_AUDIO);
        if (info == null || index == C.INDEX_UNSET) {
            return audioTracks;
        }
        TrackGroupArray groups = info.getTrackGroups(index);
        TrackSelectionArray selectionArray = player.getCurrentTrackSelections();
        TrackSelection selection = selectionArray.get( C.TRACK_TYPE_AUDIO );

        for (int i = 0; i < groups.length; ++i) {
            TrackGroup group = groups.get(i);
            Format format = group.getFormat(0);
            Track audioTrack = exoplayerTrackToGenericTrack(format, i, selection, group);
            audioTrack.setBitrate(format.bitrate == Format.NO_VALUE ? 0 : format.bitrate);
            audioTracks.add(audioTrack);
        }
        return audioTracks;
    }

    private VideoTrack exoplayerVideoTrackToGenericVideoTrack(Format format, int trackIndex) {
        VideoTrack videoTrack = new VideoTrack();
        videoTrack.setWidth(format.width == Format.NO_VALUE ? 0 : format.width);
        videoTrack.setHeight(format.height == Format.NO_VALUE ? 0 : format.height);
        videoTrack.setBitrate(format.bitrate == Format.NO_VALUE ? 0 : format.bitrate);
        videoTrack.setRotation(format.rotationDegrees);
        if (format.codecs != null) videoTrack.setCodecs(format.codecs);
        videoTrack.setTrackId(format.id == null ? String.valueOf(trackIndex) : format.id);
        videoTrack.setIndex(trackIndex);
        return videoTrack;
    }

    private ArrayList<VideoTrack> getVideoTrackInfo() {
        ArrayList<VideoTrack> videoTracks = new ArrayList<>();
        if (trackSelector == null) {
            // Likely player is unmounting so no video tracks are available anymore
            return videoTracks;
        }
        MappingTrackSelector.MappedTrackInfo info = trackSelector.getCurrentMappedTrackInfo();
        int index = getTrackRendererIndex(C.TRACK_TYPE_VIDEO);
        if (info == null || index == C.INDEX_UNSET) {
            return videoTracks;
        }

        TrackGroupArray groups = info.getTrackGroups(index);
        for (int i = 0; i < groups.length; ++i) {
            TrackGroup group = groups.get(i);

            for (int trackIndex = 0; trackIndex < group.length; trackIndex++) {
                Format format = group.getFormat(trackIndex);
                if (isFormatSupported(format)) {
                    VideoTrack videoTrack = exoplayerVideoTrackToGenericVideoTrack(format, trackIndex);
                    videoTracks.add(videoTrack);
                }
            }
        }
        return videoTracks;
    }

    private ArrayList<VideoTrack> getVideoTrackInfoFromManifest() {
        return this.getVideoTrackInfoFromManifest(0);
    }

    // We need retry count to in case where minefest request fails from poor network conditions
    @WorkerThread
    private ArrayList<VideoTrack> getVideoTrackInfoFromManifest(int retryCount) {
        ExecutorService es = Executors.newSingleThreadExecutor();
        final DataSource dataSource = this.mediaDataSourceFactory.createDataSource();
        final Uri sourceUri = source.getUri();
        final long startTime = this.contentStartTime * 1000 - 100; // s -> ms with 100ms offset

        Future<ArrayList<VideoTrack>> result = es.submit(new Callable() {
            final DataSource ds = dataSource;
            final Uri uri = sourceUri;
            final long startTimeUs = startTime * 1000; // ms -> us

            public ArrayList<VideoTrack> call() {
                ArrayList<VideoTrack> videoTracks = new ArrayList<>();
                try  {
                    DashManifest manifest = DashUtil.loadManifest(this.ds, this.uri);
                    int periodCount = manifest.getPeriodCount();
                    for (int i = 0; i < periodCount; i++) {
                        Period period = manifest.getPeriod(i);
                        for (int adaptationIndex = 0; adaptationIndex < period.adaptationSets.size(); adaptationIndex++) {
                            AdaptationSet adaptation = period.adaptationSets.get(adaptationIndex);
                            if (adaptation.type != C.TRACK_TYPE_VIDEO) {
                                continue;
                            }
                            boolean hasFoundContentPeriod = false;
                            for (int representationIndex = 0; representationIndex < adaptation.representations.size(); representationIndex++) {
                                Representation representation = adaptation.representations.get(representationIndex);
                                Format format = representation.format;
                                if (isFormatSupported(format)) {
                                    if (representation.presentationTimeOffsetUs <= startTimeUs) {
                                        break;
                                    }
                                    hasFoundContentPeriod = true;
                                    VideoTrack videoTrack = exoplayerVideoTrackToGenericVideoTrack(format, representationIndex);
                                    videoTracks.add(videoTrack);
                                }
                            }
                            if (hasFoundContentPeriod) {
                                return videoTracks;
                            }
                        }
                    }
                } catch (Exception e) {
                    DebugLog.w(TAG, "error in getVideoTrackInfoFromManifest:" + e.getMessage());
                }
                return null;
            }
        });

        try {
            ArrayList<VideoTrack> results = result.get(3000, TimeUnit.MILLISECONDS);
            if (results == null && retryCount < 1) {
                return this.getVideoTrackInfoFromManifest(++retryCount);
            }
            es.shutdown();
            return results;
        } catch (Exception e) {
            DebugLog.w(TAG, "error in getVideoTrackInfoFromManifest handling request:" + e.getMessage());
        }

        return null;
    }

    private Track exoplayerTrackToGenericTrack(Format format, int trackIndex, TrackSelection selection, TrackGroup group) {
        Track track = new Track();
        track.setIndex(trackIndex);
        if (format.sampleMimeType != null) track.setMimeType(format.sampleMimeType);
        if (format.language != null) track.setLanguage(format.language);
        if (format.label != null) track.setTitle(format.label);
        track.setSelected(isTrackSelected(selection, group, 0));
        return track;
    }

    private ArrayList<Track> getTextTrackInfo() {
        ArrayList<Track> textTracks = new ArrayList<>();
        if (trackSelector == null) {
            return textTracks;
        }
        MappingTrackSelector.MappedTrackInfo info = trackSelector.getCurrentMappedTrackInfo();
        int index = getTrackRendererIndex(C.TRACK_TYPE_TEXT);
        if (info == null || index == C.INDEX_UNSET) {
            return textTracks;
        }
        TrackSelectionArray selectionArray = player.getCurrentTrackSelections();
        TrackSelection selection = selectionArray.get( C.TRACK_TYPE_VIDEO );
        TrackGroupArray groups = info.getTrackGroups(index);

        for (int i = 0; i < groups.length; ++i) {
            TrackGroup group = groups.get(i);
            Format format = group.getFormat(0);
            Track textTrack = exoplayerTrackToGenericTrack(format, i, selection, group);
            textTracks.add(textTrack);
        }
        return textTracks;
    }

    private void onBuffering(boolean buffering) {
        if (isBuffering == buffering) {
            return;
        }

        isBuffering = buffering;
        eventEmitter.buffering(buffering);
    }

    @Override
    public void onPositionDiscontinuity(@NonNull Player.PositionInfo oldPosition, @NonNull Player.PositionInfo newPosition, @Player.DiscontinuityReason int reason) {
        if (reason == Player.DISCONTINUITY_REASON_SEEK) {
            eventEmitter.seek(player.getCurrentPosition(), newPosition.positionMs % 1000); // time are in seconds /°\
            if (isUsingContentResolution) {
                // We need to update the selected track to make sure that it still matches user selection if track list has changed in this period
                setSelectedTrack(C.TRACK_TYPE_VIDEO, videoTrackType, videoTrackValue);
            }
        }

        if (playerNeedsSource) {
            // This will only occur if the user has performed a seek whilst in the error state. Update the
            // resume position so that if the user then retries, playback will resume from the position to
            // which they seeked.
            updateResumePosition();
        }
        if (isUsingContentResolution) {
            // Discontinuity events might have a different track list so we update the selected track
            setSelectedTrack(C.TRACK_TYPE_VIDEO, videoTrackType, videoTrackValue);
            selectTrackWhenReady = true;
        }
        // When repeat is turned on, reaching the end of the video will not cause a state change
        // so we need to explicitly detect it.
        if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION
                && player.getRepeatMode() == Player.REPEAT_MODE_ONE) {
            updateProgress();
            eventEmitter.end();
        }
    }

    @Override
    public void onTimelineChanged(@NonNull Timeline timeline, int reason) {
        // Do nothing.
    }

    @Override
    public void onTracksChanged(@NonNull Tracks tracks) {
        eventEmitter.textTracks(getTextTrackInfo());
        eventEmitter.audioTracks(getAudioTrackInfo());
        eventEmitter.videoTracks(getVideoTrackInfo());
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters params) {
        eventEmitter.playbackRateChange(params.speed);
    }

    @Override
    public void onVolumeChanged(float volume) {
        eventEmitter.volumeChange(volume);
    }

    @Override
    public void onIsPlayingChanged(boolean isPlaying) {
        eventEmitter.playbackStateChanged(isPlaying);
    }

    @Override
    public void onPlayerError(@NonNull PlaybackException e) {
        String errorString = "ExoPlaybackException: " + PlaybackException.getErrorCodeName(e.errorCode);
        String errorCode = "2" + e.errorCode;
        switch(e.errorCode) {
            case PlaybackException.ERROR_CODE_DRM_DEVICE_REVOKED:
            case PlaybackException.ERROR_CODE_DRM_LICENSE_ACQUISITION_FAILED:
            case PlaybackException.ERROR_CODE_DRM_PROVISIONING_FAILED:
            case PlaybackException.ERROR_CODE_DRM_SYSTEM_ERROR:
            case PlaybackException.ERROR_CODE_DRM_UNSPECIFIED:
                if (!hasDrmFailed) {
                    // When DRM fails to reach the app level certificate server it will fail with a source error so we assume that it is DRM related and try one more time
                    hasDrmFailed = true;
                    playerNeedsSource = true;
                    updateResumePosition();
                    initializePlayer();
                    setPlayWhenReady(true);
                    return;
                }
                break;
            default:
                break;
        }
        eventEmitter.error(errorString, e, errorCode);
        playerNeedsSource = true;
        if (isBehindLiveWindow(e)) {
            clearResumePosition();
            initializePlayer();
        } else {
            updateResumePosition();
        }
    }

    private static boolean isBehindLiveWindow(PlaybackException e) {
        return e.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW;
    }

    public int getTrackRendererIndex(int trackType) {
        if (player != null) {
            int rendererCount = player.getRendererCount();
            for (int rendererIndex = 0; rendererIndex < rendererCount; rendererIndex++) {
                if (player.getRendererType(rendererIndex) == trackType) {
                    return rendererIndex;
                }
            }
        }
        return C.INDEX_UNSET;
    }

    @Override
    public void onMetadata(@NonNull Metadata metadata) {
        ArrayList<TimedMetadata> metadataArray = new ArrayList<>();
        for (int i = 0; i < metadata.length(); i++) {
            Metadata.Entry entry = metadata.get(i);

            if (entry instanceof Id3Frame) {
                Id3Frame frame = (Id3Frame) metadata.get(i);

                String value = "";

                if (frame instanceof TextInformationFrame) {
                    TextInformationFrame txxxFrame = (TextInformationFrame) frame;
                    value = txxxFrame.value;
                }
                TimedMetadata timedMetadata = new TimedMetadata(frame.id, value);
                metadataArray.add(timedMetadata);
            } else if (entry instanceof EventMessage) {
                EventMessage eventMessage = (EventMessage) entry;
                TimedMetadata timedMetadata = new TimedMetadata(eventMessage.schemeIdUri, eventMessage.value);
                metadataArray.add(timedMetadata);
            } else {
                DebugLog.d(TAG, "unhandled metadata " + entry);
            }
        }
        eventEmitter.timedMetadata(metadataArray);
    }

    public void onCues(CueGroup cueGroup) {
        if (!cueGroup.cues.isEmpty() && cueGroup.cues.get(0).text != null) {
            String subtitleText = cueGroup.cues.get(0).text.toString();
            eventEmitter.textTrackDataChanged(subtitleText);
        }
    }

    // ReactExoplayerViewManager public api

    public void setSrc(Source source) {
        if (source.getUri() != null) {
            clearResumePosition();
            boolean isSourceEqual = source.isEquals(this.source);
            hasDrmFailed = false;
            this.source = source;
            this.mediaDataSourceFactory =
                    DataSourceUtil.getDefaultDataSourceFactory(this.themedReactContext, bandwidthMeter,
                            source.getHeaders());

            if (!isSourceEqual) {
                reloadSource();
            }
        }
    }

    public void clearSrc() {
        if (source.getUri() != null) {
            if (player != null) {
                player.stop();
                player.clearMediaItems();
            }
            this.source = new Source();
            this.mediaDataSourceFactory = null;
            clearResumePosition();
        }
    }

    public void setProgressUpdateInterval(final float progressUpdateInterval) {
        mProgressUpdateInterval = progressUpdateInterval;
    }

    public void setReportBandwidth(boolean reportBandwidth) {
        mReportBandwidth = reportBandwidth;
    }

    public void setAdTagUrl(final Uri uri) {
        adTagUrl = uri;
    }

    public void setTextTracks(SideLoadedTextTrackList textTracks) {
        this.textTracks = textTracks;
        reloadSource(); // FIXME Shall be moved inside source
    }

    private void reloadSource() {
        playerNeedsSource = true;
        initializePlayer();
    }

    public void setResizeModeModifier(@ResizeMode.Mode int resizeMode) {
        if (exoPlayerView != null) {
            exoPlayerView.setResizeMode(resizeMode);
        }
    }

    private void applyModifiers() {
        setRepeatModifier(repeat);
        setMutedModifier(muted);
    }

    public void setRepeatModifier(boolean repeat) {
        if (player != null) {
            if (repeat) {
                player.setRepeatMode(Player.REPEAT_MODE_ONE);
            } else {
                player.setRepeatMode(Player.REPEAT_MODE_OFF);
            }
        }
        this.repeat = repeat;
    }

    public void setPreventsDisplaySleepDuringVideoPlayback(boolean preventsDisplaySleepDuringVideoPlayback) {
        this.preventsDisplaySleepDuringVideoPlayback = preventsDisplaySleepDuringVideoPlayback;
    }

    public void disableTrack(int rendererIndex) {
        DefaultTrackSelector.Parameters disableParameters = trackSelector.getParameters()
                .buildUpon()
                .setRendererDisabled(rendererIndex, true)
                .build();
        trackSelector.setParameters(disableParameters);
    }

    public void setSelectedTrack(int trackType, String type, String value) {
        if (player == null) return;
        int rendererIndex = getTrackRendererIndex(trackType);
        if (rendererIndex == C.INDEX_UNSET) {
            return;
        }
        MappingTrackSelector.MappedTrackInfo info = trackSelector.getCurrentMappedTrackInfo();
        if (info == null) {
            return;
        }

        TrackGroupArray groups = info.getTrackGroups(rendererIndex);
        int groupIndex = C.INDEX_UNSET;
        List<Integer> tracks = new ArrayList<>();
        tracks.add(0);

        if (TextUtils.isEmpty(type)) {
            type = "default";
        }

        if ("disabled".equals(type)) {
            disableTrack(rendererIndex);
            return;
        } else if ("language".equals(type)) {
            for (int i = 0; i < groups.length; ++i) {
                Format format = groups.get(i).getFormat(0);
                if (format.language != null && format.language.equals(value)) {
                    groupIndex = i;
                    break;
                }
            }
        } else if ("title".equals(type)) {
            for (int i = 0; i < groups.length; ++i) {
                Format format = groups.get(i).getFormat(0);
                if (format.id != null && format.id.equals(value)) {
                    groupIndex = i;
                    break;
                }
            }
        } else if ("index".equals(type)) {
            int iValue = Integer.parseInt(value);

            if (trackType == C.TRACK_TYPE_VIDEO && groups.length == 1) {
                groupIndex = 0;
                if (iValue < groups.get(groupIndex).length) {
                    tracks.set(0, iValue);
                }
            } else if (iValue < groups.length) {
                groupIndex = iValue;
            }
        } else if ("resolution".equals(type)) {
            int height = Integer.parseInt(value);
            for (int i = 0; i < groups.length; ++i) { // Search for the exact height
                TrackGroup group = groups.get(i);
                Format closestFormat = null;
                int closestTrackIndex = -1;
                boolean usingExactMatch = false;
                for (int j = 0; j < group.length; j++) {
                    Format format = group.getFormat(j);
                    if (format.height == height) {
                        groupIndex = i;
                        tracks.set(0, j);
                        closestFormat = null;
                        closestTrackIndex = -1;
                        usingExactMatch = true;
                        break;
                    } else if (isUsingContentResolution) {
                        // When using content resolution rather than ads, we need to try and find the closest match if there is no exact match
                        if (closestFormat != null) {
                            if ((format.bitrate > closestFormat.bitrate || format.height > closestFormat.height) && format.height < height) {
                                // Higher quality match
                                closestFormat = format;
                                closestTrackIndex = j;
                            }
                        } else if(format.height < height) {
                            closestFormat = format;
                            closestTrackIndex = j;
                        }
                    }
                }
                // This is a fallback if the new period contains only higher resolutions than the user has selected
                if (closestFormat == null && isUsingContentResolution && !usingExactMatch) {
                    // No close match found - so we pick the lowest quality
                    int minHeight = Integer.MAX_VALUE;
                    for (int j = 0; j < group.length; j++) {
                        Format format = group.getFormat(j);
                        if (format.height < minHeight) {
                            minHeight = format.height;
                            groupIndex = i;
                            tracks.set(0, j);
                        }
                    }
                }
                // Selecting the closest match found
                if (closestFormat != null && closestTrackIndex != -1) {
                    // We found the closest match instead of an exact one
                    groupIndex = i;
                    tracks.set(0, closestTrackIndex);
                }
            }
        } else if (trackType == C.TRACK_TYPE_TEXT && Util.SDK_INT > 18) { // Text default
            // Use system settings if possible
            CaptioningManager captioningManager
                    = (CaptioningManager)themedReactContext.getSystemService(Context.CAPTIONING_SERVICE);
            if (captioningManager != null && captioningManager.isEnabled()) {
                groupIndex = getGroupIndexForDefaultLocale(groups);
            }
        } else if (rendererIndex == C.TRACK_TYPE_AUDIO) { // Audio default
            groupIndex = getGroupIndexForDefaultLocale(groups);
        }

        if (groupIndex == C.INDEX_UNSET && trackType == C.TRACK_TYPE_VIDEO && groups.length != 0) { // Video auto
            // Add all tracks as valid options for ABR to choose from
            TrackGroup group = groups.get(0);
            ArrayList<Integer> allTracks = new ArrayList<>(group.length);
            groupIndex = 0;
            for (int j = 0; j < group.length; j++) {
                allTracks.add(j);
            }

            // Valiate list of all tracks and add only supported formats
            int supportedFormatLength = 0;
            for (int g = 0; g < allTracks.size(); g++) {
                Format format = group.getFormat(g);
                if (isFormatSupported(format)) {
                    supportedFormatLength++;
                }
            }
            if (allTracks.size() == 1) {
                // With only one tracks we can't remove any tracks so attempt to play it anyway
                tracks = allTracks;
            } else {
                tracks =  new ArrayList<>(supportedFormatLength + 1);
                for (int k = 0; k < allTracks.size(); k++) {
                    Format format = group.getFormat(k);
                    if (isFormatSupported(format)) {
                        tracks.add(allTracks.get(k));
                    }
                }
            }
        }

        if (groupIndex == C.INDEX_UNSET) {
            disableTrack(rendererIndex);
            return;
        }

        TrackSelectionOverride selectionOverride = new TrackSelectionOverride(groups.get(groupIndex), tracks);

        DefaultTrackSelector.Parameters selectionParameters = trackSelector.getParameters()
                .buildUpon()
                .setExceedAudioConstraintsIfNecessary(true)
                .setExceedRendererCapabilitiesIfNecessary(true)
                .setExceedVideoConstraintsIfNecessary(true)
                .setRendererDisabled(rendererIndex, false)
                .clearOverridesOfType(selectionOverride.getType())
                .addOverride(selectionOverride)
                .build();
        trackSelector.setParameters(selectionParameters);
    }

    private boolean isFormatSupported(Format format) {
        int width = format.width == Format.NO_VALUE ? 0 : format.width;
        int height = format.height == Format.NO_VALUE ? 0 : format.height;
        float frameRate = format.frameRate == Format.NO_VALUE ? 0 : format.frameRate;
        String mimeType = format.sampleMimeType;
        if (mimeType == null) {
            return true;
        }
        boolean isSupported;
        try {
            MediaCodecInfo codecInfo = MediaCodecUtil.getDecoderInfo(mimeType, false, false);
            isSupported = codecInfo.isVideoSizeAndRateSupportedV21(width, height, frameRate);
        } catch (Exception e) {
            // Failed to get decoder info - assume it is supported
            isSupported = true;
        }
        return isSupported;
    }

    private int getGroupIndexForDefaultLocale(TrackGroupArray groups) {
        if (groups.length == 0){
            return C.INDEX_UNSET;
        }

        int groupIndex = 0; // default if no match
        String locale2 = Locale.getDefault().getLanguage(); // 2 letter code
        String locale3 = Locale.getDefault().getISO3Language(); // 3 letter code
        for (int i = 0; i < groups.length; ++i) {
            Format format = groups.get(i).getFormat(0);
            String language = format.language;
            if (language != null && (language.equals(locale2) || language.equals(locale3))) {
                groupIndex = i;
                break;
            }
        }
        return groupIndex;
    }

    public void setSelectedVideoTrack(String type, String value) {
        videoTrackType = type;
        videoTrackValue = value;
        if (!loadVideoStarted) setSelectedTrack(C.TRACK_TYPE_VIDEO, videoTrackType, videoTrackValue);
    }

    public void setSelectedAudioTrack(String type, String value) {
        audioTrackType = type;
        audioTrackValue = value;
        setSelectedTrack(C.TRACK_TYPE_AUDIO, audioTrackType, audioTrackValue);
    }

    public void setSelectedTextTrack(String type, String value) {
        textTrackType = type;
        textTrackValue = value;
        setSelectedTrack(C.TRACK_TYPE_TEXT, textTrackType, textTrackValue);
    }

    public void setPausedModifier(boolean paused) {
        isPaused = paused;
        if (player != null) {
            if (!paused) {
                resumePlayback();
            } else {
                pausePlayback();
            }
        }
    }

    public void setMutedModifier(boolean muted) {
        this.muted = muted;
        if (player != null) {
            player.setVolume(muted ? 0.f : audioVolume);
        }
    }

    private void changeAudioOutput(AudioOutput output) {
        if (player != null) {
            int streamType = output.getStreamType();
            int usage = Util.getAudioUsageForStreamType(streamType);
            int contentType = Util.getAudioContentTypeForStreamType(streamType);
            AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(usage)
                    .setContentType(contentType)
                    .build();
            player.setAudioAttributes(audioAttributes, false);
            AudioManager audioManager = (AudioManager) themedReactContext.getSystemService(Context.AUDIO_SERVICE);
            boolean isSpeakerOutput = output == AudioOutput.SPEAKER;
            audioManager.setMode(
                    isSpeakerOutput ? AudioManager.MODE_NORMAL
                            : AudioManager.MODE_IN_COMMUNICATION);
            audioManager.setSpeakerphoneOn(isSpeakerOutput);
        }
    }

    public void setAudioOutput(AudioOutput output) {
        if (audioOutput != output) {
            this.audioOutput = output;
            changeAudioOutput(output);
        }
    }

    public void setVolumeModifier(float volume) {
        audioVolume = volume;
        if (player != null) {
            player.setVolume(audioVolume);
        }
    }

    public void seekTo(long positionMs) {
        if (player != null) {
            player.seekTo(positionMs);
        }
    }

    public void setRateModifier(float newRate) {
        if (newRate <= 0) {
            DebugLog.w(TAG, "cannot set rate <= 0");
            return;
        }

        rate = newRate;

        if (player != null) {
            PlaybackParameters params = new PlaybackParameters(rate, 1f);
            player.setPlaybackParameters(params);
        }
    }

    public void setMaxBitRateModifier(int newMaxBitRate) {
        maxBitRate = newMaxBitRate;
        if (player != null) {
            trackSelector.setParameters(trackSelector.buildUponParameters()
                    .setMaxVideoBitrate(maxBitRate == 0 ? Integer.MAX_VALUE : maxBitRate));
        }
    }

    public void setMinLoadRetryCountModifier(int newMinLoadRetryCount) {
        minLoadRetryCount = newMinLoadRetryCount;
        releasePlayer();
        initializePlayer();
    }

    public void setPlayInBackground(boolean playInBackground) {
        this.playInBackground = playInBackground;
    }

    public void setDisableFocus(boolean disableFocus) {
        this.disableFocus = disableFocus;
    }

    public void setFocusable(boolean focusable) {
        this.focusable = focusable;
        exoPlayerView.setFocusable(this.focusable);
    }

    public void setContentStartTime(int contentStartTime) {
        this.contentStartTime = contentStartTime;
    }

    public void setShowNotificationControls(boolean showNotificationControls) {
        this.showNotificationControls = showNotificationControls;

        if (playbackServiceConnection == null && showNotificationControls) {
            setupPlaybackService();
        } else if(!showNotificationControls && playbackServiceConnection != null) {
            cleanupPlaybackService();
        }
    }

    public void setBufferingStrategy(BufferingStrategy.BufferingStrategyEnum _bufferingStrategy) {
        bufferingStrategy = _bufferingStrategy;
    }

    public boolean getPreventsDisplaySleepDuringVideoPlayback() {
        return preventsDisplaySleepDuringVideoPlayback;
    }

    private void updateFullScreenButtonVisibility() {
        if (playerControlView != null) {
            final ImageButton fullScreenButton = playerControlView.findViewById(R.id.exo_fullscreen);
            //Handling the fullScreenButton click event
            if (isFullscreen && fullScreenPlayerView != null && !fullScreenPlayerView.isShowing()) {
                fullScreenButton.setVisibility(GONE);
            } else {
                fullScreenButton.setVisibility(VISIBLE);
            }
        }
    }

    public void setDisableDisconnectError(boolean disableDisconnectError) {
        this.disableDisconnectError = disableDisconnectError;
    }

    public void setFullscreen(boolean fullscreen) {
        if (fullscreen == isFullscreen) {
            return; // Avoid generating events when nothing is changing
        }
        isFullscreen = fullscreen;

        Activity activity = themedReactContext.getCurrentActivity();
        if (activity == null) {
            return;
        }

        Window window = activity.getWindow();
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, window.getDecorView());
        if (isFullscreen) {
            eventEmitter.fullscreenWillPresent();
            if (fullScreenPlayerView != null) {
                fullScreenPlayerView.show();
            }
            UiThreadUtil.runOnUiThread(() -> {
                WindowCompat.setDecorFitsSystemWindows(window, false);
                controller.hide(WindowInsetsCompat.Type.systemBars());
                controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                eventEmitter.fullscreenDidPresent();
            });
        } else {
            eventEmitter.fullscreenWillDismiss();
            if (fullScreenPlayerView != null) {
                fullScreenPlayerView.dismiss();
                reLayoutControls();
                setControls(controls);
            }
            UiThreadUtil.runOnUiThread(() -> {
                WindowCompat.setDecorFitsSystemWindows(window, true);
                controller.show(WindowInsetsCompat.Type.systemBars());
                eventEmitter.fullscreenDidDismiss();
            });
        }
        // need to be done at the end to avoid hiding fullscreen control button when fullScreenPlayerView is shown
        updateFullScreenButtonVisibility();
    }

    public void setHideShutterView(boolean hideShutterView) {
        exoPlayerView.setHideShutterView(hideShutterView);
    }

    public void setBufferConfig(BufferConfig config) {
        bufferConfig = config;
        if (bufferConfig.getCacheSize() > 0) {
            RNVSimpleCache.INSTANCE.setSimpleCache(
                    this.getContext(),
                    bufferConfig.getCacheSize()
            );
            useCache = true;
        } else {
            useCache = false;
        }
        releasePlayer();
        initializePlayer();
    }

    @Override
    public void onDrmKeysLoaded(int windowIndex, MediaSource.MediaPeriodId mediaPeriodId) {
        DebugLog.d("DRM Info", "onDrmKeysLoaded");
    }

    @Override
    public void onDrmSessionAcquired(int windowIndex, MediaSource.MediaPeriodId mediaPeriodId, int state) {
        DebugLog.d("DRM Info", "onDrmSessionAcquired");
    }

    @Override
    public void onDrmSessionReleased(int windowIndex, MediaSource.MediaPeriodId mediaPeriodId) {
        DebugLog.d("DRM Info", "onDrmSessionReleased");
    }

    @Override
    public void onDrmSessionManagerError(int windowIndex, MediaSource.MediaPeriodId mediaPeriodId, @NonNull Exception e) {
        DebugLog.d("DRM Info", "onDrmSessionManagerError");
        eventEmitter.error("onDrmSessionManagerError", e, "3002");
    }

    @Override
    public void onDrmKeysRestored(int windowIndex, MediaSource.MediaPeriodId mediaPeriodId) {
        DebugLog.d("DRM Info", "onDrmKeysRestored");
    }

    @Override
    public void onDrmKeysRemoved(int windowIndex, MediaSource.MediaPeriodId mediaPeriodId) {
        DebugLog.d("DRM Info", "onDrmKeysRemoved");
    }

    /**
     * Handling controls prop
     *
     * @param controls  Controls prop, if true enable controls, if false disable them
     */
    public void setControls(boolean controls) {
        this.controls = controls;
        if (controls) {
            addPlayerControl();
            updateFullScreenButtonVisibility();
        } else {
            int indexOfPC = indexOfChild(playerControlView);
            if (indexOfPC != -1) {
                removeViewAt(indexOfPC);
            }
        }
    }

    public void setSubtitleStyle(SubtitleStyle style) {
        exoPlayerView.setSubtitleStyle(style);
    }

    public void setShutterColor(Integer color) {
        exoPlayerView.setShutterColor(color);
    }

    @Override
    public void onAdEvent(AdEvent adEvent) {
        if (adEvent.getAdData() != null) {
            eventEmitter.receiveAdEvent(adEvent.getType().name(), adEvent.getAdData());
        } else {
            eventEmitter.receiveAdEvent(adEvent.getType().name());
        }
    }

    @Override
    public void onAdError(AdErrorEvent adErrorEvent) {
        AdError error = adErrorEvent.getError();
        eventEmitter.receiveAdErrorEvent(error.getMessage(), String.valueOf(error.getErrorCode()), String.valueOf(error.getErrorType()));
    }

    public void setControlsStyles(ControlsConfig controlsStyles) {
        controlsConfig = controlsStyles;
        refreshProgressBarVisibility();
    }
}
