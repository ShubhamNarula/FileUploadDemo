package com.anix.fileuploaddemo

import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask


class FileUploadRepo() : NetworkCalls {
    private lateinit var storageRefrence: StorageReference
    private lateinit var networkConnectivity: NetworkConnectivityService
    private var uploadTask: UploadTask? = null
    private lateinit var onProgress: (FileModel) -> Unit
    private var currentProgress: Int = 0

    init {
        storageRefrence = FirebaseStorage.getInstance().reference.child("Videos")
        networkConnectivity = NetworkConnectivityService(this)
    }

    fun fileUploadToServer(filePath: Uri, onSuccess: () -> Unit, onProgress: (FileModel) -> Unit) {
        uploadTask = storageRefrence.putFile(filePath)
        this.onProgress = onProgress
        uploadTask!!.addOnSuccessListener {
            Log.e("FILE_UPLOAD", "onSuccess:")
            networkConnectivity.interrupt()
            onSuccess()
        }.addOnProgressListener(OnProgressListener<UploadTask.TaskSnapshot>() {
            val progress = (100 * it.bytesTransferred) / it.totalByteCount
            currentProgress = progress.toInt()
            onProgress(FileModel(progress = currentProgress, isPaused = false))
        })
    }

    override fun stop() {
        if (uploadTask != null) {
            uploadTask!!.pause()
            onProgress(FileModel(progress = currentProgress, isPaused = true))
        }
    }

    override fun upload() {
        if (uploadTask != null) {
            uploadTask!!.resume()
            onProgress(FileModel(progress = currentProgress, isPaused = false))
        }
    }
}