import AVFoundation
import AVKit
import Foundation
import MediaAccessibility
import React

#if os(iOS)
    class RCTPictureInPicture: NSObject, AVPictureInPictureControllerDelegate {
        private var _onPictureInPictureEnter: (() -> Void)?
        private var _onPictureInPictureExit: (() -> Void)?
        private var _onRestoreUserInterfaceForPictureInPictureStop: (() -> Void)?
        private var _restoreUserInterfaceForPIPStopCompletionHandler: ((Bool) -> Void)?
        private var _pipController: AVPictureInPictureController?
        var isInitialized: Bool {
            return _pipController != nil
        }

        var isPictureInPictureActive: Bool {
            return _pipController?.isPictureInPictureActive ?? false
        }

        init(
            _ onPictureInPictureEnter: (() -> Void)? = nil,
            _ onPictureInPictureExit: (() -> Void)? = nil,
            _ onRestoreUserInterfaceForPictureInPictureStop: (() -> Void)? = nil
        ) {
            _onPictureInPictureEnter = onPictureInPictureEnter
            _onPictureInPictureExit = onPictureInPictureExit
            _onRestoreUserInterfaceForPictureInPictureStop = onRestoreUserInterfaceForPictureInPictureStop
        }

        func pictureInPictureControllerDidStartPictureInPicture(_: AVPictureInPictureController) {
            guard let _onPictureInPictureEnter else { return }

            _onPictureInPictureEnter()
        }

        func pictureInPictureControllerDidStopPictureInPicture(_: AVPictureInPictureController) {
            guard let _onPictureInPictureExit else { return }

            _onPictureInPictureExit()
        }

        func pictureInPictureController(
            _: AVPictureInPictureController,
            restoreUserInterfaceForPictureInPictureStopWithCompletionHandler completionHandler: @escaping (Bool) -> Void
        ) {
            guard let _onRestoreUserInterfaceForPictureInPictureStop else { return }

            _onRestoreUserInterfaceForPictureInPictureStop()

            _restoreUserInterfaceForPIPStopCompletionHandler = completionHandler
        }

        func setRestoreUserInterfaceForPIPStopCompletionHandler(_ restore: Bool) {
            guard let _restoreUserInterfaceForPIPStopCompletionHandler else { return }
            _restoreUserInterfaceForPIPStopCompletionHandler(restore)
            self._restoreUserInterfaceForPIPStopCompletionHandler = nil
        }

        func setupPipController(_ playerLayer: AVPlayerLayer?) {
            guard let playerLayer else { return }
            if !AVPictureInPictureController.isPictureInPictureSupported() { return }
            // Create new controller passing reference to the AVPlayerLayer
            _pipController = AVPictureInPictureController(playerLayer: playerLayer)
            if #available(iOS 14.2, *) {
                _pipController?.canStartPictureInPictureAutomaticallyFromInline = true
            }
            _pipController?.delegate = self
        }

        func deinitPipController() {
            _pipController = nil
        }

        func enterPictureInPicture() {
            guard let _pipController else { return }
            if !isPictureInPictureActive {
                _pipController.startPictureInPicture()
            }
        }

        func exitPictureInPicture() {
            guard let _pipController else { return }
            if isPictureInPictureActive {
                let state = UIApplication.shared.applicationState
                if state == .background || state == .inactive {
                    deinitPipController()
                    _onPictureInPictureExit?()
                    _onRestoreUserInterfaceForPictureInPictureStop?()
                } else {
                    _pipController.stopPictureInPicture()
                }
            }
        }
    }
#endif
