# Expo

## Expo plugin
From version `6.3.1`, we have added support for expo plugin. You can configure `react-native-video` properties in `app.json` (or `app.config.json` or `app.config.js`) file.
It's useful when you are using `expo` managed workflow (expo prebuild) as it will automatically configure `react-native-video` properties in native part of the expo project.

```json
// app.json
{
  {
  "name": "my app",
  "plugins": [
    [
      "react-native-video",
      {
        // ...
        "enableNotificationControls": true,
        "androidExtensions": {
          "useExoplayerRtsp": false,
          "useExoplayerSmoothStreaming": false,
          "useExoplayerHls": false,
          "useExoplayerDash": false,
        }
        // ...
      }
    ]
  ]
}
}
```

## Expo Plugin Properties

| Property | Type | Default | Description |
| --- | --- | --- | --- |
| enableNotificationControls | boolean | false | Add required changes on android to use notification controls for video player |
| enableBackgroundAudio | boolean | false | Add required changes to play video in background on iOS |
| enableADSExtension | boolean | false | Add required changes to use ads extension for video player |
| enableCacheExtension | boolean | false | Add required changes to use cache extension for video player on iOS |
| androidExtensions | object | {} | You can enable/disable extensions as per your requirement - this allow to reduce library size on android |