package com.example.sharednotes.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sharednotes.data.models.PublicNote

@Composable
fun PublicNoteDetailScreen(
    note: PublicNote,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("公开笔记详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(paddingValues)
        ) {
            Text(text = note.title, style = MaterialTheme.typography.h5)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "作者: ${note.author}", style = MaterialTheme.typography.subtitle2)
            Text(text = "创建时间: ${note.createdAt}", style = MaterialTheme.typography.subtitle2)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = note.content, style = MaterialTheme.typography.body1)
        }
    }
}