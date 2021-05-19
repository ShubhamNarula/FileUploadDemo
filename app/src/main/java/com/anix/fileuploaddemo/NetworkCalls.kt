package com.anix.fileuploaddemo

interface NetworkCalls {
    //this is used to call stop the uploading
    fun stop()
    //this is used to call upload and resume the uploading
    fun upload()
}