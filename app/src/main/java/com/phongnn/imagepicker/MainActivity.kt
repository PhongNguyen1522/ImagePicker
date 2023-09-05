package com.phongnn.imagepicker

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment.STYLE_NORMAL
import androidx.fragment.app.DialogFragment.STYLE_NO_FRAME
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import java.io.File

class MainActivity : AppCompatActivity() {

    companion object {
        var isPlaying = false
        var currentSongPosition = -1
    }

    private lateinit var myFolder: String
    private lateinit var binding: ActivityMainBinding
    private lateinit var imageLoader: ImageLoader
    private lateinit var myFileWatcher: MyFileWatcher
    private var imagesList = mutableListOf<MyImage>()
    private var topicList = mutableListOf<String>()
    private lateinit var songListDialogFragment: SongListDialogFragment
    private var imageInfoList = mutableListOf<ImageInfo>()
    private var downloadReceiver: DownloadReceiver? = null
    private lateinit var songList: List<Song>

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val state = intent?.getStringExtra("button_state")
            if (state != null) {
                when (state) {
                    CommonConstant.ACTION_PREVIOUS_SONG -> {
                        startServicePreviousSong()
                    }
                    CommonConstant.ACTION_PAUSE -> {
                        isPlaying = true
                        startServicePause()
                    }
                    CommonConstant.ACTION_PLAY -> {
                        isPlaying = false
                        startServicePlay()
                    }
                    CommonConstant.ACTION_NEXT_SONG -> {
                        startServiceNextSong()
                    }
                    else -> {
                        throw Exception("NO_ACTION")
                    }
                }

            }
        }
    }

    private fun startServiceNextSong() {
        if (currentSongPosition < songList.size - 1) {

            currentSongPosition += 1
            Log.i(CommonConstant.MY_LOG_TAG, "currentSongPosition = $currentSongPosition")

            binding.icPlayPause.visibility = View.VISIBLE
            binding.icPlayPause.setImageResource(R.drawable.ic_pause)

            val intent = Intent(this, MusicService::class.java)
            // Start to play music
            val bundle = Bundle()
            val song = songList[currentSongPosition]
            bundle.putParcelable("object_song", song)
            intent.putExtras(bundle)
            intent.action = MusicService.ACTION_PLAY
            startService(intent)
            // Update Info in MainActivity
            binding.tvSongName.text = song.title
            binding.tvSongWriter.text = song.artist
        } else {
            binding.icPlayPause.visibility = View.VISIBLE
            binding.icPlayPause.setImageResource(R.drawable.ic_pause)

            val song = songList[songList.size - 1]

            val intent = Intent(this, MusicService::class.java)
            // Start to play music
            val bundle = Bundle()
            bundle.putParcelable("object_song", song)
            intent.putExtras(bundle)
            intent.action = MusicService.ACTION_PLAY
            startService(intent)
            // Update Info in MainActivity
            binding.tvSongName.text = song.title
            binding.tvSongWriter.text = song.artist
        }
    }

    private fun startServicePlay() {
        if (!isPlaying) {
            binding.icPlayPause.visibility = View.VISIBLE
            binding.icPlayPause.setImageResource(R.drawable.ic_pause)
            val intent = Intent(this@MainActivity, MusicService::class.java)
            intent.putExtra("action", CommonConstant.PLAY)
            startService(intent)
        }
    }

    private fun startServicePause() {
        if (isPlaying) {
            binding.icPlayPause.visibility = View.VISIBLE
            binding.icPlayPause.setImageResource(R.drawable.ic_play)
            val intent = Intent(this@MainActivity, MusicService::class.java)
            intent.putExtra("action", CommonConstant.PAUSE)
            startService(intent)
        }
    }

    private fun startServicePreviousSong() {
        if (currentSongPosition > 0) {

            currentSongPosition -= 1
            Log.i(CommonConstant.MY_LOG_TAG, "currentSongPosition = $currentSongPosition")

            binding.icPlayPause.visibility = View.VISIBLE
            binding.icPlayPause.setImageResource(R.drawable.ic_pause)

            val intent = Intent(this, MusicService::class.java)
            // Start to play music
            val bundle = Bundle()
            val song = songList[currentSongPosition]
            bundle.putParcelable("object_song", song)
            intent.putExtras(bundle)
            intent.action = MusicService.ACTION_PLAY
            startService(intent)
            // Update Info in MainActivity
            binding.tvSongName.text = song.title
            binding.tvSongWriter.text = song.artist
        } else {
            binding.icPlayPause.visibility = View.VISIBLE
            binding.icPlayPause.setImageResource(R.drawable.ic_pause)

            val song = songList[0]

            val intent = Intent(this, MusicService::class.java)
            // Start to play music
            val bundle = Bundle()
            bundle.putParcelable("object_song", song)
            intent.putExtras(bundle)
            intent.action = MusicService.ACTION_PLAY
            startService(intent)
            // Update Info in MainActivity
            binding.tvSongName.text = song.title
            binding.tvSongWriter.text = song.artist
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(CommonConstant.MY_LOG_TAG, "onCreate()")
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createImagePickerFolder()

        imageLoader = ImageLoaderImpl.getInstance(this)
        songList = getSongListFromDir()

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

        // Load stored images into InfoImageList
        getInfoImageFromDir()

        // Register the LocalBroadcastReceiver to listen for data
        val filter = IntentFilter(CommonConstant.ACTION_UPDATE_BUTTON_STATE)
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)
    }


    override fun onResume() {
        Log.d(CommonConstant.MY_LOG_TAG, "onResume()")
        super.onResume()

        // Show song list dialog
        binding.cvMusicNote.setOnClickListener {
            showSongListDialog()
        }

        createImagePickerFolder()
        // Init Recycler View
        initRecyclerView()
        // start service for play music
        onClickPlayOrPauseMusic()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initRecyclerView() {

        val myChildImageLayoutManager =
            LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)

        val childImageAdapter =
            ChildImageAdapter(
                this@MainActivity,
                imagesList,
                imageInfoList,
                object : ChildImageAdapter.ItemClickListener {

                    override fun onDownloadImageToStorage(myImage: MyImage, position: Int) {
                        try {
                            imageLoader.downLoadImageToStorage(this@MainActivity, myImage)
                            // Create filter
                            val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                            downloadReceiver = DownloadReceiver(myImage, position)
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

                    override fun onClickButtonNone() {
                        binding.imvImageFrame.setImageBitmap(null)
                    }
                })

        val topicImageAdapter =
            TopicImageAdapter(topicList, object : TopicImageAdapter.TopicClickListener {
                override fun onItemClick(topic: String) {
                    for (i in 0 until imagesList.size) {
                        if (topic.equals(imagesList[i].folder, false)) {
                            myChildImageLayoutManager.scrollToPositionWithOffset(i, 0)
                            break
                        }
                    }
                }
            })


        binding.rcvImagesTopic.apply {
            layoutManager =
                LinearLayoutManager(
                    this@MainActivity,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
            adapter = topicImageAdapter
        }

        binding.rcvImages.apply {
            layoutManager = myChildImageLayoutManager
            adapter = childImageAdapter
        }

        binding.rcvImages.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val firstVisibleItem = myChildImageLayoutManager.findFirstVisibleItemPosition()
                val lastVisibleItem = myChildImageLayoutManager.findLastVisibleItemPosition()

                if (firstVisibleItem != RecyclerView.NO_POSITION && lastVisibleItem != RecyclerView.NO_POSITION) {
                    myFolder = childImageAdapter.getItemAtPosition(firstVisibleItem).folder
                    // Find the position of the topic
                    for (t in 0 until topicList.size) {
                        if (myFolder.equals(topicList[t], false)) {
                            topicImageAdapter.selectedPosition = t
                            topicImageAdapter.notifyDataSetChanged()
                        }
                    }
                }

            }
        })
    }

    private fun loadingImage(apiCallBack: ApiCallBack) {
        imageLoader.loadImage(apiCallBack)
    }

    private fun showSongListDialog() {
        songListDialogFragment =
            SongListDialogFragment(songList, object : MusicServiceListener {
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
    }

    private fun createImagePickerFolder() {
        val downloadDirectory = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        ).toString()

        val folderName = "ImagePicker"
        val subFolderPath = "$downloadDirectory/$folderName"
        val subFolder = File(subFolderPath)

        // Check if the folder "ImagePicker" exists, and create it if it doesn't
        if (!subFolder.exists()) {
            if (subFolder.mkdirs()) {
                // Folder created successfully
                Log.d(CommonConstant.MY_LOG_TAG, "Folder '$folderName' created")
            } else {
                // Failed to create folder
                Log.e(CommonConstant.MY_LOG_TAG, "Failed to create '$folderName' folder")
                // Handle the error gracefully
            }
        } else {
            // Folder already exists
            Log.d(CommonConstant.MY_LOG_TAG, "Folder '$folderName' already exists")
        }
    }

    private fun getSongListFromDir(): List<Song> {
        return imageLoader.getAllMusicFromLocalStorage(
            this@MainActivity,
            CommonConstant.MY_MUSIC_DIR
        )
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

        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)

        myFileWatcher.stopWatching()
    }

    inner class DownloadReceiver(
        private val myImage: MyImage,
        private val position: Int,
    ) :
        BroadcastReceiver() {
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

                        val localUriIndex =
                            cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                        val downloadedUriString = cursor.getString(localUriIndex)
                        val downloadedUri = Uri.parse(downloadedUriString)
                        val filePath = downloadedUri.path // File path in the device's storage
                        // Now you have the file path to the downloaded image
                        if (filePath != null) {
                            // Update recycler view
                            binding.rcvImages.adapter?.notifyItemChanged(position)
                            binding.imvImageFrame.setImageURI(Uri.parse(filePath))
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

}