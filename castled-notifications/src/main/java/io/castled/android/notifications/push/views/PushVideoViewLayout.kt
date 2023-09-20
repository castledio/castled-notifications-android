package io.castled.android.notifications.push.views

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.widget.*
import io.castled.android.notifications.R

class PushVideoViewLayout(context: Context, attrs: AttributeSet) :  FrameLayout(context, attrs) {

    private val videoTitleView: TextView
        get() = findViewById(R.id.castled_push_video_title)
    private val videoDescriptionView: TextView
        get() = findViewById(R.id.castled_push_video_description)
    val videoView: VideoView
        get() = findViewById(R.id.castled_push_video)

    fun setVideoTitle(title: String) {
        videoTitleView.text = title
    }

    fun setVideoDescription(desc: String) {
        videoDescriptionView.text = desc
    }

    fun setVideoContent(videoUrl: String) {
        // Use a MediaController to enable play, pause, forward, etc options.
        val mediaController = MediaController(context)
        mediaController.setAnchorView(videoView)

        // Set the video URL to VideoView
        videoView.setMediaController(mediaController)
        videoView.setVideoURI(Uri.parse(videoUrl))
        // videoView.requestFocus()
        // Start the VideoView
        // videoView.start()
    }

}