'use strict';

import React, {Component} from 'react';

import {
  Text,
  TouchableOpacity,
  View,
  ActivityIndicator,
  ToastAndroid,
  Platform,
  Alert,
  NativeModules,
  NativeEventEmitter,
} from 'react-native';

import Video, {
  AudioTrack,
  OnAudioTracksData,
  OnLoadData,
  OnProgressData,
  OnTextTracksData,
  OnVideoAspectRatioData,
  TextTrack,
  VideoDecoderProperties,
  OnBufferData,
  OnAudioFocusChangedData,
  OnVideoErrorData,
  VideoRef,
  ResizeMode,
  SelectedTrack,
  DRMType,
  OnTextTrackDataChangedData,
  TextTrackType,
  ISO639_1,
  OnSeekData,
  OnPlaybackStateChangedData,
  OnPlaybackRateChangeData,
  OnVideoTracksData,
  VideoTrack,
  SelectedVideoTrackType,
  SelectedVideoTrack,
  BufferingStrategyType,
  ReactVideoSource,
  Drm,
  TextTracks,
} from 'react-native-video';
import ToggleControl from './ToggleControl';
import MultiValueControl, {
  MultiValueControlPropType,
} from './MultiValueControl';
import styles from './styles';
import AudioTrackSelector from './components/AudioTracksSelector';
import TextTrackSelector from './components/TextTracksSelector';
import VideoTrackSelector from './components/VideoTracksSelector';
import Seeker from './components/Seeker';

type AdditionnalSourceInfo = {
  textTracks: TextTracks;
  adTagUrl: string;
  description: string;
  drm: Drm;
  noView: boolean;
};

type SampleVideoSource = ReactVideoSource | AdditionnalSourceInfo;

interface StateType {
  rate: number;
  volume: number;
  muted: boolean;
  resizeMode: ResizeMode;
  duration: number;
  currentTime: number;
  videoWidth: number;
  videoHeight: number;
  paused: boolean;
  fullscreen: true;
  decoration: true;
  isLoading: boolean;
  audioTracks: Array<AudioTrack>;
  textTracks: Array<TextTrack>;
  videoTracks: Array<VideoTrack>;
  selectedAudioTrack: SelectedTrack | undefined;
  selectedTextTrack: SelectedTrack | undefined;
  selectedVideoTrack: SelectedVideoTrack;
  srcListId: number;
  loop: boolean;
  showRNVControls: boolean;
  useCache: boolean;
  poster?: string;
  showNotificationControls: boolean;
  isSeeking: boolean;
}

let eventEmitter

class VideoPlayer extends Component {
  state: StateType = {
    rate: 1,
    volume: 1,
    muted: false,
    resizeMode: ResizeMode.CONTAIN,
    duration: 0.0,
    currentTime: 0.0,
    videoWidth: 0,
    videoHeight: 0,
    paused: false,
    fullscreen: true,
    decoration: true,
    isLoading: false,
    audioTracks: [],
    textTracks: [],
    videoTracks: [],
    selectedAudioTrack: undefined,
    selectedTextTrack: undefined,
    selectedVideoTrack: {
      type: SelectedVideoTrackType.AUTO,
    },
    srcListId: 0,
    loop: false,
    showRNVControls: false,
    useCache: false,
    poster: undefined,
    showNotificationControls: false,
    isSeeking: false,
  };

  // internal usage change to index if you want to select tracks by index instead of lang
  textTracksSelectionBy = 'index';

  srcAllPlatformList = [
    {
      description: 'local file landscape',
      uri: 'https://dash.akamaized.net/akamai/bbb_30fps/bbb_with_multiple_tiled_thumbnails.mpd',
      // uri: 'https://cdn.theoplayer.com/video/big_buck_bunny/big_buck_bunny.m3u8',
      //uri: 'https://demo.unified-streaming.com/k8s/features/stable/no-handler-origin/tears-of-steel/tears-of-steel-tiled-thumbnails-static.mpd',
      // uri: 'https://demo.unified-streaming.com/k8s/features/stable/no-handler-origin/tears-of-steel/tears-of-steel-tiled-thumbnails.cmfv',
      type: 'mpd',
    },
    {
      description: 'local file landscape cropped',
      uri: require('./broadchurch.mp4'),
      cropStart: 3000,
      cropEnd: 10000,
    },
    {
      description: 'local file portrait',
      uri: require('./portrait.mp4'),
      metadata: {
        title: 'Test Title',
        subtitle: 'Test Subtitle',
        artist: 'Test Artist',
        description: 'Test Description',
        imageUri:
          'https://pbs.twimg.com/profile_images/1498641868397191170/6qW2XkuI_400x400.png',
      },
    },
    {
      description: '(hls|live) red bull tv',
      textTracksAllowChunklessPreparation: false,
      uri: 'https://rbmn-live.akamaized.net/hls/live/590964/BoRB-AT/master_928.m3u8',
      metadata: {
        title: 'Custom Title',
        subtitle: 'Custom Subtitle',
        artist: 'Custom Artist',
        description: 'Custom Description',
        imageUri:
          'https://pbs.twimg.com/profile_images/1498641868397191170/6qW2XkuI_400x400.png',
      },
    },
    {
      description: 'invalid URL',
      uri: 'mmt://www.youtube.com',
      type: 'mpd',
    },
    {description: '(no url) Stopped playback', uri: undefined},
    {
      description: '(no view) no View',
      noView: true,
    },
    {
      description: 'Another live sample',
      uri: 'https://live.forstreet.cl/live/livestream.m3u8',
    },
    {
      description: 'another bunny (can be saved)',
      uri: 'https://rawgit.com/mediaelement/mediaelement-files/master/big_buck_bunny.mp4',
      headers: {referer: 'www.github.com', 'User-Agent': 'react.native.video'},
    },
    {
      description: 'sintel with subtitles',
      uri: 'https://bitmovin-a.akamaihd.net/content/sintel/hls/playlist.m3u8',
    },
    {
      description: 'sintel starts at 20sec',
      uri: 'https://bitmovin-a.akamaihd.net/content/sintel/hls/playlist.m3u8',
      startPosition: 50000,
    },
    {
      description: 'BigBugBunny sideLoaded subtitles',
      // sideloaded subtitles wont work for streaming like HLS on ios
      // mp4
      uri: 'https://d23dyxeqlo5psv.cloudfront.net/big_buck_bunny.mp4',
      textTracks: [
        {
          title: 'test',
          language: 'en' as ISO639_1,
          type: TextTrackType.VTT,
          uri: 'https://bitdash-a.akamaihd.net/content/sintel/subtitles/subtitles_en.vtt',
        },
      ],
    },
  ];

  srcIosList = [];

  srcAndroidList = [
    {
      description: 'Another live sample',
      uri: 'https://live.forstreet.cl/live/livestream.m3u8',
    },
    {
      description: 'asset file',
      uri: 'asset:///broadchurch.mp4',
    },
    {
      description: '(dash) sintel subtitles',
      uri: 'https://bitmovin-a.akamaihd.net/content/sintel/sintel.mpd',
    },
    {
      description: '(mp4) big buck bunny',
      uri: 'http://d23dyxeqlo5psv.cloudfront.net/big_buck_bunny.mp4',
    },
    {
      description: '(mp4|subtitles) demo with sintel Subtitles',
      uri: 'http://www.youtube.com/api/manifest/dash/id/bf5bb2419360daf1/source/youtube?as=fmp4_audio_clear,fmp4_sd_hd_clear&sparams=ip,ipbits,expire,source,id,as&ip=0.0.0.0&ipbits=0&expire=19000000000&signature=51AF5F39AB0CEC3E5497CD9C900EBFEAECCCB5C7.8506521BFC350652163895D4C26DEE124209AA9E&key=ik0',
      type: 'mpd',
    },
    {
      description: '(mp4) big buck bunny With Ads',
      adTagUrl:
        'https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/vmap_ad_samples&sz=640x480&cust_params=sample_ar%3Dpremidpostoptimizedpodbumper&ciu_szs=300x250&gdfp_req=1&ad_rule=1&output=vmap&unviewed_position_start=1&env=vp&impl=s&cmsid=496&vid=short_onecue&correlator=',
      uri: 'http://d23dyxeqlo5psv.cloudfront.net/big_buck_bunny.mp4',
    },
    {
      description: 'WV: Secure SD & HD (cbcs,MP4,H264)',
      uri: 'https://storage.googleapis.com/wvmedia/cbcs/h264/tears/tears_aes_cbcs.mpd',
      drm: {
        type: DRMType.WIDEVINE,
        licenseServer:
          'https://proxy.uat.widevine.com/proxy?provider=widevine_test',
      },
    },
    {
      description: 'Secure UHD (cenc)',
      uri: 'https://storage.googleapis.com/wvmedia/cenc/h264/tears/tears_uhd.mpd',
      drm: {
        type: DRMType.WIDEVINE,
        licenseServer:
          'https://proxy.uat.widevine.com/proxy?provider=widevine_test',
      },
    },
    {
      description: 'rtsp big bug bunny',
      uri: 'rtsp://rtspstream:3cfa3c36a9c00f4aa38f3cd35816b287@zephyr.rtsp.stream/movie',
      type: 'rtsp',
    },
  ];

  // poster which can be displayed
  samplePoster =
    'https://upload.wikimedia.org/wikipedia/commons/1/18/React_Native_Logo.png';

  srcList: SampleVideoSource[] = this.srcAllPlatformList.concat(
    Platform.OS === 'android' ? this.srcAndroidList : this.srcIosList,
  );

  video?: VideoRef;

  popupInfo = () => {
    VideoDecoderProperties.getWidevineLevel().then((widevineLevel: number) => {
      VideoDecoderProperties.isHEVCSupported().then((hevc: string) => {
        VideoDecoderProperties.isCodecSupported('video/avc', 1920, 1080).then(
          (avc: string) => {
            this.toast(
              true,
              'Widevine level: ' +
                widevineLevel +
                '\n hevc: ' +
                hevc +
                '\n avc: ' +
                avc,
            );
          },
        );
      });
    });
  };

  onLoad = (data: OnLoadData) => {
    this.setState({duration: data.duration, loading: false});
    this.onAudioTracks(data);
    this.onTextTracks(data);
    this.onVideoTracks(data);
  };

  onProgress = (data: OnProgressData) => {
    this.setState({currentTime: data.currentTime});
  };

  onSeek = (data: OnSeekData) => {
    this.setState({isSeeking: false});
    this.setState({currentTime: data.currentTime});
  };

  onVideoLoadStart = () => {
    console.log('onVideoLoadStart');
    this.setState({isLoading: true});
  };

  onAudioTracks = (data: OnAudioTracksData) => {
    const selectedTrack = data.audioTracks?.find((x: AudioTrack) => {
      return x.selected;
    });
    if (selectedTrack?.index) {
      this.setState({
        audioTracks: data.audioTracks,
        selectedAudioTrack: {
          type: SelectedVideoTrackType.INDEX,
          value: selectedTrack?.index,
        },
      });
    } else {
      this.setState({
        audioTracks: data.audioTracks,
      });
    }
  };

  onVideoTracks = (data: OnVideoTracksData) => {
    console.log('onVideoTracks', data.videoTracks);
    this.setState({
      videoTracks: data.videoTracks,
    });
  };

  onTextTracks = (data: OnTextTracksData) => {
    const selectedTrack = data.textTracks?.find((x: TextTrack) => {
      return x?.selected;
    });

    if (selectedTrack?.language) {
      this.setState({
        textTracks: data.textTracks,
        selectedTextTrack:
          this.textTracksSelectionBy === 'index'
            ? {
                type: 'index',
                value: selectedTrack?.index,
              }
            : {
                type: 'language',
                value: selectedTrack?.language,
              },
      });
    } else {
      this.setState({
        textTracks: data.textTracks,
      });
    }
  };

  onTextTrackDataChanged = (data: OnTextTrackDataChangedData) => {
    console.log(`Subtitles: ${JSON.stringify(data, null, 2)}`);
  };

  onAspectRatio = (data: OnVideoAspectRatioData) => {
    console.log('onAspectRadio called ' + JSON.stringify(data));
    this.setState({
      videoWidth: data.width,
      videoHeight: data.height,
    });
  };

  onVideoBuffer = (param: OnBufferData) => {
    console.log('onVideoBuffer');
    this.setState({isLoading: param.isBuffering});
  };

  onReadyForDisplay = () => {
    console.log('onReadyForDisplay');
    this.setState({isLoading: false});
  };

  onAudioBecomingNoisy = () => {
    this.setState({paused: true});
  };

  onAudioFocusChanged = (event: OnAudioFocusChangedData) => {
    this.setState({paused: !event.hasAudioFocus});
  };

  toast = (visible: boolean, message: string) => {
    if (visible) {
      if (Platform.OS === 'android') {
        ToastAndroid.showWithGravityAndOffset(
          message,
          ToastAndroid.LONG,
          ToastAndroid.BOTTOM,
          25,
          50,
        );
      } else {
        Alert.alert(message, message);
      }
    }
  };

  onError = (err: OnVideoErrorData) => {
    console.log(JSON.stringify(err));
    this.toast(true, 'error: ' + JSON.stringify(err));
  };

  onEnd = () => {
    if (!this.state.loop) {
      this.channelUp();
    }
  };

  onPlaybackRateChange = (data: OnPlaybackRateChangeData) => {
    console.log('onPlaybackRateChange', data);
  };

  onPlaybackStateChanged = (data: OnPlaybackStateChangedData) => {
    console.log('onPlaybackStateChanged', data);
  };

  toggleFullscreen() {
    this.setState({fullscreen: !this.state.fullscreen});
  }
  toggleControls() {
    this.setState({showRNVControls: !this.state.showRNVControls});
  }

  toggleDecoration() {
    this.setState({decoration: !this.state.decoration});
    this.video?.setFullScreen(!this.state.decoration);
  }

  toggleShowNotificationControls() {
    this.setState({
      showNotificationControls: !this.state.showNotificationControls,
    });
  }

  goToChannel(channel: number) {
    this.setState({
      srcListId: channel,
      duration: 0.0,
      currentTime: 0.0,
      videoWidth: 0,
      videoHeight: 0,
      isLoading: false,
      audioTracks: [],
      textTracks: [],
      selectedAudioTrack: undefined,
      selectedTextTrack: undefined,
      selectedVideoTrack: {
        type: SelectedVideoTrackType.AUTO,
      },
    });
  }

  channelUp() {
    console.log('channel up');
    this.goToChannel((this.state.srcListId + 1) % this.srcList.length);
  }

  channelDown() {
    console.log('channel down');
    this.goToChannel(
      (this.state.srcListId + this.srcList.length - 1) % this.srcList.length,
    );
  }

  videoSeek(position: number) {
    this.setState({isSeeking: true});
    this.video?.seek(position);
  }

  renderSeekBar() {
    return (
      <Seeker
        currentTime={this.state.currentTime}
        duration={this.state.duration}
        isLoading={this.state.isLoading}
        videoSeek={prop => this.videoSeek(prop)}
        isUISeeking={this.state.isSeeking}
        thumbnailData={this.state.thumbnailData}
      />
    );
  }

  IndicatorLoadingView() {
    if (this.state.isLoading) {
      return (
        <ActivityIndicator
          color="#3235fd"
          size="large"
          style={styles.IndicatorStyle}
        />
      );
    } else {
      return <View />;
    }
  }

  renderTopControl() {
    return (
      <View style={styles.topControlsContainer}>
        <Text style={styles.controlOption}>
          {(this.srcList[this.state.srcListId] as AdditionnalSourceInfo)
            ?.description || 'local file'}
        </Text>
        <View>
          <TouchableOpacity
            onPress={() => {
              this.toggleControls();
            }}>
            <Text style={styles.leftRightControlOption}>
              {this.state.showRNVControls ? 'Hide controls' : 'Show controls'}
            </Text>
          </TouchableOpacity>
        </View>
      </View>
    );
  }

  onRateSelected = (value: MultiValueControlPropType) => {
    this.setState({rate: value});
  };
  onVolumeSelected = (value: MultiValueControlPropType) => {
    this.setState({volume: value});
  };
  onResizeModeSelected = (value: MultiValueControlPropType) => {
    this.setState({resizeMode: value});
  };

  onSelectedAudioTrackChange = (itemValue: string) => {
    console.log('on audio value change ' + itemValue);
    if (itemValue === 'none') {
      this.setState({
        selectedAudioTrack: SelectedVideoTrackType.DISABLED,
      });
    } else {
      this.setState({
        selectedAudioTrack: {
          type: SelectedVideoTrackType.INDEX,
          value: itemValue,
        },
      });
    }
  };

  onSelectedTextTrackChange = (itemValue: string) => {
    console.log('on value change ' + itemValue);
    this.setState({
      selectedTextTrack: {
        type: this.textTracksSelectionBy === 'index' ? 'index' : 'language',
        value: itemValue,
      },
    });
  };

  onSelectedVideoTrackChange = (itemValue: string) => {
    console.log('on value change ' + itemValue);
    if (itemValue === undefined || itemValue === 'auto') {
      this.setState({
        selectedVideoTrack: {
          type: SelectedVideoTrackType.AUTO,
        },
      });
    } else {
      this.setState({
        selectedVideoTrack: {
          type: SelectedVideoTrackType.INDEX,
          value: itemValue,
        },
      });
    }
  };

  componentDidMount() {
    eventEmitter = new NativeEventEmitter(NativeModules.VideoPluginSample);

    eventEmitter.addListener("ImageDataAvailable", (res) => {
      console.log('ImageDataAvailable ', res);
      this.setState({
        thumbnailData: res,
      })
    });
  }

  renderOverlay() {
    return (
      <>
        {this.IndicatorLoadingView()}
        <View style={styles.topControls}>
          <View style={styles.resizeModeControl}>
            {this.renderTopControl()}
          </View>
        </View>
        {!this.state.showRNVControls ? (
          <>
            <View style={styles.leftControls}>
              <ToggleControl
                onPress={() => {
                  this.channelDown();
                }}
                text="ChDown"
              />
            </View>
            <View style={styles.rightControls}>
              <ToggleControl
                onPress={() => {
                  this.channelUp();
                }}
                text="ChUp"
              />
            </View>
            <View style={styles.bottomControls}>
              <View style={styles.generalControls}>
                {Platform.OS === 'android' ? (
                  <View style={styles.generalControls}>
                    <ToggleControl
                      onPress={() => {
                        this.popupInfo();
                      }}
                      text="decoderInfo"
                    />
                    <ToggleControl
                      isSelected={this.state.useCache}
                      onPress={() => {
                        this.setState({useCache: !this.state.useCache});
                      }}
                      selectedText="enable cache"
                      unselectedText="disable cache"
                    />
                  </View>
                ) : null}
                <ToggleControl
                  isSelected={this.state.paused}
                  onPress={() => {
                    this.setState({paused: !this.state.paused});
                  }}
                  selectedText="pause"
                  unselectedText="playing"
                />
                <ToggleControl
                  isSelected={this.state.loop}
                  onPress={() => {
                    this.setState({loop: !this.state.loop});
                  }}
                  selectedText="loop enable"
                  unselectedText="loop disable"
                />
                <ToggleControl
                  onPress={() => {
                    this.toggleFullscreen();
                  }}
                  text="fullscreen"
                />
                <ToggleControl
                  onPress={() => {
                    this.toggleDecoration();
                  }}
                  text="decoration"
                />
                <ToggleControl
                  isSelected={!!this.state.poster}
                  onPress={() => {
                    this.setState({
                      poster: this.state.poster ? undefined : this.samplePoster,
                    });
                  }}
                  selectedText="poster"
                  unselectedText="no poster"
                />
                <ToggleControl
                  isSelected={this.state.showNotificationControls}
                  onPress={() => {
                    this.toggleShowNotificationControls();
                  }}
                  selectedText="hide notification controls"
                  unselectedText="show notification controls"
                />
              </View>
              <View style={styles.generalControls}>
                {/* shall be replaced by slider */}
                <MultiValueControl
                  values={[0, 0.25, 0.5, 1.0, 1.5, 2.0]}
                  onPress={this.onRateSelected}
                  selected={this.state.rate}
                />
                {/* shall be replaced by slider */}
                <MultiValueControl
                  values={[0.5, 1, 1.5]}
                  onPress={this.onVolumeSelected}
                  selected={this.state.volume}
                />
                <MultiValueControl
                  values={[
                    ResizeMode.COVER,
                    ResizeMode.CONTAIN,
                    ResizeMode.STRETCH,
                  ]}
                  onPress={this.onResizeModeSelected}
                  selected={this.state.resizeMode}
                />
                <ToggleControl
                  isSelected={this.state.muted}
                  onPress={() => {
                    this.setState({muted: !this.state.muted});
                  }}
                  text="muted"
                />
                {Platform.OS === 'ios' ? (
                  <ToggleControl
                    isSelected={this.state.paused}
                    onPress={() => {
                      this.video
                        ?.save({})
                        ?.then(response => {
                          console.log('Downloaded URI', response);
                        })
                        .catch(error => {
                          console.log('error during save ', error);
                        });
                    }}
                    text="save"
                  />
                ) : null}
              </View>
              {this.renderSeekBar()}
              <View style={styles.generalControls}>
                <AudioTrackSelector
                  audioTracks={this.state.audioTracks}
                  selectedAudioTrack={this.state.selectedAudioTrack}
                  onValueChange={this.onSelectedAudioTrackChange}
                />
                <TextTrackSelector
                  textTracks={this.state.textTracks}
                  selectedTextTrack={this.state.selectedTextTrack}
                  onValueChange={this.onSelectedTextTrackChange}
                  textTracksSelectionBy={this.textTracksSelectionBy}
                />
                <VideoTrackSelector
                  videoTracks={this.state.videoTracks}
                  selectedVideoTrack={this.state.selectedVideoTrack}
                  onValueChange={this.onSelectedVideoTrackChange}
                />
              </View>
            </View>
          </>
        ) : null}
      </>
    );
  }

  renderVideoView() {
    const viewStyle = this.state.fullscreen
      ? styles.fullScreen
      : styles.halfScreen;

    const currentSrc = this.srcList[this.state.srcListId];
    const additionnal = currentSrc as AdditionnalSourceInfo;

    return (
      <TouchableOpacity style={viewStyle}>
        <Video
          showNotificationControls={this.state.showNotificationControls}
          ref={(ref: VideoRef) => {
            this.video = ref;
          }}
          source={currentSrc as ReactVideoSource}
          textTracks={additionnal?.textTracks}
          adTagUrl={additionnal?.adTagUrl}
          drm={additionnal?.drm}
          style={viewStyle}
          rate={this.state.rate}
          paused={this.state.paused}
          volume={this.state.volume}
          muted={this.state.muted}
          fullscreen={this.state.fullscreen}
          controls={this.state.showRNVControls}
          resizeMode={this.state.resizeMode}
          onLoad={this.onLoad}
          onAudioTracks={this.onAudioTracks}
          onTextTracks={this.onTextTracks}
          onVideoTracks={this.onVideoTracks}
          onTextTrackDataChanged={this.onTextTrackDataChanged}
          onProgress={this.onProgress}
          onEnd={this.onEnd}
          progressUpdateInterval={1000}
          onError={this.onError}
          onAudioBecomingNoisy={this.onAudioBecomingNoisy}
          onAudioFocusChanged={this.onAudioFocusChanged}
          onLoadStart={this.onVideoLoadStart}
          onAspectRatio={this.onAspectRatio}
          onReadyForDisplay={this.onReadyForDisplay}
          onBuffer={this.onVideoBuffer}
          onSeek={this.onSeek}
          repeat={this.state.loop}
          selectedTextTrack={this.state.selectedTextTrack}
          selectedAudioTrack={this.state.selectedAudioTrack}
          selectedVideoTrack={this.state.selectedVideoTrack}
          playInBackground={false}
          bufferConfig={{
            minBufferMs: 15000,
            maxBufferMs: 50000,
            bufferForPlaybackMs: 2500,
            bufferForPlaybackAfterRebufferMs: 5000,
            cacheSizeMB: this.state.useCache ? 200 : 0,
            live: {
              targetOffsetMs: 500,
            },
          }}
          preventsDisplaySleepDuringVideoPlayback={true}
          poster={this.state.poster}
          onPlaybackRateChange={this.onPlaybackRateChange}
          onPlaybackStateChanged={this.onPlaybackStateChanged}
          bufferingStrategy={BufferingStrategyType.DEFAULT}
          debug={{enable: true, thread: true}}
        />
      </TouchableOpacity>
    );
  }

  render() {
    return (
      <View style={styles.container}>
        {(this.srcList[this.state.srcListId] as AdditionnalSourceInfo)?.noView
          ? null
          : this.renderVideoView()}
        {this.renderOverlay()}
      </View>
    );
  }
}
export default VideoPlayer;
