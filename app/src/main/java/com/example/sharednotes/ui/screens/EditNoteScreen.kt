package com.example.sharednotes.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sharednotes.data.models.Note
import com.example.sharednotes.viewmodel.NoteViewModel

@Composable
fun EditNoteScreen(
    noteViewModel: NoteViewModel,
    note: Note? = null, // 可选参数，null 表示新建
    onSave: () -> Unit,
    userId: String? = null,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }
    var isPublic by remember { mutableStateOf(note?.isPublic ?: false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (note == null) "新建笔记" else "编辑笔记") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("标题") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("内容") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(
                    checked = isPublic,
                    onCheckedChange = { isPublic = it }
                )
                Text("公开此笔记")
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = onCancel) {
                    Text("取消")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        Log.d("NoteDebug", "当前UserId：${userId.toString()}")

                        if (note == null) {
                            // 新建
                            noteViewModel.addNote(title, content, isPublic, userId)
                        } else {
                            // 编辑
                            noteViewModel.updateNote(note, title, content, isPublic)
                        }
                        onSave()
                    },
                    enabled = title.isNotBlank() && content.isNotBlank()
                ) {
                    Text("保存")
                }
            }
        }
    }
}


