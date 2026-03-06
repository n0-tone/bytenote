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
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
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
import com.notone.protodatastore.enums.UserPreferencesKeysEnum
import kotlinx.coroutines.launch

class FirstScreenActivity : ComponentActivity() {

    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userPreferences = UserPreferences(this)

        setContent {
            var inputText1 by remember { mutableStateOf("") }
            var inputText2 by remember { mutableStateOf("") }
            var savedText1 by remember { mutableStateOf("") }
            var savedText2 by remember { mutableStateOf("") }
            var isSavingEnabled by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                userPreferences.getValue<Boolean>(UserPreferencesKeysEnum.Switch).collect { state: Boolean ->
                    isSavingEnabled = state
                }
            }

            LaunchedEffect(Unit) {
                userPreferences.getValue<String>(UserPreferencesKeysEnum.Text_Input_1).collect { value: String ->
                    savedText1 = value
                }
                userPreferences.getValue<String>(UserPreferencesKeysEnum.Text_Input_2).collect { value: String ->
                    savedText2 = value
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Saved Text 1: $savedText1")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Saved Text 2: $savedText2")
                Spacer(modifier = Modifier.height(16.dp))

                BasicTextField(
                    value = inputText1,
                    onValueChange = { inputText1 = it },
                    modifier = Modifier.padding(8.dp)
                        .border(1.dp, Color.Gray)
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                BasicTextField(
                    value = inputText2,
                    onValueChange = { inputText2 = it },
                    modifier = Modifier.padding(8.dp)
                        .border(1.dp, Color.Gray)
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Switch(
                    checked = isSavingEnabled,
                    onCheckedChange = { isChecked ->
                        isSavingEnabled = isChecked
                        lifecycleScope.launch {
                            userPreferences.saveValue<Boolean>(isChecked, UserPreferencesKeysEnum.Switch)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    if (isSavingEnabled) {
                        lifecycleScope.launch {
                            userPreferences.saveValue<String>(inputText1,UserPreferencesKeysEnum.Text_Input_1)
                            userPreferences.saveValue<String>(inputText2, UserPreferencesKeysEnum.Text_Input_2)
                        }
                    }
                }) {
                    Text("Save Texts")
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = {
                        val intent = Intent(this@FirstScreenActivity, MainActivity::class.java)
                        startActivity(intent)
                    }) {
                        Text("← Main Screen")
                    }

                    Button(onClick = {
                        val intent = Intent(this@FirstScreenActivity, SecondScreenActivity::class.java)
                        startActivity(intent)
                    }) {
                        Text("Second Screen →")
                    }
                }
            }
        }
    }
}
