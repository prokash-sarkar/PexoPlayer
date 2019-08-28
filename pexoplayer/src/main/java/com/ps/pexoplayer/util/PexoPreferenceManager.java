package com.ps.pexoplayer.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import static com.ps.pexoplayer.util.Constants.LAST_ARTIST;
import static com.ps.pexoplayer.util.Constants.LAST_ARTIST_IMAGE;
import static com.ps.pexoplayer.util.Constants.LAST_CATEGORY;
import static com.ps.pexoplayer.util.Constants.LAST_CONTENT_ID;
import static com.ps.pexoplayer.util.Constants.LAST_TITLE;
import static com.ps.pexoplayer.util.Constants.MEDIA_QUEUE_POSITION;
import static com.ps.pexoplayer.util.Constants.NOW_PLAYING;
import static com.ps.pexoplayer.util.Constants.PLAYLIST_ID;

public class PexoPreferenceManager {

    private static final String TAG = "PexoPreferenceManager";

    private SharedPreferences mPreferences;

    public PexoPreferenceManager(Context mContext) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public String getPlaylistId() {
        return mPreferences.getString(PLAYLIST_ID, "");
    }

    public void savePlaylistId(String playlistId) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(PLAYLIST_ID, playlistId);
        editor.apply();
    }

    public String getLastCategory() {
        return mPreferences.getString(LAST_CATEGORY, "");
    }

    public void saveLastPlayedCategory(String category) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(LAST_CATEGORY, category);
        editor.apply();
    }

    public int getQueuePosition() {
        return mPreferences.getInt(MEDIA_QUEUE_POSITION, -1);
    }

    public void saveQueuePosition(int position) {
        Log.d(TAG, "saveQueuePosition: SAVING QUEUE INDEX: " + position);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(MEDIA_QUEUE_POSITION, position);
        editor.apply();
    }

    public String getLastPlayedId() {
        return mPreferences.getString(LAST_CONTENT_ID, "");
    }

    public void saveLastPlayedId(String artist) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(LAST_CONTENT_ID, artist);
        editor.apply();
    }

    public String getLastPlayedArtist() {
        return mPreferences.getString(LAST_ARTIST, "");
    }

    public void saveLastPlayedArtist(String artist) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(LAST_ARTIST, artist);
        editor.apply();
    }

    public void saveLastPlayedTitle(String title) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(LAST_TITLE, title);
        editor.apply();
    }

    public String getLastPlayedTitle() {
        return mPreferences.getString(LAST_TITLE, "");
    }

    public String getLastPlayedMediaImage() {
        return mPreferences.getString(LAST_ARTIST_IMAGE, "");
    }

    public void saveLastPlayedMediaImage(String url) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(LAST_ARTIST_IMAGE, url);
        editor.apply();
    }

    public String getLastPlayedMediaUrl() {
        return mPreferences.getString(NOW_PLAYING, "");
    }

    public void saveLastPlayedMediaUrl(String mediaId) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(NOW_PLAYING, mediaId);
        editor.apply();
    }

}


















