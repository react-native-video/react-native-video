import React, {
  forwardRef,
  useCallback,
  useEffect,
  useImperativeHandle,
  useRef,
  type RefObject,
} from 'react';
import type {VideoRef, ReactVideoProps, VideoMetadata} from './types';

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
      showNotificationControls = false,
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
        time = Math.max(0, Math.min(time, nativeRef.current.duration));
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
        nativeHtmlRef: nativeRef,
      }),
      [
        seek,
        pause,
        resume,
        unsupported,
        setVolume,
        getCurrentPosition,
        nativeRef,
      ],
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

    useMediaSession(source?.metadata, nativeRef, showNotificationControls);

    return (
      <video
        ref={nativeRef}
        src={source?.uri as string | undefined}
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
          if (!nativeRef.current?.error) {
            return;
          }
          onError?.({
            error: {
              errorString: nativeRef.current.error.message ?? 'Unknown error',
              code: nativeRef.current.error.code,
            },
          });
        }}
        onLoadedMetadata={() => {
          if (source?.startPosition) {
            seek(source.startPosition / 1000);
          }
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
        style={{
          position: 'absolute',
          inset: 0,
          objectFit: 'contain',
          width: '100%',
          height: '100%',
        }}
      />
    );
  },
);

const useMediaSession = (
  metadata: VideoMetadata | undefined,
  nativeRef: RefObject<HTMLVideoElement>,
  showNotification: boolean,
) => {
  const isPlaying = !nativeRef.current?.paused ?? false;
  const progress = nativeRef.current?.currentTime ?? 0;
  const duration = Number.isFinite(nativeRef.current?.duration)
    ? nativeRef.current?.duration
    : undefined;
  const playbackRate = nativeRef.current?.playbackRate ?? 1;

  const enabled = 'mediaSession' in navigator && showNotification;

  useEffect(() => {
    if (enabled) {
      navigator.mediaSession.metadata = new MediaMetadata({
        title: metadata?.title,
        artist: metadata?.artist,
        artwork: metadata?.imageUri ? [{src: metadata.imageUri}] : undefined,
      });
    }
  }, [enabled, metadata]);

  useEffect(() => {
    if (!enabled) {
      return;
    }

    const seekTo = (time: number) => {
      if (nativeRef.current) {
        nativeRef.current.currentTime = time;
      }
    };

    const seekRelative = (offset: number) => {
      if (nativeRef.current) {
        nativeRef.current.currentTime = nativeRef.current.currentTime + offset;
      }
    };

    const mediaActions: [
      MediaSessionAction,
      MediaSessionActionHandler | null,
    ][] = [
      ['play', () => nativeRef.current?.play()],
      ['pause', () => nativeRef.current?.pause()],
      [
        'seekbackward',
        (evt: MediaSessionActionDetails) =>
          seekRelative(evt.seekOffset ? -evt.seekOffset : -10),
      ],
      [
        'seekforward',
        (evt: MediaSessionActionDetails) =>
          seekRelative(evt.seekOffset ? evt.seekOffset : 10),
      ],
      ['seekto', (evt: MediaSessionActionDetails) => seekTo(evt.seekTime!)],
    ];

    for (const [action, handler] of mediaActions) {
      try {
        navigator.mediaSession.setActionHandler(action, handler);
      } catch {
        // ignored
      }
    }
  }, [enabled, nativeRef]);

  useEffect(() => {
    if (enabled) {
      navigator.mediaSession.playbackState = isPlaying ? 'playing' : 'paused';
    }
  }, [isPlaying, enabled]);
  useEffect(() => {
    if (enabled && duration !== undefined) {
      navigator.mediaSession.setPositionState({
        position: Math.min(progress, duration),
        duration,
        playbackRate: playbackRate,
      });
    }
  }, [progress, duration, playbackRate, enabled]);
};

Video.displayName = 'Video';
export default Video;
