package com.brentvatne.exoplayer;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.media3.common.util.Util;
import androidx.media3.datasource.RawResourceDataSource;
import androidx.media3.exoplayer.DefaultLoadControl;

import com.brentvatne.common.api.ResizeMode;
import com.brentvatne.common.api.SubtitleStyle;
import com.brentvatne.common.react.VideoEventEmitter;
import com.brentvatne.common.toolbox.DebugLog;
import com.brentvatne.common.toolbox.ReactBridgeUtils;
import com.facebook.react.bridge.Dynamic;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

public class ReactExoplayerViewManager extends ViewGroupManager<ReactExoplayerView> {

    private static final String REACT_CLASS = "RCTVideo";
    private static final String PROP_SRC = "src";
    private static final String PROP_SRC_URI = "uri";
    private static final String PROP_SRC_START_POSITION = "startPosition";
    private static final String PROP_SRC_CROP_START = "cropStart";
    private static final String PROP_SRC_CROP_END = "cropEnd";
    private static final String PROP_AD_TAG_URL = "adTagUrl";
    private static final String PROP_SRC_TYPE = "type";
    private static final String PROP_DRM = "drm";
    private static final String PROP_DRM_TYPE = "type";
    private static final String PROP_DRM_LICENSESERVER = "licenseServer";
    private static final String PROP_DRM_HEADERS = "headers";
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
    private static final String PROP_BACK_BUFFER_DURATION_MS = "backBufferDurationMs";
    private static final String PROP_BUFFER_CONFIG = "bufferConfig";
    private static final String PROP_BUFFER_CONFIG_MIN_BUFFER_MS = "minBufferMs";
    private static final String PROP_BUFFER_CONFIG_MAX_BUFFER_MS = "maxBufferMs";
    private static final String PROP_BUFFER_CONFIG_BUFFER_FOR_PLAYBACK_MS = "bufferForPlaybackMs";
    private static final String PROP_BUFFER_CONFIG_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS = "bufferForPlaybackAfterRebufferMs";
    private static final String PROP_BUFFER_CONFIG_MAX_HEAP_ALLOCATION_PERCENT = "maxHeapAllocationPercent";
    private static final String PROP_BUFFER_CONFIG_MIN_BACK_BUFFER_MEMORY_RESERVE_PERCENT = "minBackBufferMemoryReservePercent";
    private static final String PROP_BUFFER_CONFIG_MIN_BUFFER_MEMORY_RESERVE_PERCENT = "minBufferMemoryReservePercent";
    private static final String PROP_PREVENTS_DISPLAY_SLEEP_DURING_VIDEO_PLAYBACK = "preventsDisplaySleepDuringVideoPlayback";
    private static final String PROP_PROGRESS_UPDATE_INTERVAL = "progressUpdateInterval";
    private static final String PROP_REPORT_BANDWIDTH = "reportBandwidth";
    private static final String PROP_SEEK = "seek";
    private static final String PROP_RATE = "rate";
    private static final String PROP_MIN_LOAD_RETRY_COUNT = "minLoadRetryCount";
    private static final String PROP_MAXIMUM_BIT_RATE = "maxBitRate";
    private static final String PROP_PLAY_IN_BACKGROUND = "playInBackground";
    private static final String PROP_CONTENT_START_TIME = "contentStartTime";
    private static final String PROP_DISABLE_FOCUS = "disableFocus";
    private static final String PROP_DISABLE_BUFFERING = "disableBuffering";
    private static final String PROP_DISABLE_DISCONNECT_ERROR = "disableDisconnectError";
    private static final String PROP_FOCUSABLE = "focusable";
    private static final String PROP_FULLSCREEN = "fullscreen";
    private static final String PROP_USE_TEXTURE_VIEW = "useTextureView";
    private static final String PROP_SECURE_VIEW = "useSecureView";
    private static final String PROP_SELECTED_VIDEO_TRACK = "selectedVideoTrack";
    private static final String PROP_SELECTED_VIDEO_TRACK_TYPE = "type";
    private static final String PROP_SELECTED_VIDEO_TRACK_VALUE = "value";
    private static final String PROP_HIDE_SHUTTER_VIEW = "hideShutterView";
    private static final String PROP_CONTROLS = "controls";
    private static final String PROP_SUBTITLE_STYLE = "subtitleStyle";
    private static final String PROP_SHUTTER_COLOR = "shutterColor";
    private static final String PROP_MEDIA_SESSION = "mediaSession";
    private static final String PROP_DEBUG = "debug";

    private ReactExoplayerConfig config;

    public ReactExoplayerViewManager(ReactExoplayerConfig config) {
        this.config = config;
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected ReactExoplayerView createViewInstance(ThemedReactContext themedReactContext) {
        return new ReactExoplayerView(themedReactContext, config);
    }

    @Override
    public void onDropViewInstance(ReactExoplayerView view) {
        view.cleanUpResources();
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
        if (drm != null && drm.hasKey(PROP_DRM_TYPE)) {
            String drmType = ReactBridgeUtils.safeGetString(drm, PROP_DRM_TYPE);
            String drmLicenseServer = ReactBridgeUtils.safeGetString(drm, PROP_DRM_LICENSESERVER);
            ReadableArray drmHeadersArray = ReactBridgeUtils.safeGetArray(drm, PROP_DRM_HEADERS);
            if (drmType != null && drmLicenseServer != null && Util.getDrmUuid(drmType) != null) {
                UUID drmUUID = Util.getDrmUuid(drmType);
                videoView.setDrmType(drmUUID);
                videoView.setDrmLicenseUrl(drmLicenseServer);
                if (drmHeadersArray != null) {
                    ArrayList<String> drmKeyRequestPropertiesList = new ArrayList<>();
                    for (int i = 0; i < drmHeadersArray.size(); i++) {
                        ReadableMap current = drmHeadersArray.getMap(i);
                        String key = current.hasKey("key") ? current.getString("key") : null;
                        String value = current.hasKey("value") ? current.getString("value") : null;
                        drmKeyRequestPropertiesList.add(key);
                        drmKeyRequestPropertiesList.add(value);
                    }
                    videoView.setDrmLicenseHeader(drmKeyRequestPropertiesList.toArray(new String[0]));
                }
                videoView.setUseTextureView(false);
            }
        }
    }

    @ReactProp(name = PROP_SRC)
    public void setSrc(final ReactExoplayerView videoView, @Nullable ReadableMap src) {
        Context context = videoView.getContext().getApplicationContext();
        String uriString = ReactBridgeUtils.safeGetString(src, PROP_SRC_URI, null);
        int startPositionMs = ReactBridgeUtils.safeGetInt(src, PROP_SRC_START_POSITION, -1);
        int cropStartMs = ReactBridgeUtils.safeGetInt(src, PROP_SRC_CROP_START, -1);
        int cropEndMs = ReactBridgeUtils.safeGetInt(src, PROP_SRC_CROP_END, -1);
        String extension = ReactBridgeUtils.safeGetString(src, PROP_SRC_TYPE, null);

        Map<String, String> headers = new HashMap<>();
        ReadableArray propSrcHeadersArray = ReactBridgeUtils.safeGetArray(src, PROP_SRC_HEADERS);
        if (propSrcHeadersArray != null) {
            if (propSrcHeadersArray.size() > 0) {
                for (int i = 0; i < propSrcHeadersArray.size(); i++) {
                    ReadableMap current = propSrcHeadersArray.getMap(i);
                    String key = current.hasKey("key") ? current.getString("key") : null;
                    String value = current.hasKey("value") ? current.getString("value") : null;
                    if (key != null && value != null) {
                        headers.put(key, value);
                    }
                }
            }
        }

        if (TextUtils.isEmpty(uriString)) {
            videoView.clearSrc();
            return;
        }

        if (startsWithValidScheme(uriString)) {
            Uri srcUri = Uri.parse(uriString);

            if (srcUri != null) {
                videoView.setSrc(srcUri, startPositionMs, cropStartMs, cropEndMs, extension, headers);
            }
        } else {
            int identifier = context.getResources().getIdentifier(
                uriString,
                "drawable",
                context.getPackageName()
            );
            if (identifier == 0) {
                identifier = context.getResources().getIdentifier(
                    uriString,
                    "raw",
                    context.getPackageName()
                );
            }
            if (identifier > 0) {
                Uri srcUri = RawResourceDataSource.buildRawResourceUri(identifier);
                if (srcUri != null) {
                    videoView.setRawSrc(srcUri, extension);
                }
            } else {
                videoView.clearSrc();
            }
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
                DebugLog.w("ExoPlayer Warning", "Unsupported resize mode: " + resizeMode + " - falling back to fit");
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
        videoView.setTextTracks(textTracks);
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

    @ReactProp(name = PROP_SEEK)
    public void setSeek(final ReactExoplayerView videoView, final float seek) {
        videoView.seekTo(Math.round(seek * 1000f));
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

    @ReactProp(name = PROP_BACK_BUFFER_DURATION_MS, defaultInt = 0)
    public void setBackBufferDurationMs(final ReactExoplayerView videoView, final int backBufferDurationMs) {
        videoView.setBackBufferDurationMs(backBufferDurationMs);
    }

    @ReactProp(name = PROP_CONTENT_START_TIME, defaultInt = -1)
    public void setContentStartTime(final ReactExoplayerView videoView, final int contentStartTime) {
        videoView.setContentStartTime(contentStartTime);
    }

    @ReactProp(name = PROP_DISABLE_BUFFERING, defaultBoolean = false)
    public void setDisableBuffering(final ReactExoplayerView videoView, final boolean disableBuffering) {
        videoView.setDisableBuffering(disableBuffering);
    }

    @ReactProp(name = PROP_DISABLE_DISCONNECT_ERROR, defaultBoolean = false)
    public void setDisableDisconnectError(final ReactExoplayerView videoView, final boolean disableDisconnectError) {
        videoView.setDisableDisconnectError(disableDisconnectError);
    }

    @ReactProp(name = PROP_FULLSCREEN, defaultBoolean = false)
    public void setFullscreen(final ReactExoplayerView videoView, final boolean fullscreen) {
        videoView.setFullscreen(fullscreen);
    }

    @ReactProp(name = PROP_USE_TEXTURE_VIEW, defaultBoolean = true)
    public void setUseTextureView(final ReactExoplayerView videoView, final boolean useTextureView) {
        videoView.setUseTextureView(useTextureView);
    }

    @ReactProp(name = PROP_SECURE_VIEW, defaultBoolean = true)
    public void useSecureView(final ReactExoplayerView videoView, final boolean useSecureView) {
        videoView.useSecureView(useSecureView);
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
        int minBufferMs = DefaultLoadControl.DEFAULT_MIN_BUFFER_MS;
        int maxBufferMs = DefaultLoadControl.DEFAULT_MAX_BUFFER_MS;
        int bufferForPlaybackMs = DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS;
        int bufferForPlaybackAfterRebufferMs = DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS;
        double maxHeapAllocationPercent = ReactExoplayerView.DEFAULT_MAX_HEAP_ALLOCATION_PERCENT;
        double minBackBufferMemoryReservePercent = ReactExoplayerView.DEFAULT_MIN_BACK_BUFFER_MEMORY_RESERVE;
        double minBufferMemoryReservePercent = ReactExoplayerView.DEFAULT_MIN_BUFFER_MEMORY_RESERVE;

        if (bufferConfig != null) {
            minBufferMs = ReactBridgeUtils.safeGetInt(bufferConfig, PROP_BUFFER_CONFIG_MIN_BUFFER_MS, minBufferMs);
            maxBufferMs = ReactBridgeUtils.safeGetInt(bufferConfig, PROP_BUFFER_CONFIG_MAX_BUFFER_MS, maxBufferMs);
            bufferForPlaybackMs = ReactBridgeUtils.safeGetInt(bufferConfig, PROP_BUFFER_CONFIG_BUFFER_FOR_PLAYBACK_MS, bufferForPlaybackMs);
            bufferForPlaybackAfterRebufferMs = ReactBridgeUtils.safeGetInt(bufferConfig, PROP_BUFFER_CONFIG_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS, bufferForPlaybackAfterRebufferMs);
            maxHeapAllocationPercent = ReactBridgeUtils.safeGetDouble(bufferConfig, PROP_BUFFER_CONFIG_MAX_HEAP_ALLOCATION_PERCENT, maxHeapAllocationPercent);
            minBackBufferMemoryReservePercent = ReactBridgeUtils.safeGetDouble(bufferConfig, PROP_BUFFER_CONFIG_MIN_BACK_BUFFER_MEMORY_RESERVE_PERCENT, minBackBufferMemoryReservePercent);
            minBufferMemoryReservePercent = ReactBridgeUtils.safeGetDouble(bufferConfig, PROP_BUFFER_CONFIG_MIN_BUFFER_MEMORY_RESERVE_PERCENT, minBufferMemoryReservePercent);
            videoView.setBufferConfig(minBufferMs, maxBufferMs, bufferForPlaybackMs, bufferForPlaybackAfterRebufferMs, maxHeapAllocationPercent, minBackBufferMemoryReservePercent, minBufferMemoryReservePercent);
        }
    }

    @ReactProp(name = PROP_MEDIA_SESSION)
    public void setMediaSession(final ReactExoplayerView videoView, @Nullable ReadableMap mediaSessionMetadata) {
      if (mediaSessionMetadata == null) return;

      String title = mediaSessionMetadata.getString("title");
      String subtitle = mediaSessionMetadata.getString("subtitle");
      String description = mediaSessionMetadata.getString("description");
      String imageUri = mediaSessionMetadata.getString("imageUri");

      if (title != null) { videoView.setMediaSessionTitle(title); }
      if (subtitle != null) { videoView.setMediaSessionSubtitle(subtitle); }
      if (description != null) { videoView.setMediaSessionDescription(description); }
      if (imageUri != null) { videoView.setMediaSessionImage(imageUri); }
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
    }

    private boolean startsWithValidScheme(String uriString) {
        String lowerCaseUri = uriString.toLowerCase();
        return lowerCaseUri.startsWith("http://")
                || lowerCaseUri.startsWith("https://")
                || lowerCaseUri.startsWith("content://")
                || lowerCaseUri.startsWith("file://")
                || lowerCaseUri.startsWith("asset://");
    }
}
