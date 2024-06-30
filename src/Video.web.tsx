import React, {
  forwardRef,
  useCallback,
  useEffect,
  useImperativeHandle,
  useRef,
} from 'react';
import type {VideoRef, ReactVideoProps} from './types';

const Video = forwardRef<VideoRef, ReactVideoProps>(
  (
    {
      source,
      paused,
      muted,
      volume,
      rate,
      repeat,
      controls,
      showNotificationControls,
      poster,
      onBuffer,
      onLoad,
      onProgress,
      onPlaybackRateChange,
      onError,
      onReadyForDisplay,
      onSeek,
      onVolumeChange,
      onEnd,
      onPlaybackStateChanged,
    },
    ref,
  ) => {
    const nativeRef = useRef<HTMLVideoElement>(null);
    const errorHandler = useRef<typeof onError>(onError);
    errorHandler.current = onError;

    const seek = useCallback(
      async (time: number, _tolerance?: number) => {
        if (isNaN(time)) {
          throw new Error('Specified time is not a number');
        }
        if (!nativeRef.current) {
          console.warn('Video Component is not mounted');
          return;
        }
        nativeRef.current.currentTime = time;
        onSeek?.({seekTime: time, currentTime: nativeRef.current.currentTime});
      },
      [onSeek],
    );

    const pause = useCallback(() => {
      if (!nativeRef.current) {
        return;
      }
      nativeRef.current.pause();
    }, []);

    const resume = useCallback(() => {
      if (!nativeRef.current) {
        return;
      }
      nativeRef.current.play();
    }, []);

    const setVolume = useCallback((vol: number) => {
      if (!nativeRef.current) {
        return;
      }
      nativeRef.current.volume = Math.max(0, Math.min(vol, 100)) / 100;
    }, []);

    const getCurrentPosition = useCallback(async () => {
      if (!nativeRef.current) {
        throw new Error('Video Component is not mounted');
      }
      return nativeRef.current.currentTime;
    }, []);

    const unsupported = useCallback(() => {
      throw new Error('This is unsupported on the web');
    }, []);

    useImperativeHandle(
      ref,
      () => ({
        seek,
        pause,
        resume,
        setVolume,
        getCurrentPosition,
        // making the video fullscreen does not work with some subtitles polyfils
        // so I decided to not include it.
        presentFullscreenPlayer: unsupported,
        dismissFullscreenPlayer: unsupported,
        setFullScreen: unsupported,
        save: unsupported,
        restoreUserInterfaceForPictureInPictureStopCompleted: unsupported,
      }),
      [seek, pause, resume, unsupported, setVolume, getCurrentPosition],
    );

    useEffect(() => {
      if (paused) {
        pause();
      } else {
        resume();
      }
    }, [paused, pause, resume]);
    useEffect(() => {
      if (volume === undefined) {
        return;
      }
      setVolume(volume);
    }, [volume, setVolume]);

    useEffect(() => {
      // Not sure about how to do this but we want to wait for nativeRef to be initialized
      setTimeout(() => {
        if (!nativeRef.current) {
          return;
        }

        // Set play state to the player's value (if autoplay is denied)
        // This is useful if our UI is in a play state but autoplay got denied so
        // the video is actaully in a paused state.
        onPlaybackStateChanged?.({isPlaying: !nativeRef.current.paused});
      }, 500);
    }, [onPlaybackStateChanged]);

    useEffect(() => {
      if (!nativeRef.current || rate === undefined) {
        return;
      }
      nativeRef.current.playbackRate = rate;
    }, [rate]);

    return (
      <>
        {showNotificationControls && (
          <MediaSessionManager {...source.metadata} />
        )}
        <video
          ref={nativeRef}
          src={source.uri as string | undefined}
          muted={muted}
          autoPlay={!paused}
          controls={controls}
          loop={repeat}
          playsInline
          poster={poster}
          onCanPlay={() => onBuffer?.({isBuffering: false})}
          onWaiting={() => onBuffer?.({isBuffering: true})}
          onRateChange={() => {
            if (!nativeRef.current) {
              return;
            }
            onPlaybackRateChange?.({
              playbackRate: nativeRef.current?.playbackRate,
            });
          }}
          onDurationChange={() => {
            if (!nativeRef.current) {
              return;
            }
            onLoad?.({
              currentTime: nativeRef.current.currentTime,
              duration: nativeRef.current.duration,
              videoTracks: [],
              textTracks: [],
              audioTracks: [],
              naturalSize: {
                width: nativeRef.current.videoWidth,
                height: nativeRef.current.videoHeight,
                orientation: 'landscape',
              },
            });
          }}
          onTimeUpdate={() => {
            if (!nativeRef.current) {
              return;
            }
            onProgress?.({
              currentTime: nativeRef.current.currentTime,
              playableDuration: nativeRef.current.buffered.length
                ? nativeRef.current.buffered.end(
                    nativeRef.current.buffered.length - 1,
                  )
                : 0,
              seekableDuration: 0,
            });
          }}
          onLoadedData={() => onReadyForDisplay?.()}
          onError={() => {
            if (
              nativeRef?.current?.error?.code ===
              MediaError.MEDIA_ERR_SRC_NOT_SUPPORTED
            )
              onMediaUnsupported?.call(undefined);
            else {
              onError?.call(null, {
                error: {
                  errorString:
                    nativeRef.current?.error?.message ?? 'Unknown error',
                },
              });
            }
          }}
          onLoadedMetadata={() => {
            if (source.startPosition) seek(source.startPosition / 1000);
          }}
          onPlay={() => onPlaybackStateChanged?.({isPlaying: true})}
          onPause={() => onPlaybackStateChanged?.({isPlaying: false})}
          onVolumeChange={() => {
            if (!nativeRef.current) {
              return;
            }
            onVolumeChange?.({volume: nativeRef.current.volume});
          }}
          onEnded={onEnd}
          style={{position: 'absolute', inset: 0, objectFit: 'contain'}}
        />
      </>
    );
  },
);

Video.displayName = 'Video';
export default Video;
