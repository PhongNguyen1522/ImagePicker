package com.phongnn.imagepicker

import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.phongnn.imagepicker.data.dbentity.entity.ImageEntity
import com.phongnn.imagepicker.data.model.MyImage
import com.phongnn.imagepicker.data.model.Song
import com.phongnn.imagepicker.data.utils.CommonConstant
import com.phongnn.imagepicker.databinding.ActivityMainBinding
import com.phongnn.imagepicker.presenter.ImageLoader
import com.phongnn.imagepicker.presenter.ImageLoaderImpl
import com.phongnn.imagepicker.presenter.callback.ApiCallBack
import com.phongnn.imagepicker.presenter.callback.DatabaseCallBack
import com.phongnn.imagepicker.presenter.callback.MusicServiceListener
import com.phongnn.imagepicker.presenter.callback.SongPassedListener
import com.phongnn.imagepicker.ui.adapter.ChildImageAdapter
import com.phongnn.imagepicker.ui.adapter.TopicImageAdapter
import com.phongnn.imagepicker.ui.fragment.SongListDialogFragment
import com.phongnn.imagepicker.ui.service.MusicService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var imageLoader: ImageLoader
    private var imagesList = mutableListOf<MyImage>()
    private var savedImageList = mutableListOf<ImageEntity>()
    private var topicList = mutableListOf<String>()
    private lateinit var songListDialogFragment: SongListDialogFragment

    companion object {
        var isPlaying = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        imageLoader = ImageLoaderImpl.getInstance(this)
        // Loading Images into RecyclerView
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

        // Check saved image list to determine saved images before
        runBlocking {
            launch(Dispatchers.IO) {
                savedImage(object : DatabaseCallBack {
                    override fun onImageSelected(savingImage: ImageEntity) {
                        TODO("Not yet implemented")
                    }
                    override fun onAllImagesReturn(allUsers: List<ImageEntity>) {
                        savedImageList.addAll(allUsers)
                    }
                })
            }
        }

        // Init Recycler View
        initRecyclerView()

        // Show song list dialog
        binding.tvSongName.setOnClickListener {
            showSongListDialog()
        }

        // start service for play music
        onClickPlayOrPauseMusic()
    }

    override fun onResume() {
        super.onResume()
        binding.btnNoneImage.setOnClickListener {
            binding.imvImageFrame.setImageBitmap(null)
        }
    }

    private fun initRecyclerView() {
        val childImageAdapter =
            ChildImageAdapter(
                imagesList,
                savedImageList,
                object : ChildImageAdapter.ItemClickListener {
                    override fun onDownloadImage(imageEntity: ImageEntity) {
                        try {
                            imageLoader.downloadImage(this@MainActivity, imageEntity)
                        } catch (e: SQLiteConstraintException) {
                            Log.e(CommonConstant.MY_LOG_TAG, e.message.toString())
                        }
                    }

                    override fun onShowSavedImage(imageEntity: ImageEntity) {
                        try {
                            Glide.with(this@MainActivity)
                                .load(imageEntity.imageUrl)
                                .into(binding.imvImageFrame)
                        } catch (e: Exception) {
                            Log.e(CommonConstant.MY_LOG_TAG, e.message.toString())
                        }
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

    private fun savedImage(databaseCallBack: DatabaseCallBack) {
        imageLoader.showAllImages(databaseCallBack)
    }

    override fun onDestroy() {
        super.onDestroy()
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

            Log.d(CommonConstant.MY_LOG_TAG, "scrollX = $scrollX")
            Log.d(CommonConstant.MY_LOG_TAG, "scrollViewWidth = $scrollViewWidth")
            Log.d(CommonConstant.MY_LOG_TAG, "currentPosition = $currentPosition")
        }
        return currentPosition
    }

    private fun showSongListDialog() {
        val songList = createSongList()
        songListDialogFragment = SongListDialogFragment(songList, object : MusicServiceListener{
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
//            if (checkEmptySongToPlay()) {
//                binding.icPlayPause.visibility = View.GONE
//            } else {
//                binding.icPlayPause.visibility = View.VISIBLE
//            }
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

    private fun createSongList(): List<Song> {
        return listOf(
            Song(1, "Song 1", "Artist 1", R.raw.vang_trang_khoc),
            Song(2, "Song 2", "Artist 2", R.raw.thuan_theo_y_troi),
            Song(3, "Song 3", "Artist 3", R.raw.suyt_nua_thi),
            Song(4, "Song 4", "Artist 4", R.raw.nag_tho),
            Song(5, "Song 5", "Artist 5", R.raw.mua_xa_nhau),
            Song(6, "Song 6", "Artist 6", R.raw.mot_thoi_da_xa),
            Song(7, "Song 7", "Artist 7", R.raw.ko_thuong_minh_de_thuong_nguoi),
        )
    }
}