package com.brentvatne.common.react

import com.brentvatne.common.api.TimedMetadata
import com.brentvatne.common.api.Track
import com.brentvatne.common.api.VideoTrack
import com.brentvatne.exoplayer.ReactExoplayerView
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.UIManagerHelper
import com.facebook.react.uimanager.events.Event
import com.facebook.react.uimanager.events.EventDispatcher
import java.io.PrintWriter
import java.io.StringWriter

enum class EventTypes(val eventName: String) {
    EVENT_LOAD_START("onVideoLoadStart"),
    EVENT_LOAD("onVideoLoad"),
    EVENT_ERROR("onVideoError"),
    EVENT_PROGRESS("onVideoProgress"),
    EVENT_BANDWIDTH("onVideoBandwidthUpdate"),
    EVENT_CONTROLS_VISIBILITY_CHANGE("onControlsVisibilityChange"),
    EVENT_SEEK("onVideoSeek"),
    EVENT_END("onVideoEnd"),
    EVENT_FULLSCREEN_WILL_PRESENT("onVideoFullscreenPlayerWillPresent"),
    EVENT_FULLSCREEN_DID_PRESENT("onVideoFullscreenPlayerDidPresent"),
    EVENT_FULLSCREEN_WILL_DISMISS("onVideoFullscreenPlayerWillDismiss"),
    EVENT_FULLSCREEN_DID_DISMISS("onVideoFullscreenPlayerDidDismiss"),

    EVENT_READY("onReadyForDisplay"),
    EVENT_BUFFER("onVideoBuffer"),
    EVENT_PLAYBACK_STATE_CHANGED("onVideoPlaybackStateChanged"),
    EVENT_IDLE("onVideoIdle"),
    EVENT_TIMED_METADATA("onTimedMetadata"),
    EVENT_AUDIO_BECOMING_NOISY("onVideoAudioBecomingNoisy"),
    EVENT_AUDIO_FOCUS_CHANGE("onAudioFocusChanged"),
    EVENT_PLAYBACK_RATE_CHANGE("onPlaybackRateChange"),
    EVENT_VOLUME_CHANGE("onVolumeChange"),
    EVENT_AUDIO_TRACKS("onAudioTracks"),
    EVENT_TEXT_TRACKS("onTextTracks"),

    EVENT_TEXT_TRACK_DATA_CHANGED("onTextTrackDataChanged"),
    EVENT_VIDEO_TRACKS("onVideoTracks"),
    EVENT_ON_RECEIVE_AD_EVENT("onReceiveAdEvent");

    companion object {
        fun toMap() =
            mutableMapOf<String, Any>().apply {
                EventTypes.entries.forEach { eventType ->
                    put("top${eventType.eventName.removePrefix("on")}", mapOf("registrationName" to eventType.eventName))
                }
            }
    }
}

class VideoEventEmitter {
    lateinit var onVideoLoadStart: () -> Unit
    lateinit var onVideoLoad: (
        duration: Long,
        currentPosition: Long,
        videoWidth: Int,
        videoHeight: Int,
        audioTracks: ArrayList<Track>,
        textTracks: ArrayList<Track>,
        videoTracks: ArrayList<VideoTrack>,
        trackId: String
    ) -> Unit
    lateinit var onVideoError: (errorString: String, exception: Exception, errorCode: String) -> Unit
    lateinit var onVideoProgress: (currentPosition: Long, bufferedDuration: Long, seekableDuration: Long, currentPlaybackTime: Double) -> Unit
    lateinit var onVideoBandwidthUpdate: (bitRateEstimate: Long, height: Int, width: Int, trackId: String) -> Unit
    lateinit var onVideoPlaybackStateChanged: (isPlaying: Boolean) -> Unit
    lateinit var onVideoSeek: (currentPosition: Long, seekTime: Long) -> Unit
    lateinit var onVideoEnd: () -> Unit
    lateinit var onVideoFullscreenPlayerWillPresent: () -> Unit
    lateinit var onVideoFullscreenPlayerDidPresent: () -> Unit
    lateinit var onVideoFullscreenPlayerWillDismiss: () -> Unit
    lateinit var onVideoFullscreenPlayerDidDismiss: () -> Unit
    lateinit var onReadyForDisplay: () -> Unit
    lateinit var onVideoBuffer: (isBuffering: Boolean) -> Unit
    lateinit var onControlsVisibilityChange: (isVisible: Boolean) -> Unit
    lateinit var onVideoIdle: () -> Unit
    lateinit var onTimedMetadata: (metadataArrayList: ArrayList<TimedMetadata>) -> Unit
    lateinit var onVideoAudioBecomingNoisy: () -> Unit
    lateinit var onAudioFocusChanged: (hasFocus: Boolean) -> Unit
    lateinit var onPlaybackRateChange: (rate: Float) -> Unit
    lateinit var onVolumeChange: (volume: Float) -> Unit
    lateinit var onAudioTracks: (audioTracks: ArrayList<Track>?) -> Unit
    lateinit var onTextTracks: (textTracks: ArrayList<Track>?) -> Unit
    lateinit var onVideoTracks: (videoTracks: ArrayList<VideoTrack>?) -> Unit
    lateinit var onTextTrackDataChanged: (textTrackData: String) -> Unit
    lateinit var onReceiveAdEvent: (adEvent: String, adData: Map<String?, String?>?) -> Unit

    fun addEventEmitters(reactContext: ThemedReactContext, view: ReactExoplayerView) {
        val dispatcher = UIManagerHelper.getEventDispatcherForReactTag(reactContext, view.id)
        val surfaceId = UIManagerHelper.getSurfaceId(reactContext)

        if (dispatcher != null) {
            val event = EventBuilder(surfaceId, view.id, dispatcher)

            onVideoLoadStart = {
                event.dispatch(EventTypes.EVENT_LOAD_START)
            }
            onVideoLoad = { duration, currentPosition, videoWidth, videoHeight, audioTracks, textTracks, videoTracks, trackId ->
                event.dispatch(EventTypes.EVENT_LOAD) {
                    putDouble("duration", duration / 1000.0)
                    putDouble("playableDuration", currentPosition / 1000.0)

                    val naturalSize: WritableMap = aspectRatioToNaturalSize(videoWidth, videoHeight)
                    putMap("seekableDuration", naturalSize)
                    putString("trackId", trackId)
                    putArray("videoTracks", videoTracksToArray(videoTracks))
                    putArray("audioTracks", audioTracksToArray(audioTracks))
                    putArray("textTracks", textTracksToArray(textTracks))

                    // TODO: Actually check if you can.
                    putBoolean("canPlayFastForward", true)
                    putBoolean("canPlaySlowForward", true)
                    putBoolean("canPlaySlowReverse", true)
                    putBoolean("canPlayReverse", true)
                    putBoolean("canPlayFastForward", true)
                    putBoolean("canStepBackward", true)
                    putBoolean("canStepForward", true)
                }
            }
            onVideoError = { errorString, exception, errorCode ->
                event.dispatch(EventTypes.EVENT_ERROR) {
                    putMap(
                        "error",
                        Arguments.createMap().apply {
                            // Prepare stack trace
                            val sw = StringWriter()
                            val pw = PrintWriter(sw)
                            exception.printStackTrace(pw)
                            val stackTrace = sw.toString()

                            putString("errorString", errorString)
                            putString("errorException", exception.toString())
                            putString("errorCode", errorCode)
                            putString("errorStackTrace", stackTrace)
                        }
                    )
                }
            }
            onVideoProgress = { currentPosition, bufferedDuration, seekableDuration, currentPlaybackTime ->
                event.dispatch(EventTypes.EVENT_PROGRESS) {
                    putDouble("currentTime", currentPosition / 1000.0)
                    putDouble("playableDuration", bufferedDuration / 1000.0)
                    putDouble("seekableDuration", seekableDuration / 1000.0)
                    putDouble("currentPlaybackTime", currentPlaybackTime)
                }
            }
            onVideoBandwidthUpdate = { bitRateEstimate, height, width, trackId ->
                event.dispatch(EventTypes.EVENT_BANDWIDTH) {
                    putDouble("bitrate", bitRateEstimate.toDouble())
                    putInt("width", width)
                    putInt("height", height)
                    putString("trackId", trackId)
                }
            }
            onVideoPlaybackStateChanged = { isPlaying ->
                event.dispatch(EventTypes.EVENT_PLAYBACK_STATE_CHANGED) {
                    putBoolean("isPlaying", isPlaying)
                }
            }
            onVideoSeek = { currentPosition, seekTime ->
                event.dispatch(EventTypes.EVENT_SEEK) {
                    putDouble("currentTime", currentPosition / 1000.0)
                    putDouble("seekTime", seekTime / 1000.0)
                }
            }
            onVideoEnd = {
                event.dispatch(EventTypes.EVENT_END)
            }
            onVideoFullscreenPlayerWillPresent = {
                event.dispatch(EventTypes.EVENT_FULLSCREEN_WILL_PRESENT)
            }
            onVideoFullscreenPlayerDidPresent = {
                event.dispatch(EventTypes.EVENT_FULLSCREEN_DID_PRESENT)
            }
            onVideoFullscreenPlayerWillDismiss = {
                event.dispatch(EventTypes.EVENT_FULLSCREEN_WILL_DISMISS)
            }
            onVideoFullscreenPlayerDidDismiss = {
                event.dispatch(EventTypes.EVENT_FULLSCREEN_DID_DISMISS)
            }
            onReadyForDisplay = {
                event.dispatch(EventTypes.EVENT_READY)
            }
            onVideoBuffer = { isBuffering ->
                event.dispatch(EventTypes.EVENT_BUFFER) {
                    putBoolean("isBuffering", isBuffering)
                }
            }
            onControlsVisibilityChange = { isVisible ->
                event.dispatch(EventTypes.EVENT_CONTROLS_VISIBILITY_CHANGE) {
                    putBoolean("isVisible", isVisible)
                }
            }
            onVideoIdle = {
                event.dispatch(EventTypes.EVENT_IDLE)
            }
            onTimedMetadata = fn@{ metadataArrayList ->
                if (metadataArrayList.size == 0) {
                    return@fn
                }
                event.dispatch(EventTypes.EVENT_TIMED_METADATA) {
                    putArray(
                        "metadata",
                        Arguments.createArray().apply {
                            metadataArrayList.forEachIndexed { i, metadata ->
                                pushMap(
                                    Arguments.createMap().apply {
                                        putString("identifier", metadata.identifier)
                                        putString("value", metadata.value)
                                    }
                                )
                            }
                        }
                    )
                }
            }
            onVideoAudioBecomingNoisy = {
                event.dispatch(EventTypes.EVENT_AUDIO_BECOMING_NOISY)
            }
            onAudioFocusChanged = { hasFocus ->
                event.dispatch(EventTypes.EVENT_AUDIO_FOCUS_CHANGE) {
                    putBoolean("hasAudioFocus", hasFocus)
                }
            }
            onPlaybackRateChange = { rate ->
                event.dispatch(EventTypes.EVENT_PLAYBACK_RATE_CHANGE) {
                    putDouble("playbackRate", rate.toDouble())
                }
            }
            onVolumeChange = { volume ->
                event.dispatch(EventTypes.EVENT_VOLUME_CHANGE) {
                    putDouble("volume", volume.toDouble())
                }
            }
            onAudioTracks = { audioTracks ->
                event.dispatch(EventTypes.EVENT_AUDIO_TRACKS) {
                    putArray("audioTracks", audioTracksToArray(audioTracks))
                }
            }
            onTextTracks = { textTracks ->
                event.dispatch(EventTypes.EVENT_TEXT_TRACKS) {
                    putArray("textTracks", textTracksToArray(textTracks))
                }
            }
            onVideoTracks = { videoTracks ->
                event.dispatch(EventTypes.EVENT_VIDEO_TRACKS) {
                    putArray("videoTracks", videoTracksToArray(videoTracks))
                }
            }
            onTextTrackDataChanged = { textTrackData ->
                event.dispatch(EventTypes.EVENT_TEXT_TRACK_DATA_CHANGED) {
                    putString("subtitleTracks", textTrackData)
                }
            }
            onReceiveAdEvent = { adEvent, adData ->
                event.dispatch(EventTypes.EVENT_ON_RECEIVE_AD_EVENT) {
                    putString("event", adEvent)
                    putMap(
                        "data",
                        Arguments.createMap().apply {
                            adData?.let { data ->
                                for ((key, value) in data) {
                                    putString(key!!, value)
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    private class EventBuilder(private val surfaceId: Int, private val viewId: Int, private val dispatcher: EventDispatcher) {
        fun dispatch(event: EventTypes, paramsSetter: (WritableMap.() -> Unit)? = null) =
            dispatcher.dispatchEvent(object : Event<Event<*>>(surfaceId, viewId) {
                override fun getEventName() = "top${event.eventName.removePrefix("on")}"
                override fun getEventData() = Arguments.createMap().apply(paramsSetter ?: {})
            })
    }

    private fun audioTracksToArray(audioTracks: java.util.ArrayList<Track>?): WritableArray =
        Arguments.createArray().apply {
            audioTracks?.forEachIndexed { i, format ->
                pushMap(
                    Arguments.createMap().apply {
                        putInt("index", i)
                        putString("title", format.title)
                        format.mimeType?.let { putString("type", it) }
                        format.language?.let { putString("language", it) }
                        if (format.bitrate > 0) putInt("bitrate", format.bitrate)
                        putBoolean("selected", format.isSelected)
                    }
                )
            }
        }

    private fun videoTracksToArray(videoTracks: java.util.ArrayList<VideoTrack>?): WritableArray =
        Arguments.createArray().apply {
            videoTracks?.forEachIndexed { i, vTrack ->
                pushMap(
                    Arguments.createMap().apply {
                        putInt("width", vTrack.width)
                        putInt("height", vTrack.height)
                        putInt("bitrate", vTrack.bitrate)
                        putString("codecs", vTrack.codecs)
                        putString("trackId", vTrack.trackId)
                        putInt("index", vTrack.index)
                        putBoolean("selected", vTrack.isSelected)
                        putInt("rotation", vTrack.rotation)
                    }
                )
            }
        }

    private fun textTracksToArray(textTracks: ArrayList<Track>?): WritableArray =
        Arguments.createArray().apply {
            textTracks?.forEachIndexed { i, format ->
                pushMap(
                    Arguments.createMap().apply {
                        putInt("index", i)
                        putString("title", format.title)
                        putString("type", format.mimeType)
                        putString("language", format.language)
                        putBoolean("selected", format.isSelected)
                    }
                )
            }
        }

    private fun aspectRatioToNaturalSize(videoWidth: Int, videoHeight: Int): WritableMap =
        Arguments.createMap().apply {
            putInt("width", videoWidth)
            putInt("height", videoHeight)
            val orientation = if (videoWidth > videoHeight) {
                "landscape"
            } else if (videoWidth < videoHeight) {
                "portrait"
            } else {
                "square"
            }
            putString("orientation", orientation)
        }
}
