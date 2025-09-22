package com.mylearning

import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

class MyNativeModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String {
        return "MyNativeModule"
    }

    @ReactMethod
    fun showToast(message: String,duration: Int){
         Toast.makeText(reactApplicationContext,message,duration).show()
    }

    @ReactMethod
    fun showNetWorkErrorToast() {
        val inflater = LayoutInflater.from(reactApplicationContext)
        val layout = inflater.inflate(R.layout.custom_toast, null)

        val text: TextView = layout.findViewById(R.id.toastMessage)
        text.text = "Network Error, Please check your Connection"

        val toast = Toast(reactApplicationContext)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.show()
    }


}