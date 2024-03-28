/* eslint-disable @typescript-eslint/ban-types */
import type {HostComponent, ViewProps} from 'react-native';
import {NativeModules} from 'react-native';
import codegenNativeComponent from 'react-native/Libraries/Utilities/codegenNativeComponent';
import type {
  DirectEventHandler,
  Double,
  Float,
  Int32,
  WithDefault,
} from 'react-native/Libraries/Types/CodegenTypes';

// -------- There are types for native component (future codegen) --------
// if you are looking for types for react component, see src/types/video.ts

type Headers = ReadonlyArray<
  Readonly<{
    key: string;
    value: string;
  }>
>;

export type VideoSrc = Readonly<{
  uri?: string;
  isNetwork?: boolean;
  isAsset?: boolean;
  shouldCache?: boolean;
  type?: string;
  mainVer?: Int32;
  patchVer?: Int32;
  requestHeaders?: Headers;
  startPosition?: Float;
  cropStart?: Float;
  cropEnd?: Float;
  title?: string;
  subtitle?: string;
  description?: string;
  customImageUri?: string;
}>;

type DRMType = WithDefault<
  'widevine' | 'playready' | 'clearkey' | 'fairplay',
  'widevine'
>;

type DebugConfig = Readonly<{
  enable?: boolean;
  thread?: boolean;
}>;

type Drm = Readonly<{
  type?: DRMType;
  licenseServer?: string;
  headers?: Headers;
  contentId?: string; // ios
  certificateUrl?: string; // ios
  base64Certificate?: boolean; // ios default: false
  useExternalGetLicense?: boolean; // ios
}>;

type TextTracks = ReadonlyArray<
  Readonly<{
    title: string;
    language: string;
    type: string;
    uri: string;
  }>
>;

type SelectedTextTrackType = WithDefault<
  'system' | 'disabled' | 'title' | 'language' | 'index',
  'system'
>;

type SelectedAudioTrackType = WithDefault<
  'system' | 'disabled' | 'title' | 'language' | 'index',
  'system'
>;

type SelectedTextTrack = Readonly<{
  type?: SelectedTextTrackType;
  value?: string;
}>;

type SelectedAudioTrack = Readonly<{
  type?: SelectedAudioTrackType;
  value?: string;
}>;

type SelectedVideoTrackType = WithDefault<
  'auto' | 'disabled' | 'resolution' | 'index',
  'auto'
>;

type SelectedVideoTrack = Readonly<{
  type?: SelectedVideoTrackType;
  value?: Int32;
}>;

export type Seek = Readonly<{
  time: Float;
  tolerance?: Float;
}>;

type BufferConfig = Readonly<{
  minBufferMs?: Float;
  maxBufferMs?: Float;
  bufferForPlaybackMs?: Float;
  bufferForPlaybackAfterRebufferMs?: Float;
  maxHeapAllocationPercent?: Float;
  backBufferDurationMs?: Float; // Android
  minBackBufferMemoryReservePercent?: Float;
  minBufferMemoryReservePercent?: Float;
}>;

type SubtitleStyle = Readonly<{
  fontSize?: Float;
  paddingTop?: WithDefault<Float, 0>;
  paddingBottom?: WithDefault<Float, 0>;
  paddingLeft?: WithDefault<Float, 0>;
  paddingRight?: WithDefault<Float, 0>;
  opacity?: WithDefault<Float, 1>;
}>;

export type OnLoadData = Readonly<{
  currentTime: Float;
  duration: Float;
  naturalSize: Readonly<{
    width: Float;
    height: Float;
    orientation: WithDefault<'landscape' | 'portrait', 'landscape'>;
  }>;
  audioTracks: {
    index: Int32;
    title?: string;
    language?: string;
    bitrate?: Float;
    type?: string;
    selected?: boolean;
  }[];
  textTracks: {
    index: Int32;
    title?: string;
    language?: string;
    /**
     * iOS only supports VTT, Android supports all 3
     */
    type?: WithDefault<'srt' | 'ttml' | 'vtt', 'srt'>;
    selected?: boolean;
  }[];
}>;

export type OnLoadStartData = Readonly<{
  isNetwork: boolean;
  type: string;
  uri: string;
}>;

export type OnVideoAspectRatioData = Readonly<{
  width: Float;
  height: Float;
}>;

export type OnBufferData = Readonly<{isBuffering: boolean}>;

export type OnProgressData = Readonly<{
  currentTime: Float;
  playableDuration: Float;
  seekableDuration: Float;
}>;

export type OnBandwidthUpdateData = Readonly<{
  bitrate: Int32;
  width?: Float;
  height?: Float;
  trackId?: Int32;
}>;

export type OnSeekData = Readonly<{
  currentTime: Float;
  seekTime: Float;
}>;

export type OnPlaybackStateChangedData = Readonly<{
  isPlaying: boolean;
}>;

export type OnTimedMetadataData = Readonly<{
  metadata: {
    value?: string;
    identifier: string;
  }[];
}>;

export type OnAudioTracksData = Readonly<{
  audioTracks: {
    index: Int32;
    title?: string;
    language?: string;
    bitrate?: Float;
    type?: string;
    selected?: boolean;
  }[];
}>;

export type OnTextTracksData = Readonly<{
  textTracks: {
    index: Int32;
    title?: string;
    language?: string;
    /**
     * iOS only supports VTT, Android supports all 3
     */
    type?: WithDefault<'srt' | 'ttml' | 'vtt', 'srt'>;
    selected?: boolean;
  }[];
}>;

export type OnTextTrackDataChangedData = Readonly<{
  subtitleTracks: string;
}>;

export type OnVideoTracksData = Readonly<{
  videoTracks: {
    trackId: Int32;
    codecs?: string;
    width?: Float;
    height?: Float;
    bitrate?: Float;
    selected?: boolean;
  }[];
}>;

export type OnPlaybackData = Readonly<{
  playbackRate: Float;
}>;

export type OnVolumeChangeData = Readonly<{
  volume: Float;
}>;

export type OnExternalPlaybackChangeData = Readonly<{
  isExternalPlaybackActive: boolean;
}>;

export type OnGetLicenseData = Readonly<{
  licenseUrl: string;
  loadedLicenseUrl: string;
  contentId: string;
  spcBase64: string;
}>;

export type OnPictureInPictureStatusChangedData = Readonly<{
  isActive: boolean;
}>;

export type OnReceiveAdEventData = Readonly<{
  data?: {};
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

export type OnVideoErrorData = Readonly<{
  error: Readonly<{
    errorString?: string; // android
    errorException?: string; // android
    errorStackTrace?: string; // android
    errorCode?: string; // android
    error?: string; // ios
    code?: Int32; // ios
    localizedDescription?: string; // ios
    localizedFailureReason?: string; // ios
    localizedRecoverySuggestion?: string; // ios
    domain?: string; // ios
  }>;
  target?: Int32; // ios
}>;

export type OnAudioFocusChangedData = Readonly<{
  hasAudioFocus: boolean;
}>;

export interface VideoNativeProps extends ViewProps {
  src?: VideoSrc;
  drm?: Drm;
  adTagUrl?: string;
  allowsExternalPlayback?: boolean; // ios, true
  maxBitRate?: Float;
  resizeMode?: WithDefault<'none' | 'contain' | 'cover' | 'stretch', 'none'>;
  repeat?: boolean;
  automaticallyWaitsToMinimizeStalling?: boolean;
  textTracks?: TextTracks;
  selectedTextTrack?: SelectedTextTrack;
  selectedAudioTrack?: SelectedAudioTrack;
  selectedVideoTrack?: SelectedVideoTrack; // android
  paused?: boolean;
  muted?: boolean;
  controls?: boolean;
  filter?: WithDefault<
    | ''
    | 'CIColorInvert'
    | 'CIColorMonochrome'
    | 'CIColorPosterize'
    | 'CIFalseColor'
    | 'CIMaximumComponent'
    | 'CIMinimumComponent'
    | 'CIPhotoEffectChrome'
    | 'CIPhotoEffectFade'
    | 'CIPhotoEffectInstant'
    | 'CIPhotoEffectMono'
    | 'CIPhotoEffectNoir'
    | 'CIPhotoEffectProcess'
    | 'CIPhotoEffectTonal'
    | 'CIPhotoEffectTransfer'
    | 'CISepiaTone',
    ''
  >;
  filterEnabled?: boolean;
  volume?: Float; // default 1.0
  playInBackground?: boolean;
  preventsDisplaySleepDuringVideoPlayback?: boolean;
  preferredForwardBufferDuration?: Float; //ios, 0
  playWhenInactive?: boolean; // ios, false
  pictureInPicture?: boolean; // ios, false
  ignoreSilentSwitch?: WithDefault<'inherit' | 'ignore' | 'obey', 'inherit'>; // ios, 'inherit'
  mixWithOthers?: WithDefault<'inherit' | 'mix' | 'duck', 'inherit'>; // ios, 'inherit'
  rate?: Float;
  fullscreen?: boolean; // ios, false
  fullscreenAutorotate?: boolean;
  fullscreenOrientation?: WithDefault<'all' | 'landscape' | 'portrait', 'all'>;
  progressUpdateInterval?: Float;
  restoreUserInterfaceForPIPStopCompletionHandler?: boolean;
  localSourceEncryptionKeyScheme?: string;
  debug?: DebugConfig;

  bufferConfig?: BufferConfig; // Android
  contentStartTime?: Int32; // Android
  currentPlaybackTime?: Double; // Android
  disableDisconnectError?: boolean; // Android
  focusable?: boolean; // Android
  hideShutterView?: boolean; //	Android
  minLoadRetryCount?: Int32; // Android
  reportBandwidth?: boolean; //Android
  subtitleStyle?: SubtitleStyle; // android
  trackId?: string; // Android
  useTextureView?: boolean; // Android
  useSecureView?: boolean; // Android
  onVideoLoad?: DirectEventHandler<OnLoadData>;
  onVideoLoadStart?: DirectEventHandler<OnLoadStartData>;
  onVideoAspectRatio?: DirectEventHandler<OnVideoAspectRatioData>;
  onVideoBuffer?: DirectEventHandler<OnBufferData>;
  onVideoError?: DirectEventHandler<OnVideoErrorData>;
  onVideoProgress?: DirectEventHandler<OnProgressData>;
  onVideoBandwidthUpdate?: DirectEventHandler<OnBandwidthUpdateData>;
  onVideoSeek?: DirectEventHandler<OnSeekData>;
  onVideoEnd?: DirectEventHandler<{}>; // all
  onVideoAudioBecomingNoisy?: DirectEventHandler<{}>;
  onVideoFullscreenPlayerWillPresent?: DirectEventHandler<{}>; // ios, android
  onVideoFullscreenPlayerDidPresent?: DirectEventHandler<{}>; // ios, android
  onVideoFullscreenPlayerWillDismiss?: DirectEventHandler<{}>; // ios, android
  onVideoFullscreenPlayerDidDismiss?: DirectEventHandler<{}>; // ios, android
  onReadyForDisplay?: DirectEventHandler<{}>;
  onPlaybackRateChange?: DirectEventHandler<OnPlaybackData>; // all
  onVolumeChange?: DirectEventHandler<OnVolumeChangeData>; // android, ios
  onVideoExternalPlaybackChange?: DirectEventHandler<OnExternalPlaybackChangeData>;
  onGetLicense?: DirectEventHandler<OnGetLicenseData>;
  onPictureInPictureStatusChanged?: DirectEventHandler<OnPictureInPictureStatusChangedData>;
  onRestoreUserInterfaceForPictureInPictureStop?: DirectEventHandler<{}>;
  onReceiveAdEvent?: DirectEventHandler<OnReceiveAdEventData>;
  onVideoPlaybackStateChanged?: DirectEventHandler<OnPlaybackStateChangedData>; // android only
  onVideoIdle?: DirectEventHandler<{}>; // android only (nowhere in document, so do not use as props. just type declaration)
  onAudioFocusChanged?: DirectEventHandler<OnAudioFocusChangedData>; // android only (nowhere in document, so do not use as props. just type declaration)
  onTimedMetadata?: DirectEventHandler<OnTimedMetadataData>; // ios, android
  onAudioTracks?: DirectEventHandler<OnAudioTracksData>; // android
  onTextTracks?: DirectEventHandler<OnTextTracksData>; // android
  onTextTrackDataChanged?: DirectEventHandler<OnTextTrackDataChangedData>; // iOS
  onVideoTracks?: DirectEventHandler<OnVideoTracksData>; // android
}

export type VideoComponentType = HostComponent<VideoNativeProps>;

export type VideoSaveData = {
  uri: string;
};

export interface VideoManagerType {
  save: (option: object, reactTag: number) => Promise<VideoSaveData>;
  seek: (option: Seek, reactTag: number) => Promise<void>;
  setPlayerPauseState: (paused: boolean, reactTag: number) => Promise<void>;
  setLicenseResult: (
    result: string,
    licenseUrl: string,
    reactTag: number,
  ) => Promise<void>;
  setLicenseResultError: (
    error: string,
    licenseUrl: string,
    reactTag: number,
  ) => Promise<void>;
}

export interface VideoDecoderPropertiesType {
  getWidevineLevel: () => Promise<number>;
  isCodecSupported: (
    mimeType: string,
    width: number,
    height: number,
  ) => Promise<'unsupported' | 'hardware' | 'software'>;
  isHEVCSupported: () => Promise<'unsupported' | 'hardware' | 'software'>;
}

export const VideoManager = NativeModules.VideoManager as VideoManagerType;
export const VideoDecoderProperties =
  NativeModules.VideoDecoderProperties as VideoDecoderPropertiesType;

export default codegenNativeComponent<VideoNativeProps>(
  'RCTVideo',
) as VideoComponentType;
