package com.example.ocrapp

import com.googlecode.tesseract.android.TessBaseAPI
import com.example.ocrapp.MainApplication
import android.graphics.Bitmap
import android.app.Application
import android.content.res.AssetManager
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.Exception

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // start copy file here, copy vie.trainneddata from assets to external storage ../tessdata/vie.trainneddata
        // the data path, must contain sub folder call "tessdata", if not the lib will not work.
        instance = this
        copyTessDataForTextRecognizor()
    }

    fun tessDataPath(): String {
        return instance!!.getExternalFilesDir(null).toString() + "/tessdata/"
    }

    val tessDataParentDirectory: String
        get() = instance!!.getExternalFilesDir(null)!!.absolutePath

    private fun copyTessDataForTextRecognizor() {
        val run = Runnable {
            val assetManager = instance!!.assets
            var out: OutputStream? = null
            try {
                val `in` = assetManager.open("guj.traineddata")
                val tesspath = instance!!.tessDataPath()
                val tessFolder = File(tesspath)
                if (!tessFolder.exists()) tessFolder.mkdir()
                val tessData = "$tesspath/guj.traineddata"
                val tessFile = File(tessData)
                if (!tessFile.exists()) {
                    out = FileOutputStream(tessData)
                    val buffer = ByteArray(1024)
                    var read = `in`.read(buffer)
                    while (read != -1) {
//                            Log.d("COUNT1",String.valueOf(read));
                        out.write(buffer, 0, read)
                        read = `in`.read(buffer)
                    }
                    Log.d("MainApplication", " Did finish copy tess file  ")
                } else Log.d("MainApplication", " tess file exist  ")
            } catch (e: Exception) {
                Log.d("MainApplication", "couldn't copy with the following error : $e")
            } finally {
                try {
                    out?.close()
                } catch (exx: Exception) {
                    Log.d("EXCEPTION", exx.toString())
                }
            }
        }
        Thread(run).start()
    }

    companion object {
        var instance: MainApplication? = null
    }
}