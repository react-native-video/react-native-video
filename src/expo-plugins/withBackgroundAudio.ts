import {withInfoPlist, type ConfigPlugin} from '@expo/config-plugins';

/**
 * Sets `UIBackgroundModes` in `Info.plist` to enable background audio on Apple platforms.
 * This is required for audio to continue playing when the app is in the background.
 */
export const withBackgroundAudio: ConfigPlugin<boolean> = (
  c,
  enableBackgroundAudio,
) => {
  return withInfoPlist(c, (config) => {
    const modes = config.modResults.UIBackgroundModes || [];

    if (enableBackgroundAudio) {
      if (!modes.includes('audio')) {
        modes.push('audio');
      }
    } else {
      config.modResults.UIBackgroundModes = modes.filter(
        (mode: string) => mode !== 'audio',
      );
    }

    return config;
  });
};
