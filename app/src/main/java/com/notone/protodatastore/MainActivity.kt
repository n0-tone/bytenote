package com.notone.protodatastore

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
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
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.notone.protodatastore.classes.NotesService
import com.notone.protodatastore.classes.UserPreferencesService
import com.notone.protodatastore.models.Note
import com.notone.protodatastore.ui.theme.ProtoDatastoreTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var userService: UserPreferencesService
    private lateinit var notesService : NotesService

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        userService = UserPreferencesService(this)
        notesService = NotesService(this)

        splashScreen.setKeepOnScreenCondition { false }

        setContent {
            val isDarkMode by userService.getDarkMode().collectAsState(initial = false)
            ProtoDatastoreTheme(darkTheme = isDarkMode) {
                QuickNotesMainScreen(
                    isDarkMode = isDarkMode,
                    notesService = notesService,
                    onToggleDarkMode = {
                        lifecycleScope.launch {
                            userService.saveDarkMode(!isDarkMode)
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
    notesService: NotesService,
    onToggleDarkMode: () -> Unit,
    onOpenAddNote: () -> Unit
) {
    val persistedNotes by notesService.getNotes().collectAsState(initial = emptyList())
    var notes by remember { mutableStateOf(emptyList<Note>()) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(persistedNotes) {
        notes = persistedNotes
    }

    fun persistNotes(updated: List<Note>) {
        notes = updated
        scope.launch {
            notesService.saveNotes(updated)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "ByteNote",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onToggleDarkMode) {
                        val modeIcon = if (isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode
                        Icon(modeIcon, contentDescription = "Alternar Tema")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenAddNote,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            val currentNotes = notes
                            val updated = currentNotes.filter { it.id != note.id }
                            if (updated.size < currentNotes.size) {
                                persistNotes(updated)
                                Toast.makeText(context, "Nota apagada", Toast.LENGTH_SHORT).show()
                            }
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
    note: Note,
    index: Int,
    total: Int,
    onDelete: () -> Unit,
    onMove: (Int, Int) -> Unit
) {
    val currentOnDelete by rememberUpdatedState(onDelete)
    val currentOnMove by rememberUpdatedState(onMove)

    var expanded by remember { mutableStateOf(false) }
    var itemHeightPx by remember { mutableIntStateOf(1) }
    var dragAccumulator by remember { mutableFloatStateOf(0f) }

    val extraPadding by animateDpAsState(
        targetValue = if (expanded) 24.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "paddingAnimation"
    )

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                currentOnDelete()
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
                    .padding(vertical = 4.dp)
                    .background(if (showDeleteBackground) MaterialTheme.colorScheme.errorContainer else Color.Transparent, MaterialTheme.shapes.medium)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (showDeleteBackground) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Apagar nota",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        },
        content = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
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
                                    currentOnMove(index, index + 1)
                                    dragAccumulator = 0f
                                } else if (dragAccumulator < -threshold && index > 0) {
                                    currentOnMove(index, index - 1)
                                    dragAccumulator = 0f
                                }
                            }
                        )
                    }
                    .clickable { expanded = !expanded },
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = extraPadding.coerceAtLeast(0.dp))
                    ) {
                        Text(
                            text = note.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = note.content,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = if (expanded) Int.MAX_VALUE else 2
                        )
                    }
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = if (expanded) "Ver menos" else "Ver mais"
                        )
                    }
                }
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}
