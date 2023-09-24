//
//  RCTVideoCachStorage.swift
//  react-native-video
//
//  Created by Gari Sarkisyan on 21.09.23.
//

import Foundation
import CommonCrypto

class RCTVideoCacheStorage {

    static let instance = RCTVideoCacheStorage()

    private lazy var storagePath: URL = { fileManager.temporaryDirectory.appendingPathComponent("RCTVideoCachStorage") }()
    private var fileManager: FileManager { FileManager.default }

    private init() {

        //purge()
    }

    func storeItem(from: URL, forUri url: URL) {
        let filePath = dataPathForUrl(url: url)
        
        do {
            if !fileManager.fileExists(atPath: storagePath.path) {
                try? fileManager.createDirectory(at: storagePath, withIntermediateDirectories: true, attributes: nil)
            }

            if fileManager.fileExists(atPath: filePath.path) {
                try fileManager.removeItem(atPath: filePath.path)
            }
            let bookmarkData = try from.bookmarkData()
            try bookmarkData.write(to: filePath)

        } catch {
            DebugLog("Can't store item \(url.absoluteString) \(error.localizedDescription)")
        }
    }

    func storedItemUrl(forUrl url: URL) -> URL? {
        let fileUrl = dataPathForUrl(url: url)
        guard fileManager.fileExists(atPath: fileUrl.path) else { return nil }
        do {
            return try fileUrl.urlResolvingBookmarkData
        } catch {
            DebugLog("Can't getting data for \(url.absoluteString) \(error.localizedDescription)")
            return nil
        }
    }

    func purge() {
        do {
            let bookmarksDataItems = try fileManager.contentsOfDirectory(atPath: storagePath.path)
            let bookmarksDataUrls = bookmarksDataItems.map { storagePath.appendingPathComponent($0) }

            for bookmarksDataUrl in bookmarksDataUrls {
                let bookmarkUrl = try bookmarksDataUrl.urlResolvingBookmarkData
                try fileManager.removeItem(atPath: bookmarkUrl.path)
                DebugLog("Item Removed at \(bookmarksDataUrl)")
            }

            if fileManager.fileExists(atPath: storagePath.path) {
                try fileManager.removeItem(atPath: storagePath.path)
            }
            try fileManager.createDirectory(at: storagePath, withIntermediateDirectories: true, attributes: nil)

        } catch {
            DebugLog("Can't purge storage \(error.localizedDescription)")
        }
    }

    private func dataPathForUrl(url: URL) -> URL {
        let temporaryDirectoryURL = storagePath
        let path = sha1(string: url.absoluteString)
        let extention = url.pathExtension
        return temporaryDirectoryURL.appendingPathComponent(path).appendingPathExtension(extention)
    }

    func sha1(string: String) -> String {
        let data = string.data(using: String.Encoding.utf8)!
        var digest = [UInt8](repeating: 0, count:Int(CC_SHA1_DIGEST_LENGTH))
        data.withUnsafeBytes {
            let buffer: UnsafePointer<UInt8> = $0.baseAddress!.assumingMemoryBound(to: UInt8.self)
            _ = CC_SHA1(buffer, CC_LONG(data.count), &digest)
        }
        let hexBytes = digest.map { String(format: "%02hhx", $0) }
        return hexBytes.joined()
    }
}



private extension URL {

    enum URLDataError: Error {
        case bookmarkDataIsStale
    }

    var urlResolvingBookmarkData: URL {
        get throws {
            let data = try Data(contentsOf: self)
            var bookmarkDataIsStale = false
            let bookmarkUrl = try URL(resolvingBookmarkData: data, bookmarkDataIsStale: &bookmarkDataIsStale)
            if bookmarkDataIsStale { throw URLDataError.bookmarkDataIsStale }
            return bookmarkUrl
        }
    }
}