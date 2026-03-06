package com.notone.protodatastore

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class MainActivity : ComponentActivity() {

    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()

        userPreferences = UserPreferences(this)

        enableEdgeToEdge()
        setContent {
            // State variables to hold text input and saved text
            var inputText by remember { mutableStateOf("") }
            var savedText by remember { mutableStateOf("") }

            // Observe saved text from DataStore
            LaunchedEffect(key1 = Unit) {
                userPreferences.getText().collect { value ->
                    savedText = value // Update stored value whenever DataStore is updated
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Display saved text
                Text(
                    text = "Saved Text: $savedText",
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Text input field
                BasicTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.padding(8.dp)
                        .border(1.dp, Color.Gray)
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    lifecycleScope.launch {
                        userPreferences.saveText(inputText)
                    }
                }) {
                    Text("Save Text")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = {
                        val intent = Intent(this@MainActivity, SecondScreenActivity::class.java)
                        startActivity(intent)
                    }) {
                        Text("← Second Screen")
                    }

                    Button(onClick = {
                        val intent = Intent(this@MainActivity, FirstScreenActivity::class.java)
                        startActivity(intent)
                    }) {
                        Text("First Screen →")
                    }
                }
            }
        }
    }
}