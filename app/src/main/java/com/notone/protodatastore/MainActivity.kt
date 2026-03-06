package com.notone.protodatastore

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.notone.protodatastore.ui.theme.ProtoDatastoreTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1️Instala a splash screen Android 12+ antes de setContent
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        userPreferences = UserPreferences(this)

        // 2️⃣ Mantém a splash enquanto carregas dados
        splashScreen.setKeepOnScreenCondition {
            // mantém até os dados carregarem
            false
        }

        setContent {
            val isDarkMode by userPreferences.darkModeFlow().collectAsState(initial = false)
            ProtoDatastoreTheme(darkTheme = isDarkMode) {
                QuickNotesMainScreen(
                    isDarkMode = isDarkMode,
                    userPreferences = userPreferences,
                    onToggleDarkMode = {
                        lifecycleScope.launch {
                            userPreferences.saveDarkMode(!isDarkMode)
                        }
                    },
                    onOpenAddNote = {
                        startActivity(Intent(this@MainActivity, SecondScreenActivity::class.java))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickNotesMainScreen(
    isDarkMode: Boolean,
    userPreferences: UserPreferences,
    onToggleDarkMode: () -> Unit,
    onOpenAddNote: () -> Unit
) {
    val persistedNotes by userPreferences.notesFlow().collectAsState(initial = emptyList())
    var notes by remember { mutableStateOf(emptyList<QuickNote>()) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(persistedNotes) {
        notes = persistedNotes
    }

    fun persistNotes(updated: List<QuickNote>) {
        notes = updated
        scope.launch {
            userPreferences.saveNotes(updated)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ByteNotes") },
                actions = {
                    IconButton(onClick = onToggleDarkMode) {
                        val modeIcon = if (isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode
                        val description = if (isDarkMode) "Ativar modo claro" else "Ativar modo escuro"
                        Icon(modeIcon, contentDescription = description)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onOpenAddNote) {
                Icon(Icons.Filled.Add, contentDescription = "Nova nota")
            }
        }
    ) { paddingValues ->
        if (notes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Ainda sem notas.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(
                    items = notes,
                    key = { _, note -> note.id }
                ) { index, note ->
                    NoteRow(
                        note = note,
                        index = index,
                        total = notes.size,
                        onDelete = {
                            val updated = notes.toMutableList().apply { removeAt(index) }
                            persistNotes(updated)
                            Toast.makeText(context, "Nota apagada", Toast.LENGTH_SHORT).show()
                        },
                        onMove = { from, to ->
                            if (from == to || from !in notes.indices || to !in notes.indices) return@NoteRow
                            val updated = notes.toMutableList()
                            val movedItem = updated.removeAt(from)
                            updated.add(to, movedItem)
                            persistNotes(updated)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteRow(
    note: QuickNote,
    index: Int,
    total: Int,
    onDelete: () -> Unit,
    onMove: (Int, Int) -> Unit
) {
    var itemHeightPx by remember { mutableIntStateOf(1) }
    var dragAccumulator by remember { mutableFloatStateOf(0f) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val showDeleteBackground =
                dismissState.currentValue == SwipeToDismissBoxValue.EndToStart ||
                    dismissState.targetValue == SwipeToDismissBoxValue.EndToStart
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (showDeleteBackground) MaterialTheme.colorScheme.errorContainer else Color.Transparent)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (showDeleteBackground) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Apagar nota",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "Apagar",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
            }
        },
        content = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { itemHeightPx = it.height.coerceAtLeast(1) }
                    .pointerInput(index, total, itemHeightPx) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { dragAccumulator = 0f },
                            onDragEnd = { dragAccumulator = 0f },
                            onDragCancel = { dragAccumulator = 0f },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragAccumulator += dragAmount.y
                                val threshold = itemHeightPx * 0.6f

                                if (dragAccumulator > threshold && index < total - 1) {
                                    onMove(index, index + 1)
                                    dragAccumulator = 0f
                                } else if (dragAccumulator < -threshold && index > 0) {
                                    onMove(index, index - 1)
                                    dragAccumulator = 0f
                                }
                            }
                        )
                    }
                    .padding(vertical = 1.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        androidx.compose.foundation.layout.Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = note.title,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = note.content,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 3
                            )
                        }
                    }
                    Text(
                        text = "::",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    )
}
