package com.sap.hangeddraw

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.jcloquell.androidsecurestorage.SecureStorage
import com.sap.hangeddraw.viewmodels.ViewModelUser
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        supportActionBar?.setTitle("Mi perfil")

        var user : ViewModelUser = SplashScreen.getUser()!!
        name.text = user.name + " " + user.lastName
        lastname.text = user.country
        email.text = user.email
        username.text = user.username

        if(user.picture.length > 0 )
            Picasso.get().load(user.picture).into(avatar)

        logout.setOnClickListener({
            Click()
        })
    }

    fun Click(){
        var storage = SecureStorage(SplashScreen.applicationContext())

        // guardar variables de usuario
        storage.storeObject("idUser", "")
        storage.storeObject("picture", "")
        storage.storeObject("lastName", "")
        storage.storeObject("name", "")
        storage.storeObject("userName", "")
        storage.storeObject("email", "")
        storage.storeObject("country", "")
        storage.storeObject("token", "")

        var intent : Intent = Intent(this,LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }
}
