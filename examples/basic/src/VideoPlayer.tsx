'use strict';

import React, {Component} from 'react';

import {
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
  ActivityIndicator,
  PanResponder,
  ToastAndroid,
  Platform,
  PanResponderInstance,
  Alert,
} from 'react-native';

import {Picker} from '@react-native-picker/picker';

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
  SelectedTrackType,
} from 'react-native-video';
import ToggleControl from './ToggleControl';
import MultiValueControl, {
  MultiValueControlPropType,
} from './MultiValueControl';

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
  seekerFillWidth: number;
  seekerPosition: number;
  seekerOffset: number;
  seeking: boolean;
  audioTracks: Array<AudioTrack>;
  textTracks: Array<TextTrack>;
  selectedAudioTrack: SelectedTrack | undefined;
  selectedTextTrack: SelectedTrack | undefined;
  srcListId: number;
  loop: boolean;
  showRNVControls: boolean;
}

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
    seekerFillWidth: 0,
    seekerPosition: 0,
    seekerOffset: 0,
    seeking: false,
    audioTracks: [],
    textTracks: [],
    selectedAudioTrack: undefined,
    selectedTextTrack: undefined,
    srcListId: 0,
    loop: false,
    showRNVControls: false,
  };

  seekerWidth = 0;

  srcAllPlatformList = [
    {
      description: 'local file',
      uri: require('./broadchurch.mp4'),
    },
    {
      description: '(hls|live) red bull tv',
      uri: 'https://rbmn-live.akamaized.net/hls/live/590964/BoRB-AT/master_928.m3u8',
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
    },
    {
      description: 'sintel with subtitles',
      uri: 'https://bitmovin-a.akamaihd.net/content/sintel/hls/playlist.m3u8',
    },
  ];

  srcIosList = [];

  srcAndroidList = [
    {
      description: 'Another live sample',
      uri: 'https://live.forstreet.cl/live/livestream.m3u8',
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
  ];

  srcList = this.srcAllPlatformList.concat(
    Platform.OS === 'android' ? this.srcAndroidList : this.srcIosList,
  );

  video?: VideoRef;
  seekPanResponder?: PanResponderInstance;

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
  };

  onProgress = (data: OnProgressData) => {
    if (!this.state.seeking) {
      const position = this.calculateSeekerPosition();
      this.setSeekerPosition(position);
    }
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
    if (selectedTrack?.language) {
      this.setState({
        audioTracks: data.audioTracks,
        selectedAudioTrack: {
          type: 'language',
          value: selectedTrack?.language,
        },
      });
    } else {
      this.setState({
        audioTracks: data.audioTracks,
      });
    }
  };

  onTextTracks = (data: OnTextTracksData) => {
    const selectedTrack = data.textTracks?.find((x: TextTrack) => {
      return x?.selected;
    });

    if (selectedTrack?.language) {
      this.setState({
        textTracks: data.textTracks,
        selectedTextTrack: {
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

  getCurrentTimePercentage = () => {
    if (this.state.currentTime > 0 && this.state.duration !== 0) {
      return this.state.currentTime / this.state.duration;
    }
    return 0;
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
    this.channelUp();
  };

  toggleFullscreen() {
    this.setState({fullscreen: !this.state.fullscreen});
  }
  toggleControls() {
    this.setState({showRNVControls: !this.state.showRNVControls});
  }

  toggleDecoration() {
    this.setState({decoration: !this.state.decoration});
    if (this.state.decoration) {
      this.video?.dismissFullscreenPlayer();
    } else {
      this.video?.presentFullscreenPlayer();
    }
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

  componentDidMount() {
    this.initSeekPanResponder();
  }

  /**
   * Render the seekbar and attach its handlers
   */

  /**
   * Constrain the location of the seeker to the
   * min/max value based on how big the
   * seeker is.
   *
   * @param {float} val position of seeker handle in px
   * @return {float} constrained position of seeker handle in px
   */
  constrainToSeekerMinMax(val = 0) {
    if (val <= 0) {
      return 0;
    } else if (val >= this.seekerWidth) {
      return this.seekerWidth;
    }
    return val;
  }

  /**
   * Set the position of the seekbar's components
   * (both fill and handle) according to the
   * position supplied.
   *
   * @param {float} position position in px of seeker handle}
   */
  setSeekerPosition(position = 0) {
    const state = this.state;
    position = this.constrainToSeekerMinMax(position);

    state.seekerFillWidth = position;
    state.seekerPosition = position;

    if (!state.seeking) {
      state.seekerOffset = position;
    }

    this.setState(state);
  }

  /**
   * Calculate the position that the seeker should be
   * at along its track.
   *
   * @return {float} position of seeker handle in px based on currentTime
   */
  calculateSeekerPosition() {
    const percent = this.state.currentTime / this.state.duration;
    return this.seekerWidth * percent;
  }

  /**
   * Return the time that the video should be at
   * based on where the seeker handle is.
   *
   * @return {float} time in ms based on seekerPosition.
   */
  calculateTimeFromSeekerPosition() {
    const percent = this.state.seekerPosition / this.seekerWidth;
    return this.state.duration * percent;
  }

  /**
   * Get our seekbar responder going
   */
  initSeekPanResponder() {
    this.seekPanResponder = PanResponder.create({
      // Ask to be the responder.
      onStartShouldSetPanResponder: (_evt, _gestureState) => true,
      onMoveShouldSetPanResponder: (_evt, _gestureState) => true,

      /**
       * When we start the pan tell the machine that we're
       * seeking. This stops it from updating the seekbar
       * position in the onProgress listener.
       */
      onPanResponderGrant: (evt, _gestureState) => {
        const state = this.state;
        // this.clearControlTimeout()
        const position = evt.nativeEvent.locationX;
        this.setSeekerPosition(position);
        state.seeking = true;
        this.setState(state);
      },

      /**
       * When panning, update the seekbar position, duh.
       */
      onPanResponderMove: (evt, gestureState) => {
        const position = this.state.seekerOffset + gestureState.dx;
        this.setSeekerPosition(position);
      },

      /**
       * On release we update the time and seek to it in the video.
       * If you seek to the end of the video we fire the
       * onEnd callback
       */
      onPanResponderRelease: (_evt, _gestureState) => {
        const time = this.calculateTimeFromSeekerPosition();
        const state = this.state;
        if (time >= state.duration && !state.isLoading) {
          state.paused = true;
          this.onEnd();
        } else {
          this.video?.seek(time);
          state.seeking = false;
        }
        this.setState(state);
      },
    });
  }

  renderSeekBar() {
    if (!this.seekPanResponder) {
      return null;
    }
    const seekerStyle = [
      styles.seekbarFill,
      {
        width: this.state.seekerFillWidth > 0 ? this.state.seekerFillWidth : 0,
        backgroundColor: '#FFF',
      },
    ];

    const seekerPositionStyle = [
      styles.seekbarHandle,
      {
        left: this.state.seekerPosition > 0 ? this.state.seekerPosition : 0,
      },
    ];

    const seekerPointerStyle = [
      styles.seekbarCircle,
      {backgroundColor: '#FFF'},
    ];

    return (
      <View
        style={styles.seekbarContainer}
        {...this.seekPanResponder.panHandlers}
        {...styles.generalControls}>
        <View
          style={styles.seekbarTrack}
          onLayout={event =>
            (this.seekerWidth = event.nativeEvent.layout.width)
          }
          pointerEvents={'none'}>
          <View style={seekerStyle} pointerEvents={'none'} />
        </View>
        <View style={seekerPositionStyle} pointerEvents={'none'}>
          <View style={seekerPointerStyle} pointerEvents={'none'} />
        </View>
      </View>
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
      <>
        <Text style={[styles.controlOption]}>
          {this.srcList[this.state.srcListId]?.description || 'local file'}
        </Text>
        <View>
          <TouchableOpacity
            onPress={() => {
              this.toggleControls();
            }}>
            <Text style={[styles.leftRightControlOption]}>
              {this.state.showRNVControls ? 'Hide controls' : 'Show controls'}
            </Text>
          </TouchableOpacity>
        </View>
      </>
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
                <Text style={styles.controlOption}>AudioTrack</Text>
                {this.state.audioTracks?.length <= 0 ? (
                  <Text style={styles.controlOption}>empty</Text>
                ) : (
                  <Picker
                    style={styles.picker}
                    selectedValue={this.state.selectedAudioTrack?.value}
                    onValueChange={itemValue => {
                      console.log('on audio value change ' + itemValue);
                      this.setState({
                        selectedAudioTrack: {
                          type: 'language',
                          value: itemValue,
                        },
                      });
                    }}>
                    {this.state.audioTracks.map(track => {
                      if (!track) {
                        return;
                      }
                      return (
                        <Picker.Item
                          label={track.language}
                          value={track.language}
                          key={track.language}
                        />
                      );
                    })}
                  </Picker>
                )}
                <Text style={styles.controlOption}>TextTrack</Text>
                {this.state.textTracks?.length <= 0 ? (
                  <Text style={styles.controlOption}>empty</Text>
                ) : (
                  <Picker
                    style={styles.picker}
                    selectedValue={this.state.selectedTextTrack?.value}
                    onValueChange={itemValue => {
                      console.log('on value change ' + itemValue);
                      this.setState({
                        selectedTextTrack: {
                          type: 'language',
                          value: itemValue,
                        },
                      });
                    }}>
                    <Picker.Item label={'none'} value={'none'} key={'none'} />
                    {this.state.textTracks.map(track => {
                      if (!track) {
                        return;
                      }
                      return (
                        <Picker.Item
                          label={track.language}
                          value={track.language}
                          key={track.language}
                        />
                      );
                    })}
                  </Picker>
                )}
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

    return (
      <TouchableOpacity style={viewStyle}>
        <Video
          ref={(ref: VideoRef) => {
            this.video = ref;
          }}
          limitMaxResolution="disabled"
          source={this.srcList[this.state.srcListId]}
          adTagUrl={this.srcList[this.state.srcListId]?.adTagUrl}
          drm={this.srcList[this.state.srcListId]?.drm}
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
          repeat={this.state.loop}
          selectedTextTrack={this.state.selectedTextTrack}
          selectedAudioTrack={this.state.selectedAudioTrack}
          playInBackground={false}
          preventsDisplaySleepDuringVideoPlayback={true}
        />
      </TouchableOpacity>
    );
  }

  render() {
    return (
      <View style={styles.container}>
        {this.srcList[this.state.srcListId]?.noView
          ? null
          : this.renderVideoView()}
        {this.renderOverlay()}
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'black',
  },
  halfScreen: {
    position: 'absolute',
    top: 50,
    left: 50,
    bottom: 100,
    right: 100,
  },
  fullScreen: {
    position: 'absolute',
    top: 0,
    left: 0,
    bottom: 0,
    right: 0,
  },
  bottomControls: {
    backgroundColor: 'transparent',
    borderRadius: 5,
    position: 'absolute',
    bottom: 20,
    left: 20,
    right: 20,
  },
  leftControls: {
    backgroundColor: 'transparent',
    borderRadius: 5,
    position: 'absolute',
    top: 20,
    bottom: 20,
    left: 20,
  },
  rightControls: {
    backgroundColor: 'transparent',
    borderRadius: 5,
    position: 'absolute',
    top: 20,
    bottom: 20,
    right: 20,
  },
  topControls: {
    backgroundColor: 'transparent',
    borderRadius: 4,
    position: 'absolute',
    top: 20,
    left: 20,
    right: 20,
    flex: 1,
    flexDirection: 'row',
    overflow: 'hidden',
    paddingBottom: 10,
  },
  generalControls: {
    flex: 1,
    flexDirection: 'row',
    borderRadius: 4,
    overflow: 'hidden',
    paddingBottom: 10,
  },
  rateControl: {
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'center',
  },
  volumeControl: {
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'center',
  },
  resizeModeControl: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
  },
  leftRightControlOption: {
    alignSelf: 'center',
    fontSize: 11,
    color: 'white',
    padding: 10,
    lineHeight: 12,
  },
  controlOption: {
    alignSelf: 'center',
    fontSize: 11,
    color: 'white',
    paddingLeft: 2,
    paddingRight: 2,
    lineHeight: 12,
  },
  IndicatorStyle: {
    flex: 1,
    justifyContent: 'center',
  },
  seekbarContainer: {
    flex: 1,
    flexDirection: 'row',
    borderRadius: 4,
    height: 30,
  },
  seekbarTrack: {
    backgroundColor: '#333',
    height: 1,
    position: 'relative',
    top: 14,
    width: '100%',
  },
  seekbarFill: {
    backgroundColor: '#FFF',
    height: 1,
    width: '100%',
  },
  seekbarHandle: {
    position: 'absolute',
    marginLeft: -7,
    height: 28,
    width: 28,
  },
  seekbarCircle: {
    borderRadius: 12,
    position: 'relative',
    top: 8,
    left: 8,
    height: 12,
    width: 12,
  },
  picker: {
    color: 'white',
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'center',
  },
});

export default VideoPlayer;
