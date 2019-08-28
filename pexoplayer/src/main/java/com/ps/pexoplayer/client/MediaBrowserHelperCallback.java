package com.ps.pexoplayer.client;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;

public interface MediaBrowserHelperCallback {

    void onMetadataChanged(final MediaMetadataCompat metadata);

    void onPlaybackStateChanged(PlaybackStateCompat state);

    void onShuffleChanged(int state);

    void onRepeatChanged(int state);

    void onMediaControllerConnected(MediaControllerCompat mediaController);

}









