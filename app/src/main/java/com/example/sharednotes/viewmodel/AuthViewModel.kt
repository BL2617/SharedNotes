package com.example.sharednotes.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharednotes.data.remote.ApiClient
import com.example.sharednotes.data.remote.LoginRequest
import com.example.sharednotes.data.remote.RegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.awaitResponse

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    fun getToken(): String = prefs.getString("token", "") ?: ""

    private val _isLoggedIn = MutableStateFlow(prefs.getBoolean("isLoggedIn", false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _username = MutableStateFlow(prefs.getString("username", null))
    val username: StateFlow<String?> = _username.asStateFlow()

    private val _userId = MutableStateFlow(prefs.getString("userId", null))
    val userId: StateFlow<String?> = _userId.asStateFlow()

//    private val _userIdFlow = MutableStateFlow<Long?>(null)
//    val userIdFlow: StateFlow<Long?> = _userIdFlow.asStateFlow()
    // 注册逻辑（本地模拟，用户名可重复，userId自动生成）
//    fun register(username: String, password: String, onResult: (Boolean, String?) -> Unit) {
//        viewModelScope.launch {
//            // 这里可接入云端API，当前本地模拟
//            val userId = "U" + System.currentTimeMillis().toString().takeLast(6)
//            prefs.edit()
//                .putBoolean("isLoggedIn", true)
//                .putString("username", username)
//                .putString("userId", userId)
//                .putString("password", password) // 仅本地模拟，生产环境请加密
//                .apply()
//            _isLoggedIn.value = true
//            _username.value = username
//            _userId.value = userId
//            onResult(true, userId)
//        }
//    }
//


    fun register(username: String, password: String, onResult: (Boolean, String?, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService
                    .register(RegisterRequest(username, password))
                    .awaitResponse()
                if (response.isSuccessful) {
                    val body = response.body()
                    val userId = body?.id?.toString() // 假设后端返回 id
                    prefs.edit()
                        .putBoolean("isLoggedIn", true)
                        .putString("username", username)
                        .putString("userId", userId)
                        .apply()
                    _isLoggedIn.value = true
                    _username.value = username
                    _userId.value = userId
                    onResult(true, null, userId)
                } else {
                    // 解析后端返回的错误信息
                    val errorMsg = response.errorBody()?.string()
                    val detail = try {
                        JSONObject(errorMsg ?: "").optString("detail", "注册失败")
                    } catch (e: Exception) {
                        "注册失败"
                    }
                    onResult(false, detail, null)
                }
            } catch (e: Exception) {
                onResult(false, e.message, null)
            }
        }
    }
//    // 登录逻辑（本地模拟，用户名+密码匹配）
//    fun login(username: String, password: String, onResult: (Boolean, String?) -> Unit) {
//        viewModelScope.launch {
//            val savedUsername = prefs.getString("username", null)
//            val savedPassword = prefs.getString("password", null)
//            val savedUserId = prefs.getString("userId", null)
//            if (username == savedUsername && password == savedPassword) {
//                prefs.edit().putBoolean("isLoggedIn", true).apply()
//                _isLoggedIn.value = true
//                _username.value = username
//                _userId.value = savedUserId
//                onResult(true, savedUserId)
//            } else {
//                onResult(false, null)
//            }
//        }
//    }
fun login(username: String, password: String, onResult: (Boolean, String?) -> Unit) {
    viewModelScope.launch {
        try {
            val response = ApiClient.apiService
                .login(LoginRequest(username, password))
                .awaitResponse()
            if (response.isSuccessful) {

                val body = response.body()
//                Log.d("LoginResponse", body.toString())
//                Log.d("LoginUserId", body?.user_id?.toString() ?: "null")
                val userId = body?.user_id?.toString() // 假设后端返回 id
                val token = body?.access_token
                prefs.edit()
                    .putBoolean("isLoggedIn", true)
                    .putString("username", username)
                    .putString("userId", userId)
                    .putString("token", token)
                    .apply()
                _isLoggedIn.value = true
                _username.value = username
                _userId.value = userId
                onResult(true, userId)

            } else {
                val errorMsg = response.errorBody()?.string() ?: "登录失败"
                onResult(false, errorMsg)
            }
        } catch (e: Exception) {
            onResult(false, e.message)
        }
    }
}

    // 登出
    fun logout() {
        prefs.edit().putBoolean("isLoggedIn", false).apply()
        _isLoggedIn.value = false
        _username.value = null
        _userId.value = null
    }

    // 切换账号（本地模拟为登出+清空信息）
    fun switchAccount() {
        prefs.edit().clear().apply()
        _isLoggedIn.value = false
        _username.value = null
        _userId.value = null
    }



}
