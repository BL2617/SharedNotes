package com.example.sharednotes.ui.screens

//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sharednotes.data.models.Note
import com.example.sharednotes.data.models.PublicNote
import com.example.sharednotes.viewmodel.NoteViewModel


@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    isOnlineSearch: Boolean,
    onSearchModeChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("搜索笔记...") }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(if (isOnlineSearch) "云端" else "本地")
        Switch(
            checked = isOnlineSearch,
            onCheckedChange = onSearchModeChange
        )
    }
}

@Composable
fun PublicNoteItem(
    note: PublicNote,
    onClick: () -> Unit
    ) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = note.title, style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = note.content.take(40) + if (note.content.length > 40) "..." else "")
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "作者: ${note.author}", style = MaterialTheme.typography.caption)
            Text(text = "创建时间: ${note.createdAt}", style = MaterialTheme.typography.caption)
        }
    }
}


@Composable
fun NoteItem(
    note: Note,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { if (!showConfirm) onClick() },
        elevation = 4.dp
    ) {
        // 用 Box 固定高度，AnimatedContent 平滑切换内容
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp) // 你可以根据内容调整最小高度
                .padding(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            AnimatedContent(targetState = showConfirm, label = "delete-confirm") { confirm ->
                if (confirm) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("确认删除这条笔记？", color = MaterialTheme.colors.error)
                        Row {
                            TextButton(onClick = { showConfirm = false }) {
                                Text("取消")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = onDelete,
                                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error)
                            ) {
                                Text("确认", color = MaterialTheme.colors.onError)
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = note.title, style = MaterialTheme.typography.h6)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = note.content.take(40) + if (note.content.length > 40) "..." else "")
                        }
                        IconButton(onClick = { showConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "删除")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    noteViewModel: NoteViewModel,
    onNoteClick: (Long) -> Unit,
    onCreateNote: () -> Unit,
    onProfileClick: () -> Unit,
    onPublicNoteClick: (PublicNote) -> Unit
) {
    val isOnlineSearch by noteViewModel.isOnlineSearch.collectAsState()
    var query by remember { mutableStateOf("") }

    val notes by noteViewModel.notes.collectAsState()
    val publicNotes by noteViewModel.publicSearchResults.collectAsState()


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NoteShare") },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.Person, contentDescription = "我")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateNote) {
                Icon(Icons.Default.Add, contentDescription = "新建笔记")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            SearchBar(
                query = query,
                onQueryChange = {
                    query = it
                    if (isOnlineSearch) {
                        noteViewModel.searchNotesOnline(it) // 预留联网搜索接口
                    } else {
                        noteViewModel.searchNotes(it)
                    }
                },
                isOnlineSearch = isOnlineSearch,
                onSearchModeChange = {
                    noteViewModel.setOnlineSearch(it)
                    // 切换时自动搜索
                    if (it) {
                        noteViewModel.searchNotesOnline(query)
                    } else {
                        noteViewModel.searchNotes(query)
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (isOnlineSearch) {
                // 展示云端搜索结果
                if (publicNotes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("暂无云端搜索结果")
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(publicNotes) { note ->
                            PublicNoteItem(note = note, onClick = { onPublicNoteClick(note) })
                            Divider()
                        }
                    }
                }
            } else {
                // 展示本地笔记
                if (notes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("暂无本地笔记，点击右下角 + 新建笔记")
                    }
                } else {
                    LazyColumn {
                        items(notes, key = { it.id }) { note ->
                            NoteItem(
                                note = note,
                                onClick = { onNoteClick(note.id) },
                                onDelete = { noteViewModel.deleteNote(note) }
                            )
                            Divider()
                        }
                    }
                }
            }
        }
    }

}

//@Preview
//@Composable
//fun NoteItemPreview() {
//    NoteItem(note = Note(title = "Title", content = "Content", createdAt = Date(), updatedAt = Date())) {
//
//    }
//}
