import type {RefObject} from 'react';

export type VideoSaveData = {
  uri: string;
};

export interface VideoRef {
  seek: (time: number, tolerance?: number) => void;
  resume: () => void;
  pause: () => void;
  presentFullscreenPlayer: () => void;
  dismissFullscreenPlayer: () => void;
  restoreUserInterfaceForPictureInPictureStopCompleted: (
    restore: boolean,
  ) => void;
  save: (options: object) => Promise<VideoSaveData>;
  setVolume: (volume: number) => void;
  getCurrentPosition: () => Promise<number>;
  setFullScreen: (fullScreen: boolean) => void;
  nativeHtmlRef?: RefObject<HTMLVideoElement>; // web only
}
