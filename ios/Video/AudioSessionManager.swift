import AVFoundation
import Foundation

class AudioSessionManager {
    static let shared = AudioSessionManager()

    private var videoViews = NSHashTable<RCTVideo>.weakObjects()

    func registerView(view: RCTVideo) {
        if videoViews.contains(view) {
            return
        }

        videoViews.add(view)
    }

    func removePlayer(view: RCTVideo) {
        if !videoViews.contains(view) {
            return
        }

        videoViews.remove(view)

        if videoViews.allObjects.isEmpty {
            try? AVAudioSession().setActive(false)
        }
    }

    private func getCategory(silentSwitchObey: Bool, earpiece: Bool, pip: Bool, needsPlayback: Bool) -> AVAudioSession.Category {
        if needsPlayback {
            if earpiece {
                RCTLogWarn(
                    """
                    You can't set \"audioOutput\"=\"earpiece\" and \"ignoreSilentSwitch\"=\"obey\"
                    at the same time (in same or different components) - skipping those props
                    """
                )
                return .playback
            }

            if silentSwitchObey {
                RCTLogWarn(
                    """
                    You can't use \"playInBackground or \"notificationControls with \"ignoreSilentSwitch\"=\"obey\"
                    at the same time (in same or different components) - skipping \"ignoreSilentSwitch\" prop
                    """
                )
                return .playback
            }
        }

        if silentSwitchObey {
            if earpiece {
                RCTLogWarn(
                    """
                    You can't set \"audioOutput\"=\"earpiece\" and \"ignoreSilentSwitch\"=\"obey\"
                    at the same time (in same or different components) - skipping those props
                    """
                )
                return .playback
            }

            if pip {
                RCTLogWarn(
                    """
                    You use \"pictureInPicture\"=\"true\" and \"ignoreSilentSwitch\"=\"obey\"
                    at the same time (in same or different components) - skipping those props
                    """
                )
                return .playback
            }

            return .ambient
        }

        return earpiece ? .playAndRecord : .playback
    }

    func updateAudioSessionCategory() {
        let audioSession = AVAudioSession()
        var options: AVAudioSession.CategoryOptions = []

        let isAnyPlayerPlaying = videoViews.allObjects.contains { view in
            view._player?.isMuted == false || (view._player != nil && view._player?.rate != 0)
        }

        let anyPlayerShowNotificationControls = videoViews.allObjects.contains { view in
            view._showNotificationControls
        }

        let anyPlayerNeedsPiP = videoViews.allObjects.contains { view in
            view.isPipEnabled()
        }

        let anyPlayerNeedsBackgroundPlayback = videoViews.allObjects.contains { view in
            view._playInBackground
        }

        let canAllowMixing = !anyPlayerShowNotificationControls && !anyPlayerNeedsBackgroundPlayback

        if canAllowMixing {
            let shouldEnableMixing = videoViews.allObjects.contains { view in
                view._mixWithOthers == "mix"
            }

            let shouldEnableDucking = videoViews.allObjects.contains { view in
                view._mixWithOthers == "duck"
            }

            if shouldEnableMixing && shouldEnableDucking {
                RCTLogWarn("You are trying to set \"mixWithOthers\" to \"mix\" and \"duck\" at the same time (in different components) - skiping prop")
            } else {
                if shouldEnableMixing {
                    options.insert(.mixWithOthers)
                }

                if shouldEnableDucking {
                    options.insert(.duckOthers)
                }
            }
        }

        let isAnyPlayerUsingEarpiece = videoViews.allObjects.contains { view in
            view._audioOutput == "earpiece"
        }

        var isSilentSwitchIgnore = videoViews.allObjects.contains { view in
            view._ignoreSilentSwitch == "ignore"
        }

        var isSilentSwitchObey = videoViews.allObjects.contains { view in
            view._ignoreSilentSwitch == "obey"
        }

        if isSilentSwitchObey && isSilentSwitchIgnore {
            RCTLogWarn("You are trying to set \"ignoreSilentSwitch\" to \"ignore\" and \"obey\" at the same time (in diffrent components) - skiping prop")
            isSilentSwitchObey = false
            isSilentSwitchIgnore = false
        }

        let needUpdateCategory = isAnyPlayerUsingEarpiece || isSilentSwitchIgnore || isSilentSwitchObey || canAllowMixing

        if anyPlayerNeedsPiP || anyPlayerShowNotificationControls || needUpdateCategory {
            let category = getCategory(
                silentSwitchObey: isSilentSwitchObey,
                earpiece: isAnyPlayerUsingEarpiece,
                pip: anyPlayerNeedsPiP,
                needsPlayback: canAllowMixing
            )

            do {
                try audioSession.setCategory(category, mode: .moviePlayback, options: canAllowMixing ? options : [])
            } catch {
                RCTLogWarn("Failed to update audio session category. This can cause issue with background audio playback and PiP or notification controls")
            }
        }

        if isAnyPlayerPlaying {
            do {
                try audioSession.setActive(true)
            } catch {
                RCTLogWarn("Failed activate audio session. This can cause issue audio playback")
            }
        }

        if isAnyPlayerUsingEarpiece {
            do {
                if isAnyPlayerUsingEarpiece {
                    try AVAudioSession.sharedInstance().overrideOutputAudioPort(AVAudioSession.PortOverride.none)
                } else {
                    #if os(iOS) || os(visionOS)
                        try AVAudioSession.sharedInstance().overrideOutputAudioPort(AVAudioSession.PortOverride.speaker)
                    #endif
                }
            } catch {
                print("Error occurred: \(error.localizedDescription)")
            }
        }
    }
}
