import React, {
  useState,
  useCallback,
  useMemo,
  useRef,
  forwardRef,
  useImperativeHandle,
  type ElementRef,
} from 'react';
import {
  View,
  StyleSheet,
  Image,
  Platform,
  type StyleProp,
  type ImageStyle,
  type NativeSyntheticEvent,
} from 'react-native';

import NativeVideoComponent, {Commands} from './specs/VideoNativeComponent';
import type {
  OnAudioFocusChangedData,
  OnAudioTracksData,
  OnBandwidthUpdateData,
  OnBufferData,
  OnControlsVisibilityChange,
  OnExternalPlaybackChangeData,
  OnGetLicenseData,
  OnLoadStartData,
  OnPictureInPictureStatusChangedData,
  OnPlaybackStateChangedData,
  OnProgressData,
  OnSeekData,
  OnTextTrackDataChangedData,
  OnTimedMetadataData,
  OnVideoAspectRatioData,
  OnVideoErrorData,
  OnVideoTracksData,
  VideoSrc,
} from './specs/VideoNativeComponent';
import {
  generateHeaderForNative,
  getReactTag,
  resolveAssetSourceForVideo,
} from './utils';
import type {
  OnLoadData,
  OnTextTracksData,
  OnReceiveAdEventData,
  ReactVideoProps,
} from './types';
import {ViewType} from './types';
import type {VideoSaveData} from './specs/NativeVideoManagerModule';
import NativeVideoManagerModule from './specs/NativeVideoManagerModule';

export interface VideoRef {
  seek: (time: number, tolerance?: number) => void;
  resume: () => void;
  pause: () => void;
  presentFullscreenPlayer: () => void;
  dismissFullscreenPlayer: () => void;
  restoreUserInterfaceForPictureInPictureStopCompleted: (
    restore: boolean,
  ) => void;
  setVolume: (volume: number) => void;
  setFullScreen: (fullScreen: boolean) => void;
  save: (options: object) => Promise<VideoSaveData> | void;
  getCurrentPosition: () => Promise<number>;
}

const Video = forwardRef<VideoRef, ReactVideoProps>(
  (
    {
      source,
      style,
      resizeMode,
      posterResizeMode,
      poster,
      drm,
      textTracks,
      selectedVideoTrack,
      selectedAudioTrack,
      selectedTextTrack,
      useTextureView,
      useSecureView,
      viewType,
      onLoadStart,
      onLoad,
      onError,
      onProgress,
      onSeek,
      onEnd,
      onBuffer,
      onBandwidthUpdate,
      onControlsVisibilityChange,
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
    const nativeRef = useRef<ElementRef<typeof NativeVideoComponent>>(null);
    const [showPoster, setShowPoster] = useState(!!poster);
    const [
      _restoreUserInterfaceForPIPStopCompletionHandler,
      setRestoreUserInterfaceForPIPStopCompletionHandler,
    ] = useState<boolean | undefined>();

    const hasPoster = !!poster;

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
      const isNetwork = !!(uri && uri.match(/^(rtp|rtsp|http|https):/));
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
        metadata: resolvedSource.metadata,
        textTracksAllowChunklessPreparation:
          resolvedSource.textTracksAllowChunklessPreparation,
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
      const typeOfValueProp = typeof selectedTextTrack.value;
      if (
        typeOfValueProp !== 'number' &&
        typeOfValueProp !== 'string' &&
        typeOfValueProp !== 'undefined'
      ) {
        console.warn(
          'invalid type provided to selectedTextTrack.value: ',
          typeOfValueProp,
        );
        return;
      }
      return {
        type: selectedTextTrack?.type,
        value: `${selectedTextTrack.value}`,
      };
    }, [selectedTextTrack]);

    const _selectedAudioTrack = useMemo(() => {
      if (!selectedAudioTrack) {
        return;
      }
      const typeOfValueProp = typeof selectedAudioTrack.value;
      if (
        typeOfValueProp !== 'number' &&
        typeOfValueProp !== 'string' &&
        typeOfValueProp !== 'undefined'
      ) {
        console.warn(
          'invalid type provided to selectedAudioTrack.value: ',
          typeOfValueProp,
        );
        return;
      }

      return {
        type: selectedAudioTrack?.type,
        value: `${selectedAudioTrack.value}`,
      };
    }, [selectedAudioTrack]);

    const _selectedVideoTrack = useMemo(() => {
      if (!selectedVideoTrack) {
        return;
      }
      const typeOfValueProp = typeof selectedVideoTrack.value;
      if (
        typeOfValueProp !== 'number' &&
        typeOfValueProp !== 'string' &&
        typeOfValueProp !== 'undefined'
      ) {
        console.warn(
          'invalid type provided to selectedVideoTrack.value: ',
          typeOfValueProp,
        );
        return;
      }
      return {
        type: selectedVideoTrack?.type,
        value: `${selectedVideoTrack.value}`,
      };
    }, [selectedVideoTrack]);

    const seek = useCallback(async (time: number, tolerance?: number) => {
      if (isNaN(time) || time === null) {
        throw new Error("Specified time is not a number: '" + time + "'");
      }

      if (!nativeRef.current) {
        console.warn('Video Component is not mounted');
        return;
      }

      const callSeekFunction = () => {
        nativeRef.current &&
          Commands.seek(nativeRef.current, time, tolerance || 0);
      };

      Platform.select({
        ios: callSeekFunction,
        android: callSeekFunction,
        default: () => {
          // TODO: Implement Commands.seek for windows
          nativeRef.current?.setNativeProps({seek: time});
        },
      })();
    }, []);

    const pause = useCallback(() => {
      return (
        nativeRef.current &&
        Commands.setPlayerPauseState(nativeRef.current, true)
      );
    }, []);

    const resume = useCallback(() => {
      return (
        nativeRef.current &&
        Commands.setPlayerPauseState(nativeRef.current, false)
      );
    }, []);

    const restoreUserInterfaceForPictureInPictureStopCompleted = useCallback(
      (restored: boolean) => {
        setRestoreUserInterfaceForPIPStopCompletionHandler(restored);
      },
      [setRestoreUserInterfaceForPIPStopCompletionHandler],
    );

    const setVolume = useCallback((volume: number) => {
      return (
        nativeRef.current && Commands.setVolumeCMD(nativeRef.current, volume)
      );
    }, []);

    const setFullScreen = useCallback((fullScreen: boolean) => {
      return (
        nativeRef.current &&
        Commands.setFullScreen(nativeRef.current, fullScreen)
      );
    }, []);

    const presentFullscreenPlayer = useCallback(
      () => setFullScreen(true),
      [setFullScreen],
    );

    const dismissFullscreenPlayer = useCallback(
      () => setFullScreen(false),
      [setFullScreen],
    );

    const save = useCallback((options: object) => {
      // VideoManager.save can be null on android & windows
      if (Platform.OS !== 'ios') {
        return;
      }
      // @todo Must implement it in a different way.
      return NativeVideoManagerModule.save?.(getReactTag(nativeRef), options);
    }, []);

    const getCurrentPosition = useCallback(() => {
      // @todo Must implement it in a different way.
      return NativeVideoManagerModule.getCurrentPosition(
        getReactTag(nativeRef),
      );
    }, []);

    const onVideoLoadStart = useCallback(
      (e: NativeSyntheticEvent<OnLoadStartData>) => {
        hasPoster && setShowPoster(true);
        onLoadStart?.(e.nativeEvent);
      },
      [hasPoster, onLoadStart],
    );

    const onVideoLoad = useCallback(
      (e: NativeSyntheticEvent<OnLoadData>) => {
        if (Platform.OS === 'windows') {
          hasPoster && setShowPoster(false);
        }
        onLoad?.(e.nativeEvent);
      },
      [onLoad, hasPoster, setShowPoster],
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
      hasPoster && setShowPoster(false);
      onReadyForDisplay?.();
    }, [setShowPoster, hasPoster, onReadyForDisplay]);

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

    const _onControlsVisibilityChange = useCallback(
      (e: NativeSyntheticEvent<OnControlsVisibilityChange>) => {
        onControlsVisibilityChange?.(e.nativeEvent);
      },
      [onControlsVisibilityChange],
    );

    const usingExternalGetLicense = drm?.getLicense instanceof Function;

    const onGetLicense = useCallback(
      async (event: NativeSyntheticEvent<OnGetLicenseData>) => {
        if (!usingExternalGetLicense) {
          return;
        }

        const data = event.nativeEvent;
        let result;
        if (data?.spcBase64) {
          const getLicenseOverride = drm.getLicense(
            data.spcBase64,
            data.contentId,
            data.licenseUrl,
            data.loadedLicenseUrl,
          );
          const getLicensePromise = Promise.resolve(getLicenseOverride); // Handles both scenarios, getLicenseOverride being a promise and not.
          try {
            result = await getLicensePromise;
            result ??= 'Empty license result';
          } catch {
            result = 'fetch error';
          }
        } else {
          result = 'No spc received';
        }
        if (nativeRef.current) {
          Commands.setLicenseResultError(
            nativeRef.current,
            result,
            data.loadedLicenseUrl,
          );
        }
      },
      [drm, usingExternalGetLicense],
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
        setVolume,
        getCurrentPosition,
        setFullScreen,
      }),
      [
        seek,
        presentFullscreenPlayer,
        dismissFullscreenPlayer,
        save,
        pause,
        resume,
        restoreUserInterfaceForPictureInPictureStopCompleted,
        setVolume,
        getCurrentPosition,
        setFullScreen,
      ],
    );

    const _viewType = useMemo(() => {
      const hasValidDrmProp =
        drm !== undefined && Object.keys(drm).length !== 0;

      const shallForceViewType =
        hasValidDrmProp && (viewType === ViewType.TEXTURE || useTextureView);

      if (shallForceViewType) {
        console.warn(
          'cannot use DRM on texture view. please set useTextureView={false}',
        );
      }
      if (useSecureView && useTextureView) {
        console.warn(
          'cannot use SecureView on texture view. please set useTextureView={false}',
        );
      }

      return shallForceViewType
        ? useSecureView
          ? ViewType.SURFACE_SECURE
          : ViewType.SURFACE // check if we should force the type to Surface due to DRM
        : viewType
        ? viewType // else use ViewType from source
        : useSecureView // else infer view type from useSecureView and useTextureView
        ? ViewType.SURFACE_SECURE
        : useTextureView
        ? ViewType.TEXTURE
        : ViewType.SURFACE;
    }, [drm, useSecureView, useTextureView, viewType]);

    return (
      <View style={style}>
        <NativeVideoComponent
          ref={nativeRef}
          {...rest}
          src={src}
          drm={_drm}
          style={StyleSheet.absoluteFill}
          resizeMode={resizeMode}
          restoreUserInterfaceForPIPStopCompletionHandler={
            _restoreUserInterfaceForPIPStopCompletionHandler
          }
          textTracks={textTracks}
          selectedTextTrack={_selectedTextTrack}
          selectedAudioTrack={_selectedAudioTrack}
          selectedVideoTrack={_selectedVideoTrack}
          onGetLicense={usingExternalGetLicense ? onGetLicense : undefined}
          onVideoLoad={
            onLoad || hasPoster
              ? (onVideoLoad as (e: NativeSyntheticEvent<object>) => void)
              : undefined
          }
          onVideoLoadStart={
            onLoadStart || hasPoster ? onVideoLoadStart : undefined
          }
          onVideoError={onError ? onVideoError : undefined}
          onVideoProgress={onProgress ? onVideoProgress : undefined}
          onVideoSeek={onSeek ? onVideoSeek : undefined}
          onVideoEnd={onEnd}
          onVideoBuffer={onBuffer ? onVideoBuffer : undefined}
          onVideoPlaybackStateChanged={
            onPlaybackStateChanged ? onVideoPlaybackStateChanged : undefined
          }
          onVideoBandwidthUpdate={
            onBandwidthUpdate ? _onBandwidthUpdate : undefined
          }
          onTimedMetadata={onTimedMetadata ? _onTimedMetadata : undefined}
          onAudioTracks={onAudioTracks ? _onAudioTracks : undefined}
          onTextTracks={onTextTracks ? _onTextTracks : undefined}
          onTextTrackDataChanged={
            onTextTrackDataChanged ? _onTextTrackDataChanged : undefined
          }
          onVideoTracks={onVideoTracks ? _onVideoTracks : undefined}
          onVideoFullscreenPlayerDidDismiss={onFullscreenPlayerDidDismiss}
          onVideoFullscreenPlayerDidPresent={onFullscreenPlayerDidPresent}
          onVideoFullscreenPlayerWillDismiss={onFullscreenPlayerWillDismiss}
          onVideoFullscreenPlayerWillPresent={onFullscreenPlayerWillPresent}
          onVideoExternalPlaybackChange={
            onExternalPlaybackChange ? onVideoExternalPlaybackChange : undefined
          }
          onVideoIdle={onIdle}
          onAudioFocusChanged={
            onAudioFocusChanged ? _onAudioFocusChanged : undefined
          }
          onReadyForDisplay={
            onReadyForDisplay || hasPoster ? _onReadyForDisplay : undefined
          }
          onPlaybackRateChange={
            onPlaybackRateChange ? _onPlaybackRateChange : undefined
          }
          onVolumeChange={onVolumeChange ? _onVolumeChange : undefined}
          onVideoAudioBecomingNoisy={onAudioBecomingNoisy}
          onPictureInPictureStatusChanged={
            onPictureInPictureStatusChanged
              ? _onPictureInPictureStatusChanged
              : undefined
          }
          onRestoreUserInterfaceForPictureInPictureStop={
            onRestoreUserInterfaceForPictureInPictureStop
          }
          onVideoAspectRatio={onAspectRatio ? _onVideoAspectRatio : undefined}
          onReceiveAdEvent={
            onReceiveAdEvent
              ? (_onReceiveAdEvent as (e: NativeSyntheticEvent<object>) => void)
              : undefined
          }
          onControlsVisibilityChange={
            onControlsVisibilityChange ? _onControlsVisibilityChange : undefined
          }
          viewType={_viewType}
        />
        {hasPoster && showPoster ? (
          <Image style={posterStyle} source={{uri: poster}} />
        ) : null}
      </View>
    );
  },
);

Video.displayName = 'Video';
export default Video;
