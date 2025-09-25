package com.example.sharednotes.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharednotes.data.NoteRepository
import com.example.sharednotes.data.models.Note
import com.example.sharednotes.data.models.PublicNote
import com.example.sharednotes.data.models.toLocalNote
import com.example.sharednotes.data.models.toNetworkNote
import com.example.sharednotes.data.models.toPublicNote
import com.example.sharednotes.data.remote.ApiClient.apiService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date

class NoteViewModel(
    private val repository: NoteRepository,
    private val authViewModel: AuthViewModel // 需要传入当前账号ViewModel
    ) : ViewModel() {
    // 所有本地笔记

    // 当前用户ID
//    private val userIdFlow = authViewModel.userIdFlow
    private val _noteList = MutableStateFlow<List<Note>>(emptyList())
    val noteList: StateFlow<List<Note>> = _noteList

    private var autoSyncJob: Job? = null

    fun startAutoSync(token: String, userId: String, intervalMillis: Long = 5 * 60 * 1000) {
        Log.d("NoteDebug", "开始自动同步，token: $token, userId: $userId")

        // 避免重复启动
        autoSyncJob?.cancel()
        Log.d("NoteDebug", "取消之前的同步任务")

        autoSyncJob = viewModelScope.launch {
            Log.d("NoteDebug", "启动新的同步协程")
            while (true) {
                try {
                    Log.d("NoteDebug", "开始一轮同步...")
                    syncNotes(userId.toString()) // 上传本地未同步
                    Log.d("NoteDebug", "本地同步完成")
                    repository.fetchAndSaveCloudNotes(token, userId) // 拉取云端并合并
                    Log.d("NoteDebug", "云端同步完成")
                } catch (e: Exception) {
                    // 可选：记录日志
                    Log.e("NoteDebug", "同步出现异常！", e)
                    e.printStackTrace()
                }
                Log.d("NoteDebug", "等待下次同步，间隔: ${intervalMillis}ms")
                delay(intervalMillis) // 间隔一段时间
            }
        }
        Log.d("NoteDebug", "自动同步任务已启动")
    }

    fun stopAutoSync() {
        autoSyncJob?.cancel()
        autoSyncJob = null
    }

    // 当前用户的笔记流
    //    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    @OptIn(ExperimentalCoroutinesApi::class)
    val notes: StateFlow<List<Note>> = authViewModel.userId
        .flatMapLatest { userId ->
            if (userId.isNullOrBlank()) {
                repository.getGuestNotes()
            } else {
                repository.getNotesByAuthor(userId)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun assignGuestNotesToUser(userId: String) {
        viewModelScope.launch {
            repository.assignGuestNotesToUser(userId)
        }
    }

    private val _isOnlineSearch = MutableStateFlow(false)
    val isOnlineSearch: StateFlow<Boolean> = _isOnlineSearch.asStateFlow()

    fun setOnlineSearch(value: Boolean) {
        _isOnlineSearch.value = value
    }

    // 搜索关键字
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

//    init {
//        viewModelScope.launch {
//            // 监听账号变化
//            authViewModel.userId.collect { userId ->
//                if (userId.isNullOrBlank() || userId.equals("未知")) {
//                    // 游客模式
//                    repository.getGuestNotes().collect { list ->
//                        _notes.value = list
//                    }
//                } else {
//                    // 登录用户
//                    repository.getNotesByAuthor(userId).collect { list ->
//                        _notes.value = list
//                    }
//                }
//            }
//        }
//    }

    // 添加新笔记
    fun addNote(title: String, content: String, isPublic: Boolean = false, authorId: String? = null) {
        viewModelScope.launch {
            val now = Date()
            val note = Note(
                title = title,
                content = content,
                createdAt = now,
                updatedAt = now,
                isPublic = isPublic,
                authorId = authorId,
                isSynced = false
            )
            repository.insertNote(note)
        }
    }

    // 删除笔记
    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.insertNote(note.copy(isDeleted = true, isSynced = false, updatedAt = Date()))
        }
    }

    // 编辑笔记
    fun updateNote(note: Note, newTitle: String, newContent: String, isPublic: Boolean = false) {
        viewModelScope.launch {
            val updatedNote = note.copy(
                title = newTitle,
                content = newContent,
                updatedAt = Date(),
                isPublic = isPublic,
                isSynced = false
            )
            repository.updateNote(updatedNote)
        }
    }

//     搜索笔记
    fun searchNotes(query: String) {
        _searchQuery.value = query

    }

    // 刷新全部本地笔记
//    fun refreshNotes() {
//        viewModelScope.launch {
//            repository.localNotes.collect { list ->
//                _notes.value = list
//            }
//        }
//    }

    private val _publicSearchResults = MutableStateFlow<List<PublicNote>>(emptyList())
    val publicSearchResults: StateFlow<List<PublicNote>> = _publicSearchResults.asStateFlow()

    fun searchNotesOnline(keyword: String) {
        viewModelScope.launch {
            val result = repository.searchNotesOnline(authViewModel.userId.value ?: "", keyword)
            Log.d("NoteDebug", "搜索结果数量: ${result.size}")
            _noteList.value = result // _noteList 是 MutableState/List/LiveData
            _publicSearchResults.value = result.map { it.toPublicNote() }
        }
    }

    fun syncNotes(userId: String) = viewModelScope.launch {
        Log.d("NoteDebug", "开始同步，userId=$userId")
        val unsyncedNotes = repository.getUnsyncedNotes(userId)
        Log.d("NoteDebug", "未同步笔记数量: ${unsyncedNotes.size}")
        for (note in unsyncedNotes) {
            try {
                Log.d("NoteDebug", "处理笔记: ${note.id}, isDeleted=${note.isDeleted}, serverId=${note.serverId}")
                if (!note.isDeleted) {
                    val response = if (note.serverId == null) {
                        Log.d("NoteDebug", "准备上传新笔记到云端: ${note.title}")
                        apiService.uploadNote(note.toNetworkNote(), userId)
                    } else {
                        Log.d("NoteDebug", "准备更新云端笔记: ${note.title}")
                        apiService.updateNote(note.serverId.toString(), note.toNetworkNote())
                    }
                    if (response.isSuccessful) {
                        val serverId = response.body()?.id
                        Log.d("NoteDebug", "上传/更新成功，云端ID: $serverId")
                        repository.updateNote(
                            note.copy(
                                isSynced = true,
                                serverId = serverId?.toLongOrNull()
                            )
                        )
                    } else {
                        Log.e("NoteDebug", "上传/更新失败，响应码: ${response.code()}")
                    }
                } else {
                    if (note.serverId != null) {
                        Log.d("NoteDebug", "准备删除云端笔记: ${note.title}")
                        val response = apiService.deleteNote(note.serverId.toString(), userId)
                        if (response.isSuccessful) {
                            Log.d("NoteDebug", "云端删除成功，物理删除本地笔记: ${note.id}")
                            repository.deleteNotePhysically(note.id)
                        } else {
                            Log.e("NoteDebug", "云端删除失败，响应码: ${response.code()}")
                        }
                    } else {
                        Log.d("NoteDebug", "本地新建未同步就被删除，直接物理删除: ${note.id}")
                        repository.deleteNotePhysically(note.id)
                    }
                }
            } catch (e: Exception) {
                Log.e("NoteDebug", "同步笔记失败: ${note.id}", e)
            }
        }
    }

    fun syncAll(token: String, userId: Int) = viewModelScope.launch {
        syncNotes(userId.toString()) // 先上传本地未同步
        repository.fetchAndSaveCloudNotes(token, userId.toString()) // 再拉取云端
    }

    fun fetchAndSaveCloudNotes(token: String, userId: Int) = viewModelScope.launch {
        repository.fetchAndSaveCloudNotes(token, userId.toString())
    }


}


