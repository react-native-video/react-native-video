struct VideoSource {
    let type: String?
    let uri: String?
    let isNetwork: Bool
    let isAsset: Bool
    let shouldCache: Bool
    let requestHeaders: [String: Any]?
    let startPosition: Float64?
    let cropStart: Int64?
    let cropEnd: Int64?
    let customMetadata: CustomMetadata?
    /* DRM */
    let drm: DRMParams?

    let json: NSDictionary?

    init(_ json: NSDictionary!) {
        guard json != nil else {
            self.json = nil
            self.type = nil
            self.uri = nil
            self.isNetwork = false
            self.isAsset = false
            self.shouldCache = false
            self.requestHeaders = nil
            self.startPosition = nil
            self.cropStart = nil
            self.cropEnd = nil
            self.customMetadata = nil
            self.drm = nil
            return
        }
        self.json = json
        self.type = json["type"] as? String
        self.uri = json["uri"] as? String
        self.isNetwork = json["isNetwork"] as? Bool ?? false
        self.isAsset = json["isAsset"] as? Bool ?? false
        self.shouldCache = json["shouldCache"] as? Bool ?? false
        if let requestHeaders = json["requestHeaders"] as? [[String: Any]] {
            var _requestHeaders: [String: Any] = [:]
            for requestHeader in requestHeaders {
                if let key = requestHeader["key"] as? String, let value = requestHeader["value"] {
                    _requestHeaders[key] = value
                }
            }
            self.requestHeaders = _requestHeaders
        } else {
            self.requestHeaders = nil
        }
        self.startPosition = json["startPosition"] as? Float64
        self.cropStart = (json["cropStart"] as? Float64).flatMap { Int64(round($0)) }
        self.cropEnd = (json["cropEnd"] as? Float64).flatMap { Int64(round($0)) }
        self.customMetadata = CustomMetadata(json["metadata"] as? NSDictionary)
        self.drm = DRMParams(json["drm"] as? NSDictionary)
    }
}
