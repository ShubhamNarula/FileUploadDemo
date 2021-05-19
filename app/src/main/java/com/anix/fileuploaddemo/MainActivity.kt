package com.anix.fileuploaddemo

import android.Manifest
import android.Manifest.permission.*
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.MediaStore
import android.provider.OpenableColumns
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
import androidx.core.net.toUri
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.jaiselrahman.filepicker.activity.FilePickerActivity
import com.jaiselrahman.filepicker.config.Configurations
import com.jaiselrahman.filepicker.model.MediaFile
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import org.apache.commons.io.IOUtils


class MainActivity : AppCompatActivity() {

    val PERMISSION_REQUEST_CODE=1001
    lateinit var btnUpload :  Button
    val REQUEST_TAKE_GALLERY_VIDEO = 1002
    lateinit var  storageRefrence:StorageReference
    lateinit var progressBar:ProgressBar
    lateinit var txtProgressBarPercentabe : TextView
    var countDowntimer : CountDownTimer?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        clickEvents()

    }
    fun init(){
        btnUpload = findViewById(R.id.btn_upload)
        progressBar = findViewById(R.id.progress_bar)
        txtProgressBarPercentabe=findViewById(R.id.txt_percentage)
        progressBar.visibility= View.GONE

        storageRefrence = FirebaseStorage.getInstance().reference.child("Videos")
        val netSpeed = TrafficUtils.getNetworkSpeed()
        Log.e("NetSpeed", "onActivityResult: $netSpeed", )
    }

    fun clickEvents(){

        btnUpload.setOnClickListener {
            if (!checkPermissionForGallery()){
                requestPermission()
            }else{
                pickVideoFromGallery()
            }
        }
    }
    fun pickVideoFromGallery(){
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
    fun checkPermissionForGallery() : Boolean{
        val resultCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return resultCamera == PackageManager.PERMISSION_GRANTED
    }
    private fun requestPermission() {
        requestPermissions(this, arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
    }


  override  fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults)

      when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults.size > 0) {
                val locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val cameraAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (locationAccepted && cameraAccepted) {} else {
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
                    progressBar.visibility= View.VISIBLE
                    storageRefrence.putFile(mediaFiles?.get(0)!!.uri).addOnSuccessListener(object :
                    OnSuccessListener<UploadTask.TaskSnapshot>{
                        override fun onSuccess(p0: UploadTask.TaskSnapshot?) {
                            Log.e("TAG", "onSuccess:", )
                        }

                    }).addOnProgressListener(OnProgressListener<UploadTask.TaskSnapshot>(){
                        val progress= (100*it.bytesTransferred)/it.totalByteCount
                        progressBar.setProgress(progress.toInt())
                        txtProgressBarPercentabe.text=progress.toString()+" "+"%"


                    })

                }// MEDIA GALLERY

            }
        }

    fun checkNetworkSpeed(){
        countDowntimer =   object : CountDownTimer(1000, 5) {
            override fun onTick(l: Long) {
            }

            override fun onFinish() {
                //Code hear to check network speed
                val netSpeed = TrafficUtils.getNetworkSpeed()
                Log.e("NetSpeed", "onActivityResult: $netSpeed", )
            }
        }.start()

    }
}