package com.gospell.stall.other

import android.text.TextWatcher

interface TextChangedListener :TextWatcher{
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        println(s)
    }
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        println(s)
    }
}