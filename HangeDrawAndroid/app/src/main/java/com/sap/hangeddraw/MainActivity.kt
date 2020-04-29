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
//    private val BASE_URL = "http://ec2-3-210-210-169.compute-1.amazonaws.com:5000/graphql"
//    private lateinit var client: ApolloClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        //       client = setUpApolloClient()
//        var data : LoginQuery.Login?
//        var model = ViewModelLogin("PAssword.123","agalanaw2" )
//        var modelFile = ViewModelUploadFile(file = "iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAIAAAD8GO2jAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyZpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuNi1jMTExIDc5LjE1ODMyNSwgMjAxNS8wOS8xMC0wMToxMDoyMCAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENDIDIwMTUgKFdpbmRvd3MpIiB4bXBNTTpJbnN0YW5jZUlEPSJ4bXAuaWlkOkNBNTA3QTcxMEQxRTExRTZBMkY5RUNDMEJCN0JFRDNGIiB4bXBNTTpEb2N1bWVudElEPSJ4bXAuZGlkOkNBNTA3QTcyMEQxRTExRTZBMkY5RUNDMEJCN0JFRDNGIj4gPHhtcE1NOkRlcml2ZWRGcm9tIHN0UmVmOmluc3RhbmNlSUQ9InhtcC5paWQ6Q0E1MDdBNkYwRDFFMTFFNkEyRjlFQ0MwQkI3QkVEM0YiIHN0UmVmOmRvY3VtZW50SUQ9InhtcC5kaWQ6Q0E1MDdBNzAwRDFFMTFFNkEyRjlFQ0MwQkI3QkVEM0YiLz4gPC9yZGY6RGVzY3JpcHRpb24+IDwvcmRmOlJERj4gPC94OnhtcG1ldGE+IDw/eHBhY2tldCBlbmQ9InIiPz4PUhT6AAACgUlEQVR42uyWzU4TURTHz7n3zsztwFCGAh1iA4FESJAmGFFxoQtN/HoBoys3+jCujA/ggidgYeLGKInEKIkRqIliQKryVWgppe18z1ynSwgEKJm4kLu6Nzn5/+Z/zrnnDn5o6YU4F4GY1xng3wOY8DFegKQHsQLQe31EliKD4jQOmB4eLX8KCAP/EN3InKCFGi/UlAtGVYaAYNAEhh2oXnfk9RLfrUheOSy5srzJMcm6dDudNE/KYAf0rQKWReYmmFVEJDTQvFKFMZ+cf8zS1wBcONj0YTkW03vUzSouL5HBkeDXb/puhtsCed10KA51B9dvw9Y6QQGZvhDC5hwglOvi7WLQ1YPWpwRfQmuB6Hcf0mxKP1e3pc9flmejqEw/NAsIQDe6sveeKO7zr694fqSaoTw1dJNVtKKt9ejlofGM4zMIJ5utAYHNeXTWCoslxWU03zeg5reNYkHraG1vZWvfFCdfgAR3LqOiiWOa2H/LtldUVx0QHGTN6psPf7T1v5jLl+wNK+HLrObJRsXusXdOMMP2O9DaTT+3kL5lr5oiu/pzZ/cSWFXv/TOzhRjDxUQw7m6hkhTHb1ayrwbYoqYf3W/vJ0DDoFO+asw+DSelTN0TFepZvQ/G+Gg2dE4xrpOD5fWNl5bpk5B4digkheiGXHAjfdYq5ZfeYOeU2t34lCZTNPNneGp5xOCFKzdyc7mOreSdDSfduzI9kMylLtKJ+VEhYCz1XVWcY2YJxcc9Z9dnXiDJ1JV4NHmY5bc5AWiyS1kNPDAdHsWokg3Y7LiObiqCCCOwwGiDjSOEjaI2wig2ejMQJ3gHccbIxPuieWUaLwCZiBVw9l/0HwD+CjAAg6kICYBf6LQAAAAASUVORK5CYIIxNTUz", fileName = "spain.png")
//
//        var modelUserRegister = ViewModelUserInput( name = "Prueba", lastName = "Probando", userName = "agalanaw2",
//            email ="nxp.andrez@hotmail.com", password = "PAssword.123", confirmedPassword = "PAssword.123",
//            country = "Colombia", picture = "https://hangeddrawbucket.s3.us-east-2.amazonaws.com/49c7431f-3ab4-4065-ac3a-315330164c81spain.png"
//            )
//
//        var id = "772c3e0c-1daf-4c1d-8327-8277caae19b6"
//
//        var modelUserRegisterEdit = ViewModelUserInput( name = "Prueba prueba", lastName = "Probando 2", userName = "agalanaw2",
//            email ="nxp.andrez@hotmail.com", password = "", confirmedPassword = "",
//            country = "", picture = ""
//        )
//
//        client.newBuilder().build().query(
//            LoginQuery(model)
//        ).enqueue(object : ApolloCall.Callback<LoginQuery.Data>(){
//            override fun onFailure(e: ApolloException) {
//                //data = e
//            }
//
//            override fun onResponse(response: Response<LoginQuery.Data>) {
//                data = response.data?.login
//            }
//
//        })

//        val secureStorage = SecureStorage(applicationContext())
//        secureStorage.storeObject("hola", "HangedDraw")
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

/*    private fun setUpApolloClient(): ApolloClient {

        val okHttp = OkHttpClient
            .Builder()
        return ApolloClient.builder()
            .serverUrl(BASE_URL)
            .okHttpClient(okHttp.build())
            .build()
    }*/
}
