# PexoPlayer

[![AppCenter](https://build.appcenter.ms/v0.1/apps/d918b5ba-166b-4637-be67-e9567c0b930e/branches/master/badge)](https://build.appcenter.ms/v0.1/apps/d918b5ba-166b-4637-be67-e9567c0b930e/branches/master/badge) [![](https://jitpack.io/v/prokash-sarkar/PexoPlayer.svg)](https://jitpack.io/#prokash-sarkar/PexoPlayer)[![Codacy Badge](https://api.codacy.com/project/badge/Grade/e48ceb2fe33f48e8b6cffd9494efb077)](https://www.codacy.com/app/prokash-sarkar/PexoPlayer?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=prokash-sarkar/PexoPlayer&amp;utm_campaign=Badge_Grade)

### Specs
[![API](https://img.shields.io/badge/API-15%2B-orange.svg?style=flat)](https://android-arsenal.com/api?level=16)

An audio streaming library based on [ExoPlayer](https://exoplayer.dev) and [Audio App Architecture](https://developer.android.com/guide/topics/media-apps/audio-app/building-an-audio-app)  

## Features

  - Baked with ExoPlayer 
  - Easy media controls
  - Auto notification management

## Download

Use as Gradle dependency

```gradle
allprojects {
 repositories {
  maven {
   url 'https://jitpack.io'
  }
 }
}

dependencies {
 implementation 'androidx.legacy:legacy-support-v4:1.0.0'
 implementation 'com.github.prokash-sarkar:PexoPlayer:v1.0.0-alpha-2-java'
}
```

## Usage

**Required Permissions**

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />

<service android:name="com.ps.pexoplayer.services.MediaService">
     <intent-filter>
          <action android:name="android.media.browse.MediaBrowserService" />
     </intent-filter>
</service>

<receiver android:name="androidx.media.session.MediaButtonReceiver">
      <intent-filter>
           <action android:name="android.intent.action.MEDIA_BUTTON" />
      </intent-filter>
</receiver>
```

**Initialize the** ```PexoPlayerManager```

```Java
public PexoPlayerManager pexoPlayerManager = new PexoPlayerManager(context);
```

In your activity, notify the ```PexoPlayerManager``` for ```onConfigurationChanged()```, ```onResume()``` and ```onDestroy()``` lifecycle state changes

```Java
// Call inside "onCreate()"
pexoPlayerManager.subscribeCallBack();

// Call inside "onConfigurationChanged()"
pexoPlayerManager.setConfigurationChanged(newConfig);

// Call inside "onDestroy()"
pexoPlayerManager.unSubscribeCallBack();
```

**Initialize a Playlist**

PexoPlayer takes a custom object wrapped in ```PexoMediaMetadata```. You need to build the object with your own data e.g. contentId, artistName, playUrl, etc. You can also pass your main list to retrieve it later from the PexoPlayer instance.

```java

List<PexoMediaMetadata> mediaItemList = new ArrayList<PexoMediaMetadata>();

// Assuming you have a data object named as "data"
PexoMediaMetadata pexoMediaMetadata = new PexoMediaMetadata(
                        data.getContentID(),
                        data.getArtistname(),
                        data.getTitle(),
                        data.getPlayUrl(),
                        data.getLabelname(),
                        data.getReleaseDate(),
                        data.getImage())
                        
mMediaItems.add(pexoMediaMetadata);

// playlistId must be unique
// orignalSongList is optional, you can pass it for future reference
pexoPlayerManager.setupNewPlaylist(playlistId, orignalSongList, mediaItemList, position);
```

**Start playback**

```java
pexoPlayerManager.startPlayback();
```

**Toggle playback state**

> This will perform the opposite operation for current playback state. e.g. If playing the player will stop, If stopped the player will start playing again
>

```java
pexoPlayerManager.onTogglePlayPause();
```

**Play** 

```java
pexoPlayerManager.onPlay();
```

**Pause**

```java
pexoPlayerManager.onPause();
```

**Stop**

```java
pexoPlayerManager.onStop();
```

**Skip to next**

```java
pexoPlayerManager.skipToNextTrack()
```

**Skip to previous**

```java
pexoPlayerManager.skipToPreviousTrack()
```

**Skip to a position**

```java
pexoPlayerManager.skipToTrack(position)
```

**Repeat a track**

```java
pexoPlayerManager.repeatTrack()
```

**Shuffle a track**

```java
pexoPlayerManager.shuffleTrack()
```

**Media position seeking**

> PexoPlayer comes with a built in ```SeekBar``` which can perform the media seeking by itself. The following widget will seek the media position in player automatically. Make sure to connect and disconnect the ```SeekBar``` in PexoPlayer's ```onPlayerMediaControllerConnected()``` and ```onControllerDisconnect()``` method

```xml
<com.ps.pexoplayer.widget.MediaSeekBar    
 android:id="@+id/seek_bar"    
 android:layout_width="match_parent"    
 android:layout_height="wrap_content"                                     
</com.ps.pexoplayer.widget.MediaSeekBar>
```

**Set the event listener callbacks**

```java
 pexoPlayerManager.setPexoEventListener(this);
```

 **This will override the following callbacks**

 ```java
    /*Media controller is connected*/
    @Override
    public void onPlayerMediaControllerConnected(MediaControllerCompat mediaController) {
        // Required if you want to use the Pexo MediaSeekBar
        seekBar.setMediaController(mediaController);
    }

    /*A track has been changed*/
    @Override
    public void onPlayerMetadataChanged(PexoMediaMetadata mediaMetadata) {
    }
    
    /*Playback state has changed e.g. play, pause, stop*/
    @Override
    public void onPlayerPlaybackStateChanged(PlaybackStateCompat state) {
        if (state != null) {
            switch (state.getState()) {
                case PlaybackStateCompat.STATE_BUFFERING:
                    break;
                case PlaybackStateCompat.STATE_PLAYING:
                    break;
                case PlaybackStateCompat.STATE_PAUSED:
                    break;
                case PlaybackStateCompat.STATE_ERROR:
                    break;
            }
        }
    }

    /*Controller has been disconnected*/
    @Override
    public void onControllerDisconnect() {
        // Required if you want to use the Pexo MediaSeekBar
        seekBar.disconnectController();
    }
    
    /*Update the seekbar's secondary position here*/
    @Override
    public void updatePlayerBuffer(int progress) {
        seekBar.setSecondaryProgress(progress);
    }

    /*Update the seekbar position here*/
    @Override
    public void updatePlayerSeekBar(int progress, int max) {
        seekBar.setProgress(progress);
    }

    /*Shuffle status has changed*/
    @Override
    public void updateShuffle(int state) {
        switch (state) {
            case PlaybackStateCompat.SHUFFLE_MODE_ALL:
                break;
            case PlaybackStateCompat.SHUFFLE_MODE_NONE:
                break;
        }
    }

    /*Repeat status has changed*/
    @Override
    public void updateRepeat(int state) {
        switch (state) {
            case PlaybackStateCompat.REPEAT_MODE_ALL:
                break;
            case PlaybackStateCompat.REPEAT_MODE_ONE:
                break;
            case PlaybackStateCompat.REPEAT_MODE_NONE:
                break;
        }
    }

    /*You can reload a previously played playlist by passing it here
    If you can't supply the previous list, pass an empty one*/
    @Override
    public void prepareLastPlayedMedia() {
      final List<PexoMediaMetadata> mediaItems = new ArrayList<>();
      // Prepare the list item to resume the player
      pexoPlayerManager.onFinishedGettingPreviousSessionData(mediaItems);
    }
 ```

**Notification Management**

> PexoPlayer creates and handles the notification by itself after defining the proper media data object to the Playlist. It performs the play, pause and track change events from notification by itself. Callbacks are also provided by an interface.
>

If there's a track change in notification with "Next/Previous" you will get an event in
```onPlayerMetadataChanged(PexoMediaMetadata metadata){}```

If there's a playback state change in notification with "Play/Pause" you will get an event in ```onPlayerPlaybackStateChanged(PlaybackStateCompat state){}```

Also set an activity for the pending intent, so that a click on notification will get back to your activity

```java
pexoPlayerManager.setPendingIntentClass(MainActivity.class);
```

## Acknowledgement

This project is re-written based on the ground up of streaming audio example by [@mitchtabian](https://github.com/mitchtabian)

License
----

**Free Software, Ta-Da!**
