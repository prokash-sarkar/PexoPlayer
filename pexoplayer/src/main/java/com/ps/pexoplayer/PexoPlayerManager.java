package com.ps.pexoplayer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.Toast;

import com.ps.pexoplayer.client.MediaBrowserHelper;
import com.ps.pexoplayer.client.MediaBrowserHelperCallback;
import com.ps.pexoplayer.model.PexoMediaMetadata;
import com.ps.pexoplayer.services.MediaService;
import com.ps.pexoplayer.util.PexoPreferenceManager;

import java.util.ArrayList;
import java.util.List;

import static com.ps.pexoplayer.util.Constants.BUFFER_PROGRESS;
import static com.ps.pexoplayer.util.Constants.MEDIA_QUEUE_POSITION;
import static com.ps.pexoplayer.util.Constants.QUEUE_NEW_PLAYLIST;
import static com.ps.pexoplayer.util.Constants.SEEK_BAR_MAX;
import static com.ps.pexoplayer.util.Constants.SEEK_BAR_PROGRESS;

public class PexoPlayerManager implements MediaBrowserHelperCallback, PexoCallbackListener {

    private static final String TAG = PexoPlayerManager.class.getSimpleName();

    private Context mContext;
    private PexoMediaInstance mPexoInstance;
    private MediaBrowserHelper mMediaBrowserHelper;
    private PexoPreferenceManager mPexoPrefManager;

    private BufferBroadcastReceiver mBufferBroadcastReceiver;
    private SeekBarBroadcastReceiver mSeekbarBroadcastReceiver;
    private UpdateUIBroadcastReceiver mUpdateUIBroadcastReceiver;

    private PexoEventListener mPexoEventListener;

    private boolean mIsPlaying;
    private boolean mOnAppOpen;
    private boolean mWasConfigurationChange = false;

    public PexoPlayerManager(Context context) {
        this.mContext = context;

        mPexoInstance = PexoMediaInstance.getInstance();
        mPexoPrefManager = new PexoPreferenceManager(context);

        mMediaBrowserHelper = new MediaBrowserHelper(context, MediaService.class);
        mMediaBrowserHelper.setMediaBrowserHelperCallback(this);
    }

    public void setPendingIntentClass(Class<? extends Activity> activity) {
        mPexoInstance.setMainActivity(activity);
    }

    public void setPexoEventListener(PexoEventListener listener) {
        this.mPexoEventListener = listener;
    }

    /**
     * MediaBrowserHelperCallback start
     */

    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {
        Log.d(TAG, "onMetadataChanged: called");
        if (metadata != null) {
            PexoMediaMetadata pexoMediaMetadata = new PexoMediaMetadata(metadata);
            if (pexoMediaMetadata.getMediaId() != null) {
                mPexoEventListener.onPlayerMetadataChanged(pexoMediaMetadata);
            }
        }
    }

    @Override
    public void onPlaybackStateChanged(PlaybackStateCompat state) {
        Log.d(TAG, "onPlaybackStateChanged: called.");
        mIsPlaying = state != null && state.getState() == PlaybackStateCompat.STATE_PLAYING;
        mPexoInstance.setPlaying(mIsPlaying);
        mPexoEventListener.onPlayerPlaybackStateChanged(state);
    }

    @Override
    public void onShuffleChanged(int state) {
        mPexoEventListener.updateShuffle(state);
    }

    @Override
    public void onRepeatChanged(int state) {
        mPexoEventListener.updateRepeat(state);
    }

    @Override
    public void onMediaControllerConnected(MediaControllerCompat mediaController) {
        Log.d(TAG, "onMediaControllerConnected: " + mediaController.getPackageName());
        mPexoEventListener.onPlayerMediaControllerConnected(mediaController);
    }

    /**
     * MediaBrowserHelperCallback end
     */

    /**
     * Broadcast Receiver initialization start
     */

    private void initUpdateUIBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(mContext.getString(R.string.broadcast_update_ui));
        mUpdateUIBroadcastReceiver = new UpdateUIBroadcastReceiver();
        mContext.registerReceiver(mUpdateUIBroadcastReceiver, intentFilter);
    }

    private class UpdateUIBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String newMediaId = intent.getStringExtra(mContext.getString(R.string.broadcast_new_media_id));
            //Log.d(TAG, "onReceive: " + mPexoInstance.getMediaItem(newMediaId).getDescription().getMediaId());
            saveLastPlayedSongProperties(getPexoInstance().getPlaylistId(),
                    "track", mPexoInstance.getMediaItem(newMediaId));
        }
    }

    private void initBufferBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(mContext.getString(R.string.broadcast_buffer_update));
        mBufferBroadcastReceiver = new BufferBroadcastReceiver();
        mContext.registerReceiver(mBufferBroadcastReceiver, intentFilter);
    }

    private class BufferBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            long bufferProgress = intent.getLongExtra(BUFFER_PROGRESS, 0);
            //Log.d(TAG, "onReceive: " + bufferProgress);
            mPexoEventListener.updatePlayerBuffer((int) bufferProgress);
        }

    }

    private void initSeekBarBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(mContext.getString(R.string.broadcast_seekbar_update));
        mSeekbarBroadcastReceiver = new SeekBarBroadcastReceiver();
        mContext.registerReceiver(mSeekbarBroadcastReceiver, intentFilter);
    }

    private class SeekBarBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            long seekProgress = intent.getLongExtra(SEEK_BAR_PROGRESS, 0);
            long seekMax = intent.getLongExtra(SEEK_BAR_MAX, 0);
            //Log.d(TAG, "onReceive: " + seekMax + " - " + seekMax);
            mPexoEventListener.updatePlayerSeekBar((int) seekProgress, (int) seekMax);
        }

    }

    /**
     * Broadcast Receiver initialization end
     */

    /**
     * Helper Methods start
     */

    public void setupNewPlaylist(String playlistId, List<?> songList, List<PexoMediaMetadata> mediaItemList, int position) {
        if (!playlistId.isEmpty() && mediaItemList != null && mediaItemList.size() > 0) {

            List<MediaMetadataCompat> mediaItems = getMediaMetadataCompat(mediaItemList);

            // VERY IMPORTANT
            mPexoInstance.setPlaylistId(playlistId);
            mPexoInstance.setPexoMediaMetadata(mediaItemList);
            mPexoInstance.setMediaItems(mediaItems);
            mPexoInstance.setCurrentSongPosition(position);
            mPexoInstance.setSongList(songList);

            //startPlayback();
        }
    }

    public void startPlayback() {
        String playlistId = mPexoInstance.getPlaylistId();
        List<MediaMetadataCompat> mediaMetadataCompatItems = mPexoInstance.getMediaMetadataCompatItems();
        int currentSongPosition = mPexoInstance.getCurrentSongPosition();

        if (playlistId != null && mediaMetadataCompatItems.get(currentSongPosition) != null) {
            // VERY IMPORTANT
            onMediaSelected(
                    playlistId,
                    mediaMetadataCompatItems.get(currentSongPosition),
                    currentSongPosition);

            // VERY IMPORTANT
            saveLastPlayedSongProperties(playlistId,
                    "audio",
                    mediaMetadataCompatItems.get(currentSongPosition));
        }
    }

    public boolean skipToNextTrack() {
        if (mPexoInstance.getMediaItems().size() > 1
                && mPexoInstance.getCurrentSongPosition()
                < mPexoInstance.getMediaItems().size() - 1) {
            // VERY IMPORTANT
            mPexoInstance.setCurrentSongPosition(mPexoInstance.getCurrentSongPosition() + 1);

            // VERY IMPORTANT
            startPlayback();

            return true;
        }

        return false;
    }

    public boolean skipToPreviousTrack() {
        if (mPexoInstance.getMediaMetadataCompatItems().size() > 1
                && mPexoInstance.getCurrentSongPosition() > 0) {
            // VERY IMPORTANT
            mPexoInstance.setCurrentSongPosition(mPexoInstance.getCurrentSongPosition() - 1);

            // VERY IMPORTANT
            startPlayback();

            return true;
        }

        return false;
    }

    public void saveLastPlayedSongProperties(String playlistId, String category, MediaMetadataCompat mediaMetadataCompat) {
        getPexoPreferenceManager().savePlaylistId(playlistId);
        getPexoPreferenceManager().saveLastPlayedCategory(category);
        if (mediaMetadataCompat != null) {
            getPexoPreferenceManager().saveLastPlayedId(mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
            getPexoPreferenceManager().saveLastPlayedArtist(mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
            getPexoPreferenceManager().saveLastPlayedTitle(mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
            getPexoPreferenceManager().saveLastPlayedMediaImage(mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI));
            getPexoPreferenceManager().saveLastPlayedMediaUrl(mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI));
        }
    }

    public void onFinishedGettingPreviousSessionData(List<PexoMediaMetadata> mediaMetadataList) {
        if (mediaMetadataList != null && mediaMetadataList.size() > 0) {
            List<MediaMetadataCompat> mediaItems = getMediaMetadataCompat(mediaMetadataList);
            mPexoInstance.setMediaItems(mediaItems);
        }
        mMediaBrowserHelper.onStart(mWasConfigurationChange);
    }

    private List<MediaMetadataCompat> getMediaMetadataCompat(List<PexoMediaMetadata> mediaMetadataList) {
        List<MediaMetadataCompat> mediaItems = new ArrayList<>();
        for (PexoMediaMetadata pexoMediaMetadata : mediaMetadataList) {
            mediaItems.add(pexoMediaMetadata.getMediaMetadataCompat());
        }
        return mediaItems;
    }

    /**
     * Helper Methods end
     */

    @Override
    public void onTogglePlayPause() {
        if (mMediaBrowserHelper != null) {
            if (mOnAppOpen) {
                if (mIsPlaying) {
                    mMediaBrowserHelper.getTransportControls().pause();
                } else {
                    mMediaBrowserHelper.getTransportControls().play();
                }
            } else {
                if (!getPexoPreferenceManager().getPlaylistId().equals("")) {
                    onMediaSelected(
                            getPexoPreferenceManager().getPlaylistId(),
                            mPexoInstance.getMediaItem(getPexoPreferenceManager().getLastPlayedMediaUrl()),
                            getPexoPreferenceManager().getQueuePosition()
                    );
                } else {
                    Toast.makeText(mContext, "Select something to play", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onPlay() {
        if (mMediaBrowserHelper != null) {
            if (mOnAppOpen) {
                mMediaBrowserHelper.getTransportControls().play();
            }
        }
    }

    @Override
    public void onPause() {
        if (mMediaBrowserHelper != null) {
            if (mOnAppOpen) {
                mMediaBrowserHelper.getTransportControls().pause();
            }
        }
    }

    @Override
    public void onStop() {
        if (mMediaBrowserHelper != null) {
            if (mOnAppOpen) {
                mMediaBrowserHelper.getTransportControls().stop();
            }
        }
    }

    @Override
    public void onMediaSelected(String playlistId, MediaMetadataCompat mediaItem, int position) {
        if (mMediaBrowserHelper != null && mediaItem != null) {
            Log.d(TAG, "onMediaSelected: CALLED: " + mediaItem.getDescription().getMediaId());

            String currentPlaylistId = getPexoPreferenceManager().getPlaylistId();

            Bundle bundle = new Bundle();
            bundle.putInt(MEDIA_QUEUE_POSITION, position);

            if (playlistId.equals(currentPlaylistId)) {
                mMediaBrowserHelper.getTransportControls().playFromMediaId(mediaItem.getDescription().getMediaId(), bundle);
            } else {
                bundle.putBoolean(QUEUE_NEW_PLAYLIST, true); // let the player know this is a new playlist
                mMediaBrowserHelper.subscribeToNewPlaylist(currentPlaylistId, playlistId);
                mMediaBrowserHelper.getTransportControls().playFromMediaId(mediaItem.getDescription().getMediaId(), bundle);
            }

            mOnAppOpen = true;
        } else {
            Toast.makeText(mContext, "Select something to play", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void skipToTrack(int position) {
        mPexoInstance.setCurrentSongPosition(position);

        // VERY IMPORTANT
        startPlayback();
    }

    @Override
    public void repeatTrack() {
        int repeat = mMediaBrowserHelper.getMediaController().getRepeatMode();
        switch (repeat) {
            case PlaybackStateCompat.REPEAT_MODE_ALL:
                mMediaBrowserHelper.getTransportControls()
                        .setRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE);
                break;
            case PlaybackStateCompat.REPEAT_MODE_ONE:
                mMediaBrowserHelper.getTransportControls()
                        .setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ALL);
                break;
            default:
                mMediaBrowserHelper.getTransportControls()
                        .setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE);
                break;
        }
    }

    @Override
    public void shuffleTrack() {
        int shuffle = mMediaBrowserHelper.getMediaController().getShuffleMode();
        switch (shuffle) {
            case PlaybackStateCompat.SHUFFLE_MODE_ALL:
                mMediaBrowserHelper.getTransportControls()
                        .setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE);
                break;
            default:
                mMediaBrowserHelper.getTransportControls()
                        .setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL);
                break;
        }
    }

    @Override
    public void setConfigurationChanged(Configuration newConfig) {
        mWasConfigurationChange = true;
    }

    @Override
    public void subscribeCallBack() {
        Log.d(TAG, "onStart: called.");

        initBufferBroadcastReceiver();
        initSeekBarBroadcastReceiver();
        initUpdateUIBroadcastReceiver();

        if (!mPexoPrefManager.getPlaylistId().equals("")) {
            if (mPexoEventListener != null) {
                mPexoEventListener.prepareLastPlayedMedia();
            } else {
                throw new RuntimeException("Set the PexoEventListener before calling subscribeCallBack()");
            }
        } else {
            mMediaBrowserHelper.onStart(mWasConfigurationChange);
        }
    }

    @Override
    public void unSubscribeCallBack() {
        Log.d(TAG, "onStop: called.");

        if (mBufferBroadcastReceiver != null) {
            mContext.unregisterReceiver(mBufferBroadcastReceiver);
        }
        if (mSeekbarBroadcastReceiver != null) {
            mContext.unregisterReceiver(mSeekbarBroadcastReceiver);
        }
        if (mUpdateUIBroadcastReceiver != null) {
            mContext.unregisterReceiver(mUpdateUIBroadcastReceiver);
        }

        mMediaBrowserHelper.onStop();
        mPexoEventListener.onControllerDisconnect();
    }

    @Override
    public PexoMediaInstance getPexoInstance() {
        return mPexoInstance;
    }

    @Override
    public PexoPreferenceManager getPexoPreferenceManager() {
        return mPexoPrefManager;
    }

}
