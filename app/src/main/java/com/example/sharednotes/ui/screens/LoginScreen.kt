package com.example.sharednotes.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp


@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onBack: () -> Unit,
    errorMessage: String? = null
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("账号登录", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("用户名") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("密码") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(24.dp))
        error?.let {
            Text(it, color = MaterialTheme.colors.error)
            Spacer(modifier = Modifier.height(8.dp))
        }
        errorMessage?.let {
            Text(it, color = MaterialTheme.colors.error)
            Spacer(modifier = Modifier.height(8.dp))
        }
        Button(
            onClick = {
                if (username.isBlank() || password.isBlank()) {
                    error = "用户名和密码不能为空"
                } else {
                    error = null
                    onLogin(username, password)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("登录")
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onBack) {
            Text("返回")
        }
    }
}