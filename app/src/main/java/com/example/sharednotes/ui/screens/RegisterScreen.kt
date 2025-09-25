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
fun RegisterScreen(
    onRegister: (String, String, String, (Boolean, String?) -> Unit) -> Unit,
    onBack: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("账号注册", style = MaterialTheme.typography.h6)
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
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("确认密码") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(24.dp))
        error?.let {
            Text(it, color = MaterialTheme.colors.error)
            Spacer(modifier = Modifier.height(8.dp))
        }
        Button(
            onClick = {
                if (password != confirmPassword) {
                    error = "两次密码不一致"
                } else if (username.isBlank() || password.isBlank()) {
                    error = "用户名和密码不能为空"
                } else {
                    error = null
                    onRegister(username, password, confirmPassword) { success, msg ->
                        if (!success) {
                            error = msg // 显示后端返回的错误信息
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("注册")
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onBack) {
            Text("返回")
        }
    }
}