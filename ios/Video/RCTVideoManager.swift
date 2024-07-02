import AVFoundation
import React

@objc(RCTVideoManager)
class RCTVideoManager: RCTViewManager {
    override func view() -> UIView {
        return RCTVideo(eventDispatcher: (RCTBridge.current().eventDispatcher() as! RCTEventDispatcher))
    }

    func methodQueue() -> DispatchQueue {
        return bridge.uiManager.methodQueue
    }

    func performOnVideoView(withReactTag reactTag: NSNumber, callback: @escaping (RCTVideo?) -> Void) {
        DispatchQueue.main.async { [weak self] in
            guard let self else {
                callback(nil)
                return
            }

            let view = self.bridge.uiManager.view(forReactTag: reactTag)

            guard let videoView = view as? RCTVideo else {
                DebugLog("Invalid view returned from registry, expecting RCTVideo, got: \(String(describing: self.view))")
                callback(nil)
                return
            }

            callback(videoView)
        }
    }

    @objc(seek:time:tolerance:)
    func seek(_ reactTag: NSNumber, time: NSNumber, tolerance: NSNumber) {
        performOnVideoView(withReactTag: reactTag, callback: { videoView in
            videoView?.setSeek(time, tolerance)
        })
    }

    @objc(setLicenseResult:license:licenseUrl:)
    func setLicenseResult(_ reactTag: NSNumber, license: NSString, licenseUrl: NSString) {
        performOnVideoView(withReactTag: reactTag, callback: { videoView in
            videoView?.setLicenseResult(license as String, licenseUrl as String)
        })
    }

    @objc(setLicenseResultError:error:licenseUrl:)
    func setLicenseResultError(_ reactTag: NSNumber, error: NSString, licenseUrl: NSString) {
        performOnVideoView(withReactTag: reactTag, callback: { videoView in
            videoView?.setLicenseResultError(error as String, licenseUrl as String)
        })
    }

    @objc(setPlayerPauseState:paused:)
    func setPlayerPauseState(_ reactTag: NSNumber, paused: Bool) {
        performOnVideoView(withReactTag: reactTag, callback: { videoView in
            videoView?.setPaused(paused)
        })
    }

    @objc(setVolumeCMD:volume:)
    func setVolumeCMD(_ reactTag: NSNumber, volume: Float) {
        performOnVideoView(withReactTag: reactTag, callback: { videoView in
            videoView?.setVolume(volume)
        })
    }

    @objc(setFullScreen:fullscreen:)
    func setFullScreen(_ reactTag: NSNumber, fullScreen: Bool) {
        performOnVideoView(withReactTag: reactTag, callback: { videoView in
            videoView?.setFullscreen(fullScreen)
        })
    }

    @objc(save:options:resolve:reject:)
    func save(_ reactTag: NSNumber, options: NSDictionary, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        performOnVideoView(withReactTag: reactTag, callback: { videoView in
            videoView?.save(options, resolve, reject)
        })
    }

    @objc(getCurrentPosition:resolve:reject:)
    func getCurrentPosition(_ reactTag: NSNumber, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        performOnVideoView(withReactTag: reactTag, callback: { videoView in
            videoView?.getCurrentPlaybackTime(resolve, reject)
        })
    }

    override class func requiresMainQueueSetup() -> Bool {
        return true
    }
}
