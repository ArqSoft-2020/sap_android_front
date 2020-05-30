package com.sap.hangeddraw

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

class MyFirebaseInstanceIdService : FirebaseInstanceIdService() {

    val TAG = "PushNotifService"
    lateinit var name: String

    override fun onTokenRefresh() {
        // Mengambil token perangkat
        val token = FirebaseInstanceId.getInstance().token
        //Log.d(TAG, "Token perangkat ini: ${token}")
        val a = true
        // Jika ingin mengirim push notifcation ke satu atau sekelompok perangkat,
        // simpan token ke server di sini.
    }
}
