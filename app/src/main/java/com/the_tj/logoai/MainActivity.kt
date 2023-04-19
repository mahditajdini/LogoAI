package com.the_tj.logoai

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.ImageReader
import android.media.MediaScannerConnection
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import coil.Coil
import coil.bitmap.BitmapPool
import coil.clear
import coil.load
import coil.request.ImageRequest
import coil.size.Size
import com.example.logoai.R
import com.example.logoai.databinding.ActivityMainBinding
import com.squareup.picasso.Picasso
import com.the_tj.logoai.models.PostModel
import com.the_tj.logoai.utils.TAPSELL_KEY
import com.the_tj.logoai.utils.TEST_IMAGE
import com.the_tj.logoai.utils.ZONE_ID_REWARDED_VIDEO
import com.the_tj.logoai.viewmodel.LogoMakerViewModel
import dagger.hilt.android.AndroidEntryPoint
import ir.tapsell.plus.AdRequestCallback
import ir.tapsell.plus.AdShowListener
import ir.tapsell.plus.TapsellPlus
import ir.tapsell.plus.TapsellPlusInitListener
import ir.tapsell.plus.model.AdNetworkError
import ir.tapsell.plus.model.AdNetworks
import ir.tapsell.plus.model.TapsellPlusAdModel
import ir.tapsell.plus.model.TapsellPlusErrorModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var rewardedResponseId: String

    //binding
    private lateinit var binding: ActivityMainBinding

    //send model
    lateinit var postModel: PostModel

    //viewmodel
    private val viewModel: LogoMakerViewModel by viewModels()

    //other
    lateinit var photoUrl: String
    lateinit var imageName: String
    val items = listOf("256 X 256", "512 X 512", "1024 X 1024")
    var logoSize: String = "256x256"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        TapsellPlus.setDebugMode(Log.DEBUG)
        TapsellPlus.initialize(this, TAPSELL_KEY, object : TapsellPlusInitListener {
            override fun onInitializeSuccess(adNetworks: AdNetworks?) {
                Log.d("onInitializeSuccess", adNetworks!!.name)
            }

            override fun onInitializeFailed(
                adNetworks: AdNetworks?,
                adNetworkError: AdNetworkError?
            ) {
                Log.e(
                    "onInitializeFailed",
                    "ad network: " + adNetworks!!.name + ", error: " + adNetworkError!!.errorMessage
                )
            }

        })
        TapsellPlus.setGDPRConsent(this, true)


        //Adapter for spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)

        binding.apply {
            logoSaveBtn.visibility=View.GONE
            sizePicker.adapter = adapter
            sizePicker.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    // Handle item selection here
                    val selectedItem = parent?.getItemAtPosition(position).toString()
                    when (selectedItem) {
                        "256 X 256" -> logoSize = "256x256"
                        "512 X 512" -> logoSize = "512x512"
                        "1024 X 1024" -> logoSize = "1024x1024"
                    }

                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Handle case when no item is selected
                }
            }
            makeLogoBtn.setOnClickListener {
                imageLoading.visibility = View.VISIBLE
                logoImageView.visibility = View.GONE
                logoSaveBtn.visibility=View.GONE
                TapsellPlus.requestRewardedVideoAd(
                    this@MainActivity,
                    ZONE_ID_REWARDED_VIDEO,
                    object : AdRequestCallback() {
                        override fun response(tapsellPlusAdModel: TapsellPlusAdModel) {
                            super.response(tapsellPlusAdModel)
                            //Ad is ready to show
                            //Put the ad's responseId to your responseId variable
                            rewardedResponseId = tapsellPlusAdModel.responseId
                            if (promptEdt.text.isEmpty() || promptEdt.text.length < 27) {
                                Toast.makeText(
                                    this@MainActivity,
                                    R.string.logoPromptEmptyToast,
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else if (promptEdt.text.length > 162) {
                                Toast.makeText(
                                    this@MainActivity,
                                    R.string.logoPromptTooLongToast,
                                    Toast.LENGTH_LONG
                                ).show()
                            } else if (logoSize.isNullOrBlank()) {
                                Toast.makeText(
                                    this@MainActivity,
                                    R.string.pickLogoSize,
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {

                                TapsellPlus.showRewardedVideoAd(
                                    this@MainActivity,
                                    rewardedResponseId,
                                    object : AdShowListener() {
                                        override fun onOpened(tapsellPlusAdModel: TapsellPlusAdModel) {
                                            super.onOpened(tapsellPlusAdModel)

                                            val prompt = promptEdt.text.toString()
                                            imageName = prompt.substring(0, 10) + ".jpg"
                                            postModel = PostModel(
                                                1,
                                                getString(R.string.preprompt) + prompt,
                                                logoSize
                                            )
                                            viewModel.sendPrompt(postModel)

                                            viewModel.botMessage.observe(this@MainActivity) {
                                                photoUrl = it.data[0].url

                                            }
                                        }

                                        override fun onClosed(tapsellPlusAdModel: TapsellPlusAdModel) {
                                            super.onClosed(tapsellPlusAdModel)
                                        }

                                        override fun onRewarded(tapsellPlusAdModel: TapsellPlusAdModel) {
                                            super.onRewarded(tapsellPlusAdModel)

                                            viewModel.successResult.observe(this@MainActivity) {
                                                if (it) {
                                                    //Picasso.with(this@MainActivity).load(photoUrl).into(logoImageView)
                                                    // Picasso.get().load(TEST_IMAGE).into(logoImageView)
                                                    // coil =logoImageView.load(TEST_IMAGE)
                                                    logoImageView.load(photoUrl)
                                                    logoSaveBtn.visibility=View.VISIBLE
                                                    imageLoading.visibility = View.GONE
                                                    logoImageView.visibility = View.VISIBLE
                                                } else {
                                                    // Picasso.with(this@MainActivity).load(photoUrl).into(logoImageView)
                                                    // Picasso.get().load(TEST_IMAGE).into(logoImageView)
                                                    // logoImageView.load(TEST_IMAGE)
                                                    imageLoading.visibility = View.GONE
                                                    logoImageView.visibility = View.VISIBLE
                                                    logoImageView.setImageDrawable(
                                                        ContextCompat.getDrawable(
                                                            this@MainActivity,
                                                            R.drawable.logoai
                                                        )
                                                    )
                                                }
                                            }


                                        }

                                        override fun onError(tapsellPlusErrorModel: TapsellPlusErrorModel) {
                                            super.onError(tapsellPlusErrorModel)
                                            Toast.makeText(
                                                this@MainActivity,
                                                R.string.problem,
                                                Toast.LENGTH_SHORT
                                            ).show()

                                        }

                                    })

                            }
                        }

                        override fun error(message: String) {
                        }
                    })


            }
            logoSaveBtn.setOnClickListener {

               /* //downloadAndSaveImage(this@MainActivity)
                logoImageView.load(photoUrl) {
                    transformations(object : coil.transform.Transformation {
                        override fun key(): String = "saveImageTransformation"
                        override suspend fun transform(
                            pool: BitmapPool,
                            input: Bitmap,
                            size: Size
                        ): Bitmap {
                            // Save the image to external storage
                            // saveImageToExternalStorage(this@MainActivity, input)
                            // Return the input Bitmap
                            return input
                        }

                    })
                }*/
                // Save the image to internal storage
               val request= saveImageToInternalStorage(this@MainActivity, imageName, photoUrl)
                Coil.imageLoader(this@MainActivity).enqueue(request)
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun saveImageToExternalStorage(context: Context, bitmap: Bitmap) {
        // Get the directory for saving images
        val directory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val directory2 = Environment.getStorageDirectory()
        // Create a new file in that directory
        val file = File(directory2, imageName)
        // Save the image to the file
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                out.flush()
                out.close()
            }
            // Notify the system that a new file was created
            MediaScannerConnection.scanFile(context, arrayOf(file.path), null, null)
        } catch (e: IOException) {
            // Handle error saving image
        }
    }

    fun saveImageToInternalStorage(context: Context, imageName: String, imageUrl: String): ImageRequest {
        val targetFile = File(context.filesDir, imageName)

        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .target { drawable ->
                val bitmap = (drawable as BitmapDrawable).bitmap
                targetFile.outputStream().use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)

                }

            }
            .build()
        return request

    }

    // Define a function to download and save the image
    @RequiresApi(Build.VERSION_CODES.Q)
    fun downloadAndSaveImage(context: Context) {
        // Check if we have permission to write to external storage
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1
            )
            return
        }
    }

    /*fun checkReadPhoneStatePermission(context: Context) {

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.READ_PHONE_STATE), 2
            )
            return
        }*/
}







