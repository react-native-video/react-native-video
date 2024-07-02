'use strict';

import React, {FC, useCallback, useMemo, useRef, useState} from 'react';

import {TouchableOpacity, View} from 'react-native';

import Video, {
  AudioTrack,
  OnAudioTracksData,
  OnLoadData,
  OnProgressData,
  OnTextTracksData,
  OnVideoAspectRatioData,
  TextTrack,
  OnBufferData,
  OnAudioFocusChangedData,
  OnVideoErrorData,
  VideoRef,
  OnTextTrackDataChangedData,
  OnSeekData,
  OnPlaybackStateChangedData,
  OnPlaybackRateChangeData,
  OnVideoTracksData,
  SelectedVideoTrackType,
  BufferingStrategyType,
  ReactVideoSource,
  SelectedTrackType,
  ResizeMode,
  VideoTrack,
  SelectedTrack,
  VideoDecoderProperties,
  SelectedVideoTrack,
} from 'react-native-video';
import styles from './styles';
import {AdditionalSourceInfo} from './types';
import {
  bufferConfig,
  isAndroid,
  isIos,
  samplePoster,
  srcList,
  textTracksSelectionBy,
} from './constants';
import {
  AudioTrackSelector,
  Indicator,
  Seeker,
  TextTrackSelector,
  toast,
  TopControl,
  VideoTrackSelector,
} from './components';
import ToggleControl from './ToggleControl.tsx';
import MultiValueControl, {
  MultiValueControlPropType,
} from './MultiValueControl.tsx';

type Props = NonNullable<unknown>;

const VideoPlayer: FC<Props> = ({}) => {
  const [rate, setRate] = useState(1);
  const [volume, setVolume] = useState(1);
  const [muted, setMuted] = useState(false);
  const [resizeMode, setResizeMode] = useState(ResizeMode.CONTAIN);
  const [duration, setDuration] = useState(0);
  const [currentTime, setCurrentTime] = useState(0);
  const [_, setVideoSize] = useState({videoWidth: 0, videoHeight: 0});
  const [paused, setPaused] = useState(false);
  const [fullscreen, setFullscreen] = useState(true);
  const [decoration, setDecoration] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [audioTracks, setAudioTracks] = useState<AudioTrack[]>([]);
  const [textTracks, setTextTracks] = useState<TextTrack[]>([]);
  const [videoTracks, setVideoTracks] = useState<VideoTrack[]>([]);
  const [selectedAudioTrack, setSelectedAudioTrack] = useState<
    SelectedTrack | undefined
  >(undefined);
  const [selectedTextTrack, setSelectedTextTrack] = useState<
    SelectedTrack | undefined
  >(undefined);
  const [selectedVideoTrack, setSelectedVideoTrack] =
    useState<SelectedVideoTrack>({
      type: SelectedVideoTrackType.AUTO,
    });
  const [srcListId, setSrcListId] = useState(0);
  const [repeat, setRepeat] = useState(false);
  const [controls, setControls] = useState(false);
  const [useCache, setUseCache] = useState(false);
  const [poster, setPoster] = useState<string | undefined>(undefined);
  const [showNotificationControls, setShowNotificationControls] =
    useState(false);
  const [isSeeking, setIsSeeking] = useState(false);

  const videoRef = useRef<VideoRef>(null);
  const viewStyle = fullscreen ? styles.fullScreen : styles.halfScreen;
  const currentSrc = srcList[srcListId];
  const additional = currentSrc as AdditionalSourceInfo;

  const onAudioTracks = (data: OnAudioTracksData) => {
    const selectedTrack = data.audioTracks?.find((x: AudioTrack) => {
      return x.selected;
    });
    if (selectedTrack?.index) {
      setAudioTracks(data.audioTracks);
      setSelectedAudioTrack({
        type: SelectedTrackType.INDEX,
        value: selectedTrack.index,
      });
    } else {
      setAudioTracks(data.audioTracks);
    }
  };

  const onVideoTracks = (data: OnVideoTracksData) => {
    console.log('onVideoTracks', data.videoTracks);
    setVideoTracks(data.videoTracks);
  };

  const onTextTracks = (data: OnTextTracksData) => {
    const selectedTrack = data.textTracks?.find((x: TextTrack) => {
      return x?.selected;
    });

    if (selectedTrack?.language) {
      setTextTracks(data.textTracks);
      if (textTracksSelectionBy === 'index') {
        setSelectedTextTrack({
          type: SelectedTrackType.INDEX,
          value: selectedTrack?.index,
        });
      } else {
        setSelectedTextTrack({
          type: SelectedTrackType.LANGUAGE,
          value: selectedTrack?.language,
        });
      }
    } else {
      setTextTracks(data.textTracks);
    }
  };

  const onLoad = (data: OnLoadData) => {
    setDuration(data.duration);
    onAudioTracks(data);
    onTextTracks(data);
    onVideoTracks(data);
  };

  const onProgress = (data: OnProgressData) => {
    setCurrentTime(data.currentTime);
  };

  const onSeek = (data: OnSeekData) => {
    setCurrentTime(data.currentTime);
    setIsSeeking(false);
  };

  const onVideoLoadStart = () => {
    console.log('onVideoLoadStart');
    setIsLoading(true);
  };

  const onTextTrackDataChanged = (data: OnTextTrackDataChangedData) => {
    console.log(`Subtitles: ${JSON.stringify(data, null, 2)}`);
  };

  const onAspectRatio = (data: OnVideoAspectRatioData) => {
    console.log('onAspectRadio called ' + JSON.stringify(data));
    setVideoSize({videoWidth: data.width, videoHeight: data.height});
  };

  const onVideoBuffer = (param: OnBufferData) => {
    console.log('onVideoBuffer');
    setIsLoading(param.isBuffering);
  };

  const onReadyForDisplay = () => {
    console.log('onReadyForDisplay');
    setIsLoading(false);
  };

  const onAudioBecomingNoisy = () => {
    setPaused(true);
  };

  const onAudioFocusChanged = (event: OnAudioFocusChangedData) => {
    setPaused(!event.hasAudioFocus);
  };

  const onError = (err: OnVideoErrorData) => {
    console.log(JSON.stringify(err));
    toast(true, 'error: ' + JSON.stringify(err));
  };

  const onEnd = () => {
    if (!repeat) {
      channelUp();
    }
  };

  const onPlaybackRateChange = (data: OnPlaybackRateChangeData) => {
    console.log('onPlaybackRateChange', data);
  };

  const onPlaybackStateChanged = (data: OnPlaybackStateChangedData) => {
    console.log('onPlaybackStateChanged', data);
  };

  const goToChannel = (channel: number) => {
    setSrcListId(channel);
    setDuration(0);
    setCurrentTime(0);
    setVideoSize({videoWidth: 0, videoHeight: 0});
    setIsLoading(false);
    setAudioTracks([]);
    setTextTracks([]);
    setSelectedAudioTrack(undefined);
    setSelectedTextTrack(undefined);
    setSelectedVideoTrack({
      type: SelectedVideoTrackType.AUTO,
    });
  };

  const channelUp = useCallback(() => {
    console.log('channel up');
    goToChannel((srcListId + 1) % srcList.length);
  }, [srcListId]);

  const channelDown = useCallback(() => {
    console.log('channel down');
    goToChannel((srcListId + srcList.length - 1) % srcList.length);
  }, [srcListId]);

  const popupInfo = useCallback(() => {
    VideoDecoderProperties.getWidevineLevel().then((widevineLevel: number) => {
      VideoDecoderProperties.isHEVCSupported().then((hevc: string) => {
        VideoDecoderProperties.isCodecSupported('video/avc', 1920, 1080).then(
          (avc: string) => {
            toast(
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
  }, []);

  const toggleFullscreen = useCallback(() => {
    setFullscreen(!fullscreen);
  }, [fullscreen]);

  const toggleControls = useCallback(() => {
    setControls(!controls);
  }, [controls]);

  const toggleDecoration = useCallback(() => {
    setDecoration(!decoration);
    videoRef.current?.setFullScreen(!decoration);
  }, [decoration]);

  const toggleShowNotificationControls = useCallback(() => {
    setShowNotificationControls(!showNotificationControls);
  }, [showNotificationControls]);

  const onSelectedAudioTrackChange = (itemValue: string) => {
    console.log('on audio value change ' + itemValue);
    if (itemValue === 'none') {
      setSelectedAudioTrack({
        type: SelectedTrackType.DISABLED,
      });
    } else {
      setSelectedAudioTrack({
        type: SelectedTrackType.INDEX,
        value: itemValue,
      });
    }
  };

  const onSelectedTextTrackChange = (itemValue: string) => {
    console.log('on value change ' + itemValue);
    const type =
      textTracksSelectionBy === 'index'
        ? SelectedTrackType.INDEX
        : SelectedTrackType.LANGUAGE;
    setSelectedTextTrack({type, value: itemValue});
  };

  const onSelectedVideoTrackChange = (itemValue: string) => {
    console.log('on value change ' + itemValue);
    if (itemValue === undefined || itemValue === 'auto') {
      setSelectedVideoTrack({
        type: SelectedVideoTrackType.AUTO,
      });
    } else {
      setSelectedVideoTrack({
        type: SelectedVideoTrackType.INDEX,
        value: itemValue,
      });
    }
  };

  const videoSeek = (position: number) => {
    setIsSeeking(true);
    videoRef.current?.seek(position);
  };

  const onRateSelected = (value: MultiValueControlPropType) => {
    if (typeof value === 'number') {
      setRate(value);
    }
  };
  const onVolumeSelected = (value: MultiValueControlPropType) => {
    if (typeof value === 'number') {
      setVolume(value);
    }
  };
  const onResizeModeSelected = (value: MultiValueControlPropType) => {
    if (typeof value === 'object') {
      setResizeMode(value);
    }
  };

  const renderOverlay = useMemo(() => {
    return (
      <>
        <Indicator isLoading={isLoading} />
        <View style={styles.topControls}>
          <View style={styles.resizeModeControl}>
            <TopControl
              srcListId={srcListId}
              showRNVControls={controls}
              toggleControls={toggleControls}
            />
          </View>
        </View>
        {!controls ? (
          <>
            <View style={styles.leftControls}>
              <ToggleControl onPress={channelDown} text="ChDown" />
            </View>
            <View style={styles.rightControls}>
              <ToggleControl onPress={channelUp} text="ChUp" />
            </View>
            <View style={styles.bottomControls}>
              <View style={styles.generalControls}>
                {isAndroid ? (
                  <View style={styles.generalControls}>
                    <ToggleControl onPress={popupInfo} text="decoderInfo" />
                    <ToggleControl
                      isSelected={useCache}
                      onPress={() => {
                        setUseCache(!useCache);
                      }}
                      selectedText="enable cache"
                      unselectedText="disable cache"
                    />
                  </View>
                ) : null}
                <ToggleControl
                  isSelected={paused}
                  onPress={() => {
                    setPaused(!paused);
                  }}
                  selectedText="pause"
                  unselectedText="playing"
                />
                <ToggleControl
                  isSelected={repeat}
                  onPress={() => {
                    setRepeat(!repeat);
                  }}
                  selectedText="loop enable"
                  unselectedText="loop disable"
                />
                <ToggleControl onPress={toggleFullscreen} text="fullscreen" />
                <ToggleControl onPress={toggleDecoration} text="decoration" />
                <ToggleControl
                  isSelected={!!poster}
                  onPress={() => {
                    setPoster(poster ? undefined : samplePoster);
                  }}
                  selectedText="poster"
                  unselectedText="no poster"
                />
                <ToggleControl
                  isSelected={showNotificationControls}
                  onPress={toggleShowNotificationControls}
                  selectedText="hide notification controls"
                  unselectedText="show notification controls"
                />
              </View>
              <View style={styles.generalControls}>
                {/* shall be replaced by slider */}
                <MultiValueControl
                  values={[0, 0.25, 0.5, 1.0, 1.5, 2.0]}
                  onPress={onRateSelected}
                  selected={rate}
                />
                {/* shall be replaced by slider */}
                <MultiValueControl
                  values={[0.5, 1, 1.5]}
                  onPress={onVolumeSelected}
                  selected={volume}
                />
                <MultiValueControl
                  values={[
                    ResizeMode.COVER,
                    ResizeMode.CONTAIN,
                    ResizeMode.STRETCH,
                  ]}
                  onPress={onResizeModeSelected}
                  selected={resizeMode}
                />
                <ToggleControl
                  isSelected={muted}
                  onPress={() => {
                    setMuted(!muted);
                  }}
                  text="muted"
                />
                {isIos ? (
                  <ToggleControl
                    isSelected={paused}
                    onPress={() => {
                      videoRef.current
                        ?.save({})
                        ?.then((response: unknown) => {
                          console.log('Downloaded URI', response);
                        })
                        .catch((error: unknown) => {
                          console.log('error during save ', error);
                        });
                    }}
                    text="save"
                  />
                ) : null}
              </View>
              <Seeker
                currentTime={currentTime}
                duration={duration}
                isLoading={isLoading}
                videoSeek={prop => videoSeek(prop)}
                isUISeeking={isSeeking}
              />
              <View style={styles.generalControls}>
                <AudioTrackSelector
                  audioTracks={audioTracks}
                  selectedAudioTrack={selectedAudioTrack}
                  onValueChange={onSelectedAudioTrackChange}
                />
                <TextTrackSelector
                  textTracks={textTracks}
                  selectedTextTrack={selectedTextTrack}
                  onValueChange={onSelectedTextTrackChange}
                  textTracksSelectionBy={textTracksSelectionBy}
                />
                <VideoTrackSelector
                  videoTracks={videoTracks}
                  selectedVideoTrack={selectedVideoTrack}
                  onValueChange={onSelectedVideoTrackChange}
                />
              </View>
            </View>
          </>
        ) : null}
      </>
    );
  }, [
    audioTracks,
    channelDown,
    channelUp,
    controls,
    currentTime,
    duration,
    isLoading,
    isSeeking,
    muted,
    paused,
    popupInfo,
    poster,
    rate,
    repeat,
    resizeMode,
    selectedAudioTrack,
    selectedTextTrack,
    selectedVideoTrack,
    showNotificationControls,
    srcListId,
    textTracks,
    toggleControls,
    toggleDecoration,
    toggleFullscreen,
    toggleShowNotificationControls,
    useCache,
    videoTracks,
    volume,
  ]);

  return (
    <View style={styles.container}>
      {(srcList[srcListId] as AdditionalSourceInfo)?.noView ? null : (
        <TouchableOpacity style={viewStyle}>
          <Video
            showNotificationControls={showNotificationControls}
            ref={videoRef}
            source={currentSrc as ReactVideoSource}
            textTracks={additional?.textTracks}
            adTagUrl={additional?.adTagUrl}
            drm={additional?.drm}
            style={viewStyle}
            rate={rate}
            paused={paused}
            volume={volume}
            muted={muted}
            fullscreen={fullscreen}
            controls={controls}
            resizeMode={resizeMode}
            onLoad={onLoad}
            onAudioTracks={onAudioTracks}
            onTextTracks={onTextTracks}
            onVideoTracks={onVideoTracks}
            onTextTrackDataChanged={onTextTrackDataChanged}
            onProgress={onProgress}
            onEnd={onEnd}
            progressUpdateInterval={1000}
            onError={onError}
            onAudioBecomingNoisy={onAudioBecomingNoisy}
            onAudioFocusChanged={onAudioFocusChanged}
            onLoadStart={onVideoLoadStart}
            onAspectRatio={onAspectRatio}
            onReadyForDisplay={onReadyForDisplay}
            onBuffer={onVideoBuffer}
            onSeek={onSeek}
            repeat={repeat}
            selectedTextTrack={selectedTextTrack}
            selectedAudioTrack={selectedAudioTrack}
            selectedVideoTrack={selectedVideoTrack}
            playInBackground={false}
            bufferConfig={{
              ...bufferConfig,
              cacheSizeMB: useCache ? 200 : 0,
            }}
            preventsDisplaySleepDuringVideoPlayback={true}
            poster={poster}
            onPlaybackRateChange={onPlaybackRateChange}
            onPlaybackStateChanged={onPlaybackStateChanged}
            bufferingStrategy={BufferingStrategyType.DEFAULT}
            debug={{enable: true, thread: true}}
          />
        </TouchableOpacity>
      )}
      {renderOverlay}
    </View>
  );
};
export default VideoPlayer;
