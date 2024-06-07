#if USE_GOOGLE_IMA
    import Foundation
    import GoogleInteractiveMediaAds

    class RCTIMAAdsManager: NSObject, IMAAdsLoaderDelegate, IMAAdsManagerDelegate, IMALinkOpenerDelegate {
        private weak var _video: RCTVideo?
        private var _isPictureInPictureActive: () -> Bool

        /* Entry point for the SDK. Used to make ad requests. */
        private var adsLoader: IMAAdsLoader!
        /* Main point of interaction with the SDK. Created by the SDK as the result of an ad request. */
        private var adsManager: IMAAdsManager!

        init(video: RCTVideo!, isPictureInPictureActive: @escaping () -> Bool) {
            _video = video
            _isPictureInPictureActive = isPictureInPictureActive

            super.init()
        }

        func setUpAdsLoader() {
            adsLoader = IMAAdsLoader(settings: nil)
            adsLoader.delegate = self
        }

        func requestAds() {
            guard let _video else { return }
            // Create ad display container for ad rendering.
            let adDisplayContainer = IMAAdDisplayContainer(adContainer: _video, viewController: _video.reactViewController())

            let adTagUrl = _video.getAdTagUrl()
            let contentPlayhead = _video.getContentPlayhead()

            if adTagUrl != nil && contentPlayhead != nil {
                // Create an ad request with our ad tag, display container, and optional user context.
                let request = IMAAdsRequest(
                    adTagUrl: adTagUrl!,
                    adDisplayContainer: adDisplayContainer,
                    contentPlayhead: contentPlayhead,
                    userContext: nil
                )

                adsLoader.requestAds(with: request)
            }
        }

        func releaseAds() {
            guard let adsManager else { return }
            // Destroy AdsManager may be delayed for a few milliseconds
            // But what we want is it stopped producing sound immediately
            // Issue found on tvOS 17, or iOS if view detach & STARTED event happen at the same moment
            adsManager.volume = 0
            adsManager.pause()
            adsManager.destroy()
        }

        // MARK: - Getters

        func getAdsLoader() -> IMAAdsLoader? {
            return adsLoader
        }

        func getAdsManager() -> IMAAdsManager? {
            return adsManager
        }

        // MARK: - IMAAdsLoaderDelegate

        func adsLoader(_: IMAAdsLoader, adsLoadedWith adsLoadedData: IMAAdsLoadedData) {
            guard let _video else { return }
            // Grab the instance of the IMAAdsManager and set yourself as the delegate.
            adsManager = adsLoadedData.adsManager
            adsManager?.delegate = self

            // Create ads rendering settings and tell the SDK to use the in-app browser.
            let adsRenderingSettings = IMAAdsRenderingSettings()
            adsRenderingSettings.linkOpenerDelegate = self
            adsRenderingSettings.linkOpenerPresentingController = _video.reactViewController()

            adsManager.initialize(with: adsRenderingSettings)
        }

        func adsLoader(_: IMAAdsLoader, failedWith adErrorData: IMAAdLoadingErrorData) {
            if adErrorData.adError.message != nil {
                print("Error loading ads: " + adErrorData.adError.message!)
            }

            _video?.setPaused(false)
        }

        // MARK: - IMAAdsManagerDelegate

        func adsManager(_ adsManager: IMAAdsManager, didReceive event: IMAAdEvent) {
            guard let _video else { return }
            // Mute ad if the main player is muted
            if _video.isMuted() {
                adsManager.volume = 0
            }
            // Play each ad once it has been loaded
            if event.type == IMAAdEventType.LOADED {
                if _isPictureInPictureActive() {
                    return
                }
                adsManager.start()
            }

            if _video.onReceiveAdEvent != nil {
                let type = convertEventToString(event: event.type)

                if event.adData != nil {
                    _video.onReceiveAdEvent?([
                        "event": type,
                        "data": event.adData ?? [String](),
                        "target": _video.reactTag!,
                    ])
                } else {
                    _video.onReceiveAdEvent?([
                        "event": type,
                        "target": _video.reactTag!,
                    ])
                }
            }
        }

        func adsManager(_: IMAAdsManager, didReceive error: IMAAdError) {
            if error.message != nil {
                print("AdsManager error: " + error.message!)
            }

            guard let _video else { return }

            if _video.onReceiveAdEvent != nil {
                _video.onReceiveAdEvent?([
                    "event": "ERROR",
                    "data": [
                        "message": error.message ?? "",
                        "code": error.code,
                        "type": error.type,
                    ],
                    "target": _video.reactTag!,
                ])
            }

            // Fall back to playing content
            _video.setPaused(false)
        }

        func adsManagerDidRequestContentPause(_: IMAAdsManager) {
            // Pause the content for the SDK to play ads.
            _video?.setPaused(true)
            _video?.setAdPlaying(true)
        }

        func adsManagerDidRequestContentResume(_: IMAAdsManager) {
            // Resume the content since the SDK is done playing ads (at least for now).
            _video?.setAdPlaying(false)
            _video?.setPaused(false)
        }

        // MARK: - IMALinkOpenerDelegate

        func linkOpenerDidClose(inAppLink _: NSObject) {
            adsManager?.resume()
        }

        // MARK: - Helpers

        func convertEventToString(event: IMAAdEventType!) -> String {
            var result = "UNKNOWN"

            switch event {
            case .AD_BREAK_READY:
                result = "AD_BREAK_READY"
            case .AD_BREAK_ENDED:
                result = "AD_BREAK_ENDED"
            case .AD_BREAK_STARTED:
                result = "AD_BREAK_STARTED"
            case .AD_PERIOD_ENDED:
                result = "AD_PERIOD_ENDED"
            case .AD_PERIOD_STARTED:
                result = "AD_PERIOD_STARTED"
            case .ALL_ADS_COMPLETED:
                result = "ALL_ADS_COMPLETED"
            case .CLICKED:
                result = "CLICK"
            case .COMPLETE:
                result = "COMPLETED"
            case .CUEPOINTS_CHANGED:
                result = "CUEPOINTS_CHANGED"
            case .FIRST_QUARTILE:
                result = "FIRST_QUARTILE"
            case .LOADED:
                result = "LOADED"
            case .LOG:
                result = "LOG"
            case .MIDPOINT:
                result = "MIDPOINT"
            case .PAUSE:
                result = "PAUSED"
            case .RESUME:
                result = "RESUMED"
            case .SKIPPED:
                result = "SKIPPED"
            case .STARTED:
                result = "STARTED"
            case .STREAM_LOADED:
                result = "STREAM_LOADED"
            case .TAPPED:
                result = "TAPPED"
            case .THIRD_QUARTILE:
                result = "THIRD_QUARTILE"
            default:
                result = "UNKNOWN"
            }

            return result
        }
    }
#endif
