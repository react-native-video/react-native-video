import type {ISO639_1} from './language';
import type {ReactVideoEvents} from './events';
import type {StyleProp, ViewProps, ViewStyle} from 'react-native';
import type VideoResizeMode from './ResizeMode';
import type FilterType from './FilterType';

export type Headers = Record<string, string>;

export type EnumValues<T extends string | number> = T extends string
  ? `${T}` | T
  : T;

export type ReactVideoSourceProperties = {
  uri?: string;
  isNetwork?: boolean;
  isAsset?: boolean;
  shouldCache?: boolean;
  type?: string;
  mainVer?: number;
  patchVer?: number;
  headers?: Headers;
  startPosition?: number;
  cropStart?: number;
  cropEnd?: number;
  title?: string;
  subtitle?: string;
  description?: string;
  customImageUri?: string;
};

export type ReactVideoSource = Readonly<
  Omit<ReactVideoSourceProperties, 'uri'> & {
    uri?: string | NodeRequire;
  }
>;

export type DebugConfig = Readonly<{
  enable?: boolean;
  thread?: boolean;
}>;

export enum DRMType {
  WIDEVINE = 'widevine',
  PLAYREADY = 'playready',
  CLEARKEY = 'clearkey',
  FAIRPLAY = 'fairplay',
}

export type Drm = Readonly<{
  type?: DRMType;
  licenseServer?: string;
  headers?: Headers;
  contentId?: string; // ios
  certificateUrl?: string; // ios
  base64Certificate?: boolean; // ios default: false
  /* eslint-disable @typescript-eslint/no-unused-vars */
  getLicense?: (
    licenseUrl: string,
    loadedLicenseUrl: string,
    contentId: string,
    spcBase64: string,
  ) => void; // ios
  /* eslint-enable @typescript-eslint/no-unused-vars */
}>;

export type BufferConfig = {
  minBufferMs?: number;
  maxBufferMs?: number;
  bufferForPlaybackMs?: number;
  bufferForPlaybackAfterRebufferMs?: number;
  maxHeapAllocationPercent?: number;
  minBackBufferMemoryReservePercent?: number;
  minBufferMemoryReservePercent?: number;
};

export enum SelectedTrackType {
  SYSTEM = 'system',
  DISABLED = 'disabled',
  TITLE = 'title',
  LANGUAGE = 'language',
  INDEX = 'index',
}

export type SelectedTrack = {
  type: SelectedTrackType;
  value?: string | number;
};

export enum SelectedVideoTrackType {
  AUTO = 'auto',
  DISABLED = 'disabled',
  RESOLUTION = 'resolution',
  INDEX = 'index',
}

export type SelectedVideoTrack = {
  type: SelectedVideoTrackType;
  value?: number;
};

export type SubtitleStyle = {
  fontSize?: number;
  paddingTop?: number;
  paddingBottom?: number;
  paddingLeft?: number;
  paddingRight?: number;
  opacity?: number;
};

export enum TextTracksType {
  SUBRIP = 'application/x-subrip',
  TTML = 'application/ttml+xml',
  VTT = 'text/vtt',
}

export type TextTracks = {
  title: string;
  language: ISO639_1;
  type: TextTracksType;
  uri: string;
}[];

export type TextTrackType =
  | 'system'
  | 'disabled'
  | 'title'
  | 'language'
  | 'index';

export type SelectedTextTrack = Readonly<{
  type: TextTrackType;
  value?: string | number;
}>;

export type AudioTrackType =
  | 'system'
  | 'disabled'
  | 'title'
  | 'language'
  | 'index';

export type SelectedAudioTrack = Readonly<{
  type: AudioTrackType;
  value?: string | number;
}>;

export type Chapters = {
  title: string;
  startTime: number;
  endTime: number;
  uri?: string;
};

export enum FullscreenOrientationType {
  ALL = 'all',
  LANDSCAPE = 'landscape',
  PORTRAIT = 'portrait',
}

export enum IgnoreSilentSwitchType {
  INHERIT = 'inherit',
  IGNORE = 'ignore',
  OBEY = 'obey',
}

export enum MixWithOthersType {
  INHERIT = 'inherit',
  MIX = 'mix',
  DUCK = 'duck',
}

export enum PosterResizeModeType {
  CONTAIN = 'contain',
  CENTER = 'center',
  COVER = 'cover',
  NONE = 'none',
  REPEAT = 'repeat',
  STRETCH = 'stretch',
}

export type AudioOutput = 'speaker' | 'earpiece';

export interface ReactVideoProps extends ReactVideoEvents, ViewProps {
  source?: ReactVideoSource;
  drm?: Drm;
  style?: StyleProp<ViewStyle>;
  adTagUrl?: string;
  audioOnly?: boolean;
  audioOutput?: AudioOutput; // Mobile
  automaticallyWaitsToMinimizeStalling?: boolean; // iOS
  backBufferDurationMs?: number; // Android
  bufferConfig?: BufferConfig; // Android
  chapters?: Chapters[]; // iOS
  contentStartTime?: number; // Android
  controls?: boolean;
  currentPlaybackTime?: number; // Android
  disableFocus?: boolean;
  disableDisconnectError?: boolean; // Android
  filter?: EnumValues<FilterType>; // iOS
  filterEnabled?: boolean; // iOS
  focusable?: boolean; // Android
  fullscreen?: boolean; // iOS
  fullscreenAutorotate?: boolean; // iOS
  fullscreenOrientation?: EnumValues<FullscreenOrientationType>; // iOS
  hideShutterView?: boolean; //	Android
  ignoreSilentSwitch?: EnumValues<IgnoreSilentSwitchType>; // iOS
  minLoadRetryCount?: number; // Android
  maxBitRate?: number;
  mixWithOthers?: EnumValues<MixWithOthersType>; // iOS
  muted?: boolean;
  paused?: boolean;
  pictureInPicture?: boolean;
  playInBackground?: boolean;
  playWhenInactive?: boolean; // iOS
  poster?: string;
  posterResizeMode?: EnumValues<PosterResizeModeType>;
  preferredForwardBufferDuration?: number; // iOS
  preventsDisplaySleepDuringVideoPlayback?: boolean;
  progressUpdateInterval?: number;
  rate?: number;
  repeat?: boolean;
  reportBandwidth?: boolean; //Android
  resizeMode?: EnumValues<VideoResizeMode>;
  selectedAudioTrack?: SelectedTrack;
  selectedTextTrack?: SelectedTrack;
  selectedVideoTrack?: SelectedVideoTrack; // android
  subtitleStyle?: SubtitleStyle; // android
  shutterColor?: string; // Android
  textTracks?: TextTracks;
  testID?: string;
  trackId?: string; // Android
  useTextureView?: boolean; // Android
  useSecureView?: boolean; // Android
  volume?: number;
  localSourceEncryptionKeyScheme?: string;
  debug?: DebugConfig;
  allowsExternalPlayback?: boolean; // iOS
}
