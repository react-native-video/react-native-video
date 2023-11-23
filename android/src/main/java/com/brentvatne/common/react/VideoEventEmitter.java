package com.brentvatne.common.react;

import androidx.annotation.StringDef;

import android.view.View;

import com.brentvatne.common.API.TimedMetadata;
import com.brentvatne.common.API.Track;
import com.brentvatne.common.API.VideoTrack;
import com.brentvatne.common.toolbox.DebugLog;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

public class VideoEventEmitter {

    private final RCTEventEmitter eventEmitter;

    private final String TAG = "VideoEventEmitter";

    private int viewId = View.NO_ID;

    public VideoEventEmitter(ReactContext reactContext) {
        this.eventEmitter = reactContext.getJSModule(RCTEventEmitter.class);
    }

    private static final String EVENT_LOAD_START = "onVideoLoadStart";
    private static final String EVENT_LOAD = "onVideoLoad";
    private static final String EVENT_ERROR = "onVideoError";
    private static final String EVENT_PROGRESS = "onVideoProgress";
    private static final String EVENT_BANDWIDTH = "onVideoBandwidthUpdate";
    private static final String EVENT_SEEK = "onVideoSeek";
    private static final String EVENT_END = "onVideoEnd";
    private static final String EVENT_FULLSCREEN_WILL_PRESENT = "onVideoFullscreenPlayerWillPresent";
    private static final String EVENT_FULLSCREEN_DID_PRESENT = "onVideoFullscreenPlayerDidPresent";
    private static final String EVENT_FULLSCREEN_WILL_DISMISS = "onVideoFullscreenPlayerWillDismiss";
    private static final String EVENT_FULLSCREEN_DID_DISMISS = "onVideoFullscreenPlayerDidDismiss";

    private static final String EVENT_STALLED = "onPlaybackStalled";
    private static final String EVENT_RESUME = "onPlaybackResume";
    private static final String EVENT_READY = "onReadyForDisplay";
    private static final String EVENT_BUFFER = "onVideoBuffer";
    private static final String EVENT_PLAYBACK_STATE_CHANGED = "onVideoPlaybackStateChanged";
    private static final String EVENT_IDLE = "onVideoIdle";
    private static final String EVENT_TIMED_METADATA = "onTimedMetadata";
    private static final String EVENT_AUDIO_BECOMING_NOISY = "onVideoAudioBecomingNoisy";
    private static final String EVENT_AUDIO_FOCUS_CHANGE = "onAudioFocusChanged";
    private static final String EVENT_PLAYBACK_RATE_CHANGE = "onPlaybackRateChange";
    private static final String EVENT_VOLUME_CHANGE = "onVolumeChange";
    private static final String EVENT_AUDIO_TRACKS = "onAudioTracks";
    private static final String EVENT_TEXT_TRACKS = "onTextTracks";
    private static final String EVENT_VIDEO_TRACKS = "onVideoTracks";
    private static final String EVENT_ON_RECEIVE_AD_EVENT = "onReceiveAdEvent";

    static public final String[] Events = {
            EVENT_LOAD_START,
            EVENT_LOAD,
            EVENT_ERROR,
            EVENT_PROGRESS,
            EVENT_SEEK,
            EVENT_END,
            EVENT_FULLSCREEN_WILL_PRESENT,
            EVENT_FULLSCREEN_DID_PRESENT,
            EVENT_FULLSCREEN_WILL_DISMISS,
            EVENT_FULLSCREEN_DID_DISMISS,
            EVENT_STALLED,
            EVENT_RESUME,
            EVENT_READY,
            EVENT_BUFFER,
            EVENT_PLAYBACK_STATE_CHANGED,
            EVENT_IDLE,
            EVENT_TIMED_METADATA,
            EVENT_AUDIO_BECOMING_NOISY,
            EVENT_AUDIO_FOCUS_CHANGE,
            EVENT_PLAYBACK_RATE_CHANGE,
            EVENT_VOLUME_CHANGE,
            EVENT_AUDIO_TRACKS,
            EVENT_TEXT_TRACKS,
            EVENT_VIDEO_TRACKS,
            EVENT_BANDWIDTH,
            EVENT_ON_RECEIVE_AD_EVENT
    };

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            EVENT_LOAD_START,
            EVENT_LOAD,
            EVENT_ERROR,
            EVENT_PROGRESS,
            EVENT_SEEK,
            EVENT_END,
            EVENT_FULLSCREEN_WILL_PRESENT,
            EVENT_FULLSCREEN_DID_PRESENT,
            EVENT_FULLSCREEN_WILL_DISMISS,
            EVENT_FULLSCREEN_DID_DISMISS,
            EVENT_STALLED,
            EVENT_RESUME,
            EVENT_READY,
            EVENT_BUFFER,
            EVENT_PLAYBACK_STATE_CHANGED,
            EVENT_IDLE,
            EVENT_TIMED_METADATA,
            EVENT_AUDIO_BECOMING_NOISY,
            EVENT_AUDIO_FOCUS_CHANGE,
            EVENT_PLAYBACK_RATE_CHANGE,
            EVENT_VOLUME_CHANGE,
            EVENT_AUDIO_TRACKS,
            EVENT_TEXT_TRACKS,
            EVENT_VIDEO_TRACKS,
            EVENT_BANDWIDTH,
            EVENT_ON_RECEIVE_AD_EVENT
    })
    @interface VideoEvents {
    }

    private static final String EVENT_PROP_FAST_FORWARD = "canPlayFastForward";
    private static final String EVENT_PROP_SLOW_FORWARD = "canPlaySlowForward";
    private static final String EVENT_PROP_SLOW_REVERSE = "canPlaySlowReverse";
    private static final String EVENT_PROP_REVERSE = "canPlayReverse";
    private static final String EVENT_PROP_STEP_FORWARD = "canStepForward";
    private static final String EVENT_PROP_STEP_BACKWARD = "canStepBackward";

    private static final String EVENT_PROP_DURATION = "duration";
    private static final String EVENT_PROP_PLAYABLE_DURATION = "playableDuration";
    private static final String EVENT_PROP_SEEKABLE_DURATION = "seekableDuration";
    private static final String EVENT_PROP_CURRENT_TIME = "currentTime";
    private static final String EVENT_PROP_CURRENT_PLAYBACK_TIME = "currentPlaybackTime";
    private static final String EVENT_PROP_SEEK_TIME = "seekTime";
    private static final String EVENT_PROP_NATURAL_SIZE = "naturalSize";
    private static final String EVENT_PROP_TRACK_ID = "trackId";
    private static final String EVENT_PROP_WIDTH = "width";
    private static final String EVENT_PROP_HEIGHT = "height";
    private static final String EVENT_PROP_ORIENTATION = "orientation";
    private static final String EVENT_PROP_VIDEO_TRACKS = "videoTracks";
    private static final String EVENT_PROP_AUDIO_TRACKS = "audioTracks";
    private static final String EVENT_PROP_TEXT_TRACKS = "textTracks";
    private static final String EVENT_PROP_HAS_AUDIO_FOCUS = "hasAudioFocus";
    private static final String EVENT_PROP_IS_BUFFERING = "isBuffering";
    private static final String EVENT_PROP_PLAYBACK_RATE = "playbackRate";
    private static final String EVENT_PROP_VOLUME = "volume";

    private static final String EVENT_PROP_ERROR = "error";
    private static final String EVENT_PROP_ERROR_STRING = "errorString";
    private static final String EVENT_PROP_ERROR_EXCEPTION = "errorException";
    private static final String EVENT_PROP_ERROR_TRACE = "errorStackTrace";
    private static final String EVENT_PROP_ERROR_CODE = "errorCode";

    private static final String EVENT_PROP_TIMED_METADATA = "metadata";

    private static final String EVENT_PROP_BITRATE = "bitrate";

    private static final String EVENT_PROP_IS_PLAYING = "isPlaying";

    public void setViewId(int viewId) {
        this.viewId = viewId;
    }

    public void loadStart() {
        receiveEvent(EVENT_LOAD_START, null);
    }

    WritableMap aspectRatioToNaturalSize(int videoWidth, int videoHeight) {
        WritableMap naturalSize = Arguments.createMap();
        naturalSize.putInt(EVENT_PROP_WIDTH, videoWidth);
        naturalSize.putInt(EVENT_PROP_HEIGHT, videoHeight);
        if (videoWidth > videoHeight) {
            naturalSize.putString(EVENT_PROP_ORIENTATION, "landscape");
        } else if (videoWidth < videoHeight) {
            naturalSize.putString(EVENT_PROP_ORIENTATION, "portrait");
        } else {
            naturalSize.putString(EVENT_PROP_ORIENTATION, "square");
        }
        return naturalSize;
    }

    WritableArray audioTracksToArray(ArrayList<Track> audioTracks) {
        WritableArray waAudioTracks = Arguments.createArray();
        if( audioTracks != null ){
            for (int i = 0; i < audioTracks.size(); ++i) {
                Track format = audioTracks.get(i);
                WritableMap audioTrack = Arguments.createMap();
                audioTrack.putInt("index", i);
                audioTrack.putString("title", format.getTitle());
                audioTrack.putString("type", format.getMimeType());
                audioTrack.putString("language", format.getLanguage());
                audioTrack.putInt("bitrate", format.getBitrate());
                audioTrack.putBoolean("selected", format.isSelected());
                waAudioTracks.pushMap(audioTrack);
            }
        }
        return waAudioTracks;
    }

    WritableArray videoTracksToArray(ArrayList<VideoTrack> videoTracks) {
        WritableArray waVideoTracks = Arguments.createArray();
        if( videoTracks != null ){
            for (int i = 0; i < videoTracks.size(); ++i) {
                VideoTrack vTrack = videoTracks.get(i);
                WritableMap videoTrack = Arguments.createMap();
                videoTrack.putInt("width", vTrack.getWidth());
                videoTrack.putInt("height",vTrack.getHeight());
                videoTrack.putInt("bitrate", vTrack.getBitrate());
                videoTrack.putString("codecs", vTrack.getCodecs());
                videoTrack.putInt("trackId",vTrack.getId());
                videoTrack.putBoolean("selected", vTrack.isSelected());
                waVideoTracks.pushMap(videoTrack);
            }
        }
        return waVideoTracks;
    }

    WritableArray textTracksToArray(ArrayList<Track> textTracks) {
        WritableArray waTextTracks = Arguments.createArray();
        if (textTracks != null) {
            for (int i = 0; i < textTracks.size(); ++i) {
                Track format = textTracks.get(i);
                WritableMap textTrack = Arguments.createMap();
                textTrack.putInt("index", i);
                textTrack.putString("title", format.getTitle());
                textTrack.putString("type", format.getMimeType());
                textTrack.putString("language", format.getLanguage());
                textTrack.putBoolean("selected", format.isSelected());
                waTextTracks.pushMap(textTrack);
            }
        }
        return waTextTracks;
    }

    public void load(double duration, double currentPosition, int videoWidth, int videoHeight,
                     ArrayList<Track> audioTracks, ArrayList<Track> textTracks, ArrayList<VideoTrack> videoTracks, String trackId){
        WritableArray waAudioTracks = audioTracksToArray(audioTracks);
        WritableArray waVideoTracks = videoTracksToArray(videoTracks);
        WritableArray waTextTracks = textTracksToArray(textTracks);

        load( duration,  currentPosition,  videoWidth,  videoHeight, waAudioTracks,  waTextTracks,  waVideoTracks, trackId);
    }

    void load(double duration, double currentPosition, int videoWidth, int videoHeight,
              WritableArray audioTracks, WritableArray textTracks, WritableArray videoTracks, String trackId) {
        WritableMap event = Arguments.createMap();
        event.putDouble(EVENT_PROP_DURATION, duration / 1000D);
        event.putDouble(EVENT_PROP_CURRENT_TIME, currentPosition / 1000D);

        WritableMap naturalSize = aspectRatioToNaturalSize(videoWidth, videoHeight);
        event.putMap(EVENT_PROP_NATURAL_SIZE, naturalSize);
        event.putString(EVENT_PROP_TRACK_ID, trackId);
        event.putArray(EVENT_PROP_VIDEO_TRACKS, videoTracks);
        event.putArray(EVENT_PROP_AUDIO_TRACKS, audioTracks);
        event.putArray(EVENT_PROP_TEXT_TRACKS, textTracks);

        // TODO: Actually check if you can.
        event.putBoolean(EVENT_PROP_FAST_FORWARD, true);
        event.putBoolean(EVENT_PROP_SLOW_FORWARD, true);
        event.putBoolean(EVENT_PROP_SLOW_REVERSE, true);
        event.putBoolean(EVENT_PROP_REVERSE, true);
        event.putBoolean(EVENT_PROP_FAST_FORWARD, true);
        event.putBoolean(EVENT_PROP_STEP_BACKWARD, true);
        event.putBoolean(EVENT_PROP_STEP_FORWARD, true);

        receiveEvent(EVENT_LOAD, event);
    }

    WritableMap arrayToObject(String field, WritableArray array) {
        WritableMap event = Arguments.createMap();
        event.putArray(field, array);
        return event;
    }

    public void audioTracks(ArrayList<Track> audioTracks){
        receiveEvent(EVENT_AUDIO_TRACKS, arrayToObject(EVENT_PROP_AUDIO_TRACKS, audioTracksToArray(audioTracks)));
    }

    public void textTracks(ArrayList<Track> textTracks){
        receiveEvent(EVENT_TEXT_TRACKS, arrayToObject(EVENT_PROP_TEXT_TRACKS, textTracksToArray(textTracks)));
    }

    public void videoTracks(ArrayList<VideoTrack> videoTracks){
        receiveEvent(EVENT_VIDEO_TRACKS, arrayToObject(EVENT_PROP_VIDEO_TRACKS, videoTracksToArray(videoTracks)));
    }

    public void progressChanged(double currentPosition, double bufferedDuration, double seekableDuration, double currentPlaybackTime) {
        WritableMap event = Arguments.createMap();
        event.putDouble(EVENT_PROP_CURRENT_TIME, currentPosition / 1000D);
        event.putDouble(EVENT_PROP_PLAYABLE_DURATION, bufferedDuration / 1000D);
        event.putDouble(EVENT_PROP_SEEKABLE_DURATION, seekableDuration / 1000D);
        event.putDouble(EVENT_PROP_CURRENT_PLAYBACK_TIME, currentPlaybackTime);
        receiveEvent(EVENT_PROGRESS, event);
    }

    public void bandwidthReport(double bitRateEstimate, int height, int width, String id) {
        WritableMap event = Arguments.createMap();
        event.putDouble(EVENT_PROP_BITRATE, bitRateEstimate);
        event.putInt(EVENT_PROP_WIDTH, width);
        event.putInt(EVENT_PROP_HEIGHT, height);
        event.putString(EVENT_PROP_TRACK_ID, id);
        receiveEvent(EVENT_BANDWIDTH, event);
    }

    public void seek(long currentPosition, long seekTime) {
        WritableMap event = Arguments.createMap();
        event.putDouble(EVENT_PROP_CURRENT_TIME, currentPosition / 1000D);
        event.putDouble(EVENT_PROP_SEEK_TIME, seekTime / 1000D);
        receiveEvent(EVENT_SEEK, event);
    }

    public void ready() {
        receiveEvent(EVENT_READY, null);
    }

    public void buffering(boolean isBuffering) {
        WritableMap map = Arguments.createMap();
        map.putBoolean(EVENT_PROP_IS_BUFFERING, isBuffering);
        receiveEvent(EVENT_BUFFER, map);
    }

    public void playbackStateChanged(boolean isPlaying) {
        WritableMap map = Arguments.createMap();
        map.putBoolean(EVENT_PROP_IS_PLAYING, isPlaying);
        receiveEvent(EVENT_PLAYBACK_STATE_CHANGED, map);
    }

    public void idle() {
        receiveEvent(EVENT_IDLE, null);
    }

    public void end() {
        receiveEvent(EVENT_END, null);
    }

    public void fullscreenWillPresent() {
        receiveEvent(EVENT_FULLSCREEN_WILL_PRESENT, null);
    }

    public void fullscreenDidPresent() {
        receiveEvent(EVENT_FULLSCREEN_DID_PRESENT, null);
    }

    public void fullscreenWillDismiss() {
        receiveEvent(EVENT_FULLSCREEN_WILL_DISMISS, null);
    }

    public void fullscreenDidDismiss() {
        receiveEvent(EVENT_FULLSCREEN_DID_DISMISS, null);
    }

    public void error(String errorString, Exception exception) {
        _error(errorString, exception, "0001");
    }

    public void error(String errorString, Exception exception, String errorCode) {
        _error(errorString, exception, errorCode);
    }

    void _error(String errorString, Exception exception, String errorCode) {
        // Prepare stack trace
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        String stackTrace = sw.toString();

        WritableMap error = Arguments.createMap();
        error.putString(EVENT_PROP_ERROR_STRING, errorString);
        error.putString(EVENT_PROP_ERROR_EXCEPTION, exception.toString());
        error.putString(EVENT_PROP_ERROR_CODE, errorCode);
        error.putString(EVENT_PROP_ERROR_TRACE, stackTrace);
        WritableMap event = Arguments.createMap();
        event.putMap(EVENT_PROP_ERROR, error);
        receiveEvent(EVENT_ERROR, event);
    }

    public void playbackRateChange(float rate) {
        WritableMap map = Arguments.createMap();
        map.putDouble(EVENT_PROP_PLAYBACK_RATE, (double)rate);
        receiveEvent(EVENT_PLAYBACK_RATE_CHANGE, map);
    }

    public void volumeChange(float volume) {
        WritableMap map = Arguments.createMap();
        map.putDouble(EVENT_PROP_VOLUME, volume);
        receiveEvent(EVENT_VOLUME_CHANGE, map);
    }

    public void timedMetadata(ArrayList<TimedMetadata> _metadataArrayList) {
        if (_metadataArrayList.size() == 0) {
            return;
        }
        WritableArray metadataArray = Arguments.createArray();

        for (int i = 0; i < _metadataArrayList.size(); i++) {
            WritableMap map = Arguments.createMap();
            map.putString("identifier", _metadataArrayList.get(i).getIdentifier());
            map.putString("value", _metadataArrayList.get(i).getValue());
            metadataArray.pushMap(map);
        }

        WritableMap event = Arguments.createMap();
        event.putArray(EVENT_PROP_TIMED_METADATA, metadataArray);
        receiveEvent(EVENT_TIMED_METADATA, event);
    }

    public void audioFocusChanged(boolean hasFocus) {
        WritableMap map = Arguments.createMap();
        map.putBoolean(EVENT_PROP_HAS_AUDIO_FOCUS, hasFocus);
        receiveEvent(EVENT_AUDIO_FOCUS_CHANGE, map);
    }

    public void audioBecomingNoisy() {
        receiveEvent(EVENT_AUDIO_BECOMING_NOISY, null);
    }

    public void receiveAdEvent(String event) {
        WritableMap map = Arguments.createMap();
        map.putString("event", event);
        receiveEvent(EVENT_ON_RECEIVE_AD_EVENT, map);
    }

    private void receiveEvent(@VideoEvents String type, WritableMap event) {
        DebugLog.d(TAG,"send event viewId: " + viewId + " type: " + type + " " + (event == null ? "" : event.toString()) );
        eventEmitter.receiveEvent(viewId, type, event);
    }
}
