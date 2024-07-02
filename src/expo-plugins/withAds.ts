import {
  withGradleProperties,
  type ConfigPlugin,
  withDangerousMod,
} from '@expo/config-plugins';
import {writeToPodfile} from './writeToPodfile';

/**
 * Sets whether to enable the IMA SDK to use ADS with `react-native-video`.
 */
export const withAds: ConfigPlugin<boolean> = (c, enableADSExtension) => {
  const android_key = 'RNVideo_useExoplayerIMA';
  const ios_key = 'RNVideoUseGoogleIMA';

  // -------------------- ANDROID --------------------
  const configWithAndroid = withGradleProperties(c, (config) => {
    config.modResults = config.modResults.filter((item) => {
      if (item.type === 'property' && item.key === android_key) {
        return false;
      }
      return true;
    });

    config.modResults.push({
      type: 'property',
      key: android_key,
      value: enableADSExtension.toString(),
    });

    return config;
  });

  // -------------------- IOS --------------------
  const complectedConfig = withDangerousMod(configWithAndroid, [
    'ios',
    (config) => {
      writeToPodfile(
        config.modRequest.projectRoot,
        ios_key,
        enableADSExtension.toString(),
      );
      return config;
    },
  ]);

  return complectedConfig;
};
