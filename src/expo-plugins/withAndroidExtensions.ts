import {withGradleProperties, type ConfigPlugin} from '@expo/config-plugins';
import type {ConfigProps} from './@types';

/**
 * Sets the Android extensions for ExoPlayer in `gradle.properties`.
 * You can choose which extensions to include in order to reduce the size of the app.
 */
export const withAndroidExtensions: ConfigPlugin<
  ConfigProps['androidExtensions']
> = (c, androidExtensions) => {
  const keys = [
    'RNVideo_useExoplayerRtsp',
    'RNVideo_useExoplayerSmoothStreaming',
    'RNVideo_useExoplayerDash',
    'RNVideo_useExoplayerHls',
  ];

  if (!androidExtensions) {
    androidExtensions = {
      useExoplayerRtsp: false,
      useExoplayerSmoothStreaming: true,
      useExoplayerDash: true,
      useExoplayerHls: true,
    };
  }

  return withGradleProperties(c, (config) => {
    config.modResults = config.modResults.filter((item) => {
      if (item.type === 'property' && keys.includes(item.key)) {
        return false;
      }
      return true;
    });

    for (const key of keys) {
      const valueKey = key.replace(
        'RNVideo_',
        '',
      ) as keyof typeof androidExtensions;
      const value = androidExtensions
        ? androidExtensions[valueKey] ?? false
        : false;

      config.modResults.push({
        type: 'property',
        key,
        value: value.toString(),
      });
    }

    return config;
  });
};
