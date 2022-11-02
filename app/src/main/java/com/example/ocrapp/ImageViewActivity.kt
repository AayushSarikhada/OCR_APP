package com.example.ocrapp

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView

class ImageViewActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_view)

        val view = findViewById<ImageView>(R.id.myImage)
        val bitmap = intent.extras?.get("image") as Bitmap?
        view.setImageBitmap(bitmap)


    }
}