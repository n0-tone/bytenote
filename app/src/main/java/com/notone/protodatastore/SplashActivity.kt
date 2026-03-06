package com.notone.protodatastore

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.notone.protodatastore.ui.theme.ProtoDatastoreTheme
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ProtoDatastoreTheme {
                LaunchedEffect(Unit) {
                    delay(900)
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("QuickNotes", style = MaterialTheme.typography.headlineMedium)
                    Text("Notas curtas", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

