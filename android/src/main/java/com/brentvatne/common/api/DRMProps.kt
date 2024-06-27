package com.brentvatne.common.api

import com.brentvatne.common.toolbox.ReactBridgeUtils.safeGetArray
import com.brentvatne.common.toolbox.ReactBridgeUtils.safeGetBool
import com.brentvatne.common.toolbox.ReactBridgeUtils.safeGetString
import com.facebook.react.bridge.ReadableMap
import java.util.UUID

/**
 * Class representing DRM props for host.
 * Only generic code here, no reference to the player.
 */
class DRMProps {
    /**
     * string version of configured UUID for drm prop
     */
    var drmType: String? = null

    /**
     * Configured UUID for drm prop
     */
    var drmUUID: UUID? = null

    /**
     * DRM license server to be used
     */
    var drmLicenseServer: String? = null

    /**
     * DRM Http Header to access to license server
     */
    var drmLicenseHeader: Array<String> = emptyArray<String>()

    /**
     * Flag to enable key rotation support
     */
    var multiDrm: Boolean = false

    /** return true if this and src are equals  */
    override fun equals(other: Any?): Boolean {
        if (other == null || other !is DRMProps) return false
        return drmType == other.drmType &&
            drmLicenseServer == other.drmLicenseServer &&
            multiDrm == other.multiDrm &&
            drmLicenseHeader.contentDeepEquals(other.drmLicenseHeader) // drmLicenseHeader is never null
    }

    companion object {
        private const val PROP_DRM_TYPE = "type"
        private const val PROP_DRM_LICENSE_SERVER = "licenseServer"
        private const val PROP_DRM_HEADERS = "headers"
        private const val PROP_DRM_HEADERS_KEY = "key"
        private const val PROP_DRM_HEADERS_VALUE = "value"
        private const val PROP_DRM_MULTI_DRM = "multiDrm"

        /** parse the source ReadableMap received from app */
        @JvmStatic
        fun parse(src: ReadableMap?): DRMProps? {
            var drm: DRMProps? = null
            if (src != null && src.hasKey(PROP_DRM_TYPE)) {
                drm = DRMProps()
                drm.drmType = safeGetString(src, PROP_DRM_TYPE)
                drm.drmLicenseServer = safeGetString(src, PROP_DRM_LICENSE_SERVER)
                drm.multiDrm = safeGetBool(src, PROP_DRM_MULTI_DRM, false)
                val drmHeadersArray = safeGetArray(src, PROP_DRM_HEADERS)
                if (drm.drmType != null && drm.drmLicenseServer != null) {
                    if (drmHeadersArray != null) {
                        val drmKeyRequestPropertiesList = ArrayList<String?>()
                        for (i in 0 until drmHeadersArray.size()) {
                            val current = drmHeadersArray.getMap(i)
                            drmKeyRequestPropertiesList.add(safeGetString(current, PROP_DRM_HEADERS_KEY))
                            drmKeyRequestPropertiesList.add(safeGetString(current, PROP_DRM_HEADERS_VALUE))
                        }
                        val array = emptyArray<String>()
                        drm.drmLicenseHeader = drmKeyRequestPropertiesList.toArray(array)
                    }
                } else {
                    return null
                }
            }
            return drm
        }
    }
}
