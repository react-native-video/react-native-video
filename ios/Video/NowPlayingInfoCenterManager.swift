import Foundation
import MediaPlayer

class NowPlayingInfoCenterManager {
    static let shared = NowPlayingInfoCenterManager()

    private let SEEK_INTERVAL_SECONDS: Double = 10

    private weak var currentPlayer: AVPlayer?
    private var players = NSHashTable<AVPlayer>.weakObjects()

    private var observers: [Int: NSKeyValueObservation] = [:]
    private var playbackObserver: Any?

    private var playTarget: Any?
    private var pauseTarget: Any?
    private var skipForwardTarget: Any?
    private var skipBackwardTarget: Any?
    private var playbackPositionTarget: Any?
    private var seekTarget: Any?

    private var receivingRemoveControlEvents = false {
        didSet {
            if receivingRemoveControlEvents {
                UIApplication.shared.beginReceivingRemoteControlEvents()
            } else {
                UIApplication.shared.endReceivingRemoteControlEvents()
            }

            AudioSessionManager.shared.updateAudioSessionCategory()
        }
    }

    deinit {
        cleanup()
    }

    func registerPlayer(player: AVPlayer) {
        if players.contains(player) {
            return
        }

        if receivingRemoveControlEvents == false {
            receivingRemoveControlEvents = true
        }

        if let oldObserver = observers[player.hashValue] {
            oldObserver.invalidate()
        }

        observers[player.hashValue] = observePlayers(player: player)
        players.add(player)

        if currentPlayer == nil {
            setCurrentPlayer(player: player)
        }
    }

    func removePlayer(player: AVPlayer) {
        if !players.contains(player) {
            return
        }

        if let observer = observers[player.hashValue] {
            observer.invalidate()
        }

        observers.removeValue(forKey: player.hashValue)
        players.remove(player)

        if currentPlayer == player {
            currentPlayer = nil
            updateMetadata()
        }

        if players.allObjects.isEmpty {
            cleanup()
        }
    }

    public func cleanup() {
        observers.removeAll()
        players.removeAllObjects()

        if let playbackObserver {
            currentPlayer?.removeTimeObserver(playbackObserver)
        }

        let commandCenter = MPRemoteCommandCenter.shared()
        invalidateTargets(commandCenter)

        MPNowPlayingInfoCenter.default().nowPlayingInfo = [:]
        receivingRemoveControlEvents = false
    }

    private func setCurrentPlayer(player: AVPlayer) {
        if player == currentPlayer {
            return
        }

        if let playbackObserver {
            currentPlayer?.removeTimeObserver(playbackObserver)
        }

        currentPlayer = player
        registerTargets()

        updateMetadata()

        // one second observer
        playbackObserver = player.addPeriodicTimeObserver(
            forInterval: CMTime(value: 1, timescale: 4),
            queue: .global(),
            using: { [weak self] _ in
                self?.updatePlaybackInfo()
            }
        )
    }

    private func registerTargets() {
        let commandCenter = MPRemoteCommandCenter.shared()

        invalidateTargets(commandCenter)

        playTarget = commandCenter.playCommand.addTarget { [weak self] _ in
            guard let self, let player = self.currentPlayer else {
                return .commandFailed
            }

            if player.rate == 0 {
                player.play()
            }

            return .success
        }

        pauseTarget = commandCenter.pauseCommand.addTarget { [weak self] _ in
            guard let self, let player = self.currentPlayer else {
                return .commandFailed
            }

            if player.rate != 0 {
                player.pause()
            }

            return .success
        }

        skipBackwardTarget = commandCenter.skipBackwardCommand.addTarget { [weak self] _ in
            guard let self, let player = self.currentPlayer else {
                return .commandFailed
            }
            let newTime = player.currentTime() - CMTime(seconds: self.SEEK_INTERVAL_SECONDS, preferredTimescale: .max)
            player.seek(to: newTime)
            return .success
        }

        skipForwardTarget = commandCenter.skipForwardCommand.addTarget { [weak self] _ in
            guard let self, let player = self.currentPlayer else {
                return .commandFailed
            }

            let newTime = player.currentTime() + CMTime(seconds: self.SEEK_INTERVAL_SECONDS, preferredTimescale: .max)
            player.seek(to: newTime)
            return .success
        }

        playbackPositionTarget = commandCenter.changePlaybackPositionCommand.addTarget { [weak self] event in
            guard let self, let player = self.currentPlayer else {
                return .commandFailed
            }
            if let event = event as? MPChangePlaybackPositionCommandEvent {
                player.seek(to: CMTime(seconds: event.positionTime, preferredTimescale: .max)) { _ in
                    player.play()
                }
                return .success
            }
            return .commandFailed
        }
    }

    private func invalidateTargets(_ commandCenter: MPRemoteCommandCenter) {
        commandCenter.playCommand.removeTarget(playTarget)
        commandCenter.pauseCommand.removeTarget(pauseTarget)
        commandCenter.skipForwardCommand.removeTarget(skipForwardTarget)
        commandCenter.skipBackwardCommand.removeTarget(skipBackwardTarget)
        commandCenter.changePlaybackPositionCommand.removeTarget(playbackPositionTarget)
    }

    public func updateMetadata() {
        guard let player = currentPlayer, let currentItem = player.currentItem else {
            let commandCenter = MPRemoteCommandCenter.shared()
            invalidateTargets(commandCenter)

            MPNowPlayingInfoCenter.default().nowPlayingInfo = [:]
            return
        }

        // commonMetadata is metadata from asset, externalMetadata is custom metadata set by user
        let metadata = currentItem.asset.commonMetadata + currentItem.externalMetadata
        var nowPlayingInfo: [String: Any] = [:]

        if let titleItem = AVMetadataItem.metadataItems(from: metadata, filteredByIdentifier: .commonIdentifierTitle).first?.value {
            nowPlayingInfo[MPMediaItemPropertyTitle] = titleItem
        } else {
            nowPlayingInfo[MPMediaItemPropertyTitle] = ""
        }

        if let artistItem = AVMetadataItem.metadataItems(from: metadata, filteredByIdentifier: .commonIdentifierArtist).first?.value {
            nowPlayingInfo[MPMediaItemPropertyArtist] = artistItem
        } else {
            nowPlayingInfo[MPMediaItemPropertyArtist] = ""
        }

        // I have some issue with this - setting artworkItem when it not set dont return nil but also is crashing application
        // this is very hacky workaround for it
        if let artworkItem = AVMetadataItem.metadataItems(from: metadata, filteredByIdentifier: .commonIdentifierArtwork).first?.value as? Data {
            if let image = UIImage(data: artworkItem) {
                nowPlayingInfo[MPMediaItemPropertyArtwork] = MPMediaItemArtwork(boundsSize: image.size, requestHandler: { _ in
                    return image
                })
            }
        } else {
            nowPlayingInfo[MPMediaItemPropertyArtwork] = MPMediaItemArtwork(boundsSize: UIImage().size, requestHandler: { _ in
                UIImage()
            })
        }

        if CMTIME_IS_INDEFINITE(currentItem.asset.duration) {
            nowPlayingInfo[MPNowPlayingInfoPropertyIsLiveStream] = true
        } else {
            nowPlayingInfo[MPNowPlayingInfoPropertyIsLiveStream] = false
        }

        nowPlayingInfo[MPMediaItemPropertyPlaybackDuration] = currentItem.duration.seconds
        nowPlayingInfo[MPNowPlayingInfoPropertyElapsedPlaybackTime] = currentItem.currentTime().seconds
        nowPlayingInfo[MPNowPlayingInfoPropertyPlaybackRate] = player.rate
        nowPlayingInfo[MPNowPlayingInfoPropertyMediaType] = MPNowPlayingInfoMediaType.video.rawValue

        MPNowPlayingInfoCenter.default().nowPlayingInfo = nowPlayingInfo
    }

    private func updatePlaybackInfo() {
        guard let player = currentPlayer, let currentItem = player.currentItem else {
            return
        }

        // We dont want to update playback if we did not set metadata yet
        if var nowPlayingInfo = MPNowPlayingInfoCenter.default().nowPlayingInfo {
            nowPlayingInfo[MPMediaItemPropertyPlaybackDuration] = currentItem.duration.seconds
            nowPlayingInfo[MPNowPlayingInfoPropertyElapsedPlaybackTime] = currentItem.currentTime().seconds.rounded()
            nowPlayingInfo[MPNowPlayingInfoPropertyPlaybackRate] = player.rate

            if CMTIME_IS_INDEFINITE(currentItem.asset.duration) {
                nowPlayingInfo[MPNowPlayingInfoPropertyIsLiveStream] = true
            } else {
                nowPlayingInfo[MPNowPlayingInfoPropertyIsLiveStream] = false
            }
            MPNowPlayingInfoCenter.default().nowPlayingInfo = nowPlayingInfo
        }
    }

    private func findNewCurrentPlayer() {
        if let newPlayer = players.allObjects.first(where: {
            $0.rate != 0
        }) {
            setCurrentPlayer(player: newPlayer)
        }
    }

    // We will observe players rate to find last active player that info will be displayed
    private func observePlayers(player: AVPlayer) -> NSKeyValueObservation {
        return player.observe(\.rate) { [weak self] player, change in
            guard let self else { return }

            let rate = change.newValue

            // case where there is new player that is not paused
            // In this case event is triggered by non currentPlayer
            if rate != 0 && self.currentPlayer != player {
                self.setCurrentPlayer(player: player)
                return
            }

            // case where currentPlayer was paused
            // In this case event is triggeret by currentPlayer
            if rate == 0 && self.currentPlayer == player {
                self.findNewCurrentPlayer()
            }
        }
    }
}
