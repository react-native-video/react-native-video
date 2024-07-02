#import <AVFoundation/AVFoundation.h>
#import "AVKit/AVKit.h"
//#import "UIView+FindUIViewController.h"
#import "RCTVideoPlayerViewController.h"
#import "RCTVideoPlayerViewControllerDelegate.h"
#import <React/RCTComponent.h>
#import <React/RCTBridgeModule.h>

#if __has_include(<react-native-video/RCTVideoCache.h>)
#import <react-native-video/RCTVideoCache.h>
#import <DVAssetLoaderDelegate/DVURLAsset.h>
#import <DVAssetLoaderDelegate/DVAssetLoaderDelegate.h>
#endif

# pragma mark - video event delegate

@protocol RCTVideoEventDelegate
// TODO follow up all events in RCTVideo.swift
//- (void)onVideoProgressWithCurrentTime:(NSNumber *)currentTime
//       playableDuration:(NSNumber *)playableDuration
//       seekableDuration:(NSNumber *)seekableDuration;
//- (void)onVideoLoadWithCurrentTime:(NSNumber *)currentTime duration:(NSNumber *)duration naturalSize:(NSDictionary *)naturalSize;
//- (void)onVideoLoadStartWithIsNetwork:(BOOL)isNetwork type:(NSString *)type uri:(NSString *)uri;
//- (void)onVideoBufferWithIsBuffering:(BOOL)isBuffering;
//- (void)onVideoErrorWithError:(NSDictionary *)error;
//- (void)onGetLicenseWithLicenseUrl:(NSString *)licenseUrl contentId:(NSString*)contentId spcBase64:(NSString *)spcBase64;
//- (void)onVideoSeekWithCurrentTime:(NSNumber *)currentTime seekTime:(NSNumber *)seekTime finished:(BOOL)finished;
//- (void)onVideoEnd;
//- (void)onTimedMetadata;
//- (void)onVideoAudioBecomingNoisy;
//- (void)onVideoFullscreenPlayerWillPresent;
//- (void)onVideoFullscreenPlayerDidPresent;
//- (void)onVideoFullscreenPlayerWillDismiss;
//- (void)onVideoFullscreenPlayerDidDismiss;
//- (void)onReadyForDisplay;
//- (void)onRestoreUserInterfaceForPictureInPictureStop;
//- (void)onPictureInPictureStatusChangedWithIsActive:(BOOL)isActive;
//- (void)onPlaybackRateChangeWithPlaybackRate:(NSNumber *)playbackRate;
//- (void)onVideoExternalPlaybackChangeWithIsExternalPlaybackActive:(BOOL)isExternalPlaybackActive;
//- (void)onReceiveAdEventWithEvent:(NSString *)event;

@end

#if __has_include(<react-native-video/RCTVideoCache.h>)
@interface RCTVideo : UIView <RCTVideoPlayerViewControllerDelegate, DVAssetLoaderDelegatesDelegate, AVAssetResourceLoaderDelegate>
#elif TARGET_OS_TV
@interface RCTVideo : UIView <RCTVideoPlayerViewControllerDelegate, AVAssetResourceLoaderDelegate>
#else
@interface RCTVideo : UIView <RCTVideoPlayerViewControllerDelegate, AVPictureInPictureControllerDelegate, AVAssetResourceLoaderDelegate>
#endif

@property (nonatomic, weak) id <RCTVideoEventDelegate> _Nullable eventDelegate;

// TODO follow up all errors in RCTVideoErrorHandling.swift
//typedef NS_ENUM(NSInteger, RCTVideoError) {
//    RCTVideoErrorFromJSPart,
//    RCTVideoErrorLicenseRequestNotOk,
//    RCTVideoErrorNoDataFromLicenseRequest,
//    RCTVideoErrorNoSPC,
//    RCTVideoErrorNoDataRequest,
//    RCTVideoErrorNoCertificateData,
//    RCTVideoErrorNoCertificateURL,
//    RCTVideoErrorNoFairplayDRM,
//    RCTVideoErrorNoDRMData
//};

- (instancetype)initWithFrame:(CGRect)frame;

- (AVPlayerViewController*)createPlayerViewController:(AVPlayer*)player withPlayerItem:(AVPlayerItem*)playerItem;

# pragma mark - props
// TODO follow up all setter functions in RCTVideo.swift
//- (void)setSrc:(NSDictionary *)source;
//- (void)setDrm:(NSDictionary *)drm;
//- (void)setResizeMode:(NSString *)resizeMode;
//- (void)setPaused:(BOOL)paused;
//- (void)setAdTagUrl:(NSString *)adTagUrl;
//- (void)setMaxBitRate:(float)maxBitRate;
//- (void)setRepeat:(BOOL)repeat;
//- (void)setAutomaticallyWaitsToMinimizeStalling:(BOOL)waits;
//- (void)setAllowsExternalPlayback:(BOOL)allowsExternalPlayback;
//- (void)setTextTracks:(NSArray *)textTracks;
//- (void)setSelectedTextTrack:(NSDictionary *)selectedTextTrack;
//- (void)setSelectedAudioTrack:(NSDictionary *)selectedAudioTrack;
//- (void)setMuted:(BOOL)muted;
//- (void)setControls:(BOOL)controls;
//- (void)setVolume:(float)volume;
//- (void)setPlayInBackground:(BOOL)playInBackground;
//- (void)setPreventsDisplaySleepDuringVideoPlayback:(BOOL)preventsDisplaySleepDuringVideoPlayback;
//- (void)setPreferredForwardBufferDuration:(float)preferredForwardBufferDuration;
//- (void)setPlayWhenInactive:(BOOL)playWhenInactive;
//- (void)setPictureInPicture:(BOOL)pictureInPicture;
//- (void)setIgnoreSilentSwitch:(NSString *)ignoreSilentSwitch;
//- (void)setMixWithOthers:(NSString *)mixWithOthers;
//- (void)setRate:(float)rate;
//- (void)setFullscreen:(BOOL)fullscreen;
//- (void)setFullscreenAutorotate:(BOOL)autorotate;
//- (void)setFullscreenOrientation:(NSString *)orientation;
//- (void)setFilter:(NSString *)filter;
//- (void)setFilterEnabled:(BOOL)filterEnabled;
//- (void)setProgressUpdateInterval:(float)progressUpdateInterval;
//- (void)setRestoreUserInterfaceForPIPStopCompletionHandler:(BOOL)restore;
//- (void)setLocalSourceEncryptionKeyScheme:(NSString *)keyScheme;


# pragma mark - extern methods
// TODO follow up all ref methods in RCTVideo.swift
//- (void)save:(NSDictionary *)options resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject;
//- (void)setLicenseResult:(NSString * )license;
//- (BOOL)setLicenseResultError:(NSString * )error;
//- (void)seek:(NSDictionary *)info;

@end
