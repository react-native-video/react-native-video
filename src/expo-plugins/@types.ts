export type ConfigProps = {
  /**
   * Whether to require permissions to be able to use notification controls.
   * @default false
   */
  enableNotificationControls?: boolean;
  /**
   * Whether to enable background audio feature.
   * @default false
   */
  enableBackgroundAudio?: boolean;
  /**
   * Whether to include ADS extension in the app (IMA SDK)
   * @default false
   * @see https://thewidlarzgroup.github.io/react-native-video/component/ads
   */
  enableADSExtension?: boolean;
  /**
   * Whether to enable cache extension for ios in the app.
   * @default false
   * @see https://thewidlarzgroup.github.io/react-native-video/other/caching
   */
  enableCacheExtension?: boolean;
  /**
   * Android extensions for ExoPlayer - you can choose which extensions to include in order to reduce the size of the app.
   * @default { useExoplayerRtsp: false, useExoplayerSmoothStreaming: true, useExoplayerDash: true, useExoplayerHls: true }
   */
  androidExtensions?: {
    /**
     * Whether to use ExoPlayer's RTSP extension.
     * @default false
     */
    useExoplayerRtsp?: boolean;
    /**
     * Whether to use ExoPlayer's SmoothStreaming extension.
     * @default true
     */
    useExoplayerSmoothStreaming?: boolean;
    /**
     * Whether to use ExoPlayer's Dash extension.
     * @default true
     */
    useExoplayerDash?: boolean;
    /**
     * Whether to use ExoPlayer's HLS extension.
     * @default true
     */
    useExoplayerHls?: boolean;
  };
};
