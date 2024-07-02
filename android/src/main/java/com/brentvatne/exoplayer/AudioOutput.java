package com.brentvatne.exoplayer;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.media3.common.C;

@SuppressLint("InlinedApi")
public enum AudioOutput {
    SPEAKER("speaker", C.STREAM_TYPE_MUSIC),
    EARPIECE("earpiece", C.STREAM_TYPE_VOICE_CALL);

    private final @C.StreamType int streamType;
    private final String mName;

    AudioOutput(final String name, @C.StreamType int stream) {
        mName = name;
        streamType = stream;
    }

    public static AudioOutput get(String name) {
        for (AudioOutput d : values()) {
            if (d.mName.equalsIgnoreCase(name))
                return d;
        }
        return SPEAKER;
    }

    public int getStreamType() {
        return streamType;
    }

    @NonNull
    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + this.mName + ", " + streamType + ")";
    }
}
