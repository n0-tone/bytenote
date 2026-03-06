package com.notone.protodatastore

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.notone.protodatastore.ui.theme.ProtoDatastoreTheme
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

class SecondScreenActivity : ComponentActivity() {

    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userPreferences = UserPreferences(this)

        setContent {
            val isDarkMode by userPreferences.darkModeFlow().collectAsState(initial = false)

            ProtoDatastoreTheme(
                darkTheme = isDarkMode,
                dynamicColor = false
            ) {
                QuickNotesSecondScreen(
                    userPreferences = userPreferences,
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = { enabled ->
                        lifecycleScope.launch {
                            userPreferences.saveDarkMode(enabled)
                        }
                    },
                    onBackToMain = {
                        startActivity(Intent(this@SecondScreenActivity, MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
private fun QuickNotesSecondScreen(
    userPreferences: UserPreferences,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    onBackToMain: () -> Unit
) {
    val notes by userPreferences.notesFlow().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "QuickNotes - Ecran 2",
                style = MaterialTheme.typography.headlineSmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Dark")
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = onToggleDarkMode
                )
            }
        }

        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text("Nova nota") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = {
                val trimmed = inputText.trim()
                if (trimmed.isNotEmpty()) {
                    scope.launch {
                        userPreferences.saveNotes(notes + trimmed)
                        inputText = ""
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar")
        }

        Button(
            onClick = {
                scope.launch {
                    userPreferences.saveNotes(emptyList())
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Limpar tudo")
        }

        LazyColumn(
            modifier = Modifier.weight(1f, fill = true),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(notes) { index, note ->
                Text(text = "${index + 1}. $note")
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onBackToMain, modifier = Modifier.weight(1f)) {
                Text("<-", textAlign = TextAlign.Center)
            }
            Text("", modifier = Modifier.weight(1f))
        }
    }
}
