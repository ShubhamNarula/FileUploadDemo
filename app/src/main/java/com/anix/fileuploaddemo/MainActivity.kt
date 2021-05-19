package com.anix.fileuploaddemo

import android.Manifest
import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.jaiselrahman.filepicker.activity.FilePickerActivity
import com.jaiselrahman.filepicker.config.Configurations
import com.jaiselrahman.filepicker.model.MediaFile

class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 1001
    private lateinit var btnUpload: Button
    private val REQUEST_TAKE_GALLERY_VIDEO = 1002
    private lateinit var progressBar: ProgressBar
    private lateinit var txtProgressBarPercentabe: TextView
    private lateinit var fileUploadViewModel: FileUploadViewModel
    private var isFileUploadStart = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        clickEvents()
        subscribeObserver()

    }

    fun init() {
        btnUpload = findViewById(R.id.btn_upload)
        progressBar = findViewById(R.id.progress_bar)
        txtProgressBarPercentabe = findViewById(R.id.txt_percentage)
        progressBar.visibility = View.GONE

        //We can not create ViewModel on our own. We need ViewModelProviders utility provided by Android to create ViewModels.

        //But ViewModelProviders can only instantiate ViewModels with no arg constructor.

        //So if I have a ViewModel with multiple arguments, then I need to use a Factory that I can pass to ViewModelProviders to use when an instance of MyViewModel is required
        val factory = FileUploadViewModelFactory("FileUploadToServer")
        fileUploadViewModel = ViewModelProvider(this@MainActivity, factory).get(FileUploadViewModel::class.java)
    }

    fun clickEvents() {

        btnUpload.setOnClickListener {
            if (!isFileUploadStart) {
                if (!checkPermissionForGallery()) {
                    requestPermission()
                } else {
                    pickVideoFromGallery()
                }
            }
        }
    }
    //here is observer basically in this we can
    //the observe update data
    @SuppressLint("SetTextI18n")
    fun subscribeObserver() {

        fileUploadViewModel.progressState.observe(this, {
            if (it.isPaused) {
                txtProgressBarPercentabe.text = "Network error...!!"
            } else {
                txtProgressBarPercentabe.text = "${it.progress} %"
                progressBar.progress = it.progress
            }


        })
        fileUploadViewModel.successState.observe(this, {
            if (it) {
                txtProgressBarPercentabe.visibility = View.GONE
                progressBar.visibility = View.GONE
                isFileUploadStart=false
                Toast.makeText(
                    this,
                    "File upload successfully.",
                    Toast.LENGTH_LONG
                ).show()
            }

        })
    }

    //this function is used to get image and video from gallery
    fun pickVideoFromGallery() {
        val intent = Intent(this, FilePickerActivity::class.java)
        intent.putExtra(
            FilePickerActivity.CONFIGS, Configurations.Builder()
                .setCheckPermission(true)
                .setShowFiles(false)
                .setShowImages(true)
                .setShowAudios(false)
                .setShowVideos(true)
                .setIgnoreNoMedia(false)
                .setSkipZeroSizeFiles(true)
                .enableVideoCapture(false)
                .enableImageCapture(false)
                .setIgnoreHiddenFile(true)
                .setMaxSelection(1)
                .build()
        )
        startActivityForResult(intent, REQUEST_TAKE_GALLERY_VIDEO)
    }

    //here this is a function
    //Check the Gallery Permission
    fun checkPermissionForGallery(): Boolean {
        val resultCamera =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return resultCamera == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        requestPermissions(
            this,
            arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE),
            PERMISSION_REQUEST_CODE
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults.size > 0) {
                val locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val cameraAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (locationAccepted && cameraAccepted) {
                } else {
                    Toast.makeText(
                        this,
                        "Permission Denied, You cannot access gallery data.",
                        Toast.LENGTH_LONG
                    ).show()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE)) {
                            showMessageOKCancel(
                                "You need to allow access to both the permissions"
                            ) { dialog, which ->
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    requestPermissions(
                                        arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE),
                                        PERMISSION_REQUEST_CODE
                                    )
                                }
                            }
                            return
                        }
                    }
                }
            }
        }
    }


    private fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this@MainActivity)
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }


    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                val mediaFiles: List<MediaFile>? =
                    data!!.getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES)
                progressBar.visibility = View.VISIBLE
                txtProgressBarPercentabe.visibility = View.VISIBLE
                //here we call file upload to the server
                fileUploadViewModel.uploadFileToServer(mediaFiles!![0].uri)
                //this is used to manage the check if file already uploading
                isFileUploadStart=true


            }// MEDIA GALLERY

        }
    }


}