import React, {forwardRef, memo, useCallback} from 'react';
import {Indicator} from './Indicator.tsx';
import {View} from 'react-native';
import styles from '../styles.tsx';
import ToggleControl from '../ToggleControl.tsx';
import {
  isAndroid,
  isIos,
  samplePoster,
  textTracksSelectionBy,
} from '../constants';
import MultiValueControl, {
  MultiValueControlPropType,
} from '../MultiValueControl.tsx';
import {
  ResizeMode,
  SelectedTrackType,
  SelectedVideoTrackType,
  VideoDecoderProperties,
  VideoRef,
} from 'react-native-video';
import {StateType} from '../types';
import {toast} from './Toast';
import {Seeker} from './Seeker';
import {AudioTrackSelector} from './AudioTracksSelector';
import {TextTrackSelector} from './TextTracksSelector';
import {VideoTrackSelector} from './VideoTracksSelector';
import {TopControl} from './TopControl';

type Props = {
  channelDown: () => void;
  channelUp: () => void;
  setState: (value: StateType) => void;
  state: StateType;
};

const _Overlay = forwardRef<VideoRef, Props>((props, ref) => {
  const {state, setState, channelUp, channelDown} = props;
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

  const toggleFullscreen = () => {
    setState({...state, fullscreen: !state.fullscreen});
  };
  const toggleControls = () => {
    setState({...state, showRNVControls: !state.showRNVControls});
  };

  const toggleDecoration = () => {
    setState({...state, decoration: !state.decoration});
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-expect-error
    ref.current?.setFullScreen(!state.decoration);
  };

  const toggleShowNotificationControls = () => {
    setState({
      ...state,
      showNotificationControls: !state.showNotificationControls,
    });
  };

  const onSelectedAudioTrackChange = (itemValue: string) => {
    console.log('on audio value change ' + itemValue);
    if (itemValue === 'none') {
      setState({
        ...state,
        selectedAudioTrack: {
          type: SelectedTrackType.DISABLED,
        },
      });
    } else {
      setState({
        ...state,
        selectedAudioTrack: {
          type: SelectedTrackType.INDEX,
          value: itemValue,
        },
      });
    }
  };

  const onSelectedTextTrackChange = (itemValue: string) => {
    console.log('on value change ' + itemValue);
    setState({
      ...state,
      selectedTextTrack: {
        type:
          textTracksSelectionBy === 'index'
            ? SelectedTrackType.INDEX
            : SelectedTrackType.LANGUAGE,
        value: itemValue,
      },
    });
  };

  const onSelectedVideoTrackChange = (itemValue: string) => {
    console.log('on value change ' + itemValue);
    if (itemValue === undefined || itemValue === 'auto') {
      setState({
        ...state,
        selectedVideoTrack: {
          type: SelectedVideoTrackType.AUTO,
        },
      });
    } else {
      setState({
        ...state,
        selectedVideoTrack: {
          type: SelectedVideoTrackType.INDEX,
          value: itemValue,
        },
      });
    }
  };

  const videoSeek = (position: number) => {
    setState({...state, isSeeking: true});
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-expect-error
    ref.current?.seek(position);
  };

  const onRateSelected = (value: MultiValueControlPropType) => {
    if (typeof value === 'number') {
      setState({...state, rate: value});
    }
  };
  const onVolumeSelected = (value: MultiValueControlPropType) => {
    if (typeof value === 'number') {
      setState({...state, volume: value});
    }
  };
  const onResizeModeSelected = (value: MultiValueControlPropType) => {
    if (typeof value === 'object') {
      setState({...state, resizeMode: value});
    }
  };

  return (
    <>
      <Indicator isLoading={state.isLoading} />
      <View style={styles.topControls}>
        <View style={styles.resizeModeControl}>
          <TopControl
            srcListId={state.srcListId}
            showRNVControls={state.showRNVControls}
            toggleControls={toggleControls}
          />
        </View>
      </View>
      {!state.showRNVControls ? (
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
                    isSelected={state.useCache}
                    onPress={() => {
                      setState({...state, useCache: !state.useCache});
                    }}
                    selectedText="enable cache"
                    unselectedText="disable cache"
                  />
                </View>
              ) : null}
              <ToggleControl
                isSelected={state.paused}
                onPress={() => {
                  setState({...state, paused: !state.paused});
                }}
                selectedText="pause"
                unselectedText="playing"
              />
              <ToggleControl
                isSelected={state.loop}
                onPress={() => {
                  setState({...state, loop: !state.loop});
                }}
                selectedText="loop enable"
                unselectedText="loop disable"
              />
              <ToggleControl onPress={toggleFullscreen} text="fullscreen" />
              <ToggleControl onPress={toggleDecoration} text="decoration" />
              <ToggleControl
                isSelected={!!state.poster}
                onPress={() => {
                  setState({
                    ...state,
                    poster: state.poster ? undefined : samplePoster,
                  });
                }}
                selectedText="poster"
                unselectedText="no poster"
              />
              <ToggleControl
                isSelected={state.showNotificationControls}
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
                selected={state.rate}
              />
              {/* shall be replaced by slider */}
              <MultiValueControl
                values={[0.5, 1, 1.5]}
                onPress={onVolumeSelected}
                selected={state.volume}
              />
              <MultiValueControl
                values={[
                  ResizeMode.COVER,
                  ResizeMode.CONTAIN,
                  ResizeMode.STRETCH,
                ]}
                onPress={onResizeModeSelected}
                selected={state.resizeMode}
              />
              <ToggleControl
                isSelected={state.muted}
                onPress={() => {
                  setState({...state, muted: !state.muted});
                }}
                text="muted"
              />
              {isIos ? (
                <ToggleControl
                  isSelected={state.paused}
                  onPress={() => {
                    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
                    // @ts-expect-error
                    ref.current
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
              currentTime={state.currentTime}
              duration={state.duration}
              isLoading={state.isLoading}
              videoSeek={prop => videoSeek(prop)}
              isUISeeking={state.isSeeking}
            />
            <View style={styles.generalControls}>
              <AudioTrackSelector
                audioTracks={state.audioTracks}
                selectedAudioTrack={state.selectedAudioTrack}
                onValueChange={onSelectedAudioTrackChange}
              />
              <TextTrackSelector
                textTracks={state.textTracks}
                selectedTextTrack={state.selectedTextTrack}
                onValueChange={onSelectedTextTrackChange}
                textTracksSelectionBy={textTracksSelectionBy}
              />
              <VideoTrackSelector
                videoTracks={state.videoTracks}
                selectedVideoTrack={state.selectedVideoTrack}
                onValueChange={onSelectedVideoTrackChange}
              />
            </View>
          </View>
        </>
      ) : null}
    </>
  );
});

export const Overlay = memo(_Overlay);
