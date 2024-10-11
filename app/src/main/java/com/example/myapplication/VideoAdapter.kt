package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.data.VideoModel
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import java.util.concurrent.TimeUnit

class VideoAdapter(
    private val videoList: List<VideoModel>,
    private val onDelete: (VideoModel) -> Unit,
    private val onShare: (VideoModel) -> Unit,
    private val onOpen: (VideoModel) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_VIDEO = 0
        private const val VIEW_TYPE_AD = 1
        private const val ITEMS_PER_ROW = 2
        private const val AD_POSITION_INTERVAL = ITEMS_PER_ROW * 3 // Ad after every 6 video items
    }

    override fun getItemViewType(position: Int): Int {
        return if ((position + 1) % 7 == 0) { // Every 7 item position is an ad
            VIEW_TYPE_AD
        } else {
            VIEW_TYPE_VIDEO
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_AD) {
            val adView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_native_ad, parent, false) // Custom layout for native ad
            AdViewHolder(adView)
        } else {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_video, parent, false)
            VideoViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        // Include space for ads. Add one additional item for every 9 videos
        return videoList.size + (videoList.size / AD_POSITION_INTERVAL)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is VideoViewHolder) {
            // Calculate the correct index for the video list, ignoring the ads
            val videoPosition = position - (position / 7) // Adjust index for ads
            val currentItem = videoList[videoPosition]

            holder.videoSize.text = formatSize(currentItem.size)
            holder.videoDuration.text = formatDuration(currentItem.duration)
            Glide.with(holder.itemView)
                .load(currentItem.uri)
                .into(holder.videoThumbnail)

            holder.btnOptions.setOnClickListener {
                showPopupMenu(holder.itemView, currentItem)
            }
        } else if (holder is AdViewHolder) {
            // Bind your native ad here. Example:
            holder.bindNativeAd()
        }
    }

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val videoThumbnail: ImageView = itemView.findViewById(R.id.ivVideoThumbnail)
        val videoSize: TextView = itemView.findViewById(R.id.tvVideoSize)
        val videoDuration: TextView = itemView.findViewById(R.id.tvVideoDuration)
        val btnOptions: ImageView = itemView.findViewById(R.id.btOptions)
    }

    class AdViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val template: TemplateView = itemView.findViewById(R.id.my_template)
        fun bindNativeAd() {
            val adLoader =
                AdLoader.Builder(itemView.context, "ca-app-pub-3940256099942544/2247696110")
                    .forNativeAd { nativeAd ->
                        val styles = NativeTemplateStyle.Builder().build()
                        template.setStyles(styles)
                        template.setNativeAd(nativeAd)

                    }
                    .build()

            adLoader.loadAd(AdRequest.Builder().build())
        }
    }

    fun showPopupMenu(view: View, video: VideoModel) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.inflate(R.menu.video_item_menu) // Create a menu resource for the options
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_delete -> {
                    onDelete(video)
                    true
                }

                R.id.action_share -> {
                    onShare(video)
                    true
                }

                R.id.action_open -> {
                    onOpen(video)
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }

    private fun formatDuration(durationMillis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun formatSize(sizeBytes: Long): String {
        val sizeKb = sizeBytes / 1024.0
        val sizeMb = sizeKb / 1024.0
        val sizeGb = sizeMb / 1024.0

        return when {
            sizeGb >= 1 -> String.format("%.2f GB", sizeGb)
            sizeMb >= 1 -> String.format("%.2f MB", sizeMb)
            else -> String.format("%.2f KB", sizeKb)
        }
    }
}




