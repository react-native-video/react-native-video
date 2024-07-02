# A `<Video>` component for React Native

## About
`react-native-video` is a React Native library that provides a Video component that renders media content such as videos and streams

It allows to stream video files (m3u, mpd, mp4, ...) inside your react native application.

- Exoplayer for android
- AVplayer for iOS, tvOS and visionOS
- Windows UWP for windows
- HTML5 for web
- Trick mode support
- Subtitles (embeded or side loaded)
- DRM support
- Client side Ads insertion (via google IMA)
- Pip (ios)
- Embedded playback controls
- And much more

The aim of this package is to have a thin and exhaustive control of player.

## V6.0.0 Information
> ⚠️ **Version 6**: The following documentation refer to features only available through the v6.0.0 releases.
> As major rework has been done in case of doubt, you can still use [version 5.2.x, see documentation](https://github.com/TheWidlarzGroup/react-native-video/blob/v5.2.0/README.md)

Version 6.x requires **react-native >= 0.68.2**
> ⚠️ from **6.0.0-beta.8** requires also **iOS >= 13.0** (default in react-native 0.73)

For older versions of react-native, [please use version 5.x](https://github.com/TheWidlarzGroup/react-native-video/tree/v5.2.0).

## Usage

```javascript
// Load the module

import Video, {VideoRef} from 'react-native-video';

// Within your render function, assuming you have a file called
// "background.mp4" in your project. You can include multiple videos
// on a single screen if you like.

const VideoPlayer = () => {
 const videoRef = useRef<VideoRef>(null);
 const background = require('./background.mp4');

 return (
   <Video 
    // Can be a URL or a local file.
    source={background}
    // Store reference  
    ref={videoRef}
    // Callback when remote video is buffering                                      
    onBuffer={onBuffer}
    // Callback when video cannot be loaded              
    onError={onError}               
    style={styles.backgroundVideo}
   />
 )
}

// Later on in your styles..
var styles = StyleSheet.create({
  backgroundVideo: {
    position: 'absolute',
    top: 0,
    left: 0,
    bottom: 0,
    right: 0,
  },
});
```
