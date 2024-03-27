import React, {
  useState,
  useCallback,
  useMemo,
  useRef,
  forwardRef,
  useImperativeHandle,
  type ComponentRef,
} from 'react';
import {View, StyleSheet, Image, Platform} from 'react-native';
import NativeVideoComponent, {
  type OnAudioFocusChangedData,
  type OnAudioTracksData,
  type OnBandwidthUpdateData,
  type OnBufferData,
  type OnExternalPlaybackChangeData,
  type OnGetLicenseData,
  type OnLoadData,
  type OnLoadStartData,
  type OnPictureInPictureStatusChangedData,
  type OnPlaybackStateChangedData,
  type OnProgressData,
  type OnReceiveAdEventData,
  type OnSeekData,
  type OnTextTrackDataChangedData,
  type OnTextTracksData,
  type OnTimedMetadataData,
  type OnVideoAspectRatioData,
  type OnVideoErrorData,
  type OnVideoTracksData,
  type VideoComponentType,
  type VideoSrc,
} from './specs/VideoNativeComponent';

import type {StyleProp, ImageStyle, NativeSyntheticEvent} from 'react-native';
import {
  generateHeaderForNative,
  getReactTag,
  resolveAssetSourceForVideo,
} from './utils';
import {VideoManager} from './specs/VideoNativeComponent';
import type {ReactVideoProps} from './types/video';

export type VideoSaveData = {
  uri: string;
};

export interface VideoRef {
  seek: (time: number, tolerance?: number) => void;
  resume: () => void;
  pause: () => void;
  presentFullscreenPlayer: () => void;
  dismissFullscreenPlayer: () => void;
  restoreUserInterfaceForPictureInPictureStopCompleted: (
    restore: boolean,
  ) => void;
  save: (options: object) => Promise<VideoSaveData>;
}

const Video = forwardRef<VideoRef, ReactVideoProps>(
  (
    {
      source,
      style,
      resizeMode,
      posterResizeMode,
      poster,
      fullscreen,
      drm,
      textTracks,
      selectedVideoTrack,
      selectedAudioTrack,
      selectedTextTrack,
      mediaSession,
      onLoadStart,
      onLoad,
      onError,
      onProgress,
      onSeek,
      onEnd,
      onBuffer,
      onBandwidthUpdate,
      onExternalPlaybackChange,
      onFullscreenPlayerWillPresent,
      onFullscreenPlayerDidPresent,
      onFullscreenPlayerWillDismiss,
      onFullscreenPlayerDidDismiss,
      onReadyForDisplay,
      onPlaybackRateChange,
      onVolumeChange,
      onAudioBecomingNoisy,
      onPictureInPictureStatusChanged,
      onRestoreUserInterfaceForPictureInPictureStop,
      onReceiveAdEvent,
      onPlaybackStateChanged,
      onAudioFocusChanged,
      onIdle,
      onTimedMetadata,
      onAudioTracks,
      onTextTracks,
      onTextTrackDataChanged,
      onVideoTracks,
      onAspectRatio,
      ...rest
    },
    ref,
  ) => {
    const nativeRef = useRef<ComponentRef<VideoComponentType>>(null);
    const [showPoster, setShowPoster] = useState(!!poster);
    const [isFullscreen, setIsFullscreen] = useState(fullscreen);
    const [
      _restoreUserInterfaceForPIPStopCompletionHandler,
      setRestoreUserInterfaceForPIPStopCompletionHandler,
    ] = useState<boolean | undefined>();

    const posterStyle = useMemo<StyleProp<ImageStyle>>(
      () => ({
        ...StyleSheet.absoluteFillObject,
        resizeMode:
          posterResizeMode && posterResizeMode !== 'none'
            ? posterResizeMode
            : 'contain',
      }),
      [posterResizeMode],
    );

    const src = useMemo<VideoSrc | undefined>(() => {
      if (!source) {
        return undefined;
      }
      const resolvedSource = resolveAssetSourceForVideo(source);
      let uri = resolvedSource.uri || '';
      if (uri && uri.match(/^\//)) {
        uri = `file://${uri}`;
      }
      if (!uri) {
        console.log('Trying to load empty source');
      }
      const isNetwork = !!(uri && uri.match(/^https?:/));
      const isAsset = !!(
        uri &&
        uri.match(
          /^(assets-library|ipod-library|file|content|ms-appx|ms-appdata):/,
        )
      );

      return {
        uri,
        isNetwork,
        isAsset,
        shouldCache: resolvedSource.shouldCache || false,
        type: resolvedSource.type || '',
        mainVer: resolvedSource.mainVer || 0,
        patchVer: resolvedSource.patchVer || 0,
        requestHeaders: generateHeaderForNative(resolvedSource.headers),
        startPosition: resolvedSource.startPosition ?? -1,
        cropStart: resolvedSource.cropStart || 0,
        cropEnd: resolvedSource.cropEnd,
        title: resolvedSource.title,
        subtitle: resolvedSource.subtitle,
        description: resolvedSource.description,
        customImageUri: resolvedSource.customImageUri,
      };
    }, [source]);

    const _drm = useMemo(() => {
      if (!drm) {
        return;
      }

      return {
        type: drm.type,
        licenseServer: drm.licenseServer,
        headers: generateHeaderForNative(drm.headers),
        contentId: drm.contentId,
        certificateUrl: drm.certificateUrl,
        base64Certificate: drm.base64Certificate,
        useExternalGetLicense: !!drm.getLicense,
      };
    }, [drm]);

    const _selectedTextTrack = useMemo(() => {
      if (!selectedTextTrack) {
        return;
      }
      const value = selectedTextTrack.value
        ? `${selectedTextTrack.value}`
        : undefined;

      return {
        type: selectedTextTrack?.type,
        value,
      };
    }, [selectedTextTrack]);

    const _selectedAudioTrack = useMemo(() => {
      if (!selectedAudioTrack) {
        return;
      }
      const value = selectedAudioTrack.value
        ? `${selectedAudioTrack.value}`
        : undefined;

      return {
        type: selectedAudioTrack?.type,
        value,
      };
    }, [selectedAudioTrack]);

    const _selectedVideoTrack = useMemo(() => {
      if (!selectedVideoTrack) {
        return;
      }

      return {
        type: selectedVideoTrack?.type,
        value: selectedVideoTrack?.value,
      };
    }, [selectedVideoTrack]);

    const seek = useCallback(async (time: number, tolerance?: number) => {
      if (isNaN(time)) {
        throw new Error('Specified time is not a number');
      }

      if (!nativeRef.current) {
        console.warn('Video Component is not mounted');
        return;
      }

      Platform.select({
        ios: () => {
          nativeRef.current?.setNativeProps({
            seek: {
              time,
              tolerance: tolerance || 0,
            },
          });
        },
        default: () => {
          nativeRef.current?.setNativeProps({
            seek: time,
          });
        },
      })();
    }, []);

    const presentFullscreenPlayer = useCallback(() => {
      setIsFullscreen(true);
    }, [setIsFullscreen]);

    const dismissFullscreenPlayer = useCallback(() => {
      setIsFullscreen(false);
    }, [setIsFullscreen]);

    const save = useCallback((options: object) => {
      // VideoManager.save can be null on android & windows
      return VideoManager.save?.(options, getReactTag(nativeRef));
    }, []);

    const pause = useCallback(() => {
      return VideoManager.setPlayerPauseState(true, getReactTag(nativeRef));
    }, []);

    const resume = useCallback(() => {
      return VideoManager.setPlayerPauseState(false, getReactTag(nativeRef));
    }, []);

    const restoreUserInterfaceForPictureInPictureStopCompleted = useCallback(
      (restored: boolean) => {
        setRestoreUserInterfaceForPIPStopCompletionHandler(restored);
      },
      [setRestoreUserInterfaceForPIPStopCompletionHandler],
    );

    const onVideoLoadStart = useCallback(
      (e: NativeSyntheticEvent<OnLoadStartData>) => {
        onLoadStart?.(e.nativeEvent);
      },
      [onLoadStart],
    );

    const onVideoLoad = useCallback(
      (e: NativeSyntheticEvent<OnLoadData>) => {
        if (Platform.OS === 'windows') {
          setShowPoster(false);
        }
        onLoad?.(e.nativeEvent);
      },
      [onLoad, setShowPoster],
    );

    const onVideoError = useCallback(
      (e: NativeSyntheticEvent<OnVideoErrorData>) => {
        onError?.(e.nativeEvent);
      },
      [onError],
    );

    const onVideoProgress = useCallback(
      (e: NativeSyntheticEvent<OnProgressData>) => {
        onProgress?.(e.nativeEvent);
      },
      [onProgress],
    );

    const onVideoSeek = useCallback(
      (e: NativeSyntheticEvent<OnSeekData>) => {
        onSeek?.(e.nativeEvent);
      },
      [onSeek],
    );

    const onVideoPlaybackStateChanged = useCallback(
      (e: NativeSyntheticEvent<OnPlaybackStateChangedData>) => {
        onPlaybackStateChanged?.(e.nativeEvent);
      },
      [onPlaybackStateChanged],
    );

    // android only
    const onVideoIdle = useCallback(() => {
      onIdle?.();
    }, [onIdle]);

    const _onTimedMetadata = useCallback(
      (e: NativeSyntheticEvent<OnTimedMetadataData>) => {
        onTimedMetadata?.(e.nativeEvent);
      },
      [onTimedMetadata],
    );

    const _onAudioTracks = useCallback(
      (e: NativeSyntheticEvent<OnAudioTracksData>) => {
        onAudioTracks?.(e.nativeEvent);
      },
      [onAudioTracks],
    );

    const _onTextTracks = useCallback(
      (e: NativeSyntheticEvent<OnTextTracksData>) => {
        onTextTracks?.(e.nativeEvent);
      },
      [onTextTracks],
    );

    const _onTextTrackDataChanged = useCallback(
      (
        e: NativeSyntheticEvent<OnTextTrackDataChangedData & {target?: number}>,
      ) => {
        const {...eventData} = e.nativeEvent;
        delete eventData.target;
        onTextTrackDataChanged?.(eventData as OnTextTrackDataChangedData);
      },
      [onTextTrackDataChanged],
    );

    const _onVideoTracks = useCallback(
      (e: NativeSyntheticEvent<OnVideoTracksData>) => {
        onVideoTracks?.(e.nativeEvent);
      },
      [onVideoTracks],
    );

    const _onPlaybackRateChange = useCallback(
      (e: NativeSyntheticEvent<Readonly<{playbackRate: number}>>) => {
        onPlaybackRateChange?.(e.nativeEvent);
      },
      [onPlaybackRateChange],
    );

    const _onVolumeChange = useCallback(
      (e: NativeSyntheticEvent<Readonly<{volume: number}>>) => {
        onVolumeChange?.(e.nativeEvent);
      },
      [onVolumeChange],
    );

    const _onReadyForDisplay = useCallback(() => {
      setShowPoster(false);
      onReadyForDisplay?.();
    }, [setShowPoster, onReadyForDisplay]);

    const _onPictureInPictureStatusChanged = useCallback(
      (e: NativeSyntheticEvent<OnPictureInPictureStatusChangedData>) => {
        onPictureInPictureStatusChanged?.(e.nativeEvent);
      },
      [onPictureInPictureStatusChanged],
    );

    const _onAudioFocusChanged = useCallback(
      (e: NativeSyntheticEvent<OnAudioFocusChangedData>) => {
        onAudioFocusChanged?.(e.nativeEvent);
      },
      [onAudioFocusChanged],
    );

    const onVideoBuffer = useCallback(
      (e: NativeSyntheticEvent<OnBufferData>) => {
        onBuffer?.(e.nativeEvent);
      },
      [onBuffer],
    );

    const onVideoExternalPlaybackChange = useCallback(
      (e: NativeSyntheticEvent<OnExternalPlaybackChangeData>) => {
        onExternalPlaybackChange?.(e.nativeEvent);
      },
      [onExternalPlaybackChange],
    );

    const _onBandwidthUpdate = useCallback(
      (e: NativeSyntheticEvent<OnBandwidthUpdateData>) => {
        onBandwidthUpdate?.(e.nativeEvent);
      },
      [onBandwidthUpdate],
    );

    const _onReceiveAdEvent = useCallback(
      (e: NativeSyntheticEvent<OnReceiveAdEventData>) => {
        onReceiveAdEvent?.(e.nativeEvent);
      },
      [onReceiveAdEvent],
    );

    const _onVideoAspectRatio = useCallback(
      (e: NativeSyntheticEvent<OnVideoAspectRatioData>) => {
        onAspectRatio?.(e.nativeEvent);
      },
      [onAspectRatio],
    );

    const onGetLicense = useCallback(
      (event: NativeSyntheticEvent<OnGetLicenseData>) => {
        if (drm && drm.getLicense instanceof Function) {
          const data = event.nativeEvent;
          if (data && data.spcBase64) {
            const getLicenseOverride = drm.getLicense(
              data.spcBase64,
              data.contentId,
              data.licenseUrl,
              data.loadedLicenseUrl,
            );
            const getLicensePromise = Promise.resolve(getLicenseOverride); // Handles both scenarios, getLicenseOverride being a promise and not.
            getLicensePromise
              .then((result) => {
                if (result !== undefined) {
                  nativeRef.current &&
                    VideoManager.setLicenseResult(
                      result,
                      data.loadedLicenseUrl,
                      getReactTag(nativeRef),
                    );
                } else {
                  nativeRef.current &&
                    VideoManager.setLicenseResultError(
                      'Empty license result',
                      data.loadedLicenseUrl,
                      getReactTag(nativeRef),
                    );
                }
              })
              .catch(() => {
                nativeRef.current &&
                  VideoManager.setLicenseResultError(
                    'fetch error',
                    data.loadedLicenseUrl,
                    getReactTag(nativeRef),
                  );
              });
          } else {
            VideoManager.setLicenseResultError(
              'No spc received',
              data.loadedLicenseUrl,
              getReactTag(nativeRef),
            );
          }
        }
      },
      [drm],
    );

    useImperativeHandle(
      ref,
      () => ({
        seek,
        presentFullscreenPlayer,
        dismissFullscreenPlayer,
        save,
        pause,
        resume,
        restoreUserInterfaceForPictureInPictureStopCompleted,
      }),
      [
        seek,
        presentFullscreenPlayer,
        dismissFullscreenPlayer,
        save,
        pause,
        resume,
        restoreUserInterfaceForPictureInPictureStopCompleted,
      ],
    );

    return (
      <View style={style}>
        <NativeVideoComponent
          ref={nativeRef}
          {...rest}
          src={src}
          drm={_drm}
          style={StyleSheet.absoluteFill}
          resizeMode={resizeMode}
          fullscreen={isFullscreen}
          restoreUserInterfaceForPIPStopCompletionHandler={
            _restoreUserInterfaceForPIPStopCompletionHandler
          }
          textTracks={textTracks}
          selectedTextTrack={_selectedTextTrack}
          selectedAudioTrack={_selectedAudioTrack}
          selectedVideoTrack={_selectedVideoTrack}
          onGetLicense={onGetLicense}
          onVideoLoad={onVideoLoad}
          onVideoLoadStart={onVideoLoadStart}
          onVideoError={onVideoError}
          onVideoProgress={onVideoProgress}
          onVideoSeek={onVideoSeek}
          onVideoEnd={onEnd}
          onVideoBuffer={onVideoBuffer}
          onVideoPlaybackStateChanged={onVideoPlaybackStateChanged}
          onVideoBandwidthUpdate={_onBandwidthUpdate}
          onTimedMetadata={_onTimedMetadata}
          onAudioTracks={_onAudioTracks}
          onTextTracks={_onTextTracks}
          onTextTrackDataChanged={_onTextTrackDataChanged}
          onVideoTracks={_onVideoTracks}
          onVideoFullscreenPlayerDidDismiss={onFullscreenPlayerDidDismiss}
          onVideoFullscreenPlayerDidPresent={onFullscreenPlayerDidPresent}
          onVideoFullscreenPlayerWillDismiss={onFullscreenPlayerWillDismiss}
          onVideoFullscreenPlayerWillPresent={onFullscreenPlayerWillPresent}
          onVideoExternalPlaybackChange={onVideoExternalPlaybackChange}
          onVideoIdle={onVideoIdle}
          onAudioFocusChanged={_onAudioFocusChanged}
          onReadyForDisplay={_onReadyForDisplay}
          onPlaybackRateChange={_onPlaybackRateChange}
          onVolumeChange={_onVolumeChange}
          onVideoAudioBecomingNoisy={onAudioBecomingNoisy}
          onPictureInPictureStatusChanged={_onPictureInPictureStatusChanged}
          onRestoreUserInterfaceForPictureInPictureStop={
            onRestoreUserInterfaceForPictureInPictureStop
          }
          onVideoAspectRatio={_onVideoAspectRatio}
          onReceiveAdEvent={_onReceiveAdEvent}
        />
        {showPoster ? (
          <Image style={posterStyle} source={{uri: poster}} />
        ) : null}
      </View>
    );
  },
);

Video.displayName = 'Video';
export default Video;
