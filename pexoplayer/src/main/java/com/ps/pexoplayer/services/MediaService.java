package com.ps.pexoplayer.services;

import android.app.Notification;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.media.MediaBrowserServiceCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.ps.pexoplayer.PexoMediaInstance;
import com.ps.pexoplayer.R;
import com.ps.pexoplayer.notifications.MediaNotificationManager;
import com.ps.pexoplayer.players.MediaPlayerAdapter;
import com.ps.pexoplayer.players.PlaybackInfoListener;
import com.ps.pexoplayer.util.PexoPreferenceManager;
import com.ps.pexoplayer.util.RandomUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.ps.pexoplayer.util.Constants.BUFFER_PROGRESS;
import static com.ps.pexoplayer.util.Constants.MEDIA_QUEUE_POSITION;
import static com.ps.pexoplayer.util.Constants.QUEUE_NEW_PLAYLIST;
import static com.ps.pexoplayer.util.Constants.SEEK_BAR_MAX;
import static com.ps.pexoplayer.util.Constants.SEEK_BAR_PROGRESS;

public class MediaService extends MediaBrowserServiceCompat {

    private static final String TAG = "MediaService";

    private MediaSessionCompat mSession;
    private MediaPlayerAdapter mPlayback;
    private PexoMediaInstance mPexoInstance;
    private PexoPreferenceManager mMyPrefManager;
    private MediaNotificationManager mMediaNotificationManager;
    private boolean mIsServiceStarted;

    @Override
    public void onCreate() {
        super.onCreate();
        mPexoInstance = PexoMediaInstance.getInstance();
        mMyPrefManager = new PexoPreferenceManager(this);

        //Build the MediaSession
        mSession = new MediaSessionCompat(this, TAG);

        mSession.setFlags(
                // https://developer.android.com/guide/topics/media-apps/mediabuttons#mediabuttons-and-active-mediasessions
                // Media buttons on the device
                // (handles the PendingIntents for MediaButtonReceiver.buildMediaButtonPendingIntent)
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        // Control the items in the queue (aka playlist)
                        // See https://developer.android.com/guide/topics/media-apps/mediabuttons for more info on flags
                        MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS);

        mSession.setCallback(new MediaSessionCallback());

        // A token that can be used to create a MediaController for this session
        setSessionToken(mSession.getSessionToken());

        mPlayback = new MediaPlayerAdapter(this, new MediaPlayerListener());
        mMediaNotificationManager = new MediaNotificationManager(this);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "onTaskRemoved: stopped");
        super.onTaskRemoved(rootIntent);
        mPlayback.stop();
        stopSelf();
    }

    @Override
    public void onDestroy() {
        mSession.release();
        Log.d(TAG, "onDestroy: MediaPlayerAdapter stopped, and MediaSession released");
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String s, int i, @Nullable Bundle bundle) {

        Log.d(TAG, "onGetRoot: called. ");
        if (s.equals(getApplicationContext().getPackageName())) {

            // Allowed to browse media
            return new BrowserRoot("some_real_playlist", null); // return no media
        }
        return new BrowserRoot("empty_media", null); // return no media
    }

    @Override
    public void onLoadChildren(@NonNull String s, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        Log.d(TAG, "onLoadChildren: called: " + s + ", " + result);

        //  Browsing not allowed
        if (TextUtils.equals("empty_media", s)) {
            result.sendResult(null);
            return;
        }
        result.sendResult(mPexoInstance.getMediaItems()); // return all available media
    }

    public class MediaSessionCallback extends MediaSessionCompat.Callback {

        private final List<MediaSessionCompat.QueueItem> mPlaylist = new ArrayList<>();
        private int mQueueIndex = -1;
        private MediaMetadataCompat mPreparedMedia;

        private void resetPlaylist() {
            mPlaylist.clear();
            mQueueIndex = -1;
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            Log.d(TAG, "onPlayFromMediaId: CALLED.");
            boolean extrasBoolean = extras.getBoolean(QUEUE_NEW_PLAYLIST, false);
            if (extrasBoolean) {
                resetPlaylist();
            }

            if (mPexoInstance.getTreeMap() != null) {
                mPreparedMedia = mPexoInstance.getTreeMap().get(mediaId);
                if (mPreparedMedia != null) {
                    mSession.setMetadata(mPreparedMedia);
                    if (!mSession.isActive()) {
                        mSession.setActive(true);
                    }

                    mPlayback.playFromMedia(mPreparedMedia);

                    int newQueuePosition = extras.getInt(MEDIA_QUEUE_POSITION, -1);
                    if (newQueuePosition == -1) {
                        mQueueIndex++;
                    } else {
                        mQueueIndex = extras.getInt(MEDIA_QUEUE_POSITION);
                    }

                    mMyPrefManager.saveQueuePosition(mQueueIndex);
                    mMyPrefManager.saveLastPlayedMediaUrl(mPreparedMedia.getDescription().getMediaId());
                }
            }
        }

        @Override
        public void onAddQueueItem(MediaDescriptionCompat description) {
            Log.d(TAG, "onAddQueueItem: CALLED: position in list: " + mPlaylist.size());
            mPlaylist.add(new MediaSessionCompat.QueueItem(description, description.hashCode()));
            mQueueIndex = (mQueueIndex == -1) ? 0 : mQueueIndex;
            mSession.setQueue(mPlaylist);
        }

        @Override
        public void onRemoveQueueItem(MediaDescriptionCompat description) {
            mPlaylist.remove(new MediaSessionCompat.QueueItem(description, description.hashCode()));
            mQueueIndex = (mPlaylist.isEmpty()) ? -1 : mQueueIndex;
            mSession.setQueue(mPlaylist);
        }

        @Override
        public void onPrepare() {
            if (mQueueIndex < 0 && mPlaylist.isEmpty()) {
                // Nothing to play.
                return;
            }

            String mediaId = mPlaylist.get(mQueueIndex).getDescription().getMediaId();
            mPreparedMedia = mPexoInstance.getTreeMap().get(mediaId);
            mSession.setMetadata(mPreparedMedia);

            if (!mSession.isActive()) {
                mSession.setActive(true);
            }
        }

        @Override
        public void onPlay() {
            if (!isReadyToPlay()) {
                // Nothing to play.
                return;
            }

            if (mPreparedMedia == null) {
                onPrepare();
            }

            mPlayback.playFromMedia(mPreparedMedia);

            mMyPrefManager.saveQueuePosition(mQueueIndex);
            mMyPrefManager.saveLastPlayedMediaUrl(mPreparedMedia.getDescription().getMediaId());
        }

        @Override
        public void onSetRepeatMode(int mode) {
            super.onSetRepeatMode(mode);
            // change the repeat mode
            mSession.setRepeatMode(mode);
            mPlayback.setRepeat(mode);

            // disable the shuffle mode
            if (mode == PlaybackStateCompat.REPEAT_MODE_ONE
                    || mode == PlaybackStateCompat.REPEAT_MODE_ALL) {
                mSession.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE);
                mPlayback.setShuffle(PlaybackStateCompat.SHUFFLE_MODE_NONE);
            }
        }

        @Override
        public void onSetShuffleMode(int mode) {
            super.onSetShuffleMode(mode);
            // change the shuffle mode
            mSession.setShuffleMode(mode);
            mPlayback.setShuffle(mode);

            // disable the shuffle mode
            if (mode == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
                mSession.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE);
                mPlayback.setRepeat(PlaybackStateCompat.REPEAT_MODE_NONE);
            }
        }

        @Override
        public void onPause() {
            mPlayback.pause();
        }

        @Override
        public void onStop() {
            mPlayback.stop();
            mSession.setActive(false);
        }

        @Override
        public void onSkipToNext() {
            Log.d(TAG, "onSkipToNext: SKIP TO NEXT " + mQueueIndex);
            if (mPlaylist.size() > 0) {
                if (mSession.getController().getRepeatMode() == PlaybackStateCompat.REPEAT_MODE_ONE) {
                    mPreparedMedia = null;
                    onPlay();
                } else if (mSession.getController().getRepeatMode() == PlaybackStateCompat.REPEAT_MODE_ALL) {
                    // increment and then check using modulus
                    mQueueIndex = (++mQueueIndex) % mPlaylist.size();
                    mPreparedMedia = null;
                    onPlay();
                } else if (mSession.getController().getShuffleMode() == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
                    // change the index based on a random nuit commit mber
                    mQueueIndex = RandomUtils.getRandomWithExclusion(0, mPlaylist.size() - 1, mQueueIndex);
                    mPreparedMedia = null;
                    onPlay();
                } else {
                    // don't perform any operation when the list reaches the last index
                    if (mQueueIndex < mPlaylist.size() - 1) {
                        // increment and then check using modulus
                        mQueueIndex = (++mQueueIndex) % mPlaylist.size();
                        mPreparedMedia = null;
                        onPlay();
                    }
                }
            }
        }

        @Override
        public void onSkipToPrevious() {
            Log.d(TAG, "onSkipToPrevious: SKIP TO PREVIOUS");
            mQueueIndex = mQueueIndex > 0 ? mQueueIndex - 1 : mPlaylist.size() - 1;
            mPreparedMedia = null;
            onPlay();
        }

        @Override
        public void onSeekTo(long pos) {
            mPlayback.seekTo(pos);
        }

        private boolean isReadyToPlay() {
            return (!mPlaylist.isEmpty());
        }
    }

    public class MediaPlayerListener implements PlaybackInfoListener {

        private final ServiceManager mServiceManager;

        MediaPlayerListener() {
            mServiceManager = new ServiceManager();
        }

        @Override
        public void updateUI(String newMediaId) {
            //Log.d(TAG, "updatePlayerView: CALLED: " + newMediaId);
            Intent intent = new Intent();
            intent.setAction(getString(R.string.broadcast_update_ui));
            intent.putExtra(getString(R.string.broadcast_new_media_id), newMediaId);
            sendBroadcast(intent);
        }

        @Override
        public void onPlaybackStateChange(PlaybackStateCompat state) {
            // Report the state to the MediaSession.
            mSession.setPlaybackState(state);

            // Manage the started state of this service.
            switch (state.getState()) {
                case PlaybackStateCompat.STATE_PLAYING:
                case PlaybackStateCompat.STATE_PAUSED:
                    mServiceManager.updateNotification(
                            state,
                            mPlayback.getCurrentMedia().getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI)
                    );
                    break;
                case PlaybackStateCompat.STATE_STOPPED:
                    Log.d(TAG, "onPlaybackStateChange: STOPPED.");
                    mServiceManager.moveServiceOutOfStartedState();
                    break;
            }
        }

        @Override
        public void onBufferedTo(long progress) {
            //Log.d(TAG, "onBufferedTo: CALLED: updating buffer: " + progress);
            Intent intent = new Intent();
            intent.setAction(getString(R.string.broadcast_buffer_update));
            intent.putExtra(BUFFER_PROGRESS, progress);
            sendBroadcast(intent);
        }

        @Override
        public void onSeekTo(long progress, long max) {
            //Log.d(TAG, "onSeekTo: CALLED: updating seekbar: " + progress + ", max: " + max);
            Intent intent = new Intent();
            intent.setAction(getString(R.string.broadcast_seekbar_update));
            intent.putExtra(SEEK_BAR_PROGRESS, progress);
            intent.putExtra(SEEK_BAR_MAX, max);
            sendBroadcast(intent);
        }

        @Override
        public void onPlaybackComplete() {
            Log.d(TAG, "onPlaybackComplete: SKIPPING TO NEXT.");
            mSession.getController().getTransportControls().skipToNext();
        }

        class ServiceManager implements ICallback {

            private String mDisplayImageUri;
            private Bitmap mCurrentArtistBitmap;
            private PlaybackStateCompat mState;
            private GetArtistBitmapAsyncTask mAsyncTask;

            public ServiceManager() {
            }

            public void updateNotification(PlaybackStateCompat state, String displayImageUri) {
                mState = state;

                if (!displayImageUri.equals(mDisplayImageUri)) {
                    // download new bitmap

                    mAsyncTask = new GetArtistBitmapAsyncTask(
                            Glide.with(MediaService.this)
                                    .asBitmap()
                                    .load(displayImageUri)
                                    .listener(new RequestListener<Bitmap>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                            return false;
                                        }
                                    }).submit(), this);

                    mAsyncTask.execute();

                    mDisplayImageUri = displayImageUri;
                } else {
                    displayNotification(mCurrentArtistBitmap);
                }
            }

            public void displayNotification(Bitmap bitmap) {

                // Manage the started state of this service.
                Notification notification = null;
                switch (mState.getState()) {

                    case PlaybackStateCompat.STATE_PLAYING:
                        notification =
                                mMediaNotificationManager.buildNotification(
                                        mState, getSessionToken(), mPlayback.getCurrentMedia().getDescription(), bitmap);

                        if (!mIsServiceStarted) {
                            ContextCompat.startForegroundService(
                                    MediaService.this,
                                    new Intent(MediaService.this, MediaService.class));
                            mIsServiceStarted = true;
                        }

                        startForeground(MediaNotificationManager.NOTIFICATION_ID, notification);
                        break;

                    case PlaybackStateCompat.STATE_PAUSED:
                        stopForeground(false);
                        notification =
                                mMediaNotificationManager.buildNotification(
                                        mState, getSessionToken(), mPlayback.getCurrentMedia().getDescription(), bitmap);
                        mMediaNotificationManager.getNotificationManager()
                                .notify(MediaNotificationManager.NOTIFICATION_ID, notification);
                        break;
                }
            }

            private void moveServiceOutOfStartedState() {
                stopForeground(true);
                stopSelf();
                mIsServiceStarted = false;
            }

            @Override
            public void done(Bitmap bitmap) {
                mCurrentArtistBitmap = bitmap;
                displayNotification(mCurrentArtistBitmap);
            }
        }
    }

    static class GetArtistBitmapAsyncTask extends AsyncTask<Void, Void, Bitmap> {

        private FutureTarget<Bitmap> mBitmap;
        private ICallback mICallback;

        public GetArtistBitmapAsyncTask(FutureTarget<Bitmap> bitmap, ICallback iCallback) {
            this.mBitmap = bitmap;
            this.mICallback = iCallback;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            try {
                return mBitmap.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            mICallback.done(bitmap);
        }

    }

}































