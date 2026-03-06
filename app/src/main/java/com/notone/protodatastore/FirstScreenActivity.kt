package com.notone.protodatastore

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

class FirstScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
