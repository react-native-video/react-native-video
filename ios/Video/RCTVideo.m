#import <React/RCTConvert.h>
#import "RCTVideo.h"
#import <React/RCTBridgeModule.h>
#import <React/RCTEventDispatcher.h>
#import <React/UIView+React.h>
#include <MediaAccessibility/MediaAccessibility.h>
#include <AVFoundation/AVFoundation.h>
#include "DiceUtils.h"
#include "DiceBeaconRequest.h"
#include "DiceHTTPRequester.h"

#import <AppTrackingTransparency/AppTrackingTransparency.h>
#import <AdSupport/AdSupport.h>

#import <ReactVideoSubtitleSideloader_tvOS/ReactVideoSubtitleSideloader_tvOS-Swift.h>
#import <dice_shield_ios/dice_shield_ios-Swift.h>
#import <react_native_video-Swift.h>

@import AVDoris;

static NSString *const playerVersion = @"react-native-video/3.3.1";

@implementation RCTVideo {
    NSNumber* _Nullable _startPlayingAt;
    NSNumber* _Nullable _itemDuration;
    NSNumber* _Nullable _appId;
    UIImageView* _waterMarkImageView;
    
    bool _controls;
    NSDictionary* _Nullable _source;
    NSDictionary* _Nullable _theme;
    NSDictionary* _Nullable _translations;
    NSDictionary* _Nullable _relatedVideos;
    NSString* _playerName;
    
    SubtitleResourceLoaderDelegate* _delegate;
    dispatch_queue_t delegateQueue;

    ActionToken * _actionToken;
    DiceBeaconRequest * _diceBeaconRequst;
    BOOL _diceBeaconRequestOngoing;
}

- (instancetype)initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher {
    if ((self = [super init])) {
        _diceBeaconRequestOngoing = NO;
        _controls = YES;
        _playerName = @"DicePlayer";
        
        _waterMarkImageView = [UIImageView new];
        _waterMarkImageView.contentMode = UIViewContentModeScaleAspectFit;
        _waterMarkImageView.alpha = 0.75;
        _waterMarkImageView.translatesAutoresizingMaskIntoConstraints = false;
        _waterMarkImageView.clipsToBounds = true;
    }
    
    return self;
}

- (void)didMoveToWindow {
    [super didMoveToWindow];
    
    DorisUIStyle* _Nullable style;
    if (_theme) {
        style = [DorisUIStyle createFrom:_theme];
    }
    
    DorisUITranslations* _Nullable translations;
    if (_translations) {
        translations = [DorisUITranslations createFrom:_translations];
    }
    
    self.player = [AVPlayer new];
    self.dorisUI = [DorisUIModuleFactory createCustomUIWithPlayer:self.player
                                                            style:style
                                                     translations:translations
                                                           output:self];
    [self addSubview:self.dorisUI.view];
    [self.dorisUI fillSuperView];
}

#pragma mark - Prop setters

- (void)setResizeMode:(NSString*)mode {}
- (void)setPlayInBackground:(BOOL)playInBackground {}
- (void)setAllowsExternalPlayback:(BOOL)allowsExternalPlayback {}
- (void)setPlayWhenInactive:(BOOL)playWhenInactive {}
- (void)setIgnoreSilentSwitch:(NSString *)ignoreSilentSwitch {}
- (void)setRate:(float)rate {}
- (void)setVolume:(float)volume {}
- (void)setRepeat:(BOOL)repeat {}
- (void)setTextTracks:(NSArray*) textTracks {}
- (void)setFullscreen:(BOOL)fullscreen {}
- (void)setSelectedAudioTrack:(NSDictionary *)selectedAudioTrack {}
- (void)setProgressUpdateInterval:(float)progressUpdateInterval {}

- (void)setPaused:(BOOL)paused {
    [_dorisUI.input setPausedWithIsPaused:paused];
}

- (void)setMuted:(BOOL)muted {
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t) 0), dispatch_get_main_queue(), ^{
        dispatch_async(dispatch_get_main_queue(), ^{
            [self.dorisUI.input setMuteWithIsMuted:muted];
        });
    });
}

- (void)setRelatedVideos:(NSDictionary*)relatedVideos {
    _relatedVideos = relatedVideos;
}

- (void)setButtons:(NSDictionary*)buttons {
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t) 0), dispatch_get_main_queue(), ^{
        DorisUIButtonsConfiguration* _Nullable configuration = [DorisUIButtonsConfiguration createFrom:buttons];
        
        if (configuration) {
            dispatch_async(dispatch_get_main_queue(), ^{
                [self.dorisUI.input setUIButtonsConfiguration:configuration];
            });
        }
    });
}

- (void)setIsFavourite:(BOOL)isFavourite {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.dorisUI.input setIsFavourite:isFavourite];
    });
}

- (void)setTheme:(NSDictionary *)theme {    
    _theme = theme;
}

- (void)setTranslations:(NSDictionary *)translations {
    _translations = translations;
}

- (void)setMetadata:(NSDictionary *)metadata {
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t) 0), dispatch_get_main_queue(), ^{
        DorisUIMetadataConfiguration* _Nullable configuration = [DorisUIMetadataConfiguration createFrom:metadata];
        
        if (configuration) {
            dispatch_async(dispatch_get_main_queue(), ^{
                [self.dorisUI.input setUIMetadataConfiguration:configuration];
            });
        }
    });
}

- (void)setSrc:(NSDictionary *)source {
    if ([[source valueForKey:@"uri"] isEqualToString:[_source valueForKey:@"uri"]]) {
        return;
    }
    
    _source = source;
    
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t) 0), dispatch_get_main_queue(), ^{
        // perform on next run loop, otherwise other passed react-props may not be set

        [self updateRelatedVideos];
        
        [self playerItemForSource:source withCallback:^(AVPlayerItem * playerItem) {
            NSDictionary* __nullable limitedSeekableRange = [source objectForKey:@"limitedSeekableRange"];
            [self limitSeekableRanges:limitedSeekableRange];

            id imaObject = [source objectForKey:@"ima"];
            
            if ([imaObject isKindOfClass:NSDictionary.class]) {
                NSDictionary* __nullable drmSource = [source objectForKey:@"drm"];
                [self setupPlaybackWithAds:imaObject drmDict:drmSource playerItem:playerItem];
            } else {
                PlayerItemSource *source = [[PlayerItemSource alloc] initWithPlayerItem:playerItem];
                dispatch_async(dispatch_get_main_queue(), ^{
                    [self.dorisUI.input loadWithPlayerItemSource:source startPlayingAt:self->_startPlayingAt];
                });
            }
            
            if (self.onVideoLoadStart) {
                id uri = [source objectForKey:@"uri"];
                id type = [source objectForKey:@"type"];
                self.onVideoLoadStart(@{@"src": @{
                                                  @"uri": uri ? uri : [NSNull null],
                                                  @"type": type ? type : [NSNull null],
                                                  @"isNetwork": [NSNumber numberWithBool:(bool)[source objectForKey:@"isNetwork"]]},
                                          @"target": self.reactTag
                                        });
            }
        }];
    });
}

- (void)setSeek:(NSDictionary *)info {
    NSNumber *seekTime = info[@"time"];
    _startPlayingAt = seekTime;
}

- (void)setControls:(BOOL)controls {
    if (controls != _controls) {
        _controls = controls;
        if (controls) {
            [self.dorisUI.input enableUI];
        } else {
            [self.dorisUI.input disableUI];
        }
    }
}

- (void)updateRelatedVideos {
    id headIndex = [_relatedVideos objectForKey:@"headIndex"];
    
    id _items = [_relatedVideos objectForKey:@"items"];
    if ([_items isKindOfClass:NSArray.class] &&
        [headIndex isKindOfClass:NSNumber.class]) {
        int _headIndex = [headIndex intValue];
        NSMutableArray* relatedVideos = [NSMutableArray new];
        NSArray* items = _items;
        int count = 0;
        for (id object in items) {
            if ([object isKindOfClass:NSDictionary.class] &&
                count <= _headIndex + 3 &&
                count >= _headIndex) {
                
                [relatedVideos addObject: [DorisRelatedVideo createFrom:object]];
            }
            count++;
        }
        
        [self.dorisUI.input setRelatedVideos: relatedVideos];
    }
}

- (void) setupPlaybackWithAds:(NSDictionary *)imaDict drmDict:(NSDictionary  * _Nullable)drmDict playerItem:(AVPlayerItem *)playerItem {
    NSString* __nullable assetKey = [imaDict objectForKey:@"assetKey"];
    NSString* __nullable contentSourceId = [imaDict objectForKey:@"contentSourceId"];
    NSString* __nullable videoId = [imaDict objectForKey:@"videoId"];
    NSString* __nullable authToken = [imaDict objectForKey:@"authToken"];
    NSDictionary* __nullable adTagParameters = [imaDict objectForKey:@"adTagParameters"];
    NSDate *_Nullable validFrom;
    NSDate *_Nullable validUntil;
    
    id _validFrom = [imaDict objectForKey:@"startDate"];
    id _validUntil = [imaDict objectForKey:@"endDate"];
    
    if (_validFrom &&
        [_validFrom isKindOfClass:NSNumber.class]) {
        validFrom = [[NSDate alloc] initWithTimeIntervalSince1970:[_validFrom doubleValue]];
    }
    
    if (_validUntil &&
        [_validUntil isKindOfClass:NSNumber.class]) {
        validUntil = [[NSDate alloc] initWithTimeIntervalSince1970:[_validUntil doubleValue]];
    }
    
    [self prepareAdTagParameters:adTagParameters withCallback:^(NSDictionary * _Nullable newAdTagParamerters) {
        DAISource* source = [[DAISource alloc] initWithAssetKey:assetKey
                                                contentSourceId:contentSourceId
                                                        videoId:videoId
                                                      authToken:authToken
                                                adTagParameters:adTagParameters
                                       adTagParametersValidFrom:validFrom
                                      adTagParametersValidUntil:validUntil];
        
        if (drmDict) {
            NSString* __nullable croToken = [drmDict objectForKey:@"croToken"];
            NSString* __nullable licensingServerUrl = [drmDict objectForKey:@"licensingServerUrl"];
            
            if (croToken && licensingServerUrl) {
                DorisDRMSource* drm = [DorisDRMSource.alloc initWithCroToken:croToken licensingServerUrl:licensingServerUrl];
                source.drm = drm;
            }
        }
        dispatch_async(dispatch_get_main_queue(), ^{
            [self.dorisUI.input loadWithImaSource:source startPlayingAt:self->_startPlayingAt];
        });
    }];
}


- (void)prepareAdTagParameters:(NSDictionary * _Nullable)adTagParameters withCallback:(void(^_Nonnull)(NSDictionary * _Nullable))handler {
    if (adTagParameters) {
        NSString* __nullable customParams = [adTagParameters objectForKey:@"cust_params"];
        
        if (customParams) {
            NSString* widthString = [NSString stringWithFormat: @"&pw=%.0f", self.bounds.size.width];
            NSString* heightString = [NSString stringWithFormat: @"&ph=%.0f", self.bounds.size.height];
            
            customParams = [customParams stringByAppendingString:widthString];
            customParams = [customParams stringByAppendingString:heightString];
            [adTagParameters setValue:customParams forKey:@"cust_params"];
        }
        
        if (@available(tvOS 14, *)) {
            [ATTrackingManager requestTrackingAuthorizationWithCompletionHandler:^(ATTrackingManagerAuthorizationStatus status) {
                if (status == ATTrackingManagerAuthorizationStatusAuthorized) {
                    [adTagParameters setValue:@"0" forKey:@"is_lat"];
                    [adTagParameters setValue:UIDevice.currentDevice.identifierForVendor.UUIDString forKey:@"rdid"];
                } else {
                    [adTagParameters setValue:@"1" forKey:@"is_lat"];
                }
                
                [self fetchAppIdWithCompletion:^(NSNumber * _Nullable appId) {
                    if (appId) {
                        self->_appId = appId;
                        [adTagParameters setValue:appId.stringValue forKey:@"msid"];
                    } else {
                        self->_appId = 0;
                        [adTagParameters setValue:@"0" forKey:@"msid"];
                    }
                    handler(adTagParameters);
                }];
            }];
        } else {
            [adTagParameters setValue:@"0" forKey:@"is_lat"];
            [adTagParameters setValue:UIDevice.currentDevice.identifierForVendor.UUIDString forKey:@"rdid"];
            [self fetchAppIdWithCompletion:^(NSNumber * _Nullable appId) {
                if (appId) {
                    self->_appId = appId;
                    [adTagParameters setValue:appId.stringValue forKey:@"msid"];
                } else {
                    self->_appId = 0;
                    [adTagParameters setValue:@"0" forKey:@"msid"];
                }
                handler(adTagParameters);
            }];
        }
    }
}


- (void)playerItemForSource:(NSDictionary *)source withCallback:(void(^)(AVPlayerItem *))handler {
    bool isNetwork = [RCTConvert BOOL:[source objectForKey:@"isNetwork"]];
    bool isAsset = [RCTConvert BOOL:[source objectForKey:@"isAsset"]];
    NSString *uri = [source objectForKey:@"uri"];
    NSString *type = [source objectForKey:@"type"];
    
    NSURL *url = isNetwork || isAsset
    ? [NSURL URLWithString:uri]
    : [[NSURL alloc] initFileURLWithPath:[[NSBundle mainBundle] pathForResource:uri ofType:type]];
    NSMutableDictionary *assetOptions = [[NSMutableDictionary alloc] init];
    
    [self setupMuxDataFromSource:source];
    
    if (isNetwork) {
        [self setupBeaconFromSource:source];
    }
    
    id drmObject = [source objectForKey:@"drm"];
    if (drmObject) {
        ActionToken* ac = nil;
        if ([drmObject isKindOfClass:NSDictionary.class]) {
            NSDictionary* drmDictionary = drmObject;
            ac = [[ActionToken alloc] initWithDict:drmDictionary contentUrl:uri];
        } else if ([drmObject isKindOfClass:NSString.class]) {
            NSString* drmString = drmObject;
            ac = [ActionToken createFrom: drmString contentUrl:uri];
        }
        if (ac) {
            _actionToken = ac;
            AVURLAsset* asset = [ac urlAsset];
            handler([AVPlayerItem playerItemWithAsset:asset]);
            
            return;
        } else {
            NSLog(@"Failed to created action token for playback.");
        }
    } else {
        // we can try subtitles if it's not a DRM file
        id subtitleObjects = [source objectForKey:@"subtitles"];
        if ([subtitleObjects isKindOfClass:NSArray.class]) {
            NSArray* subs = subtitleObjects;
            NSArray* subtitleTracks = [SubtitleResourceLoaderDelegate createSubtitleTracksFromArray:subs];
            SubtitleResourceLoaderDelegate* delegate = [[SubtitleResourceLoaderDelegate alloc] initWithM3u8URL:url subtitles:subtitleTracks];
            _delegate = delegate;
            url = delegate.redirectURL;
            if (!delegateQueue) {
                delegateQueue = dispatch_queue_create("SubtitleResourceLoaderDelegate", 0);
            }
            AVURLAsset* asset = [AVURLAsset URLAssetWithURL:url options:nil];
            [asset.resourceLoader setDelegate:delegate queue:delegateQueue];
            handler([AVPlayerItem playerItemWithAsset:asset]);
            
            return;
        }
    }
    
    if (isNetwork) {
        NSArray *cookies = [[NSHTTPCookieStorage sharedHTTPCookieStorage] cookies];
        [assetOptions setObject:cookies forKey:AVURLAssetHTTPCookiesKey];
        
        AVURLAsset *asset = [AVURLAsset URLAssetWithURL:url options:assetOptions];
        handler([AVPlayerItem playerItemWithAsset:asset]);
        
        return;
    }
    
    AVURLAsset *asset = [AVURLAsset URLAssetWithURL:[[NSURL alloc] initFileURLWithPath:[[NSBundle mainBundle] pathForResource:uri ofType:type]] options:nil];
    handler([AVPlayerItem playerItemWithAsset:asset]);
}

- (void)layoutSubviews {
    [super layoutSubviews];
    if(_waterMarkImageView.image == nil) {
        [WatermarkManager setupWatermarkFromSourceWithSource:_source watermarkView:_waterMarkImageView parent:_dorisUI.view];
    }
}

- (void)limitSeekableRanges:(NSDictionary * _Nullable)ranges {
    if (ranges == nil) {
        return;
    }
    
    NSNumber *_Nullable _start;
    NSNumber *_Nullable _end;
    BOOL _seekToStart;
    
    id start = [ranges objectForKey:@"start"];
    id end = [ranges objectForKey:@"end"];
    id seekToStart = [ranges objectForKey:@"seekToStart"];
    
    if (start && [start isKindOfClass:NSNumber.class]) {
        NSNumber *startNumber = start;
        NSDate *date = [[NSDate alloc] initWithTimeIntervalSince1970:startNumber.doubleValue/1000.0];
        _start = [[NSNumber alloc] initWithDouble:date.timeIntervalSinceNow];
    }
    
    if (end && [end isKindOfClass:NSNumber.class]) {
        NSNumber *endNumber = end;
        NSDate *date = [[NSDate alloc] initWithTimeIntervalSince1970:endNumber.doubleValue/1000.0];
        _end = [[NSNumber alloc] initWithDouble:date.timeIntervalSinceNow];
    }
    
    if ([seekToStart boolValue]) {
        _seekToStart = [seekToStart boolValue];
    } else {
        _seekToStart = NO;
    }
    
    [_dorisUI.input limitSeekableRangeWithStart:_start end:_end seekToStart:_seekToStart];
}



#pragma mark - DorisExternalOutputProtocol

- (void)didRequestAdTagParametersFor:(NSTimeInterval)timeInterval isBlocking:(BOOL)isBlocking {
    if(self.onRequireAdParameters) {
        NSNumber* _timeIntervalSince1970 = [[NSNumber alloc] initWithDouble:timeInterval];
        NSNumber* _isBlocking = [[NSNumber alloc] initWithBool:isBlocking];
        
        self.onRequireAdParameters(@{@"date": _timeIntervalSince1970,
                                     @"isBlocking": _isBlocking});
    }
}

- (void)didGetPlaybackError {
    if(self.onVideoError) {
        self.onVideoError(@{@"target": self.reactTag});
    }
}

- (void)didChangeCurrentPlaybackTimeWithCurrentTime:(double)currentTime {
    if( currentTime >= 0 && self.onVideoProgress) {
        self.onVideoProgress(@{@"currentTime": [NSNumber numberWithDouble:currentTime]});
    }
    
    if (self.onVideoAboutToEnd && _itemDuration) {
        bool isAboutToEnd;
        if (currentTime >= _itemDuration.doubleValue - 5) {
            isAboutToEnd = YES;
        } else {
            isAboutToEnd = NO;
        }
        self.onVideoAboutToEnd(@{@"isAboutToEnd": [NSNumber numberWithBool:isAboutToEnd]});
    }
}

- (void)didFinishPlayingWithStartTime:(double)startTime endTime:(double)endTime streamType:(NSString *)streamType {
    [_dorisUI.input seekTo:startTime];
    if(self.onVideoEnd) {
        self.onVideoEnd(@{@"target": self.reactTag,
                          @"type": streamType});
    }
}

- (void)didLoadVideo {
    if(self.onVideoLoad) {
        self.onVideoLoad(@{@"target": self.reactTag});
    }
}

- (void)didResumePlayback:(BOOL)isPlaying {
    if (isPlaying) {
        self.onPlaybackRateChange(@{@"playbackRate": [NSNumber numberWithFloat:1.0],
                                    @"target": self.reactTag});
        [self startDiceBeaconCallsAfter:0];
    } else {
        self.onPlaybackRateChange(@{@"playbackRate": [NSNumber numberWithFloat:0.0],
                                    @"target": self.reactTag});
        [_diceBeaconRequst cancel];
    }
}

- (void)didStartBuffering {
    if (self.onVideoBuffer) {
        self.onVideoBuffer(@{@"isBuffering": @(YES), @"target": self.reactTag});
    }
}

- (void)didFinishBuffering {
    if (self.onVideoBuffer) {
        self.onVideoBuffer(@{@"isBuffering": @(NO), @"target": self.reactTag});
    }
}

- (void)didChangeItemDuration:(double)duration {
    _itemDuration = [NSNumber numberWithDouble:duration];
}

- (void)didTapFavouriteButton {
    if (self.onFavouriteButtonClick) {
        self.onFavouriteButtonClick(@{@"target": self.reactTag});
    }
}

- (void)didTapMoreRelatedVideosButton {
    if (self.onRelatedVideosIconClicked) {
        self.onRelatedVideosIconClicked(@{@"target": self.reactTag});
    }
}

- (void)didSelectRelatedVideoWithIdentifier:(NSNumber *)identifier type:(NSString *)type {
    if (self.onRelatedVideoClicked) {
        self.onRelatedVideoClicked(@{@"id": identifier,
                                     @"type": type,
                                     @"target": self.reactTag});
    }
}

- (void)didTapStatsButton {
    if (self.onStatsIconClick) {
        self.onStatsIconClick(@{@"target": self.reactTag});
    }
}

- (void)didTapScheduleButton {
    if (self.onEpgIconClick) {
        self.onEpgIconClick(@{@"target": self.reactTag});
    }
}


#pragma mark - Lifecycle
- (void)dealloc {
    [_diceBeaconRequst cancel];
    _diceBeaconRequst = nil;
}










#pragma mark - DICE Beacon

- (void)startDiceBeaconCallsAfter:(long)seconds {
    [self startDiceBeaconCallsAfter:seconds ongoing:NO];
}

- (void)startDiceBeaconCallsAfter:(long)seconds ongoing:(BOOL)ongoing {
    if (_diceBeaconRequst == nil) {
        return;
    }
    if (_diceBeaconRequestOngoing && !ongoing) {
        DICELog(@"startDiceBeaconCallsAfter ONGOING request. INGNORING.");
        return;
    }
    _diceBeaconRequestOngoing = YES;
    DICELog(@"startDiceBeaconCallsAfter %ld", seconds);
    __weak RCTVideo *weakSelf = self;
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(seconds * NSEC_PER_SEC)), dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        // in case there is ongoing request
        [_diceBeaconRequst cancel];
        [_diceBeaconRequst makeRequestWithCompletionHandler:^(DiceBeaconResponse *response, NSError *error) {
            dispatch_async(dispatch_get_main_queue(), ^{
                [weakSelf handleBeaconResponse:response error:error];
            });
        }];
    });
}

-(void)handleBeaconResponse:(DiceBeaconResponse *)response error:(NSError *)error {
    DICELog(@"handleBeaconResponse error=%@", error);
    if (self.player.timeControlStatus == AVPlayerTimeControlStatusPaused) {
        // video is not playing back, so no point
        DICELog(@"handleBeaconResponse player is paused. STOP beacons.");
        _diceBeaconRequestOngoing = NO;
        return;
    }
    
    if (error != nil) {
        DICELog(@"handleBeaconResponse error on call. STOP beacons.");
        // raise an error and stop playback
        NSNumber *code = [[NSNumber alloc] initWithInt:-1];
        self.onVideoError(@{@"error": @{@"code": code,
                                        @"domain": @"DiceBeacon",
                                        @"messages": @[@"Failed to make beacon request", error.localizedDescription]
        },
                            @"rawError": RCTJSErrorFromNSError(error),
                            @"target": self.reactTag});
        _diceBeaconRequestOngoing = NO;
        return;
    }
    
    if (response == nil || !response.OK) {
        // raise an error and stop playback
        NSNumber *code = [[NSNumber alloc] initWithInt:-2];
        NSString *rawResponse = @"";
        NSArray<NSString *> *errorMessages = @[];
        if (response != nil) {
            if (response.rawResponse != nil && response.rawResponse.length > 0) {
                rawResponse = [NSString stringWithUTF8String:[response.rawResponse bytes]];
            }
            if (rawResponse == nil) {
                rawResponse = @"";
            }
            if (response.errorMessages != nil) {
                errorMessages = response.errorMessages;
            }
        }
        self.onVideoError(@{@"error": @{@"code": code,
                                        @"domain": @"DiceBeacon",
                                        @"messages": errorMessages
        },
                            @"rawResponse": rawResponse,
                            @"target": self.reactTag});
        [self setPaused:YES];
        _diceBeaconRequestOngoing = NO;
        return;
    }
    [self startDiceBeaconCallsAfter:response.frequency ongoing:YES];
}

- (void)setupBeaconFromSource:(NSDictionary *)source {
    id configObject = [source objectForKey:@"config"];
    id beaconObject = nil;
    if (configObject != nil && [configObject isKindOfClass:NSDictionary.class]) {
        beaconObject = [((NSDictionary *)configObject) objectForKey:@"beacon"];
    }
    
    if (beaconObject != nil) {
        if ([beaconObject isKindOfClass:NSString.class]) {
            NSString * beaconString = beaconObject;
            NSError *error = nil;
            beaconObject = [NSJSONSerialization JSONObjectWithData:[beaconString dataUsingEncoding:kCFStringEncodingUTF8]  options:0 error:&error];
            if (error != nil) {
                DICELog(@"Failed to create JSON object from provided beacon: %@", beaconString);
            }
        }
        if ([beaconObject isKindOfClass:NSDictionary.class]) {
            NSDictionary *beacon = beaconObject;
            NSString* url = [beacon objectForKey:@"url"];
            NSDictionary<NSString *, NSString *> *headers = [beacon objectForKey:@"headers"];
            NSDictionary* body = [beacon objectForKey:@"body"];
            _diceBeaconRequst = [DiceBeaconRequest requestWithURLString:url headers:headers body:body];
            [self startDiceBeaconCallsAfter:0];
        } else {
            DICELog(@"Failed to read dictionary object provided beacon: %@", beaconObject);
        }
    }
}







#pragma mark - Mux Data
- (NSString * _Nullable)stringFromDict:(NSDictionary *)dict forKey:(id _Nonnull)key {
    id obj = [dict objectForKey:key];
    if (obj != nil && [obj isKindOfClass:NSString.class]) {
        return obj;
    }
    return nil;
}

- (void)setupMuxDataFromSource:(NSDictionary *)source {
    id configObject = [source objectForKey:@"config"];
    id muxData = nil;
    if (configObject != nil && [configObject isKindOfClass:NSDictionary.class]) {
        muxData = [((NSDictionary *)configObject) objectForKey:@"muxData"];
    }
    
    if (muxData != nil) {
        if ([muxData isKindOfClass:NSString.class]) {
            NSString * muxDataString = muxData;
            NSError *error = nil;
            muxData = [NSJSONSerialization JSONObjectWithData:[muxDataString dataUsingEncoding:kCFStringEncodingUTF8]  options:0 error:&error];
            if (error != nil) {
                DICELog(@"Failed to create JSON object from provided playbackData: %@", muxDataString);
            }
        }
        
        if ([muxData isKindOfClass:NSDictionary.class]) {
            NSDictionary *muxDict = muxData;
            
            NSString * envKey = [muxDict objectForKey:@"envKey"];
            NSString * _Nullable playerName = [self stringFromDict:muxDict forKey:@"playerName"];

            if (envKey == nil) {
                DICELog(@"envKey is not present. Mux will not be available.");
                return;
            }
            
            if (playerName == nil) {
                playerName = @"AVDoris";
            }
                        
            DorisMuxCustomerPlayerData * playerData = [[DorisMuxCustomerPlayerData alloc] initWithPlayerName:playerName environmentKey:envKey];
            playerData.viewerUserId = [self stringFromDict:muxDict forKey:@"viewerUserId"];
            playerData.subPropertyId = [self stringFromDict:muxDict forKey:@"subPropertyId"];
            playerData.experimentName = [self stringFromDict:muxDict forKey:@"experimentName"];
            playerData.playerVersion = playerVersion;
            
            DorisMuxCustomerVideoData* videoData = [[DorisMuxCustomerVideoData alloc] init];
            
            // ...insert video metadata
            videoData.videoTitle = [self stringFromDict:muxDict forKey:@"videoTitle"];
            videoData.videoId = [self stringFromDict:muxDict forKey:@"videoId"];
            videoData.videoSeries = [self stringFromDict:muxDict forKey:@"videoSeries"];
            videoData.videoCdn = [self stringFromDict:muxDict forKey:@"videoCdn"];
            videoData.videoStreamType = [self stringFromDict:muxDict forKey:@"videoStreamType"];
            
            id videoIsLive = [muxDict objectForKey:@"videoIsLive"];
            if (videoIsLive != nil && [videoIsLive isKindOfClass:NSNumber.class]) {
                videoData.videoIsLive = videoIsLive;
            }
            
            id videoDuration = [muxDict objectForKey:@"videoDuration"];
            if (videoDuration != nil && [videoDuration isKindOfClass:NSNumber.class]) {
                videoData.videoDuration = videoDuration;
            }
                        
            [self.dorisUI.input configureMuxWithPlayerData:playerData videoData:videoData];
        } else {
            DICELog(@"Failed to read dictionary object provided playbackData: %@", muxData);
        }
    }
}


- (void)fetchAppIdWithCompletion:(void (^)(NSNumber* _Nullable appId))completionBlock {
    NSURL* url = [self iTunesURLFromString];
    NSMutableURLRequest* request = [[NSMutableURLRequest alloc] initWithURL:url];
    [request addValue:@"application/json; charset=utf-8" forHTTPHeaderField:@"Content-Type"];
    request.HTTPMethod = @"GET";
    
    if (_appId) {
        completionBlock(_appId);
    } else {
        [[NSURLSession.sharedSession dataTaskWithRequest:request completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
            completionBlock([self parseAppIdwithData:data response:response error:error]);
        }] resume];
    }
}

- (NSURL *)iTunesURLFromString {
    NSURLComponents* components = [NSURLComponents new];
    components.scheme = @"https";
    components.host = @"itunes.apple.com";
    components.path = @"/lookup";
    
    NSURLQueryItem* item = [[NSURLQueryItem alloc] initWithName:@"bundleId" value:NSBundle.mainBundle.bundleIdentifier];
    components.queryItems = @[item];
    return components.URL;
}

- (nullable NSNumber*)parseAppIdwithData:(nullable NSData*)data response:(NSURLResponse*)response error:(NSError*)error {
    if (error) {
        return nil;
    }
    
    if (data) {
        NSError *error = nil;
        id object = [NSJSONSerialization
                     JSONObjectWithData:data
                     options:0
                     error:&error];
        
        if(error) {
            return nil;
        } else if([object isKindOfClass:[NSDictionary class]]) {
            NSDictionary *dict = object;
            NSArray* results = [dict mutableArrayValueForKey:@"results"];
            NSDictionary *dict2 = [results firstObject];
            id appid = [dict2 objectForKey:@"trackId"];
            if ([appid isKindOfClass:NSNumber.class]) {
                return appid;
            }
        }
    }
    return  nil;
}

@end

