package com.example.qrcodegenerator.ui

import com.example.qrcodegenerator.activities.MainActivity
import org.jetbrains.anko.*

class MainActivityUI : AnkoComponent<MainActivity> {

    override fun createView(ui: AnkoContext<MainActivity>)= with(ui) {
        frameLayout{
            relativeLayout {
                textView("Hello World!").lparams(width = wrapContent, height = wrapContent) {
                    centerInParent()
                    centerHorizontally()
                }
            }
        }
    }
}