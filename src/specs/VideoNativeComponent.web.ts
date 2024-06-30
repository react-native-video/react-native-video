/// <reference lib="dom" />
import type {VideoDecoderPropertiesType} from './VideoNativeComponent';

class VideoDecoderProperties implements VideoDecoderPropertiesType {
  async getWidevineLevel() {
    return 0;
  }

  static canPlay(codec: string): boolean {
    // most chrome based browser (and safari I think) supports matroska but reports they do not.
    // for those browsers, only check the codecs and not the container.
    if (navigator.userAgent.search('Firefox') === -1) {
      codec = codec.replace('video/x-matroska', 'video/mp4');
    }

    // Find any video element that we could use to check, or create one that will
    // instantly be garbage collected
    const videos = document.getElementsByTagName('video');
    const video = videos.item(0) ?? document.createElement('video');

    return !!video.canPlayType(codec);
  }

  async isCodecSupported(
    mimeType: string,
    _width: number,
    _height: number,
  ): Promise<'unsupported' | 'hardware' | 'software'> {
    // TODO: Figure out if we can get hardware support information
    return VideoDecoderProperties.canPlay(mimeType)
      ? 'software'
      : 'unsupported';
  }

  async isHEVCSupported(): Promise<'unsupported' | 'hardware' | 'software'> {
    // Just a dummy vidoe mime type codec with HEVC to check.
    return VideoDecoderProperties.canPlay(
      'video/x-matroska; codecs="hvc1.1.4.L96.BO"',
    )
      ? 'software'
      : 'unsupported';
  }
}

export default VideoDecoderProperties;
