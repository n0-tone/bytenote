package com.notone.protodatastore

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.border
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class SecondScreenActivity : ComponentActivity() {

    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userPreferences = UserPreferences(this)

        setContent {
            var inputNumber by remember { mutableStateOf("") }
            var savedNumber by remember { mutableStateOf(0) }

            LaunchedEffect(Unit) {
                userPreferences.getNumber().collect { value ->
                    savedNumber = value
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Saved Number: $savedNumber",
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                BasicTextField(
                    value = inputNumber,
                    onValueChange = { inputNumber = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.padding(8.dp)
                        .border(1.dp, Color.Gray)
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    val number = inputNumber.toIntOrNull()
                    if (number != null) {
                        lifecycleScope.launch {
                            userPreferences.saveNumber(number)
                            inputNumber = ""
                        }
                    }
                }) {
                    Text("Save Number")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = {
                        val intent = Intent(this@SecondScreenActivity, FirstScreenActivity::class.java)
                        startActivity(intent)
                    }) {
                        Text("← First Screen")
                    }

                    Button(onClick = {
                        val intent = Intent(this@SecondScreenActivity, MainActivity::class.java)
                        startActivity(intent)
                    }) {
                        Text("Main Screen →")
                    }
                }
            }
        }
    }
}
