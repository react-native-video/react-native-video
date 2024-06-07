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

    @objc(save:reactTag:resolver:rejecter:)
    func save(options: NSDictionary, reactTag: NSNumber, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        performOnVideoView(withReactTag: reactTag, callback: { videoView in
            videoView?.save(options: options, resolve: resolve, reject: reject)
        })
    }

    @objc(seek:reactTag:)
    func seek(info: NSDictionary, reactTag: NSNumber) {
        performOnVideoView(withReactTag: reactTag, callback: { videoView in
            videoView?.setSeek(info)
        })
    }

    @objc(setLicenseResult:licenseUrl:reactTag:)
    func setLicenseResult(license: NSString, licenseUrl: NSString, reactTag: NSNumber) {
        performOnVideoView(withReactTag: reactTag, callback: { videoView in
            videoView?.setLicenseResult(license as String, licenseUrl as String)
        })
    }

    @objc(setLicenseResultError:licenseUrl:reactTag:)
    func setLicenseResultError(error: NSString, licenseUrl: NSString, reactTag: NSNumber) {
        performOnVideoView(withReactTag: reactTag, callback: { videoView in
            videoView?.setLicenseResultError(error as String, licenseUrl as String)
        })
    }

    @objc(dismissFullscreenPlayer:)
    func dismissFullscreenPlayer(_ reactTag: NSNumber) {
        performOnVideoView(withReactTag: reactTag, callback: { videoView in
            videoView?.dismissFullscreenPlayer()
        })
    }

    @objc(presentFullscreenPlayer:)
    func presentFullscreenPlayer(_ reactTag: NSNumber) {
        performOnVideoView(withReactTag: reactTag, callback: { videoView in
            videoView?.presentFullscreenPlayer()
        })
    }

    @objc(setPlayerPauseState:reactTag:)
    func setPlayerPauseState(paused: NSNumber, reactTag: NSNumber) {
        performOnVideoView(withReactTag: reactTag, callback: { videoView in
            videoView?.setPaused(paused.boolValue)
        })
    }

    @objc(setVolume:reactTag:)
    func setVolume(value: Float, reactTag: NSNumber) {
        performOnVideoView(withReactTag: reactTag, callback: { videoView in
            videoView?.setVolume(value)
        })
    }

    @objc(getCurrentPosition:resolver:rejecter:)
    func getCurrentPosition(reactTag: NSNumber, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        performOnVideoView(withReactTag: reactTag, callback: { videoView in
            videoView?.getCurrentPlaybackTime(resolve, reject)
        })
    }

    @objc(enterPictureInPicture:)
    func enterPictureInPicture(_ reactTag: NSNumber) {
        performOnVideoView(withReactTag: reactTag, callback: { videoView in
            videoView?.enterPictureInPicture()
        })
    }

    @objc(exitPictureInPicture:)
    func exitPictureInPicture(_ reactTag: NSNumber) {
        performOnVideoView(withReactTag: reactTag, callback: { videoView in
            videoView?.exitPictureInPicture()
        })
    }

    override class func requiresMainQueueSetup() -> Bool {
        return true
    }
}
