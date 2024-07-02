package com.brentvatne.exoplayer;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.media3.common.util.Util;

import com.brentvatne.common.api.BufferConfig;
import com.brentvatne.common.api.BufferingStrategy;
import com.brentvatne.common.api.ControlsConfig;
import com.brentvatne.common.api.DRMProps;
import com.brentvatne.common.api.ResizeMode;
import com.brentvatne.common.api.SideLoadedTextTrackList;
import com.brentvatne.common.api.Source;
import com.brentvatne.common.api.SubtitleStyle;
import com.brentvatne.common.api.ViewType;
import com.brentvatne.common.react.VideoEventEmitter;
import com.brentvatne.common.toolbox.DebugLog;
import com.brentvatne.common.toolbox.ReactBridgeUtils;
import com.brentvatne.react.ReactNativeVideoManager;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

public class ReactExoplayerViewManager extends ViewGroupManager<ReactExoplayerView> {

    private static final String TAG = "ExoViewManager";
    private static final String REACT_CLASS = "RCTVideo";
    private static final String PROP_SRC = "src";
    private static final String PROP_AD_TAG_URL = "adTagUrl";
    private static final String PROP_DRM = "drm";
    private static final String PROP_SRC_HEADERS = "requestHeaders";
    private static final String PROP_RESIZE_MODE = "resizeMode";
    private static final String PROP_REPEAT = "repeat";
    private static final String PROP_SELECTED_AUDIO_TRACK = "selectedAudioTrack";
    private static final String PROP_SELECTED_AUDIO_TRACK_TYPE = "type";
    private static final String PROP_SELECTED_AUDIO_TRACK_VALUE = "value";
    private static final String PROP_SELECTED_TEXT_TRACK = "selectedTextTrack";
    private static final String PROP_SELECTED_TEXT_TRACK_TYPE = "type";
    private static final String PROP_SELECTED_TEXT_TRACK_VALUE = "value";
    private static final String PROP_TEXT_TRACKS = "textTracks";
    private static final String PROP_PAUSED = "paused";
    private static final String PROP_MUTED = "muted";
    private static final String PROP_AUDIO_OUTPUT = "audioOutput";
    private static final String PROP_VOLUME = "volume";
    private static final String PROP_BUFFER_CONFIG = "bufferConfig";
    private static final String PROP_PREVENTS_DISPLAY_SLEEP_DURING_VIDEO_PLAYBACK = "preventsDisplaySleepDuringVideoPlayback";
    private static final String PROP_PROGRESS_UPDATE_INTERVAL = "progressUpdateInterval";
    private static final String PROP_REPORT_BANDWIDTH = "reportBandwidth";
    private static final String PROP_RATE = "rate";
    private static final String PROP_MIN_LOAD_RETRY_COUNT = "minLoadRetryCount";
    private static final String PROP_MAXIMUM_BIT_RATE = "maxBitRate";
    private static final String PROP_PLAY_IN_BACKGROUND = "playInBackground";
    private static final String PROP_CONTENT_START_TIME = "contentStartTime";
    private static final String PROP_DISABLE_FOCUS = "disableFocus";
    private static final String PROP_BUFFERING_STRATEGY = "bufferingStrategy";
    private static final String PROP_DISABLE_DISCONNECT_ERROR = "disableDisconnectError";
    private static final String PROP_FOCUSABLE = "focusable";
    private static final String PROP_FULLSCREEN = "fullscreen";
    private static final String PROP_VIEW_TYPE = "viewType";
    private static final String PROP_SELECTED_VIDEO_TRACK = "selectedVideoTrack";
    private static final String PROP_SELECTED_VIDEO_TRACK_TYPE = "type";
    private static final String PROP_SELECTED_VIDEO_TRACK_VALUE = "value";
    private static final String PROP_HIDE_SHUTTER_VIEW = "hideShutterView";
    private static final String PROP_CONTROLS = "controls";
    private static final String PROP_SUBTITLE_STYLE = "subtitleStyle";
    private static final String PROP_SHUTTER_COLOR = "shutterColor";
    private static final String PROP_SHOW_NOTIFICATION_CONTROLS = "showNotificationControls";
    private static final String PROP_DEBUG = "debug";
    private static final String PROP_CONTROLS_STYLES = "controlsStyles";


    private final ReactExoplayerConfig config;

    public ReactExoplayerViewManager(ReactExoplayerConfig config) {
        this.config = config;
    }

    @NonNull
    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @NonNull
    @Override
    protected ReactExoplayerView createViewInstance(@NonNull ThemedReactContext themedReactContext) {
        ReactNativeVideoManager.Companion.getInstance().registerView(this);
        return new ReactExoplayerView(themedReactContext, config);
    }

    @Override
    public void onDropViewInstance(ReactExoplayerView view) {
        view.cleanUpResources();
        ReactNativeVideoManager.Companion.getInstance().unregisterView(this);
    }

    @Override
    public @Nullable Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        MapBuilder.Builder<String, Object> builder = MapBuilder.builder();
        for (String event : VideoEventEmitter.Events) {
            builder.put(event, MapBuilder.of("registrationName", event));
        }
        return builder.build();
    }

    @ReactProp(name = PROP_DRM)
    public void setDRM(final ReactExoplayerView videoView, @Nullable ReadableMap drm) {
        DRMProps drmProps = DRMProps.parse(drm);
        videoView.setDrm(drmProps);
    }

    @ReactProp(name = PROP_SRC)
    public void setSrc(final ReactExoplayerView videoView, @Nullable ReadableMap src) {
        Context context = videoView.getContext().getApplicationContext();
        Source source = Source.parse(src, context);
        if (source.getUri() == null) {
            videoView.clearSrc();
        } else {
            videoView.setSrc(source);
        }
    }

    @ReactProp(name = PROP_AD_TAG_URL)
    public void setAdTagUrl(final ReactExoplayerView videoView, final String uriString) {
        if (TextUtils.isEmpty(uriString)) {
            videoView.setAdTagUrl(null);
            return;
        }

        Uri adTagUrl = Uri.parse(uriString);

        videoView.setAdTagUrl(adTagUrl);
    }

    @ReactProp(name = PROP_RESIZE_MODE)
    public void setResizeMode(final ReactExoplayerView videoView, final String resizeMode) {
        switch (resizeMode) {
            case "none":
            case "contain":
                videoView.setResizeModeModifier(ResizeMode.RESIZE_MODE_FIT);
                break;
            case "cover":
                videoView.setResizeModeModifier(ResizeMode.RESIZE_MODE_CENTER_CROP);
                break;
            case "stretch":
                videoView.setResizeModeModifier(ResizeMode.RESIZE_MODE_FILL);
                break;
            default:
                DebugLog.w(TAG, "Unsupported resize mode: " + resizeMode + " - falling back to fit");
                videoView.setResizeModeModifier(ResizeMode.RESIZE_MODE_FIT);
                break;
        }
    }

    @ReactProp(name = PROP_REPEAT, defaultBoolean = false)
    public void setRepeat(final ReactExoplayerView videoView, final boolean repeat) {
        videoView.setRepeatModifier(repeat);
    }

    @ReactProp(name = PROP_PREVENTS_DISPLAY_SLEEP_DURING_VIDEO_PLAYBACK, defaultBoolean = false)
    public void setPreventsDisplaySleepDuringVideoPlayback(final ReactExoplayerView videoView, final boolean preventsSleep) {
        videoView.setPreventsDisplaySleepDuringVideoPlayback(preventsSleep);
    }

    @ReactProp(name = PROP_SELECTED_VIDEO_TRACK)
    public void setSelectedVideoTrack(final ReactExoplayerView videoView,
                                     @Nullable ReadableMap selectedVideoTrack) {
        String typeString = null;
        String value = null;
        if (selectedVideoTrack != null) {
            typeString = ReactBridgeUtils.safeGetString(selectedVideoTrack, PROP_SELECTED_VIDEO_TRACK_TYPE);
            value = ReactBridgeUtils.safeGetString(selectedVideoTrack, PROP_SELECTED_VIDEO_TRACK_VALUE);
        }
        videoView.setSelectedVideoTrack(typeString, value);
    }

    @ReactProp(name = PROP_SELECTED_AUDIO_TRACK)
    public void setSelectedAudioTrack(final ReactExoplayerView videoView,
                                     @Nullable ReadableMap selectedAudioTrack) {
        String typeString = null;
        String value = null;
        if (selectedAudioTrack != null) {
            typeString = ReactBridgeUtils.safeGetString(selectedAudioTrack, PROP_SELECTED_AUDIO_TRACK_TYPE);
            value = ReactBridgeUtils.safeGetString(selectedAudioTrack, PROP_SELECTED_AUDIO_TRACK_VALUE);
        }
        videoView.setSelectedAudioTrack(typeString, value);
    }

    @ReactProp(name = PROP_SELECTED_TEXT_TRACK)
    public void setSelectedTextTrack(final ReactExoplayerView videoView,
                                     @Nullable ReadableMap selectedTextTrack) {
        String typeString = null;
        String value = null;
        if (selectedTextTrack != null) {
            typeString = ReactBridgeUtils.safeGetString(selectedTextTrack, PROP_SELECTED_TEXT_TRACK_TYPE);
            value = ReactBridgeUtils.safeGetString(selectedTextTrack, PROP_SELECTED_TEXT_TRACK_VALUE);
        }
        videoView.setSelectedTextTrack(typeString, value);
    }

    @ReactProp(name = PROP_TEXT_TRACKS)
    public void setPropTextTracks(final ReactExoplayerView videoView,
                                  @Nullable ReadableArray textTracks) {
        SideLoadedTextTrackList sideLoadedTextTracks = SideLoadedTextTrackList.Companion.parse(textTracks);
        videoView.setTextTracks(sideLoadedTextTracks);
    }

    @ReactProp(name = PROP_PAUSED, defaultBoolean = false)
    public void setPaused(final ReactExoplayerView videoView, final boolean paused) {
        videoView.setPausedModifier(paused);
    }

    @ReactProp(name = PROP_MUTED, defaultBoolean = false)
    public void setMuted(final ReactExoplayerView videoView, final boolean muted) {
        videoView.setMutedModifier(muted);
    }

    @ReactProp(name = PROP_AUDIO_OUTPUT)
    public void setAudioOutput(final ReactExoplayerView videoView, final String audioOutput) {
        videoView.setAudioOutput(AudioOutput.get(audioOutput));
    }

    @ReactProp(name = PROP_VOLUME, defaultFloat = 1.0f)
    public void setVolume(final ReactExoplayerView videoView, final float volume) {
        videoView.setVolumeModifier(volume);
    }

    @ReactProp(name = PROP_PROGRESS_UPDATE_INTERVAL, defaultFloat = 250.0f)
    public void setProgressUpdateInterval(final ReactExoplayerView videoView, final float progressUpdateInterval) {
        videoView.setProgressUpdateInterval(progressUpdateInterval);
    }

    @ReactProp(name = PROP_REPORT_BANDWIDTH, defaultBoolean = false)
    public void setReportBandwidth(final ReactExoplayerView videoView, final boolean reportBandwidth) {
        videoView.setReportBandwidth(reportBandwidth);
    }

    @ReactProp(name = PROP_RATE)
    public void setRate(final ReactExoplayerView videoView, final float rate) {
        videoView.setRateModifier(rate);
    }

    @ReactProp(name = PROP_MAXIMUM_BIT_RATE)
    public void setMaxBitRate(final ReactExoplayerView videoView, final int maxBitRate) {
        videoView.setMaxBitRateModifier(maxBitRate);
    }

    @ReactProp(name = PROP_MIN_LOAD_RETRY_COUNT)
    public void minLoadRetryCount(final ReactExoplayerView videoView, final int minLoadRetryCount) {
        videoView.setMinLoadRetryCountModifier(minLoadRetryCount);
    }

    @ReactProp(name = PROP_PLAY_IN_BACKGROUND, defaultBoolean = false)
    public void setPlayInBackground(final ReactExoplayerView videoView, final boolean playInBackground) {
        videoView.setPlayInBackground(playInBackground);
    }

    @ReactProp(name = PROP_DISABLE_FOCUS, defaultBoolean = false)
    public void setDisableFocus(final ReactExoplayerView videoView, final boolean disableFocus) {
        videoView.setDisableFocus(disableFocus);
    }

    @ReactProp(name = PROP_FOCUSABLE, defaultBoolean = true)
    public void setFocusable(final ReactExoplayerView videoView, final boolean focusable) {
        videoView.setFocusable(focusable);
    }

    @ReactProp(name = PROP_CONTENT_START_TIME, defaultInt = -1)
    public void setContentStartTime(final ReactExoplayerView videoView, final int contentStartTime) {
        videoView.setContentStartTime(contentStartTime);
    }

    @ReactProp(name = PROP_BUFFERING_STRATEGY)
    public void setBufferingStrategy(final ReactExoplayerView videoView, final String bufferingStrategy) {
        BufferingStrategy.BufferingStrategyEnum strategy = BufferingStrategy.Companion.parse(bufferingStrategy);
        videoView.setBufferingStrategy(strategy);
    }

    @ReactProp(name = PROP_DISABLE_DISCONNECT_ERROR, defaultBoolean = false)
    public void setDisableDisconnectError(final ReactExoplayerView videoView, final boolean disableDisconnectError) {
        videoView.setDisableDisconnectError(disableDisconnectError);
    }

    @ReactProp(name = PROP_FULLSCREEN, defaultBoolean = false)
    public void setFullscreen(final ReactExoplayerView videoView, final boolean fullscreen) {
        videoView.updateFullScreenProp();
    }

    @ReactProp(name = PROP_VIEW_TYPE, defaultInt = ViewType.VIEW_TYPE_SURFACE)
    public void setViewType(final ReactExoplayerView videoView, final int viewType) {
        videoView.setViewType(viewType);
    }

    @ReactProp(name = PROP_HIDE_SHUTTER_VIEW, defaultBoolean = false)
    public void setHideShutterView(final ReactExoplayerView videoView, final boolean hideShutterView) {
        videoView.setHideShutterView(hideShutterView);
    }

    @ReactProp(name = PROP_CONTROLS, defaultBoolean = false)
    public void setControls(final ReactExoplayerView videoView, final boolean controls) {
        videoView.setControls(controls);
    }

    @ReactProp(name = PROP_SUBTITLE_STYLE)
    public void setSubtitleStyle(final ReactExoplayerView videoView, @Nullable final ReadableMap src) {
        videoView.setSubtitleStyle(SubtitleStyle.parse(src));
    }

    @ReactProp(name = PROP_SHUTTER_COLOR, customType = "Color")
    public void setShutterColor(final ReactExoplayerView videoView, final Integer color) {
        videoView.setShutterColor(color == null ? Color.BLACK : color);
    }


    @ReactProp(name = PROP_BUFFER_CONFIG)
    public void setBufferConfig(final ReactExoplayerView videoView, @Nullable ReadableMap bufferConfig) {
        BufferConfig config = BufferConfig.parse(bufferConfig);
        videoView.setBufferConfig(config);
    }

    @ReactProp(name = PROP_SHOW_NOTIFICATION_CONTROLS)
    public void setShowNotificationControls(final ReactExoplayerView videoView, final boolean showNotificationControls) {
        videoView.setShowNotificationControls(showNotificationControls);
    }

    @ReactProp(name = PROP_DEBUG, defaultBoolean = false)
    public void setDebug(final ReactExoplayerView videoView,
                         @Nullable final ReadableMap debugConfig) {
        boolean enableDebug = ReactBridgeUtils.safeGetBool(debugConfig, "enable", false);
        boolean enableThreadDebug = ReactBridgeUtils.safeGetBool(debugConfig, "thread", false);
        if (enableDebug) {
            DebugLog.setConfig(Log.VERBOSE, enableThreadDebug);
        } else {
            DebugLog.setConfig(Log.WARN, enableThreadDebug);
        }
        videoView.setDebug(enableDebug);
    }

    @ReactProp(name = PROP_CONTROLS_STYLES)
    public void setControlsStyles(final ReactExoplayerView videoView, @Nullable ReadableMap controlsStyles) {
        ControlsConfig controlsConfig = ControlsConfig.parse(controlsStyles);
        videoView.setControlsStyles(controlsConfig);
    }
}
