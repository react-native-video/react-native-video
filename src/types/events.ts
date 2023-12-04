import type Orientation from './Orientation';
import type {AdEvent} from './Ads';

export type OnLoadData = Readonly<{
  currentTime: number;
  duration: number;
  naturalSize: Readonly<{
    width: number;
    height: number;
    orientation: Orientation;
  }>;
}> &
  OnAudioTracksData &
  OnTextTracksData;

export type OnVideoAspectRatioData = Readonly<{
  width: number;
  height: number;
}>;

export type OnLoadStartData = Readonly<{
  isNetwork: boolean;
  type: string;
  uri: string;
}>;

export type OnProgressData = Readonly<{
  currentTime: number;
  playableDuration: number;
  seekableDuration: number;
}>;

export type OnSeekData = Readonly<{
  currentTime: number;
  seekTime: number;
}>;

export type OnPlaybackStateChangedData = Readonly<{
  isPlaying: boolean;
}>;

export type OnTimedMetadataData = Readonly<{
  metadata: ReadonlyArray<
    Readonly<{
      value?: string;
      identifier: string;
    }>
  >;
}>;

export type AudioTrack = Readonly<{
  index: number;
  title?: string;
  language?: string;
  bitrate?: number;
  type?: string;
  selected?: boolean;
}>;

export type OnAudioTracksData = Readonly<{
  audioTracks: ReadonlyArray<AudioTrack>;
}>;

export enum OnTextTracksTypeData {
  SRT = 'srt',
  TTML = 'ttml',
  VTT = 'vtt',
}

export type TextTrack = Readonly<{
  index: number;
  title?: string;
  language?: string;
  type?: OnTextTracksTypeData;
  selected?: boolean;
}>;

export type OnTextTracksData = Readonly<{
  textTracks: ReadonlyArray<TextTrack>;
}>;

export type OnVideoTracksData = Readonly<{
  videoTracks: ReadonlyArray<
    Readonly<{
      trackId: string;
      codecs?: string;
      width?: number;
      height?: number;
      bitrate?: number;
      selected?: boolean;
    }>
  >;
}>;

export type OnPlaybackData = Readonly<{
  playbackRate: number;
}>;

export type OnVolumeChangeData = Readonly<{
  volume: number;
}>;

export type OnExternalPlaybackChangeData = Readonly<{
  isExternalPlaybackActive: boolean;
}>;

export type OnGetLicenseData = Readonly<{
  licenseUrl: string;
  contentId: string;
  spcBase64: string;
}>;

export type OnPictureInPictureStatusChangedData = Readonly<{
  isActive: boolean;
}>;

export type OnReceiveAdEventData = Readonly<{
  data?: Record<string, string>;
  event: AdEvent;
}>;

export type OnVideoErrorData = Readonly<{
  error: OnVideoErrorDataDetails;
  target?: number; // ios
}>;

export type OnVideoErrorDataDetails = Readonly<{
  errorString?: string; // android
  errorException?: string; // android
  errorStackTrace?: string; // android
  errorCode?: string; // android
  error?: string; // ios
  code?: number; // ios
  localizedDescription?: string; // ios
  localizedFailureReason?: string; // ios
  localizedRecoverySuggestion?: string; // ios
  domain?: string; // ios
}>;
export type OnAudioFocusChangedData = Readonly<{
  hasAudioFocus: boolean;
}>;

export type OnBufferData = Readonly<{isBuffering: boolean}>;

export type OnBandwidthUpdateData = Readonly<
  | {
      bitrate: number;
      width: number;
      height: number;
      trackId: number;
    }
  | {bitrate: number}
>;

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
  ) => void; //iOS
  onPlaybackRateChange?: (e: OnPlaybackData) => void; //All
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
  onVideoTracks?: (e: OnVideoTracksData) => void; //Android
  onAspectRatio?: (e: OnVideoAspectRatioData) => void;
}
