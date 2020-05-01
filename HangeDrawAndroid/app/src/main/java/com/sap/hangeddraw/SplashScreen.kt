package com.sap.hangeddraw

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.os.Handler
import com.jcloquell.androidsecurestorage.SecureStorage
import com.sap.hangeddraw.viewmodels.ViewModelUser


class SplashScreen : AppCompatActivity() {

    private val SPLASH_TIME_OUT:Long = 1500 // 1 sec

    init {
        instanceStorage = this
    }

    companion object {
        private var instanceStorage: SplashScreen? = null
        private var context: AppCompatActivity? = null
        private var User: ViewModelUser? = null

        fun applicationContext() : Context {
            return instanceStorage!!.applicationContext
        }

        fun setContext(activity: AppCompatActivity){
            context = activity
        }

        fun getContext() : Context{
            return context!!.applicationContext
        }

        fun getActivity() : AppCompatActivity{
            return context!!
        }

        fun getUser(): ViewModelUser? {
            return User;
        }

        fun setUser(user : ViewModelUser?){
            User = user;
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        var storage = SecureStorage(this)

        Handler().postDelayed({

            var idUser = storage.getObject("idUser", String::class.java)
            if(idUser != null && idUser.length > 0)
            {
                var picture = storage.getObject("picture", String::class.java)
                var lastName = storage.getObject("lastName", String::class.java)
                var name = storage.getObject("name", String::class.java)
                var userName = storage.getObject("userName", String::class.java)
                var email = storage.getObject("email", String::class.java)
                var country = storage.getObject("country", String::class.java)
                var token = storage.getObject("token", String::class.java)


                User = ViewModelUser(idUser = idUser, lostGames = 0, totalGames = 0,
                    imageBytes = "", picture = picture!!,lastName = lastName!!, name = name!!,
                    username = userName!!, email = email!!, country = country!!, Token = token!!, wontGames = 0 )

                var intent : Intent = Intent(this,HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }
            else
            {
                var intent : Intent = Intent(this,LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }

            // close this activity
            finish()

        }, SPLASH_TIME_OUT)
    }


    override fun onBackPressed() {
        //super.onBackPressed()
    }


}
