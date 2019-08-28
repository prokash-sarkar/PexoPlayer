package com.ps.pexoplayer;

import android.app.Activity;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;

import com.ps.pexoplayer.model.PexoMediaMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class PexoMediaInstance {

    private static final String TAG = PexoMediaInstance.class.getSimpleName();

    private static PexoMediaInstance mInstance;

    private Class<? extends Activity> mMainActivity;

    private List<PexoMediaMetadata> mPexoMediaMetadatas = new ArrayList<>();

    private List<MediaMetadataCompat> mMediaMetadataCompatItems = new ArrayList<>();

    private List<MediaBrowserCompat.MediaItem> mMediaItems = new ArrayList<>();

    private TreeMap<String, MediaMetadataCompat> mTreeMap = new TreeMap<>();

    private String mPlaylistId = "";

    private int mSongPosition = 0;

    private boolean isPlaying = false;

    private List<?> songList;

    private PexoMediaInstance() {
    }

    public static PexoMediaInstance getInstance() {
        if (mInstance == null) {
            mInstance = new PexoMediaInstance();
            return mInstance;
        } else {
            return mInstance;
        }
    }

    public Class<? extends Activity> getMainActivity() {
        return mMainActivity;
    }

    protected void setMainActivity(Class<? extends Activity> activity) {
        this.mMainActivity = activity;
    }

    public List<PexoMediaMetadata> getPexoMediaMetadata() {
        return mPexoMediaMetadatas;
    }

    protected void setPexoMediaMetadata(List<PexoMediaMetadata> mPexoMediaMetadatas) {
        this.mPexoMediaMetadatas = mPexoMediaMetadatas;
    }

    public List<MediaMetadataCompat> getMediaMetadataCompatItems() {
        return mMediaMetadataCompatItems;
    }

    public List<MediaBrowserCompat.MediaItem> getMediaItems() {
        return mMediaItems;
    }

    public TreeMap<String, MediaMetadataCompat> getTreeMap() {
        return mTreeMap;
    }

    public MediaMetadataCompat getMediaItem(String mediaId) {
        return mTreeMap.get(mediaId);
    }

    public int getMediaPosition(String mediaId) {
        return mMediaMetadataCompatItems.indexOf(getMediaItem(mediaId));
    }

    protected void setMediaItems(List<MediaMetadataCompat> mediaItems) {
        mMediaMetadataCompatItems.clear();
        mMediaItems.clear();
        mTreeMap.clear();

        for (MediaMetadataCompat item : mediaItems) {
            mMediaMetadataCompatItems.add(item);
            mMediaItems.add(
                    new MediaBrowserCompat.MediaItem(
                            item.getDescription(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
            );
            mTreeMap.put(item.getDescription().getMediaId(), item);
        }
    }

    public String getPlaylistId() {
        return mPlaylistId;
    }

    protected void setPlaylistId(String mPlaylistId) {
        this.mPlaylistId = mPlaylistId;
    }

    public int getCurrentSongPosition() {
        return mSongPosition;
    }

    public void setCurrentSongPosition(int mSongPosition) {
        this.mSongPosition = mSongPosition;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    protected void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public List<?> getSongList() {
        return songList;
    }

    public void setSongList(List<?> songList) {
        this.songList = songList;
    }

}
