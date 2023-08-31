package com.phongnn.imagepicker

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.phongnn.imagepicker.data.model.ImageInfo
import com.phongnn.imagepicker.data.model.MyImage
import com.phongnn.imagepicker.data.model.Song
import com.phongnn.imagepicker.data.utils.CommonConstant
import com.phongnn.imagepicker.databinding.ActivityMainBinding
import com.phongnn.imagepicker.presenter.*
import com.phongnn.imagepicker.presenter.callback.*
import com.phongnn.imagepicker.ui.adapter.ChildImageAdapter
import com.phongnn.imagepicker.ui.adapter.TopicImageAdapter
import com.phongnn.imagepicker.ui.fragment.SongListDialogFragment
import com.phongnn.imagepicker.ui.service.MusicService
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    companion object {
        var isPlaying = false
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var imageLoader: ImageLoader
    private lateinit var myFileWatcher: MyFileWatcher
    private var imagesList = mutableListOf<MyImage>()
    private var topicList = mutableListOf<String>()
    private lateinit var songListDialogFragment: SongListDialogFragment
    private var imageInfoList = mutableListOf<ImageInfo>()
    private var downloadReceiver: DownloadReceiver? = null
    // Define an action string for the notification click
    private val notificationClickAction = "com.example.app.NOTIFICATION_CLICK"


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(CommonConstant.MY_LOG_TAG, "onCreate()")
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageLoader = ImageLoaderImpl.getInstance(this)

        // Loading Images From API into RecyclerView
        runBlocking {
            launch(Dispatchers.IO) {
                loadingImage(object : ApiCallBack {

                    override fun onImageTopicReturn(topic: String) {
                        val tempTopicList = mutableListOf<String>()
                        tempTopicList.add(topic)
                        topicList.addAll(tempTopicList)
                    }

                    override fun onImageReturn(returnImage: MyImage) {
                        val tempImageList = mutableListOf<MyImage>()
                        tempImageList.add(returnImage)
                        imagesList.addAll(tempImageList)
                    }
                })
            }
        }

        // Initialize myWatcher
        myFileWatcher = MyFileWatcher(this, CommonConstant.MY_IMAGE_DIR, object :
            MyFileWatcher.MyOwnFileChanged {
            override fun onChangedFile() {
                getInfoImageFromDir()
            }

        })

        // Show song list dialog
        binding.cvMusicNote.setOnClickListener {
            showSongListDialog()
        }

        // Load stored images into InfoImageList
        getInfoImageFromDir()

        // Song Info

        // start service for play music
        onClickPlayOrPauseMusic()
    }


    override fun onResume() {
        Log.d(CommonConstant.MY_LOG_TAG, "onResume()")
        super.onResume()
        // Init Recycler View
        initRecyclerView()

        binding.btnNoneImage.setOnClickListener {
            binding.imvImageFrame.setImageBitmap(null)
        }
    }

    private fun initRecyclerView() {
        val childImageAdapter =
            ChildImageAdapter(
                this@MainActivity,
                imagesList,
                imageInfoList,
                object : ChildImageAdapter.ItemClickListener {

                    override fun onDownloadImageToStorage(myImage: MyImage) {
                        try {
                            imageLoader.downLoadImageToStorage(this@MainActivity, myImage)
                            // Create filter
                            val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                            downloadReceiver = DownloadReceiver(myImage)
                            registerReceiver(downloadReceiver, filter)

                        } catch (e: Exception) {
                            Log.e(
                                CommonConstant.MY_LOG_TAG,
                                "onDownloadImage: ${e.message.toString()}"
                            )
                        }
                    }

                    override fun onShowDownloadedImage(imageInfo: ImageInfo) {
                        binding.imvImageFrame.setImageURI(imageInfo.uriPath)
                    }
                })
        binding.rcvImages.apply {
            layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = childImageAdapter
        }

        val topicImageAdapter =
            TopicImageAdapter(topicList, object : TopicImageAdapter.TopicClickListener {
                override fun onItemClick(position: Int) {
                    scrollToPosition(position * 5)
                }

                override fun onItemScroll(position: Int) {

                }

            }, onScrollViewListener())
        binding.rcvImagesTopic.apply {
            layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = topicImageAdapter
        }
    }

    private fun loadingImage(apiCallBack: ApiCallBack) {
        imageLoader.loadImage(apiCallBack)
    }


    private fun scrollToPosition(position: Int) {
        val scrollSize = 160
        binding.hsvMenu.smoothScrollTo(position * scrollSize, 0)
    }

    private fun onScrollViewListener(): Int {

        var currentPosition = 0
        binding.hsvMenu.viewTreeObserver.addOnScrollChangedListener {
            val scrollX = binding.hsvMenu.scrollX
            val scrollViewWidth = binding.hsvMenu.getChildAt(0).width - 700

            val part = scrollViewWidth / 5

            currentPosition = scrollX / part
        }
        return currentPosition
    }

    private fun showSongListDialog() {
        val songList = getSongListFromDir()
        songListDialogFragment = SongListDialogFragment(songList, object : MusicServiceListener {
            override fun onMusicIsPlaying(song: Song) {
                binding.apply {
                    songListDialogFragment.dismiss()
                    tvSongName.text = song.title
                    tvSongWriter.text = song.artist
                    icPlayPause.setImageResource(R.drawable.ic_pause)
                }
            }

        })
        songListDialogFragment.show(supportFragmentManager, "song_list_dialog")
        binding.icPlayPause.visibility = View.VISIBLE
    }

    private fun onClickPlayOrPauseMusic() {
        val startBtn = binding.icPlayPause

        startBtn.setOnClickListener {
            if (!isPlaying) {
                binding.icPlayPause.visibility = View.VISIBLE
                binding.icPlayPause.setImageResource(R.drawable.ic_pause)
                val intent = Intent(this, MusicService::class.java)
                intent.putExtra("action", CommonConstant.PLAY)
                startService(intent)
            } else {
                binding.icPlayPause.visibility = View.VISIBLE
                binding.icPlayPause.setImageResource(R.drawable.ic_play)
                val intent = Intent(this, MusicService::class.java)
                intent.putExtra("action", CommonConstant.PAUSE)
                startService(intent)
            }
        }
    }


    private fun getInfoImageFromDir() {
        imageInfoList.clear()
        val tmp = imageLoader.getAllImagesFromLocalStorage(CommonConstant.MY_IMAGE_DIR)
        imageInfoList.addAll(tmp)
        Log.d(CommonConstant.MY_LOG_TAG, "imageInfoList size: ${imageInfoList.size}")
    }

    private fun getSongListFromDir(): List<Song> {
        val songList =
            imageLoader.getAllMusicFromLocalStorage(this@MainActivity, CommonConstant.MY_MUSIC_DIR)

        for (song in songList) {
            Log.i(CommonConstant.MY_LOG_TAG, song.toString())
        }

        return songList
    }

    override fun onDestroy() {
        Log.d(CommonConstant.MY_LOG_TAG, "onDestroy()")
        super.onDestroy()

        Intent(this@MainActivity, MusicService::class.java).also {
            stopService(it)
        }

        if (downloadReceiver != null) {
            unregisterReceiver(downloadReceiver)
        }

        myFileWatcher.stopWatching()
    }

    inner class DownloadReceiver(private val myImage: MyImage) : BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged")
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {

                // Update myInfoImageList
                Log.d(CommonConstant.MY_LOG_TAG, "Download STATUS_SUCCESSFUL")
                getInfoImageFromDir()

                val downloadManager =
                    context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

                val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

                val query = DownloadManager.Query().setFilterById(downloadId)

                val cursor = downloadManager.query(query)
                if (cursor != null && cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val status = cursor.getInt(columnIndex)

                    if (status == DownloadManager.STATUS_SUCCESSFUL) {

                        val localUriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                        val downloadedUriString = cursor.getString(localUriIndex)
                        val downloadedUri = Uri.parse(downloadedUriString)
                        val filePath = downloadedUri.path // File path in the device's storage
                        // Now you have the file path to the downloaded image
                        if (filePath != null) {
                            binding.imvImageFrame.setImageURI(Uri.parse(filePath))
                            // Update recycler view
//                            binding.rcvImages.adapter?.notifyItemChanged()
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "Non-existed image",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        cursor.close()
                    }
                }

            }
        }
    }


    override fun onPause() {
        Log.d(CommonConstant.MY_LOG_TAG, "onPause()")
        super.onPause()
    }

    override fun onStop() {
        Log.d(CommonConstant.MY_LOG_TAG, "onStop()")
        super.onStop()
    }

    override fun onStart() {
        Log.d(CommonConstant.MY_LOG_TAG, "onStart()")
        super.onStart()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action == notificationClickAction) {
            val mainIntent = Intent(this, MainActivity::class.java)
            mainIntent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT // This brings the activity to the front if it's already running
            startActivity(mainIntent)
        }
    }

}