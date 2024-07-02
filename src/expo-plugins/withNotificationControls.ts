import {withAndroidManifest, type ConfigPlugin} from '@expo/config-plugins';

export const withNotificationControls: ConfigPlugin<boolean> = (
  c,
  enableNotificationControls,
) => {
  return withAndroidManifest(c, (config) => {
    const manifest = config.modResults.manifest;

    if (!enableNotificationControls) {
      return config;
    }

    if (!manifest.application) {
      console.warn(
        'AndroidManifest.xml is missing an <application> element - skipping adding notification controls related config.',
      );
      return config;
    }

    // Add the service to the AndroidManifest.xml
    manifest.application.map((application) => {
      if (!application.service) {
        application.service = [];
      }

      application.service.push({
        $: {
          'android:name': 'com.brentvatne.exoplayer.VideoPlaybackService',
          'android:exported': 'false',
          // @ts-expect-error: 'android:foregroundServiceType' does not exist in type 'ManifestServiceAttributes'.
          'android:foregroundServiceType': 'mediaPlayback',
        },
        'intent-filter': [
          {
            action: [
              {
                $: {
                  'android:name': 'androidx.media3.session.MediaSessionService',
                },
              },
            ],
          },
        ],
      });

      return application;
    });

    return config;
  });
};
