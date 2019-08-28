package com.ps.pexoplayer.players;

import android.support.v4.media.session.PlaybackStateCompat;

public interface PlaybackInfoListener {

    void onPlaybackStateChange(PlaybackStateCompat state);

    void onBufferedTo(long progress);

    void onSeekTo(long progress, long max);

    void onPlaybackComplete();

    void updateUI(String newMediaId);
}
