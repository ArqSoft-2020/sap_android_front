package com.sap.hangeddraw

import android.content.Context
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import apollo.graphql.api_gateway.UserInfoQuery
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.jcloquell.androidsecurestorage.SecureStorage

import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient

class MainActivity : AppCompatActivity() {
    init {
        instance = this
    }

    companion object {
        private var instance: MainActivity? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }

    private val BASE_URL = "http://ec2-3-210-210-169.compute-1.amazonaws.com:5000/graphql"
    private lateinit var client: ApolloClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        client = setUpApolloClient()
        var data : UserInfoQuery.User? = null
        client.newBuilder().build().query(
            UserInfoQuery("42735a2c-54ed-4e30-9865-98b4a42933d5")
        ).enqueue(object : ApolloCall.Callback<UserInfoQuery.Data>(){
            override fun onFailure(e: ApolloException) {
                //data = e
            }

            override fun onResponse(response: Response<UserInfoQuery.Data>) {
                data = response.data?.userInfo?.user
            }

        })

        val secureStorage = SecureStorage(applicationContext())
        secureStorage.storeObject("hola", "HangedDraw")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setUpApolloClient(): ApolloClient {

        val okHttp = OkHttpClient
            .Builder()
        return ApolloClient.builder()
            .serverUrl(BASE_URL)
            .okHttpClient(okHttp.build())
            .build()
    }
}
