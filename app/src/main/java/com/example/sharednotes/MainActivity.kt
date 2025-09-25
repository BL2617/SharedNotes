package com.example.sharednotes

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sharednotes.data.NoteRepository
import com.example.sharednotes.data.local.NoteDatabase
import com.example.sharednotes.ui.screens.EditNoteScreen
import com.example.sharednotes.ui.screens.LoginScreen
import com.example.sharednotes.ui.screens.MainScreen
import com.example.sharednotes.ui.screens.ProfileScreen
import com.example.sharednotes.ui.screens.PublicNoteDetailScreen
import com.example.sharednotes.ui.screens.RegisterScreen
import com.example.sharednotes.viewmodel.AuthViewModel
import com.example.sharednotes.viewmodel.NoteViewModel


class MainActivity : ComponentActivity() {
    @SuppressLint("StateFlowValueCalledInComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化数据库和仓库
        val database = NoteDatabase.getDatabase(applicationContext)
        val repository = NoteRepository(database.noteDao(), this)
        val authViewModel: AuthViewModel by viewModels()
        val noteViewModel = NoteViewModel(repository, authViewModel)

        setContent {
            val navController = rememberNavController()
            var errorMessage by remember { mutableStateOf<String?>(null) }

            NavHost(navController = navController, startDestination = "main") {
                composable("main") {
                    MainScreen(
                        noteViewModel = noteViewModel,
                        onNoteClick = { noteId: Long ->
                            navController.navigate("edit/$noteId")
                        },
                        onCreateNote = {
                            navController.navigate("edit/-1")
                        },
                        onProfileClick = {
                             navController.navigate("profile") // 你可以后续添加
                        },
                        onPublicNoteClick = { note ->
                            navController.navigate("publicNoteDetail/${note.id}")
                        }
                    )
                }
                composable("edit/{noteId}") { backStackEntry ->
                    val noteId = backStackEntry.arguments?.getString("noteId")?.toLongOrNull()
                    val notes by noteViewModel.notes.collectAsState()
                    val note = notes.find { it.id == noteId }
                    Log.d("NoteDebug", "当前用户名：${authViewModel.username.value} 是否登录： ${authViewModel.isLoggedIn.value}")
                    EditNoteScreen(
                         noteViewModel = noteViewModel,
                         note = if (noteId == -1L) null else note,
                        onSave = { navController.popBackStack() },
                        userId = if (authViewModel.userId.value.isNullOrBlank() || authViewModel.userId.value == "未知") null
                            else authViewModel.userId.value,
                        onCancel = { navController.popBackStack()
                        }
                    )

                }
                // 你可以继续添加 profile、register 等界面
                composable("profile") {
                    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
                    val username by authViewModel.username.collectAsState()
                    val userId by authViewModel.userId.collectAsState()
                    ProfileScreen(
                        isLoggedIn = isLoggedIn,
                        username = username,
                        userId = userId,
                        onLoginClick = { navController.navigate("login") },
                        onRegisterClick = { navController.navigate("register") },
                        onLogoutClick = {
                            authViewModel.logout()
                            noteViewModel.stopAutoSync()
                        },
                        onSwitchAccountClick = {
                            authViewModel.switchAccount()
                            navController.navigate("login")
                            noteViewModel.stopAutoSync()
                        }
                    )
                }
                composable("login") {
                    LoginScreen(
                        onLogin = { username, password ->
                            authViewModel.login(username, password) { success, userId ->
                                if (success) {
                                    noteViewModel.assignGuestNotesToUser(userId = userId ?: "未知")
                                    navController.popBackStack()
                                    errorMessage = null
                                    noteViewModel.startAutoSync(token = authViewModel.getToken(), userId ?: "")
                                } else {
                                    errorMessage = "用户名或密码错误"
                                }
                            }
                        },
                        onBack = {
                            navController.popBackStack()
                        },
                        errorMessage = errorMessage
                    )
                }
                composable("register") {
                    RegisterScreen(
                        onRegister = { username, password, _, callback ->
                            authViewModel.register(username, password) { success, msg, userId ->
                                if (success) {
                                    navController.popBackStack("profile", false)
                                    noteViewModel.assignGuestNotesToUser(userId ?: "未知")
                                }
                                callback(success, msg)
                            }
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable("publicNoteDetail/{noteId}") { backStackEntry ->
                    val noteId = backStackEntry.arguments?.getString("noteId")?.toLongOrNull()
                    val publicNotes by noteViewModel.publicSearchResults.collectAsState()
                    val note = publicNotes.find { it.id == noteId }
                    if (note != null) {
                        PublicNoteDetailScreen(
                            note = note,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
