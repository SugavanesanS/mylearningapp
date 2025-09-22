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
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.os.Build

class CameraModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext), ActivityEventListener {
    private var photoPromise: Promise? = null
    private var photoUri: Uri? = null
    private var cropDestinationUri: Uri? = null

    companion object {
        private const val REQUEST_CAMERA_CAPTURE = 101
        private const val REQUEST_GALLERY_PICK = 102
        private const val REQUEST_CAMERA_PERMISSION = 201
        private const val REQUEST_GALLERY_PERMISSION = 202
    }

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

            activity.startActivityForResult(intent, REQUEST_CAMERA_CAPTURE)


        } catch (e: Exception) {
            photoPromise?.reject("Error", e.message)
            photoPromise = null
        }
    }

    @ReactMethod
    fun pickImage(promise: Promise) {
        photoPromise = promise
        checkGalleryPermission {
            launchGallery()
        }
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_GALLERY_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchGallery() // now launch gallery after user grants permission
                } else {
                    photoPromise?.reject("PERMISSION_DENIED", "Gallery permission denied")
                    photoPromise = null
                }
            }
        }
    }


    private fun checkGalleryPermission(onGranted: () -> Unit) {
        val activity = reactContext.currentActivity ?: run {
            photoPromise?.reject("NO_ACTIVITY", "Current activity is null")
            return
        }

        val permission = if (Build.VERSION.SDK_INT >= 33) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(
                reactContext,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            launchGallery() // only if already granted
        } else {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(permission),
                REQUEST_GALLERY_PERMISSION
            )
        }
    }

    @ReactMethod
    fun launchGallery() {
        val activity = reactContext.currentActivity
        if (activity == null) {
            photoPromise?.reject("NO_ACTIVITY", "Activity doesn't exist")
            return
        }

        try {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            activity.startActivityForResult(intent, REQUEST_GALLERY_PICK)
        } catch (e: Exception) {
            photoPromise?.reject("ERROR", e.message)
            photoPromise = null
        }
    }

    private fun startUCrop(sourceUri: Uri) {
        try {
            val activity = reactContext.currentActivity ?: run {
                photoPromise?.reject("NO_ACTIVITY", "Current activity is null")
                photoPromise = null
                return
            }

            val destinationFile = File.createTempFile(
                "CROP_${System.currentTimeMillis()}_",
                ".jpg",
                reactContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            )
            cropDestinationUri = Uri.fromFile(destinationFile)

            val intent = UCrop.of(sourceUri, cropDestinationUri!!)
                .withAspectRatio(1f, 1f)
                .withMaxResultSize(800, 800)
                .getIntent(activity)

            // Grant temporary read/write permission
            intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )

            activity.startActivityForResult(intent, UCrop.REQUEST_CROP)

        } catch (e: Exception) {
            photoPromise?.reject("UCROP_INIT_ERROR", e.message)
            photoPromise = null
        }
    }


    override fun onActivityResult(
        activity: Activity,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        when (requestCode) {

            REQUEST_CAMERA_CAPTURE -> { // Camera capture
                if (resultCode == Activity.RESULT_OK && photoUri != null) {
                    startUCrop(photoUri!!) // Launch UCrop for camera photo
                } else {
                    photoPromise?.reject("CANCELLED", "User cancelled camera capture")
                    photoPromise = null
                }
            }

            REQUEST_GALLERY_PICK -> { // Gallery picker
                val selectedImageUri = data?.data
                if (resultCode == Activity.RESULT_OK && selectedImageUri != null) {
                    startUCrop(selectedImageUri) // Launch UCrop for gallery image
                } else {
                    photoPromise?.reject("CANCELLED", "User cancelled image pick")
                    photoPromise = null
                }
            }

            UCrop.REQUEST_CROP -> { // After cropping
                if (resultCode == Activity.RESULT_OK && cropDestinationUri != null) {
                    photoPromise?.resolve(cropDestinationUri.toString()) // Return cropped URI
                } else if (resultCode == UCrop.RESULT_ERROR) {
                    val cropError = data?.let { UCrop.getError(it) }
                    photoPromise?.reject("UCROP_ERROR", cropError?.message)
                } else {
                    photoPromise?.reject("CANCELLED", "User cancelled crop")
                }
                photoPromise = null
            }

            else -> { // Unknown request code
                photoPromise?.reject("UNKNOWN_REQUEST", "Unknown request code: $requestCode")
                photoPromise = null
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        // Leave empty unless you need to handle deep links
    }


}