package com.sap.hangeddraw

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import apollo.graphql.api_gateway.RegisterMutation
import apollo.graphql.api_gateway.UploadFileQuery
import apollo.graphql.api_gateway.type.ViewModelUploadFile
import apollo.graphql.api_gateway.type.ViewModelUserInput
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import kotlinx.android.synthetic.main.activity_login.password
import kotlinx.android.synthetic.main.activity_login.username
import kotlinx.android.synthetic.main.activity_register.*
import okhttp3.OkHttpClient
import java.io.ByteArrayOutputStream
import java.util.*


class RegisterActivity : AppCompatActivity() {

    private val BASE_URL = "http://ec2-3-227-32-190.compute-1.amazonaws.com/graphql"
    private lateinit var client: ApolloClient
    private var data : RegisterMutation.Register? = null
    private var UriFile : String = ""

    companion object {
        //image pick code
        private val IMAGE_PICK_CODE = 1000;
        //Permission code
        private val PERMISSION_CODE = 1001;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        SplashScreen.setContext(this)

        client = setUpApolloClient()

        avatar.setOnClickListener({
            ClickAvatar()
        })

        register.setOnClickListener({
            ClickButton()
        })
    }

    private fun setUpApolloClient(): ApolloClient {

        val okHttp = OkHttpClient
            .Builder()
        return ApolloClient.builder()
            .serverUrl(BASE_URL)
            .okHttpClient(okHttp.build())
            .build()
    }

    private fun ClickAvatar()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_DENIED){
                //permission denied
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE);
                //show popup to request runtime permission
                requestPermissions(permissions, PERMISSION_CODE);
            }
            else{
                //permission already granted
                pickImageFromGallery();
            }
        }
        else{
            //system OS is < Marshmallow
            pickImageFromGallery();
        }
    }

    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        SplashScreen.setContext(this)

        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            avatar.setImageURI(data?.data)

            val bitmap = (avatar.getDrawable() as BitmapDrawable).getBitmap()
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 1, stream)
            val imageArray = stream.toByteArray()
            var image : String = Base64.encodeToString(imageArray, imageArray.size).replace("\n", "")

            var modelFile = ViewModelUploadFile(file = image, fileName = UUID.randomUUID().toString() + ".png")
            val pd = setProgressDialog(this, "Subiendo foto")
            pd.show()

            var dataUploadFile : UploadFileQuery.UploadFile?

            var exceptionUploadFile : ApolloException
            client.newBuilder().build().query(
                UploadFileQuery(modelFile)
            ).enqueue(object : ApolloCall.Callback<UploadFileQuery.Data>(){
                override fun onFailure(e: ApolloException) {
                    exceptionUploadFile = e

                    pd.dismiss()

                    SplashScreen.getActivity().runOnUiThread {

                        val builder = AlertDialog.Builder(SplashScreen.getActivity())
                        builder.setTitle("Error")
                        var errorStr = "El tamaño es superior a 1mg"

                        builder.setMessage( errorStr )

                        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                            dialog.dismiss()
                        }

                        builder.show()
                    }
                }
                override fun onResponse(response: Response<UploadFileQuery.Data>) {

                    dataUploadFile = response.data?.uploadFile

                    if(dataUploadFile != null && dataUploadFile?.error != null && dataUploadFile?.error == false)
                    {
                        pd.dismiss()

                        UriFile = dataUploadFile?.uri.toString()

                    }
                    else
                    {
                        pd.dismiss()

                        SplashScreen.getActivity().runOnUiThread {

                            val builder = AlertDialog.Builder(SplashScreen.getActivity())
                            builder.setTitle("Error")
                            var errorStr = dataUploadFile?.response

                            builder.setMessage( errorStr )

                            builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                                dialog.dismiss()
                            }

                            builder.show()
                        }


                    }
                }

            })
        }

        super.onActivityResult(requestCode, resultCode, data)
    }


    private fun ClickButton(){
        SplashScreen.setContext(this)


        var name = name.text.toString()
        var lastname = username.text.toString()
        var country = country.text.toString()
        var username = username.text.toString()
        var email = email.text.toString()
        var password = password.text.toString()
        var confirmedPassword = confirmedPassword.text.toString()

        if(name.length < 1 || lastname.length < 1
            || country.length < 1 || username.length < 1
            || email.length < 1 || password.length < 1
            || confirmedPassword.length < 1 || UriFile.length < 1)
        {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Error")
            builder.setMessage("Ingresa todos los campos o selecciona una imagen")
            //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))

            builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                dialog.dismiss()
            }

            builder.show()
            return
        }


        if(hasNetworkAvailable(this))
        {
            val pd = setProgressDialog(this, "Registrándote")
            pd.show()

            var modelUserRegister = ViewModelUserInput( name = name, lastName = lastname, userName = username,
                email =email, password = password, confirmedPassword = confirmedPassword,
                country = country, picture = UriFile
                )


            var exceptionRegister : ApolloException

            client.newBuilder().build().mutate(
                RegisterMutation(modelUserRegister)
            ).enqueue(object : ApolloCall.Callback<RegisterMutation.Data>(){
                override fun onFailure(e: ApolloException) {
                    exceptionRegister = e
                    pd.dismiss()

                    SplashScreen.getActivity().runOnUiThread {

                        val builder = AlertDialog.Builder(SplashScreen.getActivity())
                        builder.setTitle("Error")
                        var errorStr = "Ocurrio un error. Intente nuevamente"

                        builder.setMessage( errorStr )

                        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                            dialog.dismiss()
                        }

                        builder.show()
                    }
                }
                override fun onResponse(response: Response<RegisterMutation.Data>) {
                    data = response.data?.register!!

                    if(data != null && data?.error != null && data?.error == false)
                    {
                        pd.dismiss()

                        SplashScreen.getActivity().runOnUiThread {

                            val builder = AlertDialog.Builder(SplashScreen.getActivity())
                            builder.setTitle("Registro exitoso")
                            var errorStr = data?.response
                            errorStr += "Verifica tu correo electrónico. Si no encuentras en la bandeja de entrada, revisa SPAM."
                            builder.setMessage( errorStr )
                            //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))

                            builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                                finish()
                            }

                            builder.show()

                        }
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
