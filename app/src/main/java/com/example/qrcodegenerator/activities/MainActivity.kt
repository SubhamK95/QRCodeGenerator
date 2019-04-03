package com.example.qrcodegenerator.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.qrcodegenerator.ui.MainActivityUI
import org.jetbrains.anko.setContentView

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainActivityUI().setContentView(this)

    }
}
