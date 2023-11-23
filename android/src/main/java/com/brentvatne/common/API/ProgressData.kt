package com.brentvatne.common.API

/*
* This class is a helper to build onProgress notification
*/

class ProgressData {
    var position: Long
    var bufferedDuration: Long
    var duration: Long
    var isOnLivePoint: Boolean

    init {
        position = -1
        bufferedDuration = -1
        duration = -1
        isOnLivePoint = false
    }
}