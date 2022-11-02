package com.example.ocrapp

import com.googlecode.tesseract.android.TessBaseAPI
import com.example.ocrapp.MainApplication
import android.graphics.Bitmap
import android.app.Application
import android.content.res.AssetManager
import android.util.Log

class OCRManager {
    var baseAPI: TessBaseAPI? = null
    fun initAPI() {
        baseAPI = TessBaseAPI()
        // after copy, my path to trainned data is getExternalFilesDir(null)+"/tessdata/"+"vie.traineddata";
        // but init() function just need parent folder path of "tessdata", so it is getExternalFilesDir(null)
        val dataPath: String = MainApplication.instance!!.tessDataParentDirectory
        Log.d("path", dataPath)
        val flag = baseAPI!!.init(dataPath, "guj")
        Log.d("FLAG", flag.toString())
        // language code is name of trainned data file, except extendsion part
        // "vie.traineddata" => language code is "vie"

        // first param is datapath which is  part to the your trainned data, second is language code
        // now, your trainned data stored in assets folder, we need to copy it to another external storage folder.
        // It is better do this work when application start firt time
    }

    fun startRecognize(bitmap: Bitmap?): String {
        if (baseAPI == null) initAPI()
        baseAPI!!.setImage(bitmap)
        return baseAPI!!.utF8Text
    }
}