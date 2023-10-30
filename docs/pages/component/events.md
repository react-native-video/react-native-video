# Events
This page shows the list of available callbacks to handle player notifications

## List
| Name                                                                                            | Platforms Support         | 
|-------------------------------------------------------------------------------------------------|---------------------------|
| [onAudioBecomingNoisy](#onaudiobecomingnoisy)                                                   | Android, iOS              |
| [onAudioFocusChanged](#onaudiofocuschanged)                                                     | Android                   |
| [onAudioTracks](#onaudiotracks)                                                                 | Android                   |
| [onBandwidthUpdate](#onbandwidthupdate)                                                         | Android                   |
| [onBuffer](#onbuffer)                                                                           | Android, iOS              |
| [onEnd](#onend)                                                                                 | All                       |
| [onError](#onerror)                                                                             | Android, iOS, Windows UWP |
| [onExternalPlaybackChange](#onexternalplaybackchange)                                           | iOS                       |
| [onFullscreenPlayerWillPresent](#onfullscreenplayerwillpresent)                                 | Android, iOS              |
| [onFullscreenPlayerDidPresent](#onfullscreenplayerdidpresent)                                   | Android, iOS              |
| [onFullscreenPlayerWillDismiss](#onfullscreenplayerwilldismiss)                                 | Android, iOS              |
| [onFullscreenPlayerDidDismiss](#onfullscreenplayerdiddismiss)                                   | Android, iOS              |
| [onLoad](#onload)                                                                               | All                       |
| [onLoadStart](#onloadstart)                                                                     | All                       |
| [onPictureInPictureStatusChanged](#onpictureinpicturestatuschanged)                             | iOS                       |
| [onPlaybackRateChange](#onplaybackratechange)                                                   | All                       |
| [onPlaybackStateChanged](#onplaybackstatechanged)                                               | Android, iOS              |
| [onProgress](#onprogress)                                                                       | All                       |
| [onReadyForDisplay](#onreadyfordisplay)                                                         | Android, iOS, Web         |
| [onReceiveAdEvent](#onreceiveadevent)                                                           | Android, iOS              |
| [onRestoreUserInterfaceForPictureInPictureStop](#onrestoreuserinterfaceforpictureinpicturestop) | iOS                       |
| [onSeek](#onseek)                                                                               | Android, iOS, Windows UWP |
| [onTimedMetadata](#ontimedmetadata)                                                             | Android, iOS              |
| [onTextTracks](#ontexttracks)                                                                   | Android                   |
| [onVideoTracks](#onvideotracks)                                                                 | Android                   |


## Details
### `onAudioBecomingNoisy`
Callback function that is called when the audio is about to become 'noisy' due to a change in audio outputs. Typically this is called when audio output is being switched from an external source like headphones back to the internal speaker. It's a good idea to pause the media when this happens so the speaker doesn't start blasting sound.

Payload: none

Platforms: Android, iOS

### `onAudioFocusChanged`
Callback function that is called when the audio focus changes. This is called when the audio focus is gained or lost. This is useful for determining if the media should be paused or not.

Payload:
Property | Type | Description
--- | --- | ---
hasAudioFocus | boolean | Boolean indicating whether the media has audio focus

Example:
```javascript
{
  hasAudioFocus: true
}
```

### `onAudioTracks`
Callback function that is called when audio tracks change

Payload:

An **array** of
Property | Type | Description
--- | --- | ---
index | number | Index number of the track
title | string | Description of the track
language | string | 2 letter [ISO 639-1](https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes) or 3 letter [ISO 639-2](https://en.wikipedia.org/wiki/List_of_ISO_639-2_codes) language code
type | string | Mime type of track

Example:
```javascript
{
  audioTracks: [
    { language: 'es', title: 'Spanish', type: 'audio/mpeg', index: 0 },
    { language: 'en', title: 'English', type: 'audio/mpeg', index: 1 }
  ]
}
```

### `onBandwidthUpdate`
Callback function that is called when the available bandwidth changes.

Payload:

Property | Type | Description
--- | --- | ---
bitrate | number | The estimated bitrate in bits/sec

Example:
```javascript
{
  bitrate: 1000000
}
```

Note: On Android, you must set the [reportBandwidth](#reportbandwidth) prop to enable this event. This is due to the high volume of events generated.

Platforms: Android

### `onBuffer`
Callback function that is called when the player buffers.

Payload:

Property | Type | Description
--- | --- | ---
isBuffering | boolean | Boolean indicating whether buffering is active

Example:
```javascript
{
  isBuffering: true
}
```

Platforms: Android, iOS

### `onEnd`
Callback function that is called when the player reaches the end of the media.

Payload: none

Platforms: all

### `onError`
Callback function that is called when the player experiences a playback error.

Payload:

Property | Type | Description
--- | --- | ---
error | object | Object containing properties with information about the error

Platforms: all

### `onExternalPlaybackChange`
Callback function that is called when external playback mode for current playing video has changed. Mostly useful when connecting/disconnecting to Apple TV – it's called on connection/disconnection.

Payload:

Property | Type | Description
--- | --- | ---
isExternalPlaybackActive | boolean | Boolean indicating whether external playback mode is active

Example:
```javascript
{
  isExternalPlaybackActive: true
}
```

Platforms: iOS

### `onFullscreenPlayerWillPresent`
Callback function that is called when the player is about to enter fullscreen mode.

Payload: none

Platforms: Android, iOS

### `onFullscreenPlayerDidPresent`
Callback function that is called when the player has entered fullscreen mode.

Payload: none

Platforms: Android, iOS

### `onFullscreenPlayerWillDismiss`
Callback function that is called when the player is about to exit fullscreen mode.

Payload: none

Platforms: Android, iOS

### `onFullscreenPlayerDidDismiss`
Callback function that is called when the player has exited fullscreen mode.

Payload: none

Platforms: Android, iOS

### `onLoad`
Callback function that is called when the media is loaded and ready to play.

Payload:

Property | Type | Description
--- | --- | ---
currentTime | number | Time in seconds where the media will start
duration | number | Length of the media in seconds
naturalSize | object | Properties:<br> * width - Width in pixels that the video was encoded at<br> * height - Height in pixels that the video was encoded at<br> * orientation - "portrait" or "landscape"
audioTracks | array | An array of audio track info objects with the following properties:<br> * index - Index number<br> * title - Description of the track<br> * language - 2 letter [ISO 639-1](https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes) or 3 letter [ISO639-2](https://en.wikipedia.org/wiki/List_of_ISO_639-2_codes) language code<br> * type - Mime type of track
textTracks | array | An array of text track info objects with the following properties:<br> * index - Index number<br> * title - Description of the track<br> * language - 2 letter [ISO 639-1](https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes) or 3 letter [ISO 639-2](https://en.wikipedia.org/wiki/List_of_ISO_639-2_codes) language code<br> * type - Mime type of track
videoTracks | array | An array of video track info objects with the following properties:<br> * trackId - ID for the track<br> * bitrate - Bit rate in bits per second<br> * codecs - Comma separated list of codecs<br> * height - Height of the video<br> * width - Width of the video

Example:
```javascript
{ 
  canPlaySlowForward: true,
  canPlayReverse: false,
  canPlaySlowReverse: false,
  canPlayFastForward: false,
  canStepForward: false,
  canStepBackward: false,
  currentTime: 0,
  duration: 5910.208984375,
  naturalSize: {
     height: 1080
     orientation: 'landscape'
     width: '1920'
  },
  audioTracks: [
    { language: 'es', title: 'Spanish', type: 'audio/mpeg', index: 0 },
    { language: 'en', title: 'English', type: 'audio/mpeg', index: 1 }
  ],
  textTracks: [
    { title: '#1 French', language: 'fr', index: 0, type: 'text/vtt' },
    { title: '#2 English CC', language: 'en', index: 1, type: 'text/vtt' },
    { title: '#3 English Director Commentary', language: 'en', index: 2, type: 'text/vtt' }
  ],
  videoTracks: [
    { bitrate: 3987904, codecs: "avc1.640028", height: 720, trackId: "f1-v1-x3", width: 1280 },
    { bitrate: 7981888, codecs: "avc1.640028", height: 1080, trackId: "f2-v1-x3", width: 1920 },
    { bitrate: 1994979, codecs: "avc1.4d401f", height: 480, trackId: "f3-v1-x3", width: 848 }
  ]
}
```

Platforms: all

### `onLoadStart`
Callback function that is called when the media starts loading.

Payload:

Property | Description
--- | ---
isNetwork | boolean | Boolean indicating if the media is being loaded from the network
type | string | Type of the media. Not available on Windows
uri | string | URI for the media source. Not available on Windows

Example:
```javascript
{
  isNetwork: true,
  type: '',
  uri: 'https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8'
}
```

Platforms: all

### `onPlaybackStateChanged`
Callback function that is called when the playback state changes.

Payload:

Property | Description
--- | ---
isPlaying | boolean | Boolean indicating if the media is playing or not

Example:
```javascript
{
  isPlaying: true,
}
```

Platforms: Android, iOS

### `onPictureInPictureStatusChanged`
Callback function that is called when picture in picture becomes active or inactive.

Property | Type | Description
--- | --- | ---
isActive | boolean | Boolean indicating whether picture in picture is active

Example:
```javascript
{
isActive: true
}
```

Platforms:  iOS

### `onPlaybackRateChange`
Callback function that is called when the rate of playback changes - either paused or starts/resumes.

Property | Type | Description
--- | --- | ---
playbackRate | number | 0 when playback is paused, 1 when playing at normal speed. Other values when playback is slowed down or sped up

Example:
```javascript
{
  playbackRate: 0, // indicates paused
}
```

Platforms: all

### `onProgress`
Callback function that is called every progressUpdateInterval milliseconds with info about which position the media is currently playing.

Property | Type | Description
--- | --- | ---
currentTime | number | Current position in seconds
playableDuration | number | Position to where the media can be played to using just the buffer in seconds
seekableDuration | number | Position to where the media can be seeked to in seconds. Typically, the total length of the media

Example:
```javascript
{
  currentTime: 5.2,
  playableDuration: 34.6,
  seekableDuration: 888
}
```

Platforms: all

### `onReadyForDisplay`
Callback function that is called when the first video frame is ready for display. This is when the poster is removed.

Payload: none

* iOS: [readyForDisplay](https://developer.apple.com/documentation/avkit/avplayerviewcontroller/1615830-readyfordisplay?language=objc)
* Android [STATE_READY](https://exoplayer.dev/doc/reference/com/google/android/exoplayer2/Player.html#STATE_READY)

Platforms: Android, iOS, Web

### `onReceiveAdEvent`
Callback function that is called when an AdEvent is received from the IMA's SDK.

Enum `AdEvent` possible values for [Android](https://developers.google.com/interactive-media-ads/docs/sdks/html5/client-side/reference/js/google.ima.AdEvent) and [iOS](https://developers.google.com/interactive-media-ads/docs/sdks/ios/client-side/reference/Enums/IMAAdEventType):

<details>
<summary>Events</summary>

| Event                      | Platform      | Description                                                                                                                                                                                                 |
|----------------------------|---------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `AD_BREAK_ENDED`           | iOS           | Fired the first time each ad break ends. Applications must reenable seeking when this occurs (only used for dynamic ad insertion).                                                                          |
| `AD_BREAK_READY`           | Android, iOS  | Fires when an ad rule or a VMAP ad break would have played if autoPlayAdBreaks is false.                                                                                                                    |
| `AD_BREAK_STARTED`         | iOS           | Fired first time each ad break begins playback. If an ad break is watched subsequent times this will not be fired. Applications must disable seeking when this occurs (only used for dynamic ad insertion). |
| `AD_BUFFERING`             | Android       | Fires when the ad has stalled playback to buffer.                                                                                                                                                           |
| `AD_CAN_PLAY`              | Android       | Fires when the ad is ready to play without buffering, either at the beginning of the ad or after buffering completes.                                                                                       |
| `AD_METADATA`              | Android       | Fires when an ads list is loaded.                                                                                                                                                                           |
| `AD_PERIOD_ENDED`          | iOS           | Fired every time the stream switches from advertising or slate to content. This will be fired even when an ad is played a second time or when seeking into an ad (only used for dynamic ad insertion).      |
| `AD_PERIOD_STARTED`        | iOS           | Fired every time the stream switches from content to advertising or slate. This will be fired even when an ad is played a second time or when seeking into an ad (only used for dynamic ad insertion).      |
| `AD_PROGRESS`              | Android       | Fires when the ad's current time value changes. Calling getAdData() on this event will return an AdProgressData object.                                                                                     |
| `ALL_ADS_COMPLETED`        | Android, iOS  | Fires when the ads manager is done playing all the valid ads in the ads response, or when the response doesn't return any valid ads.                                                                        |
| `CLICK`                    | Android, iOS  | Fires when the ad is clicked.                                                                                                                                                                               |
| `COMPLETE`                 | Android, iOS  | Fires when the ad completes playing.                                                                                                                                                                        |
| `CONTENT_PAUSE_REQUESTED`  | Android       | Fires when content should be paused. This usually happens right before an ad is about to cover the content.                                                                                                 |
| `CONTENT_RESUME_REQUESTED` | Android       | Fires when content should be resumed. This usually happens when an ad finishes or collapses.                                                                                                                |
| `CUEPOINTS_CHANGED`        | iOS           | Cuepoints changed for VOD stream (only used for dynamic ad insertion).                                                                                                                                      |
| `DURATION_CHANGE`          | Android       | Fires when the ad's duration changes.                                                                                                                                                                       |
| `FIRST_QUARTILE`           | Android, iOS  | Fires when the ad playhead crosses first quartile.                                                                                                                                                          |
| `IMPRESSION`               | Android       | Fires when the impression URL has been pinged.                                                                                                                                                              |
| `INTERACTION`              | Android       | Fires when an ad triggers the interaction callback. Ad interactions contain an interaction ID string in the ad data.                                                                                        |
| `LINEAR_CHANGED`           | Android       | Fires when the displayed ad changes from linear to nonlinear, or the reverse.                                                                                                                               |
| `LOADED`                   | Android,  iOS | Fires when ad data is available.                                                                                                                                                                            |
| `LOG`                      | Android, iOS  | Fires when a non-fatal error is encountered. The user need not take any action since the SDK will continue with the same or next ad playback depending on the error situation.                              |
| `MIDPOINT`                 | Android, iOS  | Fires when the ad playhead crosses midpoint.                                                                                                                                                                |
| `PAUSED`                   | Android, iOS  | Fires when the ad is paused.                                                                                                                                                                                |
| `RESUMED`                  | Android, iOS  | Fires when the ad is resumed.                                                                                                                                                                               |
| `SKIPPABLE_STATE_CHANGED`  | Android       | Fires when the displayed ads skippable state is changed.                                                                                                                                                    |
| `SKIPPED`                  | Android, iOS  | Fires when the ad is skipped by the user.                                                                                                                                                                   |
| `STARTED`                  | Android, iOS  | Fires when the ad starts playing.                                                                                                                                                                           |
| `STREAM_LOADED`            | iOS           | Stream request has loaded (only used for dynamic ad insertion).                                                                                                                                             |
| `TAPPED`                   | iOS           | Fires when the ad is tapped.                                                                                                                                                                                |
| `THIRD_QUARTILE`           | Android, iOS  | Fires when the ad playhead crosses third quartile.                                                                                                                                                          |
| `UNKNOWN`                  | iOS           | An unknown event has fired                                                                                                                                                                                  |
| `USER_CLOSE`               | Android       | Fires when the ad is closed by the user.                                                                                                                                                                    |
| `VIDEO_CLICKED`            | Android       | Fires when the non-clickthrough portion of a video ad is clicked.                                                                                                                                           |                                                                                                                                           
| `VIDEO_ICON_CLICKED`       | Android       | Fires when a user clicks a video icon.                                                                                                                                                                      |                                                                                                                                                                     
| `VOLUME_CHANGED`           | Android       | Fires when the ad volume has changed.                                                                                                                                                                       |                                                                                                                                                                      
| `VOLUME_MUTED`             | Android       | Fires when the ad volume has been muted.                                                                                                                                                                    |                                                                                                                                                                   
</details>

Payload:

| Property | Type    | Description           |
|----------|---------|-----------------------|
|  event   | AdEvent | The ad event received |

Example:
```javascript
{
  "event": "LOADED"
}
```

Platforms: Android, iOS

### `onRestoreUserInterfaceForPictureInPictureStop`
Callback function that corresponds to Apple's [`restoreUserInterfaceForPictureInPictureStopWithCompletionHandler`](https://developer.apple.com/documentation/avkit/avpictureinpicturecontrollerdelegate/1614703-pictureinpicturecontroller?language=objc). Call `restoreUserInterfaceForPictureInPictureStopCompleted` inside of this function when done restoring the user interface.

Payload: none

Platforms: iOS

### `onSeek`
Callback function that is called when a seek completes.

Payload:

Property | Type | Description
--- | --- | ---
currentTime | number | The current time after the seek
seekTime | number | The requested time

Example:
```javascript
{
  currentTime: 100.5
  seekTime: 100
}
```

Both the currentTime & seekTime are reported because the video player may not seek to the exact requested position in order to improve seek performance.


Platforms: Android, iOS, Windows UWP

### `onTimedMetadata`
Callback function that is called when timed metadata becomes available

Payload:

Property | Type | Description
--- | --- | ---
metadata | array | Array of metadata objects

Example:
```javascript
{
  metadata: [
    { value: 'Streaming Encoder', identifier: 'TRSN' },
    { value: 'Internet Stream', identifier: 'TRSO' },
    { value: 'Any Time You Like', identifier: 'TIT2' }
  ]
}
```

Platforms: Android, iOS

### `onTextTracks`
Callback function that is called when text tracks change

Payload:

Property | Type | Description
--- | --- | ---
index | number | Internal track ID
title | string | Descriptive name for the track
language | string | 2 letter [ISO 639-1 code](https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes) representing the language
type | string | Mime type of the track<br> * TextTrackType.SRT - SubRip (.srt)<br> * TextTrackType.TTML - TTML (.ttml)<br> * TextTrackType.VTT - WebVTT (.vtt)<br>iOS only supports VTT, Android supports all 3
selected | boolean | true if track is playing


Example:
```javascript
{
  textTracks: [
    {
      index: 0,
      title: 'Any Time You Like',
      type: 'srt',
      selected: true
    }
  ]
}
```

Platforms: Android

### `onVideoTracks`
Callback function that is called when video tracks change

Payload:

Property | Type | Description
--- | --- | ---
trackId | string | Internal track ID
codecs | string | MimeType of codec used for this track
width | number | Track width
height | number | Track height
bitrate | number | Bitrate in bps
selected | boolean | true if track is selected for playing


Example:
```javascript
{
  videoTracks: [
    {
      trackId: '0',
      codecs: 'video/mp4',
      width: 1920,
      height: 1080,
      bitrate: 10000,
      selected: true
    }
  ]
}
```

Platforms: Android