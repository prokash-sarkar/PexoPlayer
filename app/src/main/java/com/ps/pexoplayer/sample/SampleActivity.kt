package com.ps.pexoplayer.sample

import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ps.pexoplayer.PexoEventListener
import com.ps.pexoplayer.PexoPlayerManager
import com.ps.pexoplayer.model.PexoMediaMetadata

class SampleActivity : AppCompatActivity(), PexoEventListener {

    private val TAG: String = SampleActivity::class.java.simpleName

    private lateinit var pexoPlayerManager: PexoPlayerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)

        pexoPlayerManager = PexoPlayerManager(this)
        pexoPlayerManager.setPendingIntentClass(SampleActivity::class.java)
        pexoPlayerManager.subscribeCallBack()
        pexoPlayerManager.setPexoEventListener(this)

        val mediaItemList = ArrayList<PexoMediaMetadata>()

        val pexoMediaMetadata = PexoMediaMetadata(
            "1",
            "Benjamin Tissot",
            "A New Beginning",
            "https://www.bensound.com/royalty-free-music?download=anewbeginning",
            "NA",
            "2019",
            "https://www.bensound.com/bensound-img/anewbeginning.jpg",
            ""
        )

        mediaItemList.add(pexoMediaMetadata)

        pexoPlayerManager.setupNewPlaylist(
            "1", ArrayList<PexoMediaMetadata>(),
            mediaItemList, 0
        )

        Handler().postDelayed({
            pexoPlayerManager.startPlayback()
        }, 15 * 1000)

        Handler().postDelayed({
            pexoPlayerManager.onStop()
        }, 60 * 1000)
    }

    override fun onPlayerMediaControllerConnected(mediaController: MediaControllerCompat?) {
    }

    override fun onPlayerMetadataChanged(pexoMediaMetadata: PexoMediaMetadata?) {
    }

    override fun onPlayerPlaybackStateChanged(state: PlaybackStateCompat?) {
        if (state != null) {
            when (state.state) {
                PlaybackStateCompat.STATE_BUFFERING -> {
                }
                PlaybackStateCompat.STATE_PLAYING -> {
                }
                PlaybackStateCompat.STATE_PAUSED -> {
                }
                PlaybackStateCompat.STATE_ERROR -> {
                }
            }
        }
    }

    override fun onControllerDisconnect() {
    }

    override fun updatePlayerBuffer(progress: Int) {

    }

    override fun updatePlayerSeekBar(progress: Int, max: Int) {
    }

    override fun updateShuffle(state: Int) {
        when (state) {
            PlaybackStateCompat.REPEAT_MODE_ALL -> {
            }
            PlaybackStateCompat.REPEAT_MODE_ONE -> {
            }
            PlaybackStateCompat.REPEAT_MODE_NONE -> {
            }
        }
    }

    override fun updateRepeat(state: Int) {
        when (state) {
            PlaybackStateCompat.SHUFFLE_MODE_ALL -> {
            }
            PlaybackStateCompat.SHUFFLE_MODE_NONE -> {
            }
        }
    }

    override fun prepareLastPlayedMedia() {
        // Prepare the list item to resume the player
        pexoPlayerManager.onFinishedGettingPreviousSessionData(ArrayList<PexoMediaMetadata>())
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.i(TAG, "onConfigurationChanged()")
        pexoPlayerManager.setConfigurationChanged(newConfig)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy()")
        pexoPlayerManager.unSubscribeCallBack()
    }

}

