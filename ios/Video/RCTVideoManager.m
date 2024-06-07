#import "React/RCTViewManager.h"
#import <React/RCTBridge.h>

@interface RCT_EXTERN_MODULE (RCTVideoManager, RCTViewManager)

RCT_EXPORT_VIEW_PROPERTY(src, NSDictionary);
RCT_EXPORT_VIEW_PROPERTY(drm, NSDictionary);
RCT_EXPORT_VIEW_PROPERTY(adTagUrl, NSString);
RCT_EXPORT_VIEW_PROPERTY(maxBitRate, float);
RCT_EXPORT_VIEW_PROPERTY(resizeMode, NSString);
RCT_EXPORT_VIEW_PROPERTY(repeat, BOOL);
RCT_EXPORT_VIEW_PROPERTY(automaticallyWaitsToMinimizeStalling, BOOL);
RCT_EXPORT_VIEW_PROPERTY(allowsExternalPlayback, BOOL);
RCT_EXPORT_VIEW_PROPERTY(textTracks, NSArray);
RCT_EXPORT_VIEW_PROPERTY(selectedTextTrack, NSDictionary);
RCT_EXPORT_VIEW_PROPERTY(selectedAudioTrack, NSDictionary);
RCT_EXPORT_VIEW_PROPERTY(chapters, NSArray);
RCT_EXPORT_VIEW_PROPERTY(paused, BOOL);
RCT_EXPORT_VIEW_PROPERTY(muted, BOOL);
RCT_EXPORT_VIEW_PROPERTY(controls, BOOL);
RCT_EXPORT_VIEW_PROPERTY(audioOutput, NSString);
RCT_EXPORT_VIEW_PROPERTY(volume, float);
RCT_EXPORT_VIEW_PROPERTY(playInBackground, BOOL);
RCT_EXPORT_VIEW_PROPERTY(preventsDisplaySleepDuringVideoPlayback, BOOL);
RCT_EXPORT_VIEW_PROPERTY(preferredForwardBufferDuration, float);
RCT_EXPORT_VIEW_PROPERTY(playWhenInactive, BOOL);
RCT_EXPORT_VIEW_PROPERTY(enterPictureInPictureOnLeave, BOOL);
RCT_EXPORT_VIEW_PROPERTY(ignoreSilentSwitch, NSString);
RCT_EXPORT_VIEW_PROPERTY(mixWithOthers, NSString);
RCT_EXPORT_VIEW_PROPERTY(rate, float);
RCT_EXPORT_VIEW_PROPERTY(fullscreen, BOOL);
RCT_EXPORT_VIEW_PROPERTY(fullscreenAutorotate, BOOL);
RCT_EXPORT_VIEW_PROPERTY(fullscreenOrientation, NSString);
RCT_EXPORT_VIEW_PROPERTY(filter, NSString);
RCT_EXPORT_VIEW_PROPERTY(filterEnabled, BOOL);
RCT_EXPORT_VIEW_PROPERTY(progressUpdateInterval, float);
RCT_EXPORT_VIEW_PROPERTY(restoreUserInterfaceForPIPStopCompletionHandler, BOOL);
RCT_EXPORT_VIEW_PROPERTY(localSourceEncryptionKeyScheme, NSString);
RCT_EXPORT_VIEW_PROPERTY(subtitleStyle, NSDictionary);
RCT_EXPORT_VIEW_PROPERTY(showNotificationControls, BOOL)
/* Should support: onLoadStart, onLoad, and onError to stay consistent with Image */
RCT_EXPORT_VIEW_PROPERTY(onVideoLoadStart, RCTDirectEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onVideoLoad, RCTDirectEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onVideoBuffer, RCTDirectEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onVideoError, RCTDirectEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onVideoProgress, RCTDirectEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onVideoBandwidthUpdate, RCTDirectEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onVideoSeek, RCTDirectEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onVideoEnd, RCTDirectEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onTimedMetadata, RCTDirectEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onVideoAudioBecomingNoisy, RCTDirectEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onVideoFullscreenPlayerWillPresent, RCTDirectEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onVideoFullscreenPlayerDidPresent, RCTDirectEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onVideoFullscreenPlayerWillDismiss, RCTDirectEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onVideoFullscreenPlayerDidDismiss, RCTDirectEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onReadyForDisplay, RCTDirectEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onPlaybackStalled, RCTDirectEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onPlaybackResume, RCTDirectEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onPlaybackRateChange, RCTDirectEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onVolumeChange, RCTDirectEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onVideoPlaybackStateChanged, RCTDirectEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onVideoExternalPlaybackChange, RCTDirectEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onGetLicense, RCTDirectEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onPictureInPictureStatusChanged, RCTDirectEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onRestoreUserInterfaceForPictureInPictureStop, RCTDirectEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onReceiveAdEvent, RCTDirectEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onTextTracks, RCTDirectEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onAudioTracks, RCTDirectEventBlock);
RCT_EXPORT_VIEW_PROPERTY(onTextTrackDataChanged, RCTDirectEventBlock);

RCT_EXTERN_METHOD(save
                  : (NSDictionary*)options reactTag
                  : (nonnull NSNumber*)reactTag resolver
                  : (RCTPromiseResolveBlock)resolve rejecter
                  : (RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(seek : (NSDictionary*)info reactTag : (nonnull NSNumber*)reactTag)

RCT_EXTERN_METHOD(setLicenseResult : (NSString*)license licenseUrl : (NSString*)licenseUrl reactTag : (nonnull NSNumber*)reactTag)

RCT_EXTERN_METHOD(setLicenseResultError : (NSString*)error licenseUrl : (NSString*)licenseUrl reactTag : (nonnull NSNumber*)reactTag)

RCT_EXTERN_METHOD(setPlayerPauseState : (nonnull NSNumber*)paused reactTag : (nonnull NSNumber*)reactTag)

RCT_EXTERN_METHOD(presentFullscreenPlayer : (nonnull NSNumber*)reactTag)

RCT_EXTERN_METHOD(dismissFullscreenPlayer : (nonnull NSNumber*)reactTag)

RCT_EXTERN_METHOD(setVolume : (nonnull float*)volume reactTag : (nonnull NSNumber*)reactTag)

RCT_EXTERN_METHOD(getCurrentPosition
                  : (nonnull NSNumber*)reactTag resolver
                  : (RCTPromiseResolveBlock)resolve rejecter
                  : (RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(enterPictureInPicture : (nonnull NSNumber*)reactTag)

RCT_EXTERN_METHOD(exitPictureInPicture : (nonnull NSNumber*)reactTag)
@end
