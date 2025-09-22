package com.mylearning

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.facebook.react.bridge.ActivityEventListener
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.yalantis.ucrop.UCrop


class CameraModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext), ActivityEventListener {
    private var photoPromise: Promise? = null
    private var photoUri: Uri? = null
    private var cropDestinationUri: Uri? = null


    init {
        reactContext.addActivityEventListener(this)
    }

    override fun getName(): String {
        return "CameraModule"
    }

    @ReactMethod
    fun captureImage(promise: Promise) {
        val activity = reactContext.currentActivity
        if (activity == null) {
            promise.reject("No Activity", "Activity doesn't exist")
            return
        }
        photoPromise = promise

        try {

            val timestamp: String =
                SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir: File? = reactContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val photoFile = File.createTempFile("JPEG_${timestamp}_", ".jpg", storageDir)

            photoUri = FileProvider.getUriForFile(
                reactContext,
                reactContext.packageName + ".provider",
                photoFile
            )

//            // Save Images in Gallary tooo // Prepare ContentValues for MediaStore
//            val values = ContentValues().apply {
//                put(MediaStore.Images.Media.TITLE, "MyApp_$timestamp")
//                put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_$timestamp.jpg")
//                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
//                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MyApp") // Custom folder in gallery
//            }

//            val contentResolver = reactContext.contentResolver
//            photoUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

            val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoUri)

            activity.startActivityForResult(intent, 101)


        } catch (e: Exception) {
            photoPromise?.reject("Error", e.message)
            photoPromise = null
        }
    }

    @ReactMethod
    fun pickImage(promise: Promise) {
        val activity = reactContext.currentActivity
        if (activity == null) {
            promise.reject("NO_ACTIVITY", "Activity doesn't exist")
            return
        }

        photoPromise = promise

        try {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            activity.startActivityForResult(intent, 102)
        } catch (e: Exception) {
            photoPromise?.reject("ERROR", e.message)
            photoPromise = null
        }
    }

    private fun startUCrop(sourceUri: Uri) {
        val destinationFile = File.createTempFile(
            "CROP_${System.currentTimeMillis()}_",
            ".jpg",
            reactContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )
        cropDestinationUri = Uri.fromFile(destinationFile)

        UCrop.of(sourceUri, cropDestinationUri!!)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(800, 800)
            .start(reactContext.currentActivity!!)

    }

    override fun onActivityResult(
        activity: Activity,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        when (requestCode) {
            101 -> { // Camera capture
                if (resultCode == Activity.RESULT_OK) {
                    photoUri?.let { startUCrop(it) } // immediately launch uCrop
                } else {
                    photoPromise?.reject("CANCELLED", "User cancelled camera capture")
                    photoPromise = null
                }
            }

            UCrop.REQUEST_CROP -> { // After crop
                if (resultCode == Activity.RESULT_OK) {
                    cropDestinationUri?.let {
                        photoPromise?.resolve(it.toString()) // Return cropped URI
                    } ?: run {
                        photoPromise?.reject("CROP_FAILED", "No output from crop")
                    }
                } else if (resultCode == UCrop.RESULT_ERROR) {
                    val cropError = data?.let { UCrop.getError(it) }
                    photoPromise?.reject("UCROP_ERROR", cropError?.message)
                } else {
                    photoPromise?.reject("CANCELLED", "User cancelled crop")
                }
                photoPromise = null
            }

            102 -> { // Gallery picker
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val selectedImage: Uri? = data.data
                    photoPromise?.resolve(selectedImage.toString())
                } else {
                    photoPromise?.reject("CANCELLED", "User cancelled image pick")
                }
                photoPromise = null
            }

            else -> {
                photoPromise?.reject("UNKNOWN_REQUEST", "Unknown request code: $requestCode")
                photoPromise = null
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        // Leave empty unless you need to handle deep links
    }


}