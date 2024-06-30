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
      onBuffer,
      onLoad,
      onProgress,
      onError,
      onEnd,
      onPlaybackStateChanged,
    },
    ref,
  ) => {
    const nativeRef = useRef<HTMLVideoElement>(null);
    const errorHandler = useRef<typeof onError>(onError);
    errorHandler.current = onError;

    const seek = useCallback(async (time: number, _tolerance?: number) => {
      if (isNaN(time)) {
        throw new Error('Specified time is not a number');
      }
      if (!nativeRef.current) {
        console.warn('Video Component is not mounted');
        return;
      }
      nativeRef.current.currentTime = time;
    }, []);

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

    const unsupported = useCallback(() => {
      throw new Error('This is unsupported on the web');
    }, []);

    useImperativeHandle(
      ref,
      () => ({
        seek,
        pause,
        resume,
        // making the video fullscreen does not work with some subtitles polyfils
        // so I decided to not include it.
        presentFullscreenPlayer: unsupported,
        dismissFullscreenPlayer: unsupported,
        save: unsupported,
        restoreUserInterfaceForPictureInPictureStopCompleted: unsupported,
      }),
      [seek, pause, resume, unsupported],
    );

    useEffect(() => {
      if (paused) {
        pause();
      } else {
        resume();
      }
    }, [paused, pause, resume]);
    useEffect(() => {
      if (!nativeRef.current || !volume) {
        return;
      }
      nativeRef.current.volume = Math.max(0, Math.min(volume, 100)) / 100;
    }, [volume]);

    const setPlay = useSetAtom(playAtom);
    useEffect(() => {
      if (!nativeRef.current) return;
      // Set play state to the player's value (if autoplay is denied)
      setPlay(!nativeRef.current.paused);
    }, [setPlay]);

    const setProgress = useSetAtom(progressAtom);

    return (
      <>
        <MediaSessionManager {...source.metadata} />
        <video
          ref={nativeRef}
          src={source.uri as string | undefined}
          muted={muted}
          autoPlay={!paused}
          controls={false}
          playsInline
          onCanPlay={() => onBuffer?.({isBuffering: false})}
          onWaiting={() => onBuffer?.({isBuffering: true})}
          onDurationChange={() => {
            if (!nativeRef.current) {
              return;
            }
            onLoad?.({duration: nativeRef.current.duration} as any);
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
            if (source.startPosition) setProgress(source.startPosition / 1000);
          }}
          onPlay={() => onPlaybackStateChanged?.({isPlaying: true})}
          onPause={() => onPlaybackStateChanged?.({isPlaying: false})}
          onEnded={onEnd}
          style={{position: 'absolute', inset: 0, objectFit: 'contain'}}
        />
      </>
    );
  },
);

Video.displayName = 'Video';
export default Video;
