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

class MainActivity : ComponentActivity() {

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
                QuickNotesMainScreen(
                    userPreferences = userPreferences,
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = { enabled ->
                        lifecycleScope.launch {
                            userPreferences.saveDarkMode(enabled)
                        }
                    },
                    onOpenSecondScreen = {
                        startActivity(Intent(this@MainActivity, SecondScreenActivity::class.java))
                    }
                )
            }
        }
    }
}

@Composable
private fun QuickNotesMainScreen(
    userPreferences: UserPreferences,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    onOpenSecondScreen: () -> Unit
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
                text = "QuickNotes",
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
            label = { Text("Nota curta") },
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

        Text(
            text = "Notas guardadas (${notes.size})",
            style = MaterialTheme.typography.titleMedium
        )

        if (notes.isEmpty()) {
            Text(
                text = "Ainda sem notas.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f, fill = true),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(notes) { index, note ->
                    Text(text = "${index + 1}. $note")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("", modifier = Modifier.weight(1f))
            Button(onClick = onOpenSecondScreen, modifier = Modifier.weight(1f)) {
                Text("->", textAlign = TextAlign.Center)
            }
        }
    }
}