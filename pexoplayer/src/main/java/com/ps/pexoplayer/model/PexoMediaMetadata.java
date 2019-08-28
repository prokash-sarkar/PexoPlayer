package com.ps.pexoplayer.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.media.MediaMetadataCompat;

/**
 * @Author: Prokash Sarkar
 * @Date: 7/31/19
 */
public class PexoMediaMetadata implements Parcelable {

    private String mediaId;
    private String artistName;
    private String title;
    private String mediaUrl;
    private String displayDescription;
    private String date;
    private String displayIconUrl;
    private String contentType;

    public PexoMediaMetadata(String mediaId, String artistName, String title, String mediaUrl, String displayDescription, String date, String displayIconUrl, String contentType) {
        this.mediaId = mediaId;
        this.artistName = artistName;
        this.title = title;
        this.mediaUrl = mediaUrl;
        this.displayDescription = displayDescription;
        this.date = date;
        this.displayIconUrl = displayIconUrl;
        this.contentType = contentType;
    }

    public PexoMediaMetadata(MediaMetadataCompat mediaMetadataCompat) {
        if (mediaMetadataCompat != null) {
            this.mediaId = mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
            this.artistName = mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
            this.title = mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
            this.mediaUrl = mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI);
            this.displayDescription = mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION);
            this.date = mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_DATE);
            this.displayIconUrl = mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI);
        }
    }

    public MediaMetadataCompat getMediaMetadataCompat() {
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artistName)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, mediaUrl)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, displayDescription)
                .putString(MediaMetadataCompat.METADATA_KEY_DATE, date)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, displayIconUrl)
                .build();
    }

    protected PexoMediaMetadata(Parcel in) {
        mediaId = in.readString();
        artistName = in.readString();
        title = in.readString();
        mediaUrl = in.readString();
        displayDescription = in.readString();
        date = in.readString();
        displayIconUrl = in.readString();
        contentType = in.readString();
    }

    public static final Creator<PexoMediaMetadata> CREATOR = new Creator<PexoMediaMetadata>() {
        @Override
        public PexoMediaMetadata createFromParcel(Parcel in) {
            return new PexoMediaMetadata(in);
        }

        @Override
        public PexoMediaMetadata[] newArray(int size) {
            return new PexoMediaMetadata[size];
        }
    };

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getDisplayDescription() {
        return displayDescription;
    }

    public void setDisplayDescription(String displayDescription) {
        this.displayDescription = displayDescription;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDisplayIconUrl() {
        return displayIconUrl;
    }

    public void setDisplayIconUrl(String displayIcon) {
        this.displayIconUrl = displayIcon;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mediaId);
        parcel.writeString(artistName);
        parcel.writeString(title);
        parcel.writeString(mediaUrl);
        parcel.writeString(displayDescription);
        parcel.writeString(date);
        parcel.writeString(displayIconUrl);
        parcel.writeString(contentType);
    }

}
