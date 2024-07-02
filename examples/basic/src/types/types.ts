import {Drm, ReactVideoSource, TextTracks} from 'react-native-video';

export type AdditionalSourceInfo = {
  textTracks: TextTracks;
  adTagUrl: string;
  description: string;
  drm: Drm;
  noView: boolean;
};

export type SampleVideoSource = ReactVideoSource | AdditionalSourceInfo;
