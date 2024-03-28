import AVFoundation
import AVKit
import Foundation

// MARK: - RCTPlayerObserverHandlerObjc

@objc
protocol RCTPlayerObserverHandlerObjc {
    func handleDidFailToFinishPlaying(notification: NSNotification!)
    func handlePlaybackStalled(notification: NSNotification!)
    func handlePlayerItemDidReachEnd(notification: NSNotification!)
    func handleAVPlayerAccess(notification: NSNotification!)
}

// MARK: - RCTPlayerObserverHandler

protocol RCTPlayerObserverHandler: RCTPlayerObserverHandlerObjc {
    func handleTimeUpdate(time: CMTime)
    func handleReadyForDisplay(changeObject: Any, change: NSKeyValueObservedChange<Bool>)
    func handleTimeMetadataChange(timedMetadata: [AVMetadataItem])
    func handlePlayerItemStatusChange(playerItem: AVPlayerItem, change: NSKeyValueObservedChange<AVPlayerItem.Status>)
    func handlePlaybackBufferKeyEmpty(playerItem: AVPlayerItem, change: NSKeyValueObservedChange<Bool>)
    func handlePlaybackLikelyToKeepUp(playerItem: AVPlayerItem, change: NSKeyValueObservedChange<Bool>)
    func handlePlaybackRateChange(player: AVPlayer, change: NSKeyValueObservedChange<Float>)
    func handleVolumeChange(player: AVPlayer, change: NSKeyValueObservedChange<Float>)
    func handleExternalPlaybackActiveChange(player: AVPlayer, change: NSKeyValueObservedChange<Bool>)
    func handleViewControllerOverlayViewFrameChange(overlayView: UIView, change: NSKeyValueObservedChange<CGRect>)
    func handleTracksChange(playerItem: AVPlayerItem, change: NSKeyValueObservedChange<[AVPlayerItemTrack]>)
    func handleLegibleOutput(strings: [NSAttributedString])
    func handlePictureInPictureEnter()
    func handlePictureInPictureExit()
    func handleRestoreUserInterfaceForPictureInPictureStop()
}

// MARK: - RCTPlayerObserver

class RCTPlayerObserver: NSObject, AVPlayerItemMetadataOutputPushDelegate, AVPlayerItemLegibleOutputPushDelegate, AVPlayerViewControllerDelegate {
    weak var _handlers: RCTPlayerObserverHandler?

    var player: AVPlayer? {
        willSet {
            removePlayerObservers()
            removePlayerTimeObserver()
        }
        didSet {
            if player != nil {
                addPlayerObservers()
                addPlayerTimeObserver()
            }
        }
    }

    var subtitleStyle: SubtitleStyle?

    var playerItem: AVPlayerItem? {
        willSet {
            removePlayerItemObservers()
        }
        didSet {
            guard let playerItem else { return }

            addPlayerItemObservers()

            // handle timedMetadata
            let metadataOutput = AVPlayerItemMetadataOutput()
            let legibleOutput = AVPlayerItemLegibleOutput()
            playerItem.add(metadataOutput)
            playerItem.add(legibleOutput)
            metadataOutput.setDelegate(self, queue: .main)
            legibleOutput.setDelegate(self, queue: .main)
            legibleOutput.suppressesPlayerRendering = subtitleStyle?.opacity == 0 ? true : false
        }
    }

    var playerViewController: AVPlayerViewController? {
        willSet {
            removePlayerViewControllerObservers()
        }
        didSet {
            if playerViewController != nil {
                addPlayerViewControllerObservers()
            }
        }
    }

    var playerLayer: AVPlayerLayer? {
        willSet {
            removePlayerLayerObserver()
        }
        didSet {
            if playerLayer != nil {
                addPlayerLayerObserver()
            }
        }
    }

    private var _progressUpdateInterval: TimeInterval = 250
    private var _timeObserver: Any?

    private var _playerRateChangeObserver: NSKeyValueObservation?
    private var _playerVolumeChangeObserver: NSKeyValueObservation?
    private var _playerExternalPlaybackActiveObserver: NSKeyValueObservation?
    private var _playerItemStatusObserver: NSKeyValueObservation?
    private var _playerPlaybackBufferEmptyObserver: NSKeyValueObservation?
    private var _playerPlaybackLikelyToKeepUpObserver: NSKeyValueObservation?
    private var _playerTimedMetadataObserver: NSKeyValueObservation?
    private var _playerViewControllerReadyForDisplayObserver: NSKeyValueObservation?
    private var _playerLayerReadyForDisplayObserver: NSKeyValueObservation?
    private var _playerViewControllerOverlayFrameObserver: NSKeyValueObservation?
    private var _playerTracksObserver: NSKeyValueObservation?
    private var _restoreUserInterfaceForPIPStopCompletionHandler: ((Bool) -> Void)?

    deinit {
        if let _handlers {
            NotificationCenter.default.removeObserver(_handlers)
        }
    }

    func metadataOutput(_: AVPlayerItemMetadataOutput, didOutputTimedMetadataGroups groups: [AVTimedMetadataGroup], from _: AVPlayerItemTrack?) {
        guard let _handlers else { return }

        for metadataGroup in groups {
            _handlers.handleTimeMetadataChange(timedMetadata: metadataGroup.items)
        }
    }

    func legibleOutput(_: AVPlayerItemLegibleOutput,
                       didOutputAttributedStrings strings: [NSAttributedString],
                       nativeSampleBuffers _: [Any],
                       forItemTime _: CMTime) {
        guard let _handlers else { return }
        _handlers.handleLegibleOutput(strings: strings)
    }

    func addPlayerObservers() {
        guard let player, let _handlers else {
            return
        }

        _playerRateChangeObserver = player.observe(\.rate, options: [.old], changeHandler: _handlers.handlePlaybackRateChange)
        _playerVolumeChangeObserver = player.observe(\.volume, options: [.old], changeHandler: _handlers.handleVolumeChange)
        #if !os(visionOS)
            _playerExternalPlaybackActiveObserver = player.observe(\.isExternalPlaybackActive, changeHandler: _handlers.handleExternalPlaybackActiveChange)
        #endif
    }

    func removePlayerObservers() {
        _playerRateChangeObserver?.invalidate()
        _playerExternalPlaybackActiveObserver?.invalidate()
        _playerVolumeChangeObserver?.invalidate()
    }

    func addPlayerItemObservers() {
        guard let playerItem, let _handlers else { return }
        _playerItemStatusObserver = playerItem.observe(\.status, options: [.new, .old], changeHandler: _handlers.handlePlayerItemStatusChange)
        _playerPlaybackBufferEmptyObserver = playerItem.observe(
            \.isPlaybackBufferEmpty,
            options: [.new, .old],
            changeHandler: _handlers.handlePlaybackBufferKeyEmpty
        )
        _playerPlaybackLikelyToKeepUpObserver = playerItem.observe(
            \.isPlaybackLikelyToKeepUp,
            options: [.new, .old],
            changeHandler: _handlers.handlePlaybackLikelyToKeepUp
        )

        // observe tracks update
        _playerTracksObserver = playerItem.observe(
            \.tracks,
            options: [.new, .old],
            changeHandler: _handlers.handleTracksChange
        )
    }

    func removePlayerItemObservers() {
        _playerItemStatusObserver?.invalidate()
        _playerPlaybackBufferEmptyObserver?.invalidate()
        _playerPlaybackLikelyToKeepUpObserver?.invalidate()
        _playerTimedMetadataObserver?.invalidate()
        _playerTracksObserver?.invalidate()
    }

    func addPlayerViewControllerObservers() {
        guard let playerViewController, let _handlers else { return }

        #if !os(visionOS)
            _playerViewControllerReadyForDisplayObserver = playerViewController.observe(
                \.isReadyForDisplay,
                options: [.new],
                changeHandler: _handlers.handleReadyForDisplay
            )
        #endif

        _playerViewControllerOverlayFrameObserver = playerViewController.contentOverlayView?.observe(
            \.frame,
            options: [.new, .old],
            changeHandler: _handlers.handleViewControllerOverlayViewFrameChange
        )

        playerViewController.delegate = self
    }

    func removePlayerViewControllerObservers() {
        _playerViewControllerReadyForDisplayObserver?.invalidate()
        _playerViewControllerOverlayFrameObserver?.invalidate()
        playerViewController?.delegate = nil
    }

    func addPlayerLayerObserver() {
        guard let _handlers else { return }
        _playerLayerReadyForDisplayObserver = playerLayer?.observe(\.isReadyForDisplay, options: [.new], changeHandler: _handlers.handleReadyForDisplay)
    }

    func removePlayerLayerObserver() {
        _playerLayerReadyForDisplayObserver?.invalidate()
    }

    func addPlayerTimeObserver() {
        guard let _handlers else { return }
        removePlayerTimeObserver()
        let progressUpdateIntervalMS: Float64 = _progressUpdateInterval / 1000
        // @see endScrubbing in AVPlayerDemoPlaybackViewController.m
        // of https://developer.apple.com/library/ios/samplecode/AVPlayerDemo/Introduction/Intro.html
        _timeObserver = player?.addPeriodicTimeObserver(
            forInterval: CMTimeMakeWithSeconds(progressUpdateIntervalMS, preferredTimescale: Int32(NSEC_PER_SEC)),
            queue: nil,
            using: _handlers.handleTimeUpdate
        )
    }

    /* Cancels the previously registered time observer. */
    func removePlayerTimeObserver() {
        if _timeObserver != nil {
            player?.removeTimeObserver(_timeObserver)
            _timeObserver = nil
        }
    }

    func addTimeObserverIfNotSet() {
        if _timeObserver == nil {
            addPlayerTimeObserver()
        }
    }

    func replaceTimeObserverIfSet(_ newUpdateInterval: Float64? = nil) {
        if let newUpdateInterval {
            _progressUpdateInterval = newUpdateInterval
        }
        if _timeObserver != nil {
            addPlayerTimeObserver()
        }
    }

    func attachPlayerEventListeners() {
        guard let _handlers else { return }
        NotificationCenter.default.removeObserver(_handlers,
                                                  name: NSNotification.Name.AVPlayerItemDidPlayToEndTime,
                                                  object: player?.currentItem)

        NotificationCenter.default.addObserver(_handlers,
                                               selector: #selector(RCTPlayerObserverHandler.handlePlayerItemDidReachEnd(notification:)),
                                               name: NSNotification.Name.AVPlayerItemDidPlayToEndTime,
                                               object: player?.currentItem)

        NotificationCenter.default.removeObserver(_handlers,
                                                  name: NSNotification.Name.AVPlayerItemPlaybackStalled,
                                                  object: nil)

        NotificationCenter.default.addObserver(_handlers,
                                               selector: #selector(RCTPlayerObserverHandler.handlePlaybackStalled(notification:)),
                                               name: NSNotification.Name.AVPlayerItemPlaybackStalled,
                                               object: nil)

        NotificationCenter.default.removeObserver(_handlers,
                                                  name: NSNotification.Name.AVPlayerItemFailedToPlayToEndTime,
                                                  object: nil)

        NotificationCenter.default.addObserver(_handlers,
                                               selector: #selector(RCTPlayerObserverHandler.handleDidFailToFinishPlaying(notification:)),
                                               name: NSNotification.Name.AVPlayerItemFailedToPlayToEndTime,
                                               object: nil)

        NotificationCenter.default.removeObserver(_handlers, name: NSNotification.Name.AVPlayerItemNewAccessLogEntry, object: player?.currentItem)

        NotificationCenter.default.addObserver(_handlers,
                                               selector: #selector(RCTPlayerObserverHandlerObjc.handleAVPlayerAccess(notification:)),
                                               name: NSNotification.Name.AVPlayerItemNewAccessLogEntry,
                                               object: player?.currentItem)
    }

    func clearPlayer() {
        player = nil
        playerItem = nil
        if let _handlers {
            NotificationCenter.default.removeObserver(_handlers)
        }
    }

    func playerViewControllerDidStartPictureInPicture(_: AVPlayerViewController) {
        guard let _handlers else { return }

        _handlers.handlePictureInPictureEnter()
    }

    func playerViewControllerDidStopPictureInPicture(_: AVPlayerViewController) {
        guard let _handlers else { return }

        _handlers.handlePictureInPictureExit()
    }

    func playerViewController(
        _: AVPlayerViewController,
        restoreUserInterfaceForPictureInPictureStopWithCompletionHandler completionHandler: @escaping (Bool) -> Void
    ) {
        guard let _handlers else { return }

        _handlers.handleRestoreUserInterfaceForPictureInPictureStop()

        _restoreUserInterfaceForPIPStopCompletionHandler = completionHandler
    }

    func setRestoreUserInterfaceForPIPStopCompletionHandler(_ restore: Bool) {
        guard let _restoreUserInterfaceForPIPStopCompletionHandler else { return }

        _restoreUserInterfaceForPIPStopCompletionHandler(restore)
        self._restoreUserInterfaceForPIPStopCompletionHandler = nil
    }
}
