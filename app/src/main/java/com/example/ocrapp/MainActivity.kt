package com.example.ocrapp

import android.Manifest
import android.app.Dialog
import android.app.Notification.Action
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.DialogCompat
import androidx.core.content.ContextCompat
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions


class MainActivity : AppCompatActivity() {

    enum class Language(val id: Int) {
        English(0),
        Gujarati(1)
    }

    private lateinit var button_capture:Button
    private lateinit var button_copy:Button
    private lateinit var textView_data:TextView
    private lateinit var radioGroup:RadioGroup
    private lateinit var manager:OCRManager
    private var uriContent:Uri? = null
    private var bitmap:Bitmap? = null
    private val REQUEST_CAMERA_CODE = 100
    private var LANGUAGE_FLAG = Language.English

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.CAMERA),REQUEST_CAMERA_CODE)
        }

        button_capture = findViewById(R.id.button_capture)
        button_copy = findViewById(R.id.button_copy)
        textView_data = findViewById(R.id.text_data)
        manager = OCRManager()
        manager.initAPI()

        button_capture.setOnClickListener {
            AlertDialog.Builder(this).setTitle("Select language in the photo")
                .setSingleChoiceItems(arrayOf("English","Gujarati"),0) { _, i ->
                    LANGUAGE_FLAG = if (i == 0) {
                        Language.English
                    } else {
                        Language.Gujarati
                    }
                }
                .setPositiveButton("Conform"){  dialogInterface,_->
                    dialogInterface.dismiss()
                    startCrop()
                }
                .setNegativeButton("Cancel"){ di,_->
                    di.cancel()
                    startCrop()
                    Toast.makeText(this,"Default English Selected",Toast.LENGTH_SHORT).show()
                }
                .setCancelable(false)
                .show()

        }

        button_copy.setOnClickListener {
            val scannedText = textView_data.text.toString()
            copyToClipboard(scannedText)
        }



    }

    private val cropImage = registerForActivityResult(CropImageContract()){result->
        if(result.isSuccessful){
            uriContent = result.uriContent
            uriContent?.let {
                bitmap = if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    Log.d("MAIN","less than 28")
                    MediaStore.Images.Media.getBitmap(
                        this.contentResolver,
                        uriContent
                    )

                } else {
                    Log.d("MAIN","greater than 28")
                    Log.d("MAIN",uriContent.toString())
                    val source = ImageDecoder.createSource(contentResolver, uriContent!!)
                    ImageDecoder.decodeBitmap(source)
                }




                if(LANGUAGE_FLAG == Language.English){
                    getTextFromImage(uriContent!!)
                }
                else{

                    val res:String = if(bitmap != null) {
                        manager.startRecognize(bitmap!!.copy(Bitmap.Config.ARGB_8888, false))
                    }
                    else {"Bitmap Null"}

                    Log.d("CHECK BEFORE: ",res)
                    if(res.isEmpty()){
                        textView_data.text = "empty"
                    }else {
                        textView_data.text = res
                    }
                    Log.d("CHECK AFTER: ",res)
                    button_capture.text = "Retake"
                    button_copy.visibility = View.VISIBLE

                }
            }

        }else{
            Toast.makeText(this,"IMAGE TAKING FAILED",Toast.LENGTH_SHORT).show()
        }
    }

    private fun getTextFromImage(uri:Uri){


        val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        val inputImage = InputImage.fromFilePath(this,uri)

        val stringBuilder = StringBuilder()
        val textBlockTask = textRecognizer.process(inputImage)
            .addOnSuccessListener {visionText->
                Log.d("MAIN","success listner worked")

                val resultText = visionText.text
                Log.d("MAIN-resultText",resultText)
                for (block in visionText.textBlocks) {
                    val blockText = block.text
                    Log.d("MAIN-block",blockText)
                    for (line in block.lines) {
                        val lineText = line.text
                        Log.d("MAIN-line",lineText)
                        stringBuilder.append(lineText)
                    }
                }
                if(stringBuilder.isEmpty()){
                    textView_data.text = "empty"
                }else {
                    textView_data.text = stringBuilder.toString()
                }
                button_capture.text = "Retake"
                button_copy.visibility = View.VISIBLE

//                val textBlock = textBlockTask.result
//                stringBuilder = StringBuilder()
//                stringBuilder.append(textBlock.text)
            }
            .addOnFailureListener {
                Log.d("MAIN","failed listener worked")
                Toast.makeText(this,"failed listener worked",Toast.LENGTH_SHORT).show()
            }

    }

    private fun copyToClipboard(text:String){
        val clipBoardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied data",text)
        clipBoardManager.setPrimaryClip(clip)
        Toast.makeText(this,"Text Copied!!",Toast.LENGTH_SHORT).show()
    }


    private fun startCrop(){
        cropImage.launch(options{
            setGuidelines(CropImageView.Guidelines.ON)
        })

    }
}