import AVFoundation
import AVKit
import Foundation
#if USE_GOOGLE_IMA
    import GoogleInteractiveMediaAds
#endif
import React

// MARK: - RCTVideo

class RCTVideo: UIView, RCTVideoPlayerViewControllerDelegate, RCTPlayerObserverHandler {
    private var _player: AVPlayer?
    private var _playerItem: AVPlayerItem?
    private var _source: VideoSource?
    private var _playerLayer: AVPlayerLayer?
    private var _chapters: [Chapter]?

    private var _playerViewController: RCTVideoPlayerViewController?
    private var _videoURL: NSURL?

    /* DRM */
    private var _drm: DRMParams?

    private var _localSourceEncryptionKeyScheme: String?

    /* Required to publish events */
    private var _eventDispatcher: RCTEventDispatcher?
    private var _videoLoadStarted = false

    private var _pendingSeek = false
    private var _pendingSeekTime: Float = 0.0
    private var _lastSeekTime: Float = 0.0

    /* For sending videoProgress events */
    private var _controls = false

    /* Keep track of any modifiers, need to be applied after each play */
    private var _audioOutput: String = "speaker"
    private var _volume: Float = 1.0
    private var _rate: Float = 1.0
    private var _maxBitRate: Float?

    private var _automaticallyWaitsToMinimizeStalling = true
    private var _muted = false
    private var _paused = false
    private var _repeat = false
    private var _isPlaying = false
    private var _allowsExternalPlayback = true
    private var _textTracks: [TextTrack]?
    private var _selectedTextTrackCriteria: SelectedTrackCriteria?
    private var _selectedAudioTrackCriteria: SelectedTrackCriteria?
    private var _playbackStalled = false
    private var _playInBackground = false
    private var _preventsDisplaySleepDuringVideoPlayback = true
    private var _preferredForwardBufferDuration: Float = 0.0
    private var _playWhenInactive = false
    private var _ignoreSilentSwitch: String! = "inherit" // inherit, ignore, obey
    private var _mixWithOthers: String! = "inherit" // inherit, mix, duck
    private var _resizeMode: String! = "cover"
    private var _fullscreen = false
    private var _fullscreenAutorotate = true
    private var _fullscreenOrientation: String! = "all"
    private var _fullscreenPlayerPresented = false
    private var _fullscreenUncontrolPlayerPresented = false // to call events switching full screen mode from player controls
    private var _filterName: String!
    private var _filterEnabled = false
    private var _presentingViewController: UIViewController?
    private var _startPosition: Float64 = -1
    private var _showNotificationControls = false
    private var _enterPictureInPictureOnLeave = false {
        didSet {
            #if os(iOS)
                if isPictureInPictureActive() { return }
                if _enterPictureInPictureOnLeave {
                    initPictureinPicture()
                    _playerViewController?.allowsPictureInPicturePlayback = true
                } else {
                    _pip?.deinitPipController()
                    _playerViewController?.allowsPictureInPicturePlayback = false
                }
            #endif
        }
    }

    private var _isBuffering = false {
        didSet {
            onVideoBuffer?(["isBuffering": _isBuffering, "target": reactTag as Any])
        }
    }

    /* IMA Ads */
    private var _adTagUrl: String?
    #if USE_GOOGLE_IMA
        private var _imaAdsManager: RCTIMAAdsManager!
        /* Playhead used by the SDK to track content video progress and insert mid-rolls. */
        private var _contentPlayhead: IMAAVPlayerContentPlayhead?
    #endif
    private var _didRequestAds = false
    private var _adPlaying = false

    private var _resouceLoaderDelegate: RCTResourceLoaderDelegate?
    private var _playerObserver: RCTPlayerObserver = .init()

    #if USE_VIDEO_CACHING
        private let _videoCache: RCTVideoCachingHandler = .init()
    #endif

    #if os(iOS)
        private var _pip: RCTPictureInPicture?
    #endif

    // Events
    @objc var onVideoLoadStart: RCTDirectEventBlock?
    @objc var onVideoLoad: RCTDirectEventBlock?
    @objc var onVideoBuffer: RCTDirectEventBlock?
    @objc var onVideoError: RCTDirectEventBlock?
    @objc var onVideoProgress: RCTDirectEventBlock?
    @objc var onVideoBandwidthUpdate: RCTDirectEventBlock?
    @objc var onVideoSeek: RCTDirectEventBlock?
    @objc var onVideoEnd: RCTDirectEventBlock?
    @objc var onTimedMetadata: RCTDirectEventBlock?
    @objc var onVideoAudioBecomingNoisy: RCTDirectEventBlock?
    @objc var onVideoFullscreenPlayerWillPresent: RCTDirectEventBlock?
    @objc var onVideoFullscreenPlayerDidPresent: RCTDirectEventBlock?
    @objc var onVideoFullscreenPlayerWillDismiss: RCTDirectEventBlock?
    @objc var onVideoFullscreenPlayerDidDismiss: RCTDirectEventBlock?
    @objc var onReadyForDisplay: RCTDirectEventBlock?
    @objc var onPlaybackStalled: RCTDirectEventBlock?
    @objc var onPlaybackResume: RCTDirectEventBlock?
    @objc var onPlaybackRateChange: RCTDirectEventBlock?
    @objc var onVolumeChange: RCTDirectEventBlock?
    @objc var onVideoPlaybackStateChanged: RCTDirectEventBlock?
    @objc var onVideoExternalPlaybackChange: RCTDirectEventBlock?
    @objc var onPictureInPictureStatusChanged: RCTDirectEventBlock?
    @objc var onRestoreUserInterfaceForPictureInPictureStop: RCTDirectEventBlock?
    @objc var onGetLicense: RCTDirectEventBlock?
    @objc var onReceiveAdEvent: RCTDirectEventBlock?
    @objc var onTextTracks: RCTDirectEventBlock?
    @objc var onAudioTracks: RCTDirectEventBlock?
    @objc var onTextTrackDataChanged: RCTDirectEventBlock?

    @objc
    func _onPictureInPictureEnter() {
        onPictureInPictureStatusChanged?(["isActive": NSNumber(value: true)])
    }

    @objc
    func _onPictureInPictureExit() {
        onPictureInPictureStatusChanged?(["isActive": NSNumber(value: false)])
    }

    func handlePictureInPictureEnter() {
        onPictureInPictureStatusChanged?(["isActive": NSNumber(value: true)])
    }

    func handlePictureInPictureExit() {
        onPictureInPictureStatusChanged?(["isActive": NSNumber(value: false)])
    }

    func handleRestoreUserInterfaceForPictureInPictureStop() {
        onRestoreUserInterfaceForPictureInPictureStop?([:])
    }

    func isPictureInPictureActive() -> Bool {
        return _pip?.isPictureInPictureActive ?? false
    }

    func initPictureinPicture() {
        #if os(iOS)
            if _pip == nil {
                _pip = RCTPictureInPicture({ [weak self] in
                    self?._onPictureInPictureEnter()
                }, { [weak self] in
                    self?._onPictureInPictureExit()
                }, { [weak self] in
                    self?.onRestoreUserInterfaceForPictureInPictureStop?([:])
                })
            }

            if _playerLayer != nil && !_controls && !(_pip?.isInitialized ?? false) {
                _pip?.setupPipController(_playerLayer)
            }
        #else
            DebugLog("Picture in Picture is not supported on this platform")
        #endif
    }

    init(eventDispatcher: RCTEventDispatcher!) {
        super.init(frame: CGRect(x: 0, y: 0, width: 100, height: 100))
        #if USE_GOOGLE_IMA
            _imaAdsManager = RCTIMAAdsManager(video: self, isPictureInPictureActive: isPictureInPictureActive)
        #endif

        _eventDispatcher = eventDispatcher

        #if os(iOS)
            if _enterPictureInPictureOnLeave {
                initPictureinPicture()
            }
        #endif

        NotificationCenter.default.addObserver(
            self,
            selector: #selector(applicationWillResignActive(notification:)),
            name: UIApplication.willResignActiveNotification,
            object: nil
        )

        NotificationCenter.default.addObserver(
            self,
            selector: #selector(applicationDidBecomeActive(notification:)),
            name: UIApplication.didBecomeActiveNotification,
            object: nil
        )

        NotificationCenter.default.addObserver(
            self,
            selector: #selector(applicationDidEnterBackground(notification:)),
            name: UIApplication.didEnterBackgroundNotification,
            object: nil
        )

        NotificationCenter.default.addObserver(
            self,
            selector: #selector(applicationWillEnterForeground(notification:)),
            name: UIApplication.willEnterForegroundNotification,
            object: nil
        )

        NotificationCenter.default.addObserver(
            self,
            selector: #selector(screenWillLock),
            name: UIApplication.protectedDataWillBecomeUnavailableNotification,
            object: nil
        )

        NotificationCenter.default.addObserver(
            self,
            selector: #selector(screenDidUnlock),
            name: UIApplication.protectedDataDidBecomeAvailableNotification,
            object: nil
        )

        NotificationCenter.default.addObserver(
            self,
            selector: #selector(audioRouteChanged(notification:)),
            name: AVAudioSession.routeChangeNotification,
            object: nil
        )
        _playerObserver._handlers = self
        #if USE_VIDEO_CACHING
            _videoCache.playerItemPrepareText = playerItemPrepareText
        #endif
    }

    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        #if USE_GOOGLE_IMA
            _imaAdsManager = RCTIMAAdsManager(video: self, isPictureInPictureActive: isPictureInPictureActive)
        #endif
    }

    deinit {
        NotificationCenter.default.removeObserver(self)
        self.removePlayerLayer()
        _playerObserver.clearPlayer()

        if let player = _player {
            NowPlayingInfoCenterManager.shared.removePlayer(player: player)
        }

        #if os(iOS)
            _pip = nil
        #endif
    }

    // MARK: - App lifecycle handlers

    @objc
    func applicationWillResignActive(notification _: NSNotification!) {
        let isExternalPlaybackActive = _player?.isExternalPlaybackActive ?? false
        if _playInBackground || _playWhenInactive || !_isPlaying || isExternalPlaybackActive { return }

        _player?.pause()
        _player?.rate = 0.0
    }

    @objc
    func applicationDidBecomeActive(notification _: NSNotification!) {
        let isExternalPlaybackActive = _player?.isExternalPlaybackActive ?? false
        if _playInBackground || _playWhenInactive || !_isPlaying || isExternalPlaybackActive { return }

        // Resume the player or any other tasks that should continue when the app becomes active.
        _player?.play()
        _player?.rate = _rate
    }

    @objc
    func applicationDidEnterBackground(notification _: NSNotification!) {
        let isExternalPlaybackActive = _player?.isExternalPlaybackActive ?? false
        if _playInBackground || isExternalPlaybackActive { return }
        if isPictureInPictureActive() {
            _player?.play()
            _player?.rate = _rate
        } else {
            // Needed to play sound in background. See https://developer.apple.com/library/ios/qa/qa1668/_index.html
            _playerLayer?.player?.pause()
            _playerLayer?.player?.rate = 0.0
            _playerLayer?.player = nil
            _playerViewController?.player = nil
        }
    }

    @objc
    func applicationWillEnterForeground(notification _: NSNotification!) {
        self.applyModifiers()
        _playerLayer?.player = _player
        _playerViewController?.player = _player
    }

    @objc
    func screenWillLock() {
        let isActiveBackgroundPip = isPictureInPictureActive() && UIApplication.shared.applicationState != .active
        if _playInBackground || !_isPlaying || !isActiveBackgroundPip { return }

        _player?.pause()
        _player?.rate = 0.0
    }

    @objc
    func screenDidUnlock() {
        let isActiveBackgroundPip = isPictureInPictureActive() && UIApplication.shared.applicationState != .active
        if _paused || !isActiveBackgroundPip { return }

        _player?.play()
        _player?.rate = _rate
    }

    // MARK: - Audio events

    @objc
    func audioRouteChanged(notification: NSNotification!) {
        if let userInfo = notification.userInfo {
            let reason: AVAudioSession.RouteChangeReason! = userInfo[AVAudioSessionRouteChangeReasonKey] as? AVAudioSession.RouteChangeReason
            //            let previousRoute:NSNumber! = userInfo[AVAudioSessionRouteChangePreviousRouteKey] as? NSNumber
            if reason == .oldDeviceUnavailable, let onVideoAudioBecomingNoisy {
                onVideoAudioBecomingNoisy(["target": reactTag as Any])
            }
        }
    }

    // MARK: - Progress

    func sendProgressUpdate(didEnd: Bool = false) {
        #if !USE_GOOGLE_IMA
            // If we dont use Ads and onVideoProgress is not defined we dont need to run this code
            guard onVideoProgress != nil else { return }
        #endif

        if let video = _player?.currentItem,
           video == nil || video.status != AVPlayerItem.Status.readyToPlay {
            return
        }

        let playerDuration: CMTime = RCTVideoUtils.playerItemDuration(_player)
        if CMTIME_IS_INVALID(playerDuration) {
            return
        }

        var currentTime = _player?.currentTime()
        if currentTime != nil && _source?.cropStart != nil {
            currentTime = CMTimeSubtract(currentTime!, CMTimeMake(value: _source?.cropStart ?? 0, timescale: 1000))
        }
        let currentPlaybackTime = _player?.currentItem?.currentDate()
        let duration = CMTimeGetSeconds(playerDuration)
        var currentTimeSecs = CMTimeGetSeconds(currentTime ?? .zero)

        if currentTimeSecs > duration || didEnd {
            currentTimeSecs = duration
        }

        if currentTimeSecs >= 0 {
            #if USE_GOOGLE_IMA
                if !_didRequestAds && currentTimeSecs >= 0.0001 && _adTagUrl != nil {
                    _imaAdsManager.requestAds()
                    _didRequestAds = true
                }
            #endif
            onVideoProgress?([
                "currentTime": currentTimeSecs,
                "playableDuration": RCTVideoUtils.calculatePlayableDuration(_player, withSource: _source),
                "atValue": currentTime?.value ?? .zero,
                "currentPlaybackTime": NSNumber(value: Double(currentPlaybackTime?.timeIntervalSince1970 ?? 0 * 1000)).int64Value,
                "target": reactTag,
                "seekableDuration": RCTVideoUtils.calculateSeekableDuration(_player),
            ])
        }
    }

    var isSetSourceOngoing = false
    var nextSource: NSDictionary?

    func applyNextSource() {
        if self.nextSource != nil {
            DebugLog("apply next source")
            self.isSetSourceOngoing = false
            let nextSrc = self.nextSource
            self.nextSource = nil
            self.setSrc(nextSrc)
        }
    }

    // MARK: - Player and source

    func preparePlayerItem() async throws -> AVPlayerItem {
        guard let source = _source else {
            DebugLog("The source not exist")
            isSetSourceOngoing = false
            applyNextSource()
            throw NSError(domain: "", code: 0, userInfo: nil)
        }

        // Perform on next run loop, otherwise onVideoLoadStart is nil
        onVideoLoadStart?([
            "src": [
                "uri": _source?.uri ?? NSNull(),
                "type": _source?.type ?? NSNull(),
                "isNetwork": NSNumber(value: _source?.isNetwork ?? false),
            ],
            "drm": _drm?.json ?? NSNull(),
            "target": reactTag,
        ])

        if let uri = source.uri, uri.starts(with: "ph://") {
            let photoAsset = await RCTVideoUtils.preparePHAsset(uri: uri)
            return await playerItemPrepareText(asset: photoAsset, assetOptions: nil, uri: source.uri ?? "")
        }

        guard let assetResult = RCTVideoUtils.prepareAsset(source: source),
              let asset = assetResult.asset,
              let assetOptions = assetResult.assetOptions else {
            DebugLog("Could not find video URL in source '\(String(describing: _source))'")
            isSetSourceOngoing = false
            applyNextSource()
            throw NSError(domain: "", code: 0, userInfo: nil)
        }

        guard let assetResult = RCTVideoUtils.prepareAsset(source: source),
              let asset = assetResult.asset,
              let assetOptions = assetResult.assetOptions else {
            DebugLog("Could not find video URL in source '\(String(describing: _source))'")
            isSetSourceOngoing = false
            applyNextSource()
            throw NSError(domain: "", code: 0, userInfo: nil)
        }

        if let startPosition = _source?.startPosition {
            _startPosition = startPosition / 1000
        }

        #if USE_VIDEO_CACHING
            if _videoCache.shouldCache(source: source, textTracks: _textTracks) {
                return try await _videoCache.playerItemForSourceUsingCache(uri: source.uri, assetOptions: assetOptions)
            }
        #endif

        if _drm != nil || _localSourceEncryptionKeyScheme != nil {
            _resouceLoaderDelegate = RCTResourceLoaderDelegate(
                asset: asset,
                drm: _drm,
                localSourceEncryptionKeyScheme: _localSourceEncryptionKeyScheme,
                onVideoError: onVideoError,
                onGetLicense: onGetLicense,
                reactTag: reactTag
            )
        }

        return await playerItemPrepareText(asset: asset, assetOptions: assetOptions, uri: source.uri ?? "")
    }

    func setupPlayer(playerItem: AVPlayerItem) async throws {
        if !isSetSourceOngoing {
            DebugLog("setSrc has been canceled last step")
            return
        }

        _player?.pause()
        _playerItem = playerItem
        _playerObserver.playerItem = _playerItem
        setPreferredForwardBufferDuration(_preferredForwardBufferDuration)
        setPlaybackRange(playerItem, withCropStart: _source?.cropStart, withCropEnd: _source?.cropEnd)
        setFilter(_filterName)
        if let maxBitRate = _maxBitRate {
            _playerItem?.preferredPeakBitRate = Double(maxBitRate)
        }

        if _player == nil {
            _player = AVPlayer()
            _player!.replaceCurrentItem(with: playerItem)

            if _showNotificationControls {
                // We need to register player after we set current item and only for init
                NowPlayingInfoCenterManager.shared.registerPlayer(player: _player!)
            }
        } else {
            _player?.replaceCurrentItem(with: playerItem)

            // later we can just call "updateMetadata:
            NowPlayingInfoCenterManager.shared.updateMetadata()
        }

        _playerObserver.player = _player
        applyModifiers()
        _player?.actionAtItemEnd = .none

        if #available(iOS 10.0, *) {
            setAutomaticallyWaitsToMinimizeStalling(_automaticallyWaitsToMinimizeStalling)
        }

        #if USE_GOOGLE_IMA
            if _adTagUrl != nil {
                // Set up your content playhead and contentComplete callback.
                _contentPlayhead = IMAAVPlayerContentPlayhead(avPlayer: _player!)

                _imaAdsManager.setUpAdsLoader()
            }
        #endif
        isSetSourceOngoing = false
        applyNextSource()
    }

    @objc
    func setSrc(_ source: NSDictionary!) {
        if self.isSetSourceOngoing || self.nextSource != nil {
            DebugLog("setSrc buffer request")
            self._player?.replaceCurrentItem(with: nil)
            nextSource = source
            return
        }
        self.isSetSourceOngoing = true

        let initializeSource = {
            self._source = VideoSource(source)
            if self._source?.uri == nil || self._source?.uri == "" {
                self._player?.replaceCurrentItem(with: nil)
                self.isSetSourceOngoing = false
                self.applyNextSource()

                if let player = self._player {
                    NowPlayingInfoCenterManager.shared.removePlayer(player: player)
                }

                DebugLog("setSrc Stopping playback")
                return
            }
            self.removePlayerLayer()
            self._playerObserver.player = nil
            self._resouceLoaderDelegate = nil
            self._playerObserver.playerItem = nil

            // perform on next run loop, otherwise other passed react-props may not be set
            RCTVideoUtils.delay { [weak self] in
                do {
                    guard let self else { throw NSError(domain: "", code: 0, userInfo: nil) }

                    let playerItem = try await self.preparePlayerItem()
                    try await self.setupPlayer(playerItem: playerItem)
                } catch {
                    DebugLog("An error occurred: \(error.localizedDescription)")

                    if let self {
                        self.onVideoError?(["error": error.localizedDescription])
                        self.isSetSourceOngoing = false
                        self.applyNextSource()

                        if let player = self._player {
                            NowPlayingInfoCenterManager.shared.removePlayer(player: player)
                        }
                    }
                }
            }

            self._videoLoadStarted = true
            self.applyNextSource()
        }

        DispatchQueue.global(qos: .default).async(execute: initializeSource)
    }

    @objc
    func setDrm(_ drm: NSDictionary) {
        _drm = DRMParams(drm)
    }

    @objc
    func setLocalSourceEncryptionKeyScheme(_ keyScheme: String) {
        _localSourceEncryptionKeyScheme = keyScheme
    }

    func playerItemPrepareText(asset: AVAsset!, assetOptions: NSDictionary?, uri: String) async -> AVPlayerItem {
        if (self._textTracks == nil) || self._textTracks?.isEmpty == true || (uri.hasSuffix(".m3u8")) {
            return await self.playerItemPropegateMetadata(AVPlayerItem(asset: asset))
        }

        // AVPlayer can't airplay AVMutableCompositions
        self._allowsExternalPlayback = false
        let mixComposition = await RCTVideoUtils.generateMixComposition(asset)
        let validTextTracks = await RCTVideoUtils.getValidTextTracks(
            asset: asset,
            assetOptions: assetOptions,
            mixComposition: mixComposition,
            textTracks: self._textTracks
        )

        if validTextTracks.count != self._textTracks?.count {
            self.setTextTracks(validTextTracks)
        }

        return await self.playerItemPropegateMetadata(AVPlayerItem(asset: mixComposition))
    }

    func playerItemPropegateMetadata(_ playerItem: AVPlayerItem!) async -> AVPlayerItem {
        var mapping: [AVMetadataIdentifier: Any] = [:]

        if let title = _source?.customMetadata?.title {
            mapping[.commonIdentifierTitle] = title
        }

        if let artist = _source?.customMetadata?.artist {
            mapping[.commonIdentifierArtist] = artist
        }

        if let subtitle = _source?.customMetadata?.subtitle {
            mapping[.iTunesMetadataTrackSubTitle] = subtitle
        }

        if let description = _source?.customMetadata?.description {
            mapping[.commonIdentifierDescription] = description
        }

        if let imageUri = _source?.customMetadata?.imageUri,
           let imageData = await RCTVideoUtils.createImageMetadataItem(imageUri: imageUri) {
            mapping[.commonIdentifierArtwork] = imageData
        }

        if #available(iOS 12.2, *), !mapping.isEmpty {
            playerItem.externalMetadata = RCTVideoUtils.createMetadataItems(for: mapping)
        }

        #if os(tvOS)
            if let chapters = _chapters {
                playerItem.navigationMarkerGroups = RCTVideoTVUtils.makeNavigationMarkerGroups(chapters)
            }
        #endif

        return playerItem
    }

    // MARK: - Prop setters

    @objc
    func setResizeMode(_ mode: String) {
        var resizeMode: AVLayerVideoGravity = .resizeAspect

        switch mode {
        case "contain":
            resizeMode = .resizeAspect
        case "none":
            resizeMode = .resizeAspect
        case "cover":
            resizeMode = .resizeAspectFill
        case "stretch":
            resizeMode = .resize
        default:
            resizeMode = .resizeAspect
        }

        if _controls {
            _playerViewController?.videoGravity = resizeMode
        } else {
            _playerLayer?.videoGravity = resizeMode
        }

        _resizeMode = mode
    }

    @objc
    func setPlayInBackground(_ playInBackground: Bool) {
        _playInBackground = playInBackground
    }

    @objc
    func setPreventsDisplaySleepDuringVideoPlayback(_ preventsDisplaySleepDuringVideoPlayback: Bool) {
        _preventsDisplaySleepDuringVideoPlayback = preventsDisplaySleepDuringVideoPlayback
        self.applyModifiers()
    }

    @objc
    func setAllowsExternalPlayback(_ allowsExternalPlayback: Bool) {
        _allowsExternalPlayback = allowsExternalPlayback
        #if !os(visionOS)
            _player?.allowsExternalPlayback = _allowsExternalPlayback
        #endif
    }

    @objc
    func setPlayWhenInactive(_ playWhenInactive: Bool) {
        _playWhenInactive = playWhenInactive
    }

    @objc
    func setEnterPictureInPictureOnLeave(_ enterPictureInPictureOnLeave: Bool) {
        #if os(iOS)
            let audioSession = AVAudioSession.sharedInstance()
            do {
                try audioSession.setCategory(.playback)
                try audioSession.setActive(true, options: [])
            } catch {}
            if _enterPictureInPictureOnLeave != enterPictureInPictureOnLeave {
                _enterPictureInPictureOnLeave = enterPictureInPictureOnLeave
            }
        #endif
    }

    @objc
    func setRestoreUserInterfaceForPIPStopCompletionHandler(_ restore: Bool) {
        #if os(iOS)
            if _pip != nil {
                _pip?.setRestoreUserInterfaceForPIPStopCompletionHandler(restore)
            } else {
                _playerObserver.setRestoreUserInterfaceForPIPStopCompletionHandler(restore)
            }
        #endif
    }

    @objc
    func setIgnoreSilentSwitch(_ ignoreSilentSwitch: String?) {
        _ignoreSilentSwitch = ignoreSilentSwitch
        RCTPlayerOperations.configureAudio(ignoreSilentSwitch: _ignoreSilentSwitch, mixWithOthers: _mixWithOthers, audioOutput: _audioOutput)
        applyModifiers()
    }

    @objc
    func setMixWithOthers(_ mixWithOthers: String?) {
        _mixWithOthers = mixWithOthers
        applyModifiers()
    }

    @objc
    func setPaused(_ paused: Bool) {
        if paused {
            if _adPlaying {
                #if USE_GOOGLE_IMA
                    _imaAdsManager.getAdsManager()?.pause()
                #endif
            } else {
                _player?.pause()
                _player?.rate = 0.0
            }
        } else {
            RCTPlayerOperations.configureAudio(ignoreSilentSwitch: _ignoreSilentSwitch, mixWithOthers: _mixWithOthers, audioOutput: _audioOutput)

            if _adPlaying {
                #if USE_GOOGLE_IMA
                    _imaAdsManager.getAdsManager()?.resume()
                #endif
            } else {
                if #available(iOS 10.0, *), !_automaticallyWaitsToMinimizeStalling {
                    _player?.playImmediately(atRate: _rate)
                } else {
                    _player?.play()
                    _player?.rate = _rate
                }
                _player?.rate = _rate
            }
        }

        _paused = paused
    }

    @objc
    func setSeek(_ info: NSDictionary!) {
        let seekTime: NSNumber! = info["time"] as! NSNumber
        let seekTolerance: NSNumber! = info["tolerance"] as! NSNumber
        let item: AVPlayerItem? = _player?.currentItem
        guard item != nil, let player = _player, let item, item.status == AVPlayerItem.Status.readyToPlay else {
            _pendingSeek = true
            _pendingSeekTime = seekTime.floatValue
            return
        }

        RCTPlayerOperations.seek(
            player: player,
            playerItem: item,
            paused: _paused,
            seekTime: seekTime.floatValue,
            seekTolerance: seekTolerance.floatValue
        ) { [weak self] (_: Bool) in
            guard let self else { return }

            self._playerObserver.addTimeObserverIfNotSet()
            self.setPaused(self._paused)
            self.onVideoSeek?(["currentTime": NSNumber(value: Float(CMTimeGetSeconds(item.currentTime()))),
                               "seekTime": seekTime,
                               "target": self.reactTag])
        }

        _pendingSeek = false
    }

    @objc
    func setRate(_ rate: Float) {
        if _rate != 1 {
            // This is a workaround
            // when player change from rate != 1 to another rate != 1 we see some video blocking
            // To bypass it we shall force the rate to 1 and apply real valut afterward
            _player?.rate = 1
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                self._rate = rate
                self.applyModifiers()
            }
        } else {
            // apply it directly
            self._rate = rate
            self.applyModifiers()
        }
    }

    @objc
    func isMuted() -> Bool {
        return _muted
    }

    @objc
    func setMuted(_ muted: Bool) {
        _muted = muted
        applyModifiers()
    }

    @objc
    func setAudioOutput(_ audioOutput: String) {
        _audioOutput = audioOutput
        RCTPlayerOperations.configureAudio(ignoreSilentSwitch: _ignoreSilentSwitch, mixWithOthers: _mixWithOthers, audioOutput: _audioOutput)
        do {
            if audioOutput == "speaker" {
                #if os(iOS) || os(visionOS)
                    try AVAudioSession.sharedInstance().overrideOutputAudioPort(AVAudioSession.PortOverride.speaker)
                #endif
            } else if audioOutput == "earpiece" {
                try AVAudioSession.sharedInstance().overrideOutputAudioPort(AVAudioSession.PortOverride.none)
            }
        } catch {
            print("Error occurred: \(error.localizedDescription)")
        }
    }

    @objc
    func setVolume(_ volume: Float) {
        _volume = volume
        applyModifiers()
    }

    @objc
    func setMaxBitRate(_ maxBitRate: Float) {
        _maxBitRate = maxBitRate
        _playerItem?.preferredPeakBitRate = Double(maxBitRate)
    }

    @objc
    func setPreferredForwardBufferDuration(_ preferredForwardBufferDuration: Float) {
        _preferredForwardBufferDuration = preferredForwardBufferDuration
        if #available(iOS 10.0, *) {
            _playerItem?.preferredForwardBufferDuration = TimeInterval(preferredForwardBufferDuration)
        } else {
            // Fallback on earlier versions
        }
    }

    @objc
    func setAutomaticallyWaitsToMinimizeStalling(_ waits: Bool) {
        _automaticallyWaitsToMinimizeStalling = waits
        if #available(iOS 10.0, *) {
            _player?.automaticallyWaitsToMinimizeStalling = waits
        } else {
            // Fallback on earlier versions
        }
    }

    func setPlaybackRange(_ item: AVPlayerItem!, withCropStart cropStart: Int64?, withCropEnd cropEnd: Int64?) {
        if let cropStart {
            let start = CMTimeMake(value: cropStart, timescale: 1000)
            item.reversePlaybackEndTime = start
            _pendingSeekTime = Float(CMTimeGetSeconds(start))
            _pendingSeek = true
        }
        if let cropEnd {
            item.forwardPlaybackEndTime = CMTimeMake(value: cropEnd, timescale: 1000)
        }
    }

    func applyModifiers() {
        if let video = _player?.currentItem,
           video == nil || video.status != AVPlayerItem.Status.readyToPlay {
            return
        }
        if _muted {
            if !_controls {
                _player?.volume = 0
            }
            _player?.isMuted = true
        } else {
            _player?.volume = _volume
            _player?.isMuted = false
        }

        if #available(iOS 12.0, tvOS 12.0, *) {
            #if !os(visionOS)
                _player?.preventsDisplaySleepDuringVideoPlayback = _preventsDisplaySleepDuringVideoPlayback
            #endif
        } else {
            // Fallback on earlier versions
        }

        if let _maxBitRate {
            setMaxBitRate(_maxBitRate)
        }

        setAudioOutput(_audioOutput)
        setSelectedAudioTrack(_selectedAudioTrackCriteria)
        setSelectedTextTrack(_selectedTextTrackCriteria)
        setResizeMode(_resizeMode)
        setRepeat(_repeat)
        setControls(_controls)
        setPaused(_paused)
        setAllowsExternalPlayback(_allowsExternalPlayback)
    }

    @objc
    func setRepeat(_ repeat: Bool) {
        _repeat = `repeat`
    }

    @objc
    func setSelectedAudioTrack(_ selectedAudioTrack: NSDictionary?) {
        setSelectedAudioTrack(SelectedTrackCriteria(selectedAudioTrack))
    }

    func setSelectedAudioTrack(_ selectedAudioTrack: SelectedTrackCriteria?) {
        _selectedAudioTrackCriteria = selectedAudioTrack
        Task {
            await RCTPlayerOperations.setMediaSelectionTrackForCharacteristic(player: _player, characteristic: AVMediaCharacteristic.audible,
                                                                              criteria: _selectedAudioTrackCriteria)
        }
    }

    @objc
    func setSelectedTextTrack(_ selectedTextTrack: NSDictionary?) {
        setSelectedTextTrack(SelectedTrackCriteria(selectedTextTrack))
    }

    func setSelectedTextTrack(_ selectedTextTrack: SelectedTrackCriteria?) {
        _selectedTextTrackCriteria = selectedTextTrack
        if _textTracks != nil { // sideloaded text tracks
            RCTPlayerOperations.setSideloadedText(player: _player, textTracks: _textTracks!, criteria: _selectedTextTrackCriteria)
        } else { // text tracks included in the HLS playlist§
            Task {
                await RCTPlayerOperations.setMediaSelectionTrackForCharacteristic(player: _player, characteristic: AVMediaCharacteristic.legible,
                                                                                  criteria: _selectedTextTrackCriteria)
            }
        }
    }

    @objc
    func setTextTracks(_ textTracks: [NSDictionary]?) {
        setTextTracks(textTracks?.map { TextTrack($0) })
    }

    func setTextTracks(_ textTracks: [TextTrack]?) {
        _textTracks = textTracks

        // in case textTracks was set after selectedTextTrack
        if _selectedTextTrackCriteria != nil { setSelectedTextTrack(_selectedTextTrackCriteria) }
    }

    @objc
    func setChapters(_ chapters: [NSDictionary]?) {
        setChapters(chapters?.map { Chapter($0) })
    }

    func setChapters(_ chapters: [Chapter]?) {
        _chapters = chapters
    }

    @objc
    func setFullscreen(_ fullscreen: Bool) {
        if fullscreen && !_fullscreenPlayerPresented && _player != nil {
            // Ensure player view controller is not null
            // Controls will be displayed even if it is disabled in configuration
            if _playerViewController == nil {
                self.usePlayerViewController()
            }

            // Set presentation style to fullscreen
            _playerViewController?.modalPresentationStyle = .fullScreen

            // Find the nearest view controller
            var viewController: UIViewController! = self.firstAvailableUIViewController()
            if viewController == nil {
                guard let keyWindow = RCTVideoUtils.getCurrentWindow() else { return }

                viewController = keyWindow.rootViewController
                if !viewController.children.isEmpty {
                    viewController = viewController.children.last
                }
            }
            if viewController != nil {
                _presentingViewController = viewController

                self.onVideoFullscreenPlayerWillPresent?(["target": reactTag as Any])

                if let playerViewController = _playerViewController {
                    if _controls {
                        // prevents crash https://github.com/TheWidlarzGroup/react-native-video/issues/3040
                        self._playerViewController?.removeFromParent()
                    }

                    viewController.present(playerViewController, animated: true, completion: { [weak self] in
                        guard let self else { return }
                        // In fullscreen we must display controls
                        self._playerViewController?.showsPlaybackControls = true
                        self._fullscreenPlayerPresented = fullscreen
                        self._playerViewController?.autorotate = self._fullscreenAutorotate

                        self.onVideoFullscreenPlayerDidPresent?(["target": self.reactTag])
                    })
                }
            }
        } else if !fullscreen && _fullscreenPlayerPresented, let _playerViewController {
            self.videoPlayerViewControllerWillDismiss(playerViewController: _playerViewController)
            _presentingViewController?.dismiss(animated: true, completion: { [weak self] in
                self?.videoPlayerViewControllerDidDismiss(playerViewController: _playerViewController)
            })
        }
    }

    @objc
    func setFullscreenAutorotate(_ autorotate: Bool) {
        _fullscreenAutorotate = autorotate
        if _fullscreenPlayerPresented {
            _playerViewController?.autorotate = autorotate
        }
    }

    @objc
    func setFullscreenOrientation(_ orientation: String?) {
        _fullscreenOrientation = orientation
        if _fullscreenPlayerPresented {
            _playerViewController?.preferredOrientation = orientation
        }
    }

    func usePlayerViewController() {
        guard let _player, let _playerItem else { return }

        if _playerViewController == nil {
            _playerViewController = createPlayerViewController(player: _player, withPlayerItem: _playerItem)
        }
        // to prevent video from being animated when resizeMode is 'cover'
        // resize mode must be set before subview is added
        setResizeMode(_resizeMode)

        guard let _playerViewController else { return }

        if _controls {
            let viewController: UIViewController! = self.reactViewController()
            viewController?.addChild(_playerViewController)
            self.addSubview(_playerViewController.view)
        }

        _playerObserver.playerViewController = _playerViewController
    }

    func createPlayerViewController(player: AVPlayer, withPlayerItem _: AVPlayerItem) -> RCTVideoPlayerViewController {
        let viewController = RCTVideoPlayerViewController()
        viewController.showsPlaybackControls = self._controls
        #if !os(tvOS)
            viewController.updatesNowPlayingInfoCenter = false
        #endif
        viewController.rctDelegate = self
        viewController.preferredOrientation = _fullscreenOrientation

        viewController.view.frame = self.bounds
        viewController.player = player
        if #available(tvOS 14.0, *) {
            viewController.allowsPictureInPicturePlayback = _enterPictureInPictureOnLeave
        }
        #if os(iOS)
            viewController.allowsPictureInPicturePlayback = _enterPictureInPictureOnLeave
        #endif
        return viewController
    }

    func usePlayerLayer() {
        if let _player {
            _playerLayer = AVPlayerLayer(player: _player)
            _playerLayer?.frame = self.bounds
            _playerLayer?.needsDisplayOnBoundsChange = true

            // to prevent video from being animated when resizeMode is 'cover'
            // resize mode must be set before layer is added
            setResizeMode(_resizeMode)
            _playerObserver.playerLayer = _playerLayer

            if let _playerLayer {
                self.layer.addSublayer(_playerLayer)
            }
            self.layer.needsDisplayOnBoundsChange = true
            #if os(iOS)
                if _enterPictureInPictureOnLeave {
                    _pip?.setupPipController(_playerLayer)
                }
            #endif
        }
    }

    @objc
    func setControls(_ controls: Bool) {
        if _controls != controls || ((_playerLayer == nil) && (_playerViewController == nil)) {
            _controls = controls
            if _controls {
                self.removePlayerLayer()
                self.usePlayerViewController()
            } else {
                _playerViewController?.view.removeFromSuperview()
                _playerViewController?.removeFromParent()
                _playerViewController = nil
                _playerObserver.playerViewController = nil
                self.usePlayerLayer()
            }
        }
    }

    @objc
    func setShowNotificationControls(_ showNotificationControls: Bool) {
        guard let player = _player else {
            return
        }

        if showNotificationControls {
            NowPlayingInfoCenterManager.shared.registerPlayer(player: player)
        } else {
            NowPlayingInfoCenterManager.shared.removePlayer(player: player)
        }

        _showNotificationControls = showNotificationControls
    }

    @objc
    func setProgressUpdateInterval(_ progressUpdateInterval: Float) {
        _playerObserver.replaceTimeObserverIfSet(Float64(progressUpdateInterval))
    }

    func removePlayerLayer() {
        _playerLayer?.removeFromSuperlayer()
        _playerLayer = nil
        _playerObserver.playerLayer = nil
    }

    @objc
    func setSubtitleStyle(_ style: [String: Any]) {
        let subtitleStyle = SubtitleStyle.parse(from: style)
        _playerObserver.subtitleStyle = subtitleStyle
    }

    // MARK: - RCTVideoPlayerViewControllerDelegate

    func videoPlayerViewControllerWillDismiss(playerViewController: AVPlayerViewController) {
        if _playerViewController == playerViewController
            && _fullscreenPlayerPresented,
            let onVideoFullscreenPlayerWillDismiss {
            _playerObserver.removePlayerViewControllerObservers()
            onVideoFullscreenPlayerWillDismiss(["target": reactTag as Any])
        }
    }

    func videoPlayerViewControllerDidDismiss(playerViewController: AVPlayerViewController) {
        if _playerViewController == playerViewController && _fullscreenPlayerPresented {
            _fullscreenPlayerPresented = false
            _presentingViewController = nil
            _playerViewController = nil
            _playerObserver.playerViewController = nil
            self.applyModifiers()

            onVideoFullscreenPlayerDidDismiss?(["target": reactTag as Any])
        }
    }

    @objc
    func setFilter(_ filterName: String!) {
        _filterName = filterName

        if !_filterEnabled {
            return
        } else if let uri = _source?.uri, uri.contains("m3u8") {
            return // filters don't work for HLS... return
        } else if _playerItem?.asset == nil {
            return
        }

        let filter: CIFilter! = CIFilter(name: filterName)
        Task {
            let composition = await RCTVideoUtils.generateVideoComposition(asset: _playerItem!.asset, filter: filter)
            self._playerItem?.videoComposition = composition
        }
    }

    @objc
    func setFilterEnabled(_ filterEnabled: Bool) {
        _filterEnabled = filterEnabled
    }

    // MARK: - RCTIMAAdsManager

    func getAdTagUrl() -> String? {
        return _adTagUrl
    }

    @objc
    func setAdTagUrl(_ adTagUrl: String!) {
        _adTagUrl = adTagUrl
    }

    #if USE_GOOGLE_IMA
        func getContentPlayhead() -> IMAAVPlayerContentPlayhead? {
            return _contentPlayhead
        }
    #endif
    func setAdPlaying(_ adPlaying: Bool) {
        _adPlaying = adPlaying
    }

    // MARK: - React View Management

    func insertReactSubview(view: UIView!, atIndex: Int) {
        if _controls {
            view.frame = self.bounds
            _playerViewController?.contentOverlayView?.insertSubview(view, at: atIndex)
        } else {
            RCTLogError("video cannot have any subviews")
        }
        return
    }

    func removeReactSubview(subview: UIView!) {
        if _controls {
            subview.removeFromSuperview()
        } else {
            RCTLog("video cannot have any subviews")
        }
        return
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        if _controls, let _playerViewController {
            _playerViewController.view.frame = bounds

            // also adjust all subviews of contentOverlayView
            for subview in _playerViewController.contentOverlayView?.subviews ?? [] {
                subview.frame = bounds
            }
        } else {
            CATransaction.begin()
            CATransaction.setAnimationDuration(0)
            _playerLayer?.frame = bounds
            CATransaction.commit()
        }
    }

    // MARK: - Lifecycle

    override func removeFromSuperview() {
        self._player?.replaceCurrentItem(with: nil)
        if let player = _player {
            player.pause()
            NowPlayingInfoCenterManager.shared.removePlayer(player: player)
        }
        _playerItem = nil
        _source = nil
        _chapters = nil
        _drm = nil
        _textTracks = nil
        _selectedTextTrackCriteria = nil
        _selectedAudioTrackCriteria = nil
        _presentingViewController = nil

        _player = nil
        _resouceLoaderDelegate = nil
        _playerObserver.clearPlayer()

        #if USE_GOOGLE_IMA
            _imaAdsManager.releaseAds()
            _imaAdsManager = nil
        #endif

        self.removePlayerLayer()

        if let _playerViewController {
            _playerViewController.view.removeFromSuperview()
            _playerViewController.removeFromParent()
            _playerViewController.rctDelegate = nil
            _playerViewController.player = nil
            self._playerViewController = nil
            _playerObserver.playerViewController = nil
        }

        _eventDispatcher = nil
        // swiftlint:disable:next notification_center_detachment
        NotificationCenter.default.removeObserver(self)

        super.removeFromSuperview()
    }

    // MARK: - Export

    @objc
    func save(options: NSDictionary!, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        RCTVideoSave.save(
            options: options,
            resolve: resolve,
            reject: reject,
            playerItem: _playerItem
        )
    }

    func setLicenseResult(_ license: String!, _ licenseUrl: String!) {
        _resouceLoaderDelegate?.setLicenseResult(license, licenseUrl)
    }

    func setLicenseResultError(_ error: String!, _ licenseUrl: String!) {
        _resouceLoaderDelegate?.setLicenseResultError(error, licenseUrl)
    }

    func dismissFullscreenPlayer() {
        setFullscreen(false)
    }

    func presentFullscreenPlayer() {
        setFullscreen(true)
    }

    // MARK: - RCTPlayerObserverHandler

    func handleTimeUpdate(time _: CMTime) {
        sendProgressUpdate()
    }

    func handleReadyForDisplay(changeObject _: Any, change _: NSKeyValueObservedChange<Bool>) {
        if _isBuffering {
            _isBuffering = false
        }
        onReadyForDisplay?([
            "target": reactTag,
        ])
    }

    // When timeMetadata is read the event onTimedMetadata is triggered
    func handleTimeMetadataChange(timedMetadata: [AVMetadataItem]) {
        guard onTimedMetadata != nil else { return }

        var metadata: [[String: String?]?] = []
        for item in timedMetadata {
            let value = item.value as? String
            let identifier = item.identifier?.rawValue

            if let value {
                metadata.append(["value": value, "identifier": identifier])
            }
        }

        onTimedMetadata?([
            "target": reactTag,
            "metadata": metadata,
        ])
    }

    // Handle player item status change.
    func handlePlayerItemStatusChange(playerItem _: AVPlayerItem, change _: NSKeyValueObservedChange<AVPlayerItem.Status>) {
        guard let _playerItem else {
            return
        }

        if _playerItem.status == .readyToPlay {
            handleReadyToPlay()
        } else if _playerItem.status == .failed {
            handlePlaybackFailed()
        }
    }

    func handleReadyToPlay() {
        guard let _playerItem else { return }

        Task {
            if self._pendingSeek {
                self.setSeek([
                    "time": NSNumber(value: self._pendingSeekTime),
                    "tolerance": NSNumber(value: 100),
                ])
                self._pendingSeek = false
            }

            if self._startPosition >= 0 {
                self.setSeek([
                    "time": NSNumber(value: self._startPosition),
                    "tolerance": NSNumber(value: 100),
                ])
                self._startPosition = -1
            }

            if onVideoLoad != nil, self._videoLoadStarted {
                var duration = Float(CMTimeGetSeconds(_playerItem.asset.duration))

                if duration.isNaN || duration == 0 {
                    // This is a safety check for live video.
                    // AVPlayer report a 0 duration
                    duration = RCTVideoUtils.calculateSeekableDuration(_player).floatValue
                    if duration.isNaN {
                        duration = 0
                    }
                }

                var width: Float = 0
                var height: Float = 0
                var orientation = "undefined"

                let tracks = await RCTVideoAssetsUtils.getTracks(asset: _playerItem.asset, withMediaType: .video)
                var presentationSize = _playerItem.presentationSize
                if presentationSize.height != 0.0 {
                    width = Float(presentationSize.width)
                    height = Float(presentationSize.height)
                } else if let videoTrack = tracks?.first {
                    let naturalSize = videoTrack.naturalSize
                    width = Float(naturalSize.width)
                    height = Float(naturalSize.height)
                }
                orientation = width > height ? "landscape" : width == height ? "square" : "portrait"

                let audioTracks = await RCTVideoUtils.getAudioTrackInfo(self._player)
                let textTracks = await RCTVideoUtils.getTextTrackInfo(self._player)
                self.onVideoLoad?(["duration": NSNumber(value: duration),
                                   "currentTime": NSNumber(value: Float(CMTimeGetSeconds(_playerItem.currentTime()))),
                                   "canPlayReverse": NSNumber(value: _playerItem.canPlayReverse),
                                   "canPlayFastForward": NSNumber(value: _playerItem.canPlayFastForward),
                                   "canPlaySlowForward": NSNumber(value: _playerItem.canPlaySlowForward),
                                   "canPlaySlowReverse": NSNumber(value: _playerItem.canPlaySlowReverse),
                                   "canStepBackward": NSNumber(value: _playerItem.canStepBackward),
                                   "canStepForward": NSNumber(value: _playerItem.canStepForward),
                                   "naturalSize": [
                                       "width": width,
                                       "height": height,
                                       "orientation": orientation,
                                   ],
                                   "audioTracks": audioTracks,
                                   "textTracks": self._textTracks?.compactMap { $0.json } ?? textTracks.map(\.json),
                                   "target": self.reactTag as Any])
            }

            self._videoLoadStarted = false
            self._playerObserver.attachPlayerEventListeners()
            self.applyModifiers()
        }
    }

    func handlePlaybackFailed() {
        if let player = _player {
            NowPlayingInfoCenterManager.shared.removePlayer(player: player)
        }

        guard let _playerItem else { return }
        onVideoError?(
            [
                "error": [
                    "code": NSNumber(value: (_playerItem.error! as NSError).code),
                    "localizedDescription": _playerItem.error?.localizedDescription == nil ? "" : _playerItem.error?.localizedDescription,
                    "localizedFailureReason": ((_playerItem.error! as NSError).localizedFailureReason == nil ?
                        "" : (_playerItem.error! as NSError).localizedFailureReason) ?? "",
                    "localizedRecoverySuggestion": ((_playerItem.error! as NSError).localizedRecoverySuggestion == nil ?
                        "" : (_playerItem.error! as NSError).localizedRecoverySuggestion) ?? "",
                    "domain": (_playerItem.error as! NSError).domain,
                ],
                "target": reactTag,
            ]
        )
    }

    func handlePlaybackBufferKeyEmpty(playerItem _: AVPlayerItem, change _: NSKeyValueObservedChange<Bool>) {
        if !_isBuffering {
            _isBuffering = true
        }
    }

    // Continue playing (or not if paused) after being paused due to hitting an unbuffered zone.
    func handlePlaybackLikelyToKeepUp(playerItem _: AVPlayerItem, change _: NSKeyValueObservedChange<Bool>) {
        if _isBuffering {
            _isBuffering = false
        }
    }

    func handleTimeControlStatusChange(player: AVPlayer, change: NSKeyValueObservedChange<AVPlayer.TimeControlStatus>) {
        if player.timeControlStatus == change.oldValue && change.oldValue != nil {
            return
        }
        guard [.paused, .playing].contains(player.timeControlStatus) else {
            return
        }
        let isPlaying = player.timeControlStatus == .playing

        guard _isPlaying != isPlaying else { return }
        _isPlaying = isPlaying
        onVideoPlaybackStateChanged?(["isPlaying": isPlaying, "target": reactTag as Any])
    }

    func handlePlaybackRateChange(player: AVPlayer, change: NSKeyValueObservedChange<Float>) {
        guard let _player else { return }

        if player.rate == change.oldValue && change.oldValue != nil {
            return
        }

        onPlaybackRateChange?(["playbackRate": NSNumber(value: _player.rate),
                               "target": reactTag as Any])

        if _playbackStalled && _player.rate > 0 {
            onPlaybackResume?(["playbackRate": NSNumber(value: _player.rate),
                               "target": reactTag as Any])
            _playbackStalled = false
        }
    }

    func handleVolumeChange(player: AVPlayer, change: NSKeyValueObservedChange<Float>) {
        guard let _player, onVolumeChange != nil else { return }

        if player.rate == change.oldValue && change.oldValue != nil {
            return
        }

        onVolumeChange?(["volume": NSNumber(value: _player.volume),
                         "target": reactTag as Any])
    }

    func handleExternalPlaybackActiveChange(player _: AVPlayer, change _: NSKeyValueObservedChange<Bool>) {
        #if !os(visionOS)
            guard let _player else { return }
            if !_playInBackground && UIApplication.shared.applicationState == .background {
                _playerLayer?.player = nil
                _playerViewController?.player = nil
            }
            guard onVideoExternalPlaybackChange != nil else { return }
            onVideoExternalPlaybackChange?(["isExternalPlaybackActive": NSNumber(value: _player.isExternalPlaybackActive),
                                            "target": reactTag as Any])
        #endif
    }

    func handleViewControllerOverlayViewFrameChange(overlayView _: UIView, change: NSKeyValueObservedChange<CGRect>) {
        let oldRect = change.oldValue
        let newRect = change.newValue

        guard let bounds = RCTVideoUtils.getCurrentWindow()?.bounds else { return }

        if !oldRect!.equalTo(newRect!) {
            // https://github.com/TheWidlarzGroup/react-native-video/issues/3085#issuecomment-1557293391
            if newRect!.equalTo(bounds) {
                RCTLog("in fullscreen")
                if !_fullscreenUncontrolPlayerPresented {
                    _fullscreenUncontrolPlayerPresented = true

                    self.onVideoFullscreenPlayerWillPresent?(["target": self.reactTag as Any])
                    self.onVideoFullscreenPlayerDidPresent?(["target": self.reactTag as Any])
                }
            } else {
                NSLog("not fullscreen")
                if _fullscreenUncontrolPlayerPresented {
                    _fullscreenUncontrolPlayerPresented = false

                    self.onVideoFullscreenPlayerWillDismiss?(["target": self.reactTag as Any])
                    self.onVideoFullscreenPlayerDidDismiss?(["target": self.reactTag as Any])
                }
            }

            self.reactViewController().view.frame = bounds
            self.reactViewController().view.setNeedsLayout()
        }
    }

    @objc
    func handleDidFailToFinishPlaying(notification: NSNotification!) {
        guard onVideoError != nil else { return }

        let error: NSError! = notification.userInfo?[AVPlayerItemFailedToPlayToEndTimeErrorKey] as? NSError
        onVideoError?(
            [
                "error": [
                    "code": NSNumber(value: (error as NSError).code),
                    "localizedDescription": error.localizedDescription ?? "",
                    "localizedFailureReason": (error as NSError).localizedFailureReason ?? "",
                    "localizedRecoverySuggestion": (error as NSError).localizedRecoverySuggestion ?? "",
                    "domain": (error as NSError).domain,
                ],
                "target": reactTag,
            ]
        )
    }

    @objc
    func handlePlaybackStalled(notification _: NSNotification!) {
        onPlaybackStalled?(["target": reactTag as Any])
        _playbackStalled = true
    }

    @objc
    func handlePlayerItemDidReachEnd(notification: NSNotification!) {
        sendProgressUpdate(didEnd: true)
        onVideoEnd?(["target": reactTag as Any])
        #if USE_GOOGLE_IMA
            if notification.object as? AVPlayerItem == _player?.currentItem {
                _imaAdsManager.getAdsLoader()?.contentComplete()
            }
        #endif
        if _repeat {
            let item: AVPlayerItem! = notification.object as? AVPlayerItem

            item.seek(
                to: _source?.cropStart != nil ? CMTime(value: _source!.cropStart!, timescale: 1000) : CMTime.zero,
                toleranceBefore: CMTime.zero,
                toleranceAfter: CMTime.zero,
                completionHandler: { [weak self] _ in
                    guard let self else { return }
                    self.applyModifiers()
                }
            )
        } else {
            _playerObserver.removePlayerTimeObserver()
        }
    }

    @objc
    func handleAVPlayerAccess(notification: NSNotification!) {
        guard onVideoBandwidthUpdate != nil else { return }

        guard let accessLog = (notification.object as? AVPlayerItem)?.accessLog() else {
            return
        }

        guard let lastEvent = accessLog.events.last else { return }
        onVideoBandwidthUpdate?(["bitrate": lastEvent.observedBitrate, "target": reactTag])
    }

    func handleTracksChange(playerItem _: AVPlayerItem, change _: NSKeyValueObservedChange<[AVPlayerItemTrack]>) {
        if onTextTracks != nil {
            Task {
                let textTracks = await RCTVideoUtils.getTextTrackInfo(self._player)
                self.onTextTracks?(["textTracks": self._textTracks?.compactMap { $0.json } ?? textTracks.compactMap(\.json)])
            }
        }

        if onAudioTracks != nil {
            Task {
                let audioTracks = await RCTVideoUtils.getAudioTrackInfo(self._player)
                self.onAudioTracks?(["audioTracks": audioTracks])
            }
        }
    }

    func handleLegibleOutput(strings: [NSAttributedString]) {
        guard onTextTrackDataChanged != nil else { return }

        if let subtitles = strings.first {
            self.onTextTrackDataChanged?(["subtitleTracks": subtitles.string])
        }
    }

    @objc
    func getCurrentPlaybackTime(_ resolve: @escaping RCTPromiseResolveBlock, _ reject: @escaping RCTPromiseRejectBlock) {
        if let player = _playerItem {
            let currentTime = RCTVideoUtils.getCurrentTime(playerItem: player)
            resolve(currentTime)
        } else {
            reject("PLAYER_NOT_AVAILABLE", "Player is not initialized.", nil)
        }
    }

    @objc
    func enterPictureInPicture() {
        if (_pip?.isInitialized ?? false) == false {
            initPictureinPicture()
            _playerViewController?.allowsPictureInPicturePlayback = true
        }
        _pip?.enterPictureInPicture()
    }

    @objc
    func exitPictureInPicture() {
        guard _pip?.isPictureInPictureActive ?? false else { return }

        _pip?.exitPictureInPicture()
        #if os(iOS)
            if _enterPictureInPictureOnLeave {
                initPictureinPicture()
                _playerViewController?.allowsPictureInPicturePlayback = true
            } else {
                _pip?.deinitPipController()
                _playerViewController?.allowsPictureInPicturePlayback = false
            }
        #endif
    }

    // Workaround for #3418 - https://github.com/TheWidlarzGroup/react-native-video/issues/3418#issuecomment-2043508862
    @objc
    func setOnClick(_: Any) {}
}
