package com.sap.hangeddraw

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import apollo.graphql.api_gateway.LoginQuery
import apollo.graphql.api_gateway.type.ViewModelLogin
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.jcloquell.androidsecurestorage.SecureStorage
import com.sap.hangeddraw.viewmodels.ViewModelUser
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.OkHttpClient


class LoginActivity :  AppCompatActivity() {
    private val BASE_URL = "http://ec2-3-227-32-190.compute-1.amazonaws.com/graphql"
    private lateinit var client: ApolloClient
    private var data : LoginQuery.Login? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        SplashScreen.setContext(this)

        client = setUpApolloClient()
        login.setOnClickListener({
            ClickButton()
        })

        register.setOnClickListener({
            startActivity(Intent(SplashScreen.getContext(), RegisterActivity::class.java))
        })
    }

    private fun ClickButton(){
        var pass = password.text.toString()
        var usr = username.text.toString()

        SplashScreen.setContext(this)

        if(pass.length < 1 || usr.length < 1){
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Error")
            builder.setMessage("Ingresa tu nombre de usuario y contraseña")

            builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                dialog.dismiss()
            }

            builder.show()
            return
        }

        if(hasNetworkAvailable(this))
        {
            val pd = setProgressDialog(this, "Iniciando sesión")
            pd.show()

            var model = ViewModelLogin(password = password.text.toString(),userName = username.text.toString() )
            var exception : ApolloException

            client.newBuilder().build().query(
                LoginQuery(model)
            ).enqueue(object : ApolloCall.Callback<LoginQuery.Data>(){
                override fun onFailure(e: ApolloException) {
                    exception = e
                    pd.dismiss()

                    SplashScreen.getActivity().runOnUiThread {

                        val builder = AlertDialog.Builder(SplashScreen.getActivity())
                        builder.setTitle("Error")
                        var errorStr = "Ocurrió un error"

                        builder.setMessage( errorStr )
                        //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))

                        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                            dialog.dismiss()
                        }

                        builder.show()
                    }
                }
                override fun onResponse(response: Response<LoginQuery.Data>) {
                    data = response.data?.login

                    if(data != null && data?.error != null && data?.error == false)
                    {
                        pd.dismiss()

                        var storage = SecureStorage(SplashScreen.applicationContext())

                        // guardar variables de usuario
                        storage.storeObject("idUser", data?.user?.id!!)
                        storage.storeObject("picture", data?.user?.picture!!)
                        storage.storeObject("lastName", data?.user?.lastName!!)
                        storage.storeObject("name", data?.user?.name!!)
                        storage.storeObject("userName", data?.user?.userName!!)
                        storage.storeObject("email", data?.user?.email!!)
                        storage.storeObject("country", data?.user?.country!!)
                        storage.storeObject("token", data?.token!!)

                        var idUser = storage.getObject("idUser", String::class.java)
                        var picture = storage.getObject("picture", String::class.java)
                        var lastName = storage.getObject("lastName", String::class.java)
                        var name = storage.getObject("name", String::class.java)
                        var userName = storage.getObject("userName", String::class.java)
                        var email = storage.getObject("email", String::class.java)
                        var country = storage.getObject("country", String::class.java)
                        var token = storage.getObject("token", String::class.java)

                        var user = ViewModelUser(idUser = idUser!!, lostGames = 0, totalGames = 0,
                            imageBytes = "", picture = picture!!,lastName = lastName!!, name = name!!,
                            username = userName!!, email = email!!, country = country!!, Token = token!!, wontGames = 0 )

                        SplashScreen.setUser(user)
                        startActivity(Intent(SplashScreen.getContext(),HomeActivity::class.java))
                    }
                    else
                    {
                        pd.dismiss()

                        SplashScreen.getActivity().runOnUiThread {

                            val builder = AlertDialog.Builder(SplashScreen.getActivity())
                            builder.setTitle("Error")
                            var errorStr = data?.response

                            builder.setMessage( errorStr )
                            //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))

                            builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                                dialog.dismiss()
                            }

                            builder.show()
                        }


                    }
                }

            })


        }
        else
        {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Error")
            builder.setMessage("¡Verifica tu conexión a internet!")

            builder.setPositiveButton("Aceptar") { dialog, which ->
                dialog.dismiss()
            }

            builder.show()
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

    fun setProgressDialog(context: Context, message: String): AlertDialog {
        val padding = 50
        val linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayout.setPadding(padding, padding, padding, padding)
        linearLayout.gravity = Gravity.START
        var params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.gravity = Gravity.CENTER
        linearLayout.layoutParams = params

        val progressBar = ProgressBar(context)
        progressBar.isIndeterminate = true
        progressBar.setPadding(0, 0, padding, 0)
        progressBar.layoutParams = params

        params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.gravity = Gravity.CENTER
        val tvText = TextView(context)
        tvText.text = message
        tvText.setTextColor(Color.parseColor("#000000"))
        tvText.textSize = 20.toFloat()
        tvText.layoutParams = params

        linearLayout.addView(progressBar)
        linearLayout.addView(tvText)

        val builder = AlertDialog.Builder(context)
        builder.setCancelable(false)
        builder.setView(linearLayout)

        val dialog = builder.create()
        val window = dialog.window
        if (window != null) {
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(dialog.window?.attributes)
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            dialog.window?.attributes = layoutParams
        }
        return dialog
    }

    private fun hasNetworkAvailable(context: Context): Boolean {
        val service = Context.CONNECTIVITY_SERVICE
        val manager = context.getSystemService(service) as ConnectivityManager?
        val network = manager?.activeNetwork
        return (network != null)
    }
}
