package com.anix.fileuploaddemo

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class FileUploadViewModel(var arg: String) : ViewModel() {
    val _progressState: MutableLiveData<FileModel> = MutableLiveData()
    val progressState: LiveData<FileModel>
        get() {
            return _progressState
        }

    val _successState: MutableLiveData<Boolean> = MutableLiveData()
    val successState: LiveData<Boolean>
        get() {
            return _successState
        }
    val fileUploadRepo = FileUploadRepo()

    //as we are getting the response from repo class in background thread
    //and the viewModel works on main UI thread
    //Thus we have to use [postValue]
    fun uploadFileToServer(filePath: Uri) {
        fileUploadRepo.fileUploadToServer(filePath, {
            _successState.postValue(true)
        }, {
            _progressState.postValue(it)
        })


    }
}