## Changelog

### Version 5.2.0

- Fix for tvOS native audio menu language selector
- Update ExoPlayer to allow pre-init and content clear [#2412] (https://github.com/react-native-video/react-native-video/pull/2412)
- iOS rate is reset to 1.0 after play/pause [#2167] (https://github.com/react-native-video/react-native-video/pull/2167)
- Upgrade ExoPlayer to 2.13.2 [#2317] (https://github.com/react-native-video/react-native-video/pull/2317)
- Fix AudioFocus pausing video when attempting to play [#2311] (https://github.com/react-native-video/react-native-video/pull/2311)
- Fix back gesture handler on Android Video Player [#2896] (https://github.com/react-native-video/react-native-video/pull/2896)

### Version 5.1.0-alpha9

- Add ARM64 support for windows [#2137](https://github.com/react-native-community/react-native-video/pull/2137)
- Fix deprecated API bug for windows [#2119](https://github.com/react-native-video/react-native-video/pull/2119)
- Added `rate` property and autolinking support for windows [#2206](https://github.com/react-native-video/react-native-video/pull/2206)

### Version 5.1.0-alpha8

- Fixing ID3 Frame Error When Receiving EventMessage in TimedMetadata [#2116](https://github.com/react-native-community/react-native-video/pull/2116)

### Version 5.1.0-alpha7

- Basic support for DRM on iOS and Android [#1445](https://github.com/react-native-community/react-native-video/pull/1445)

### Version 5.1.0-alpha6 

- Fix iOS bug which would break size of views when video is displayed with controls on a non full-screen React view. [#1931](https://github.com/react-native-community/react-native-video/pull/1931)
- Fix video dimensions being undefined when playing HLS in ios. [#1992](https://github.com/react-native-community/react-native-video/pull/1992)
- Add support for audio mix with other apps for iOS. [#1978](https://github.com/react-native-community/react-native-video/pull/1978)
- Properly implement pending seek for iOS. [#1994](https://github.com/react-native-community/react-native-video/pull/1994)
- Added `preferredForwardBufferDuration` (iOS) - the duration the player should buffer media from the network ahead of the playhead to guard against playback disruption. (#1944)
- Added `currentPlaybackTime` (Android ExoPlayer, iOS) - when playing an HLS live stream with a `EXT-X-PROGRAM-DATE-TIME` tag configured, then this property will contain the epoch value in msec. (#1944)
- Added `trackId` (Android ExoPlayer) - Configure an identifier for the video stream to link the playback context to the events emitted. (#1944)
- Added preventsDisplaySleepDuringVideoPlayback (#2019)
- Reverted the JS fullscreening for Android. [#2013](https://github.com/react-native-community/react-native-video/pull/2013)
- Set iOS request headers without needing to edit RCTVideo.m. [#2014](https://github.com/react-native-community/react-native-video/pull/2014)
- Fix exoplayer aspect ratio update on source changes [#2053](https://github.com/react-native-community/react-native-video/pull/2053)

### Version 5.1.0-alpha5

- Add support for react-native Windows Cpp/WinRT [#1893]((https://github.com/react-native-community/react-native-video/pull/1893))

### Version 5.1.0-alpha4

- Fix android play/pause bug related to full-screen mode [#1916](https://github.com/react-native-community/react-native-video/pull/1916)

### Version 5.1.0-alpha3

- Improve Android Audio Focus [#1897](https://github.com/react-native-community/react-native-video/pull/1897)

### Version 5.1.0-alpha2

- Added support for full-screen functionality in Android Exoplayer [#1730](https://github.com/react-native-community/react-native-video/pull/1730)

### Version 5.1.0-alpha1

- Fixed Exoplayer doesn't work with mute=true (Android). [#1696](https://github.com/react-native-community/react-native-video/pull/1696)
- Added support for automaticallyWaitsToMinimizeStalling property (iOS) [#1723](https://github.com/react-native-community/react-native-video/pull/1723)
- Bump Exoplayer to 2.10.4, remove deprecated usages of Exoplayer methods (Android). [#1753](https://github.com/react-native-community/react-native-video/pull/1753)
- Preserve Exoplayer BandwidthMeter instance across video plays, this should noticeably improve streaming bandwidth detection (Android).

### Version 5.0.2

- Fix crash when RCTVideo's superclass doesn't observe the keyPath 'frame' (iOS) [#1720](https://github.com/react-native-community/react-native-video/pull/1720)

### Version 5.0.1

- Fix AndroidX Support bad merge

### Version 5.0.0 [Deprecated]

- AndroidX Support

### Version 4.4.4

- Handle racing conditions when props are settled on Exoplayer

### Version 4.4.3

- Fix mute/unmute when controls are present (iOS) [#1654](https://github.com/react-native-community/react-native-video/pull/1654)
- Fix Android videos being able to play with background music/audio from other apps.
- Fixed memory leak on iOS when using `controls` [#1647](https://github.com/react-native-community/react-native-video/pull/1647)
- (Android) Update gradle and target SDK [#1629](https://github.com/react-native-community/react-native-video/pull/1629)
- Fix iOS stressed mount/unmount crash [#1646](https://github.com/react-native-community/react-native-video/pull/1646)

### Version 4.4.2

- Change compileOnly to implementation on gradle (for newer gradle versions and react-native 0.59 support) [#1592](https://github.com/react-native-community/react-native-video/pull/1592)
- Replaced RCTBubblingEventBlock events by RCTDirectEventBlock to avoid event name collisions [#1625](https://github.com/react-native-community/react-native-video/pull/1625)
- Added `onPlaybackRateChange` to README [#1578](https://github.com/react-native-community/react-native-video/pull/1578)
- Added `onReadyForDisplay` to README [#1627](https://github.com/react-native-community/react-native-video/pull/1627)
- Improved handling of poster image. Fixes bug with displaying video and poster simultaneously. [#1627](https://github.com/react-native-community/react-native-video/pull/1627)
- Fix background audio stopping on iOS when using `controls` [#1614](https://github.com/react-native-community/react-native-video/pull/1614)

### Version 4.4.1

- Fix tvOS picture-in-picture compilation regression [#1518](https://github.com/react-native-community/react-native-video/pull/1518)
- fullscreen rotation issues with iOS built-in controls [#1441](https://github.com/react-native-community/react-native-video/pull/1441)
- Fix player freeze when playing audio files on ExoPlayer [#1529](https://github.com/react-native-community/react-native-video/pull/1529)

### Version 4.4.0

- Fix runtime warning by replacing `UIManager.RCTVideo` with `UIManager.getViewManagerConfig('RCTVideo')` (and ensuring backwards compat) [#1487](https://github.com/react-native-community/react-native-video/pull/1487)
- Fix loading package resolved videos when using video-caching [#1438](https://github.com/react-native-community/react-native-video/pull/1438)
- Fix "message sent to deallocated instance" crash on ios [#1482](https://github.com/react-native-community/react-native-video/pull/1482)
- Display a warning when source is empty [#1478](https://github.com/react-native-community/react-native-video/pull/1478)
- Don't crash on iOS for an empty source [#1246](https://github.com/react-native-community/react-native-video/pull/1246)
- Recover from from transient internet failures when loading on ExoPlayer [#1448](https://github.com/react-native-community/react-native-video/pull/1448)
- Add controls support for ExoPlayer [#1414](https://github.com/react-native-community/react-native-video/pull/1414)
- Fix check for text tracks when iOS caching enabled [#1387](https://github.com/react-native-community/react-native-video/pull/1387)
- Add support for Picture in Picture on iOS [#1325](https://github.com/react-native-community/react-native-video/pull/1325)
- Fix UIManager undefined variable [#1488](https://github.com/react-native-community/react-native-video/pull/1488)

### Version 4.3.0

- Fix iOS video not displaying after switching source [#1395](https://github.com/react-native-community/react-native-video/pull/1395)
- Add the filterEnabled flag, fixes iOS video start time regression [#1384](https://github.com/react-native-community/react-native-video/pull/1384)
- Fix text not appearing in release builds of Android apps [#1373](https://github.com/react-native-community/react-native-video/pull/1373)
- Update to ExoPlayer 2.9.3 [#1406](https://github.com/react-native-community/react-native-video/pull/1406)
- Add video track selection & onBandwidthUpdate [#1199](https://github.com/react-native-community/react-native-video/pull/1199)
- Recovery from transient internet failures and props to configure the custom retry count [#1448](https://github.com/react-native-community/react-native-video/pull/1448)

### Version 4.2.0

- Don't initialize filters on iOS unless a filter is set. This was causing a startup performance regression [#1360](https://github.com/react-native-community/react-native-video/pull/1360)
- Support setting the maxBitRate [#1310](https://github.com/react-native-community/react-native-video/pull/1310)
- Fix useTextureView not defaulting to true [#1383](https://github.com/react-native-community/react-native-video/pull/1383)
- Fix crash on MediaPlayer w/ Android 4.4 & avoid memory leak [#1328](https://github.com/react-native-community/react-native-video/pull/1328)

### Version 4.1.0

- Generate onSeek on Android ExoPlayer & MediaPlayer after seek completes [#1351](https://github.com/react-native-community/react-native-video/pull/1351)
- Remove unneeded onVideoSaved event [#1350](https://github.com/react-native-community/react-native-video/pull/1350)
- Disable AirPlay if sidecar text tracks are enabled [#1304](https://github.com/react-native-community/react-native-video/pull/1304)
- Add possibility to remove black screen while video is loading in Exoplayer [#1355](https://github.com/react-native-community/react-native-video/pull/1355)

### Version 4.0.1

- Add missing files to package.json [#1342](https://github.com/react-native-community/react-native-video/pull/1342)

### Version 4.0.0

- Partial support for timed metadata on Android MediaPlayer [#707](https://github.com/react-native-community/react-native-video/pull/707)
- Support video caching for iOS [#955](https://github.com/react-native-community/react-native-video/pull/955)
- Video caching cleanups [#1172](https://github.com/react-native-community/react-native-video/pull/1172)
- Add ipod-library support [#926](https://github.com/react-native-community/react-native-video/pull/926/files)
- Fix crash on ExoPlayer when there are no audio tracks [#1233](https://github.com/react-native-community/react-native-video/pull/1233)
- Reduce package size [#1231](https://github.com/react-native-community/react-native-video/pull/1231)
- Remove unnecessary import in TextTrackType [#1229](https://github.com/react-native-community/react-native-video/pull/1229)
- Prevent flash between poster and video [#1167](https://github.com/react-native-community/react-native-video/pull/1167)
- Support react-native-dom [#1253](https://github.com/react-native-community/react-native-video/pull/1253)
- Update to ExoPlayer 2.8.2. Android SDK 26 now required [#1170](https://github.com/react-native-community/react-native-video/pull/1170)
- Update to ExoPlayer 2.8.4 [#1266](https://github.com/react-native-community/react-native-video/pull/1266)
- Add fullscreenOrientation option for iOS [#1215](https://github.com/react-native-community/react-native-video/pull/1215)
- Update to ExoPlayer 2.9.0 [#1285](https://github.com/react-native-community/react-native-video/pull/1285)
- Switch useTextureView to default to `true` [#1286](https://github.com/react-native-community/react-native-video/pull/1286)
- Re-add fullscreenAutorotate prop [#1303](https://github.com/react-native-community/react-native-video/pull/1303)
- Make seek throw a useful error for NaN values [#1283](https://github.com/react-native-community/react-native-video/pull/1283)
- Video Filters and Save Video [#1306](https://github.com/react-native-community/react-native-video/pull/1306)
- Fix: volume should not change on onAudioFocusChange event [#1327](https://github.com/react-native-community/react-native-video/pull/1327)
- Update ExoPlayer to 2.9.1 and OkHTTP to 3.12.0 [#1338](https://github.com/react-native-community/react-native-video/pull/1338)

### Version 3.2.0

- Basic fullscreen support for Android MediaPlayer [#1138](https://github.com/react-native-community/react-native-video/pull/1138)
- Simplify default Android SDK code [#1145](https://github.com/react-native-community/react-native-video/pull/1145) [#1146](https://github.com/react-native-community/react-native-video/pull/1146)
- Various iOS sideloaded text track fixes [#1157](https://github.com/react-native-community/react-native-video/pull/1157)
- Fix #1150 where assets with bundled assets don't work on iOS in release mode [#1162](https://github.com/react-native-community/react-native-video/pull/1162)
- Support configuring the buffer on Android ExoPlayer [#1160](https://github.com/react-native-community/react-native-video/pull/1160)
- Prevent sleep from sleeping while videos are playing on Android MediaPlayer [#1117](https://github.com/react-native-community/react-native-video/pull/1117)
- Update NewtonSoft JSON to match react-native-windows version [#1169](https://github.com/react-native-community/react-native-video/pull/1169)

### Version 3.1.0

- Support sidecar text tracks on iOS [#1109](https://github.com/react-native-community/react-native-video/pull/1109)
- Support onAudioBecomingNoisy on iOS [#1131](https://github.com/react-native-community/react-native-video/pull/1131)

### Version 3.0

- Inherit Android buildtools and SDK version from the root project [#1081](https://github.com/react-native-community/react-native-video/pull/1081)
- Automatically play on ExoPlayer when the paused prop is not set [#1083](https://github.com/react-native-community/react-native-video/pull/1083)
- Preserve Android MediaPlayer paused prop when backgrounding [#1082](https://github.com/react-native-community/react-native-video/pull/1082)
- Support specifying headers on ExoPlayer as part of the source [#805](https://github.com/react-native-community/react-native-video/pull/805)
- Prevent iOS onLoad event during seeking [#1088](https://github.com/react-native-community/react-native-video/pull/1088)
- ExoPlayer playableDuration incorrect [#1089](https://github.com/react-native-community/react-native-video/pull/1089)

### Version 2.3.1

- Revert PR to inherit Android SDK versions from root project. Re-add in 3.0 [#1080](https://github.com/react-native-community/react-native-video/pull/1080)

### Version 2.3.0

- Support allowsExternalPlayback on iOS [#1057](https://github.com/react-native-community/react-native-video/pull/1057)
- Inherit Android buildtools and SDK version from the root project [#999](https://github.com/react-native-community/react-native-video/pull/999)
- Fix bug that caused ExoPlayer to start paused if playInBackground was set [#833](https://github.com/react-native-community/react-native-video/pull/833)
- Fix crash if clearing an observer on iOS that was already cleared [#1075](https://github.com/react-native-community/react-native-video/pull/1075)
- Add audioOnly prop for music files [#1039](https://github.com/react-native-community/react-native-video/pull/1039)
- Support seeking with more exact tolerance on iOS [#1076](https://github.com/react-native-community/react-native-video/pull/1076)

### Version 2.2.0

- Text track selection support for iOS & ExoPlayer [#1049](https://github.com/react-native-community/react-native-video/pull/1049)
- Support outputting to a TextureView on Android ExoPlayer [#1058](https://github.com/react-native-community/react-native-video/pull/1058)
- Support changing the left/right balance on Android MediaPlayer [#1051](https://github.com/react-native-community/react-native-video/pull/1051)
- Prevent multiple onEnd notifications on iOS [#832](https://github.com/react-native-community/react-native-video/pull/832)
- Fix doing a partial swipe on iOS causing a black screen [#1048](https://github.com/react-native-community/react-native-video/pull/1048)
- Fix crash when switching to a new source on iOS [#974](https://github.com/react-native-community/react-native-video/pull/974)
- Add cookie support for ExoPlayer [#922](https://github.com/react-native-community/react-native-video/pull/922)
- Remove ExoPlayer onMetadata that wasn't being used [#1040](https://github.com/react-native-community/react-native-video/pull/1040)
- Fix bug where setting the progress interval on iOS didn't work [#800](https://github.com/react-native-community/react-native-video/pull/800)
- Support setting the poster resize mode [#595](https://github.com/react-native-community/react-native-video/pull/595)
