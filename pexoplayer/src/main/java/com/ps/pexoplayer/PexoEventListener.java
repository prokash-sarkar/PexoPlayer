package com.ps.pexoplayer;

import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.ps.pexoplayer.model.PexoMediaMetadata;

/**
 * @Author: Prokash Sarkar
 * @Date: 7/30/19
 */
public interface PexoEventListener {

    // controller
    void onPlayerMediaControllerConnected(MediaControllerCompat mediaController);

    void onPlayerMetadataChanged(PexoMediaMetadata pexoMediaMetadata);

    void onPlayerPlaybackStateChanged(PlaybackStateCompat state);

    void onControllerDisconnect();

    // view updates

    void updatePlayerBuffer(int progress);

    void updatePlayerSeekBar(int progress, int max);

    void updateShuffle(int state);

    void updateRepeat(int state);

    // others
    void prepareLastPlayedMedia();

}
