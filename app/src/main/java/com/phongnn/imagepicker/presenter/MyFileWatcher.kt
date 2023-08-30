package com.phongnn.imagepicker.presenter

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.*
import java.nio.file.FileSystems
import java.nio.file.StandardWatchEventKinds

@Suppress("UNCHECKED_CAST")
class MyFileWatcher(
    private val context: Context,
    private val directoryPath: String,
    private val myOwnFileChanged: MyOwnFileChanged,
) {

    private val watchScope = CoroutineScope(Dispatchers.Default + Job())
    private lateinit var watchJob: Job

    interface MyOwnFileChanged {
        fun onChangedFile()
    }

    init {
        startWatching()
    }

    private fun startWatching() {
        watchJob = watchScope.launch {
            val path = java.nio.file.Paths.get(directoryPath)
            val watchService = withContext(Dispatchers.IO) {
                FileSystems.getDefault().newWatchService()
            }
            withContext(Dispatchers.IO) {
                path.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_CREATE
                )
            }

            while (isActive) {
                val key = withContext(Dispatchers.IO) {
                    watchService.take()
                }
                key.pollEvents().forEach { event ->
                    val kind = event.kind() as java.nio.file.WatchEvent.Kind<*>
                    val fileName =
                        (event as java.nio.file.WatchEvent<java.nio.file.Path>).context().fileName.toString()

                    when (kind) {
                        StandardWatchEventKinds.ENTRY_CREATE -> {
                            showToast("File created: $fileName")
                            myOwnFileChanged.onChangedFile()
                        }
                        StandardWatchEventKinds.ENTRY_DELETE -> {
                            showToast("File deleted: $fileName")
                            myOwnFileChanged.onChangedFile()
                        }
                        else -> {

                        }
                    }
                }
                key.reset()
            }
        }
    }

    private suspend fun showToast(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun stopWatching() {
        watchJob.cancel()
    }
}
