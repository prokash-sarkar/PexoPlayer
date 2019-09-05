package com.ps.pexoplayer;

import android.content.res.Configuration;
import android.support.v4.media.MediaMetadataCompat;

import com.ps.pexoplayer.util.PexoPreferenceManager;

public interface PexoCallbackListener {

    // for callback events

    void onTogglePlayPause();

    void onPlay();

    void onPause();

    void onStop();

    void onMediaSelected(String playlistId, MediaMetadataCompat mediaItem, int position);

    // for project structure

    void skipToTrack(int position);

    void repeatTrack();

    void shuffleTrack();

    void setConfigurationChanged(Configuration newConfig);

    void subscribeCallBack();

    void unSubscribeCallBack();

    PexoMediaInstance getPexoInstance();

    PexoPreferenceManager getPexoPreferenceManager();

}
