package com.anix.fileuploaddemo

import android.os.CountDownTimer
import android.util.Log

class NetworkConnectivityService(val reference : NetworkCalls) : Thread() {

    //here we start a thread
    //due to this we can check real time net speed with [TrafficUtils.getNetworkSpeed()]
    override fun run() {
        super.run()
        var i =0
        val count:Int=10000
        while(i<count){
            i++
            val netSpeed = TrafficUtils.getNetworkSpeed()
            Log.e("NetworkSpeed", "netSpeed: $netSpeed", )
            if (netSpeed > "100KB"){
                reference.upload()
            }else{
                reference.stop()
            }
            try{
                sleep(500)
            }catch (e : Exception){
            }
        }
    }


}
