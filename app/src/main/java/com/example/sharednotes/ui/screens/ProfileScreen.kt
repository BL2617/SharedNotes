package com.example.sharednotes.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(
    isLoggedIn: Boolean,
    username: String?,
    userId: String?,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onSwitchAccountClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoggedIn) {
            Text("当前账号：${username ?: "未知"}")
            Text("用户ID：${userId ?: "未知"}")
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onLogoutClick, modifier = Modifier.fillMaxWidth()) {
                Text("退出登录")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onSwitchAccountClick, modifier = Modifier.fillMaxWidth()) {
                Text("切换账号")
            }
        } else {
            Text("未登录")
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onLoginClick, modifier = Modifier.fillMaxWidth()) {
                Text("登录")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRegisterClick, modifier = Modifier.fillMaxWidth()) {
                Text("注册")
            }
        }
    }
}

