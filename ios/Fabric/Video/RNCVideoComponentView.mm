#ifdef RCT_NEW_ARCH_ENABLED
#import "RNCVideoComponentView.h"

#import <react/renderer/components/RNCVideo/ComponentDescriptors.h>
#import <react/renderer/components/RNCVideo/EventEmitters.h>
#import <react/renderer/components/RNCVideo/Props.h>
#import <react/renderer/components/RNCVideo/RCTComponentViewHelpers.h>

#import <React/RCTConversions.h>

#import "RCTFabricComponentsPlugins.h"
#import "RCTVideo.h"
//#import "RNCVideoStructComparer.h"
//#import "RNCVideoFabricConversions.h"

using namespace facebook::react;

@interface RNCVideoComponentView () <RCTRNCVideoViewProtocol>

@end

@implementation RNCVideoComponentView {
    RCTVideo * _view;
}

+ (ComponentDescriptorProvider)componentDescriptorProvider
{
    return concreteComponentDescriptorProvider<RNCVideoComponentDescriptor>();
}

# pragma mark - initWithFrame

- (instancetype)initWithFrame:(CGRect)frame
{
  if (self = [super initWithFrame:frame]) {
    static const auto defaultProps = std::make_shared<const RNCVideoProps>();
    _props = defaultProps;

    _view = [[RCTVideo alloc] initWithFrame: frame];
    _view.eventDelegate = self;

    self.contentView = _view;
  }

  return self;
}

# pragma mark - Life cycle

- (void)prepareForRecycle {
    [super prepareForRecycle];
    static const auto defaultProps = std::make_shared<const RNCVideoProps>();
    _props = defaultProps;

    _view = [[RCTVideo alloc] initWithFrame: self.bounds];
    _view.eventDelegate = self;

    self.contentView = _view;
}

# pragma mark - updateProps

- (void)updateProps:(Props::Shared const &)props oldProps:(Props::Shared const &)oldProps
{
    const auto &oldViewProps = *std::static_pointer_cast<RNCVideoProps const>(_props);
    const auto &newViewProps = *std::static_pointer_cast<RNCVideoProps const>(props);

//    if (!isSrcStructEqual(oldViewProps.src, newViewProps.src)) {
//        NSDictionary *source = srcDictFromCppStruct(newViewProps.src);
//        [_view setSrc:source];
//    }
//
//    if (!isDrmStructEqual(oldViewProps.drm, newViewProps.drm)) {
//        NSDictionary *drm = drmDictFromCppStruct(newViewProps.drm);
//        [_view setDrm:drm];
//    }
//
//    if (oldViewProps.resizeMode != newViewProps.resizeMode) {
//        [_view setResizeMode:RCTNSStringFromStringNilIfEmpty(toString(newViewProps.resizeMode))];
//    }
//
//    if (oldViewProps.paused != newViewProps.paused) {
//        [_view setPaused:newViewProps.paused];
//    }
//
//    if (oldViewProps.adTagUrl != newViewProps.adTagUrl) {
//        [_view setAdTagUrl:RCTNSStringFromStringNilIfEmpty(newViewProps.adTagUrl)];
//    }
//
//    if (oldViewProps.maxBitRate != newViewProps.maxBitRate) {
//        [_view setMaxBitRate:newViewProps.maxBitRate];
//    }
//
//    if (oldViewProps.repeat != newViewProps.repeat) {
//        [_view setRepeat:newViewProps.repeat];
//    }
//
//    if (oldViewProps.automaticallyWaitsToMinimizeStalling != newViewProps.automaticallyWaitsToMinimizeStalling) {
//        [_view setAutomaticallyWaitsToMinimizeStalling:newViewProps.automaticallyWaitsToMinimizeStalling];
//    }
//
//    if (oldViewProps.allowsExternalPlayback != newViewProps.allowsExternalPlayback) {
//        [_view setAllowsExternalPlayback:newViewProps.allowsExternalPlayback];
//    }
//
//    if (!isTextTracksVectorEqual(oldViewProps.textTracks, newViewProps.textTracks)) {
//        [_view setTextTracks:textTracksArrayFromCppVector(newViewProps.textTracks)];
//    }
//
//    if (!isSelectedTextTrackStructEqual(oldViewProps.selectedTextTrack, newViewProps.selectedTextTrack)) {
//        [_view setSelectedTextTrack:selectedTextTrackDictFromCppStruct(newViewProps.selectedTextTrack)];
//    }
//
//    if (!isSelectedAudioTrackStructEqual(oldViewProps.selectedAudioTrack, newViewProps.selectedAudioTrack)) {
//        [_view setSelectedAudioTrack:selectedAudioTrackDictFromCppStruct(newViewProps.selectedAudioTrack)];
//    }
//
//    if (oldViewProps.muted != newViewProps.muted) {
//        [_view setMuted:newViewProps.muted];
//    }
//
//    if (oldViewProps.controls != newViewProps.controls) {
//        [_view setControls:newViewProps.controls];
//    }
//
//    if (oldViewProps.volume != newViewProps.volume) {
//        [_view setVolume:newViewProps.volume];
//    }
//
//    if (oldViewProps.playInBackground != newViewProps.playInBackground) {
//        [_view setPlayInBackground:newViewProps.playInBackground];
//    }
//
//    if (oldViewProps.preventsDisplaySleepDuringVideoPlayback != newViewProps.preventsDisplaySleepDuringVideoPlayback) {
//        [_view setPreventsDisplaySleepDuringVideoPlayback:newViewProps.preventsDisplaySleepDuringVideoPlayback];
//    }
//
//    if (oldViewProps.preferredForwardBufferDuration != newViewProps.preferredForwardBufferDuration) {
//        [_view setPreferredForwardBufferDuration:newViewProps.preferredForwardBufferDuration];
//    }
//
//    if (oldViewProps.playWhenInactive != newViewProps.playWhenInactive) {
//        [_view setPlayWhenInactive:newViewProps.playWhenInactive];
//    }
//
//    if (oldViewProps.pictureInPicture != newViewProps.pictureInPicture) {
//        [_view setPictureInPicture:newViewProps.pictureInPicture];
//    }
//
//    if (oldViewProps.ignoreSilentSwitch != newViewProps.ignoreSilentSwitch) {
//        [_view setIgnoreSilentSwitch:RCTNSStringFromStringNilIfEmpty(toString(newViewProps.ignoreSilentSwitch))];
//    }
//
//    if (oldViewProps.mixWithOthers != newViewProps.mixWithOthers) {
//        [_view setMixWithOthers:RCTNSStringFromStringNilIfEmpty(toString(newViewProps.mixWithOthers))];
//    }
//
//    if (oldViewProps.rate != newViewProps.rate) {
//        [_view setRate:newViewProps.rate];
//    }
//
//    if (oldViewProps.fullscreen != newViewProps.fullscreen) {
//        [_view setFullscreen:newViewProps.fullscreen];
//    }
//
//    if (oldViewProps.fullscreenAutorotate != newViewProps.fullscreenAutorotate) {
//        [_view setFullscreenAutorotate:newViewProps.fullscreenAutorotate];
//    }
//
//    if (oldViewProps.fullscreenOrientation != newViewProps.fullscreenOrientation) {
//        [_view setFullscreenOrientation:RCTNSStringFromStringNilIfEmpty(toString(newViewProps.fullscreenOrientation))];
//    }
//
//    if (oldViewProps.filter != newViewProps.filter) {
//        NSString *filter = RCTNSStringFromStringNilIfEmpty(toString(newViewProps.filter));
//        if (filter != nil && ![filter isEqualToString:@"None"]) {
//            [_view setFilter:filter];
//        }
//    }
//
//    if (oldViewProps.filterEnabled != newViewProps.filterEnabled) {
//        [_view setFilterEnabled:newViewProps.filterEnabled];
//    }
//
//    if (oldViewProps.progressUpdateInterval != newViewProps.progressUpdateInterval) {
//        [_view setProgressUpdateInterval:newViewProps.progressUpdateInterval];
//    }
//
//    if (oldViewProps.restoreUserInterfaceForPIPStopCompletionHandler != newViewProps.restoreUserInterfaceForPIPStopCompletionHandler) {
//        [_view setRestoreUserInterfaceForPIPStopCompletionHandler:newViewProps.restoreUserInterfaceForPIPStopCompletionHandler];
//    }
//
//    if (oldViewProps.localSourceEncryptionKeyScheme != newViewProps.localSourceEncryptionKeyScheme) {
//        [_view setLocalSourceEncryptionKeyScheme:RCTNSStringFromStringNilIfEmpty(newViewProps.localSourceEncryptionKeyScheme)];
//    }

    [super updateProps:props oldProps:oldProps];
}

# pragma mark - methods

//- (void)handleCommand:(const NSString *)commandName args:(const NSArray *)args {
//    RCTRNCVideoHandleCommand(self, commandName, args);
//}

//- (void)seek:(float)time tolerance:(float)tolerance {
//    NSMutableDictionary *info = @{
//        @"time": @(time),
//        @"tolerance": @(tolerance),
//    };
//
//    [_view seek:info];
//}
//
//- (void)setLicenseResult:(NSString *)result {
//    [_view setLicenseResult:result];
//}
//
//- (void)setLicenseResultError:(NSString *)error {
//    [_view setLicenseResultError:error];
//}

# pragma mark - event

//- (void)onVideoProgressWithCurrentTime:(NSNumber *)currentTime playableDuration:(NSNumber *)playableDuration seekableDuration:(NSNumber *)seekableDuration
//{
//    if(!_eventEmitter) {
//        return;
//    }
//
//    RNCVideoEventEmitter::OnVideoProgress event = {
//        .currentTime = [currentTime floatValue],
//        .playableDuration = [playableDuration floatValue],
//        .seekableDuration = [seekableDuration floatValue]
//    };
//
//    std::dynamic_pointer_cast<const RNCVideoEventEmitter>(_eventEmitter)->onVideoProgress(event);
//}
//
//- (void)onVideoLoadWithCurrentTime:(NSNumber *)currentTime duration:(NSNumber *)duration naturalSize:(NSDictionary *)naturalSize {
//
//    if(!_eventEmitter) {
//        return;
//    }
//
//    NSNumber *width = naturalSize[@"width"] ?: @(0.0);
//    NSNumber *height = naturalSize[@"height"] ?: @(0.0);
//
//    RNCVideoEventEmitter::OnVideoLoadNaturalSize naturalSizeValue = {
//        .width = [width floatValue],
//        .height = [height floatValue],
//        .orientation = RCTStringFromNSString(naturalSize[@"orientation"])
//    };
//
//    RNCVideoEventEmitter::OnVideoLoad event = {
//        .currentTime = [currentTime floatValue],
//        .duration = [duration floatValue],
//        .naturalSize = naturalSizeValue
//    };
//
//    std::dynamic_pointer_cast<const RNCVideoEventEmitter>(_eventEmitter)->onVideoLoad(event);
//}
//
//- (void)onVideoLoadStartWithIsNetwork:(BOOL)isNetwork type:(NSString *)type uri:(NSString *)uri {
//
//    if(!_eventEmitter) {
//        return;
//    }
//
//    RNCVideoEventEmitter::OnVideoLoadStart event = {
//        .isNetwork = isNetwork,
//        .type = RCTStringFromNSString(type),
//        .uri = RCTStringFromNSString(uri)
//    };
//
//    std::dynamic_pointer_cast<const RNCVideoEventEmitter>(_eventEmitter)->onVideoLoadStart(event);
//}
//
//- (void)onVideoBufferWithIsBuffering:(BOOL)isBuffering {
//    if(!_eventEmitter) {
//        return;
//    }
//    RNCVideoEventEmitter::OnVideoBuffer event = {
//        .isBuffering = isBuffering
//    };
//
//    std::dynamic_pointer_cast<const RNCVideoEventEmitter>(_eventEmitter)->onVideoBuffer(event);
//}
//
//- (void)onVideoErrorWithError:(NSDictionary *)error {
//    if(!_eventEmitter) {
//        return;
//    }
//    NSData *errorJson = [NSJSONSerialization dataWithJSONObject:error options:NSJSONWritingPrettyPrinted error:nil];
//    NSString *jsonString = [[NSString alloc] initWithData:errorJson encoding:NSUTF8StringEncoding];
//
//    RNCVideoEventEmitter::OnVideoError event = {
//        .error = RCTStringFromNSString(jsonString),
//    };
//
//    std::dynamic_pointer_cast<const RNCVideoEventEmitter>(_eventEmitter)->onVideoError(event);
//}
//
//- (void)onGetLicenseWithLicenseUrl:(NSString *)licenseUrl contentId:(NSString*)contentId spcBase64:(NSString *)spcBase64 {
//    if(!_eventEmitter) {
//        return;
//    }
//
//    RNCVideoEventEmitter::OnGetLicense event = {
//        .licenseUrl = RCTStringFromNSString(licenseUrl),
//        .contentId = RCTStringFromNSString(contentId),
//        .spcBase64 = RCTStringFromNSString(spcBase64),
//    };
//
//    std::dynamic_pointer_cast<const RNCVideoEventEmitter>(_eventEmitter)->onGetLicense(event);
//}
//
//- (void)onVideoSeekWithCurrentTime:(NSNumber *)currentTime seekTime:(NSNumber *)seekTime finished:(BOOL)finished {
//    if(!_eventEmitter) {
//        return;
//    }
//
//    RNCVideoEventEmitter::OnVideoSeek event = {
//        .currentTime = [currentTime floatValue],
//        .seekTime = [seekTime floatValue],
//        .finished = finished
//    };
//
//    std::dynamic_pointer_cast<const RNCVideoEventEmitter>(_eventEmitter)->onVideoSeek(event);
//}
//
//- (void)onVideoEnd {
//    if(!_eventEmitter) {
//        return;
//    }
//
//    RNCVideoEventEmitter::OnVideoEnd event = {};
//
//    std::dynamic_pointer_cast<const RNCVideoEventEmitter>(_eventEmitter)->onVideoEnd(event);
//}
//
//- (void)onTimedMetadata {
//    if(!_eventEmitter) {
//        return;
//    }
//
//    RNCVideoEventEmitter::OnTimedMetadata event = {};
//
//    std::dynamic_pointer_cast<const RNCVideoEventEmitter>(_eventEmitter)->onTimedMetadata(event);
//}
//
//- (void)onVideoAudioBecomingNoisy {
//    if(!_eventEmitter) {
//        return;
//    }
//
//    RNCVideoEventEmitter::OnVideoAudioBecomingNoisy event = {};
//
//    std::dynamic_pointer_cast<const RNCVideoEventEmitter>(_eventEmitter)->onVideoAudioBecomingNoisy(event);
//}
//
//- (void)onVideoFullscreenPlayerWillPresent {
//    if(!_eventEmitter) {
//        return;
//    }
//
//    RNCVideoEventEmitter::OnVideoFullscreenPlayerWillPresent event = {};
//
//    std::dynamic_pointer_cast<const RNCVideoEventEmitter>(_eventEmitter)->onVideoFullscreenPlayerWillPresent(event);
//}
//
//- (void)onVideoFullscreenPlayerDidPresent {
//    if(!_eventEmitter) {
//        return;
//    }
//    RNCVideoEventEmitter::OnVideoFullscreenPlayerDidPresent event = {};
//
//    std::dynamic_pointer_cast<const RNCVideoEventEmitter>(_eventEmitter)->onVideoFullscreenPlayerDidPresent(event);
//}
//
//- (void)onVideoFullscreenPlayerWillDismiss {
//    if(!_eventEmitter) {
//        return;
//    }
//    RNCVideoEventEmitter::OnVideoFullscreenPlayerWillDismiss event = {};
//
//    std::dynamic_pointer_cast<const RNCVideoEventEmitter>(_eventEmitter)->onVideoFullscreenPlayerWillDismiss(event);
//}
//
//- (void)onVideoFullscreenPlayerDidDismiss {
//    if(!_eventEmitter) {
//        return;
//    }
//    RNCVideoEventEmitter::OnVideoFullscreenPlayerDidDismiss event = {};
//
//    std::dynamic_pointer_cast<const RNCVideoEventEmitter>(_eventEmitter)->onVideoFullscreenPlayerDidDismiss(event);
//}
//
//- (void)onReadyForDisplay {
//    if(!_eventEmitter) {
//        return;
//    }
//    RNCVideoEventEmitter::OnReadyForDisplay event = {};
//
//    std::dynamic_pointer_cast<const RNCVideoEventEmitter>(_eventEmitter)->onReadyForDisplay(event);
//}
//
//- (void)onRestoreUserInterfaceForPictureInPictureStop {
//    if(!_eventEmitter) {
//        return;
//    }
//    RNCVideoEventEmitter::OnRestoreUserInterfaceForPictureInPictureStop event = {};
//
//    std::dynamic_pointer_cast<const RNCVideoEventEmitter>(_eventEmitter)->onRestoreUserInterfaceForPictureInPictureStop(event);
//}
//
//- (void)onPictureInPictureStatusChangedWithIsActive:(BOOL)isActive {
//    if(!_eventEmitter) {
//        return;
//    }
//    RNCVideoEventEmitter::OnPictureInPictureStatusChanged event = {
//        .isActive = isActive
//    };
//
//    std::dynamic_pointer_cast<const RNCVideoEventEmitter>(_eventEmitter)->onPictureInPictureStatusChanged(event);
//}
//
//- (void)onPlaybackRateChangeWithPlaybackRate:(NSNumber *)playbackRate {
//    if(!_eventEmitter) {
//        return;
//    }
//    RNCVideoEventEmitter::OnPlaybackRateChange event = {
//        .playbackRate = [playbackRate floatValue]
//    };
//
//    std::dynamic_pointer_cast<const RNCVideoEventEmitter>(_eventEmitter)->onPlaybackRateChange(event);
//}
//
//- (void)onVideoExternalPlaybackChangeWithIsExternalPlaybackActive:(BOOL)isExternalPlaybackActive {
//    if(_eventEmitter) {
//        return;
//    }
//
//    RNCVideoEventEmitter::OnVideoExternalPlaybackChange event = {
//        .isExternalPlaybackActive = isExternalPlaybackActive
//    };
//
//    std::dynamic_pointer_cast<const RNCVideoEventEmitter>(_eventEmitter)->onVideoExternalPlaybackChange(event);
//
//}
//
//- (void)onReceiveAdEventWithEvent:(NSString *)event {
//    if(_eventEmitter) {
//        return;
//    }
//    RNCVideoEventEmitter::OnReceiveAdEvent eventParam = {
//        .event = RCTStringFromNSString(event)
//    };
//
//    std::dynamic_pointer_cast<const RNCVideoEventEmitter>(_eventEmitter)->onReceiveAdEvent(eventParam);
//}

# pragma mark - RNCVideoCls

Class<RCTComponentViewProtocol> RNCVideoCls(void)
{
    return RNCVideoComponentView.class;
}

@end
#endif
