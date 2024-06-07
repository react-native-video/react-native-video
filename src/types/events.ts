import type {WithDefault} from 'react-native/Libraries/Types/CodegenTypes';
import type {
  OnAudioFocusChangedData,
  OnAudioTracksData,
  OnBandwidthUpdateData,
  OnBufferData,
  OnExternalPlaybackChangeData,
  OnLoadStartData,
  OnPictureInPictureStatusChangedData,
  OnPlaybackRateChangeData,
  OnPlaybackStateChangedData,
  OnProgressData,
  OnSeekData,
  OnTextTrackDataChangedData,
  OnTimedMetadataData,
  OnVideoAspectRatioData,
  OnVideoErrorData,
  OnVideoTracksData,
  OnVolumeChangeData,
} from '../specs/VideoNativeComponent';

export type AudioTrack = OnAudioTracksData['audioTracks'][number];
export type TextTrack = OnTextTracksData['textTracks'][number];
export type VideoTrack = OnVideoTracksData['videoTracks'][number];

export type OnLoadData = Readonly<{
  currentTime: number;
  duration: number;
  naturalSize: Readonly<{
    width: number;
    height: number;
    orientation: WithDefault<'landscape' | 'portrait', 'landscape'>;
  }>;
  audioTracks: {
    index: number;
    title?: string;
    language?: string;
    bitrate?: number;
    type?: string;
    selected?: boolean;
  }[];
  textTracks: {
    index: number;
    title?: string;
    language?: string;
    /**
     * iOS only supports VTT, Android supports all 3
     */
    type?: WithDefault<'srt' | 'ttml' | 'vtt', 'srt'>;
    selected?: boolean;
  }[];
  videoTracks: {
    index: number;
    tracksID?: string;
    codecs?: string;
    width?: number;
    height?: number;
    bitrate?: number;
    selected?: boolean;
  }[];
}>;

export type OnTextTracksData = Readonly<{
  textTracks: {
    index: number;
    title?: string;
    language?: string;
    /**
     * iOS only supports VTT, Android supports all 3
     */
    type?: WithDefault<string, 'srt'>;
    selected?: boolean;
  }[];
}>;

export type OnReceiveAdEventData = Readonly<{
  data?: object;
  event: WithDefault<
    /**
     * iOS only: Fired the first time each ad break ends. Applications must reenable seeking when this occurs (only used for dynamic ad insertion).
     */ | 'AD_BREAK_ENDED'
    /**
     * Fires when an ad rule or a VMAP ad break would have played if autoPlayAdBreaks is false.
     */
    | 'AD_BREAK_READY'
    /**
     * iOS only: Fired first time each ad break begins playback. If an ad break is watched subsequent times this will not be fired. Applications must disable seeking when this occurs (only used for dynamic ad insertion).
     */
    | 'AD_BREAK_STARTED'
    /**
     * Android only: Fires when the ad has stalled playback to buffer.
     */
    | 'AD_BUFFERING'
    /**
     * Android only: Fires when the ad is ready to play without buffering, either at the beginning of the ad or after buffering completes.
     */
    | 'AD_CAN_PLAY'
    /**
     * Android only: Fires when an ads list is loaded.
     */
    | 'AD_METADATA'
    /**
     * iOS only: Fired every time the stream switches from advertising or slate to content. This will be fired even when an ad is played a second time or when seeking into an ad (only used for dynamic ad insertion).
     */
    | 'AD_PERIOD_ENDED'
    /**
     * iOS only: Fired every time the stream switches from content to advertising or slate. This will be fired even when an ad is played a second time or when seeking into an ad (only used for dynamic ad insertion).
     */
    | 'AD_PERIOD_STARTED'
    /**
     * Android only: Fires when the ad's current time value changes. The event `data` will be populated with an AdProgressData object.
     */
    | 'AD_PROGRESS'
    /**
     * Fires when the ads manager is done playing all the valid ads in the ads response, or when the response doesn't return any valid ads.
     */
    | 'ALL_ADS_COMPLETED'
    /**
     * Fires when the ad is clicked.
     */
    | 'CLICK'
    /**
     * Fires when the ad completes playing.
     */
    | 'COMPLETED'
    /**
     * Android only: Fires when content should be paused. This usually happens right before an ad is about to cover the content.
     */
    | 'CONTENT_PAUSE_REQUESTED'
    /**
     * Android only: Fires when content should be resumed. This usually happens when an ad finishes or collapses.
     */
    | 'CONTENT_RESUME_REQUESTED'
    /**
     * iOS only: Cuepoints changed for VOD stream (only used for dynamic ad insertion).
     */
    | 'CUEPOINTS_CHANGED'
    /**
     * Android only: Fires when the ad's duration changes.
     */
    | 'DURATION_CHANGE'
    /**
     * Fires when an error is encountered and the ad can't be played.
     */
    | 'ERROR'
    /**
     * Fires when the ad playhead crosses first quartile.
     */
    | 'FIRST_QUARTILE'
    /**
     * Android only: Fires when the impression URL has been pinged.
     */
    | 'IMPRESSION'
    /**
     * Android only: Fires when an ad triggers the interaction callback. Ad interactions contain an interaction ID string in the ad data.
     */
    | 'INTERACTION'
    /**
     * Android only: Fires when the displayed ad changes from linear to nonlinear, or the reverse.
     */
    | 'LINEAR_CHANGED'
    /**
     * Fires when ad data is available.
     */
    | 'LOADED'
    /**
     * Fires when a non-fatal error is encountered. The user need not take any action since the SDK will continue with the same or next ad playback depending on the error situation.
     */
    | 'LOG'
    /**
     * Fires when the ad playhead crosses midpoint.
     */
    | 'MIDPOINT'
    /**
     * Fires when the ad is paused.
     */
    | 'PAUSED'
    /**
     * Fires when the ad is resumed.
     */
    | 'RESUMED'
    /**
     * Android only: Fires when the displayed ads skippable state is changed.
     */
    | 'SKIPPABLE_STATE_CHANGED'
    /**
     * Fires when the ad is skipped by the user.
     */
    | 'SKIPPED'
    /**
     * Fires when the ad starts playing.
     */
    | 'STARTED'
    /**
     * iOS only: Stream request has loaded (only used for dynamic ad insertion).
     */
    | 'STREAM_LOADED'
    /**
     * iOS only: Fires when the ad is tapped.
     */
    | 'TAPPED'
    /**
     * Fires when the ad playhead crosses third quartile.
     */
    | 'THIRD_QUARTILE'
    /**
     * iOS only: An unknown event has fired
     */
    | 'UNKNOWN'
    /**
     * Android only: Fires when the ad is closed by the user.
     */
    | 'USER_CLOSE'
    /**
     * Android only: Fires when the non-clickthrough portion of a video ad is clicked.
     */
    | 'VIDEO_CLICKED'
    /**
     * Android only: Fires when a user clicks a video icon.
     */
    | 'VIDEO_ICON_CLICKED'
    /**
     * Android only: Fires when the ad volume has changed.
     */
    | 'VOLUME_CHANGED'
    /**
     * Android only: Fires when the ad volume has been muted.
     */
    | 'VOLUME_MUTED',
    'AD_BREAK_ENDED'
  >;
}>;

export interface ReactVideoEvents {
  onAudioBecomingNoisy?: () => void; //Android, iOS
  onAudioFocusChanged?: (e: OnAudioFocusChangedData) => void; // Android
  onIdle?: () => void; // Android
  onBandwidthUpdate?: (e: OnBandwidthUpdateData) => void; //Android
  onBuffer?: (e: OnBufferData) => void; //Android, iOS
  onEnd?: () => void; //All
  onError?: (e: OnVideoErrorData) => void; //Android, iOS
  onExternalPlaybackChange?: (e: OnExternalPlaybackChangeData) => void; //iOS
  onFullscreenPlayerWillPresent?: () => void; //Android, iOS
  onFullscreenPlayerDidPresent?: () => void; //Android, iOS
  onFullscreenPlayerWillDismiss?: () => void; //Android, iOS
  onFullscreenPlayerDidDismiss?: () => void; //Android, iOS
  onLoad?: (e: OnLoadData) => void; //All
  onLoadStart?: (e: OnLoadStartData) => void; //All
  onPictureInPictureStatusChanged?: (
    e: OnPictureInPictureStatusChangedData,
  ) => void; //Android, iOS
  onPlaybackRateChange?: (e: OnPlaybackRateChangeData) => void; //All
  onVolumeChange?: (e: OnVolumeChangeData) => void; //Android, iOS
  onProgress?: (e: OnProgressData) => void; //All
  onReadyForDisplay?: () => void; //Android, iOS
  onReceiveAdEvent?: (e: OnReceiveAdEventData) => void; //Android, iOS
  onRestoreUserInterfaceForPictureInPictureStop?: () => void; //iOS
  onSeek?: (e: OnSeekData) => void; //Android, iOS, Windows UWP
  onPlaybackStateChanged?: (e: OnPlaybackStateChangedData) => void; // Android, iOS
  onTimedMetadata?: (e: OnTimedMetadataData) => void; //Android, iOS
  onAudioTracks?: (e: OnAudioTracksData) => void; // Android
  onTextTracks?: (e: OnTextTracksData) => void; //Android
  onTextTrackDataChanged?: (e: OnTextTrackDataChangedData) => void; // iOS
  onVideoTracks?: (e: OnVideoTracksData) => void; //Android
  onAspectRatio?: (e: OnVideoAspectRatioData) => void;
}
