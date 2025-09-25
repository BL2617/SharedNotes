package com.example.sharednotes.data

//import com.example.sharednotes.data.local.NoteDao
import android.content.Context
import android.util.Log
import com.example.sharednotes.data.local.NoteDao
import com.example.sharednotes.data.models.NetworkNote
import com.example.sharednotes.data.models.Note
import com.example.sharednotes.data.models.toLocalNote
import com.example.sharednotes.data.models.toNetworkNote
import com.example.sharednotes.data.remote.ApiClient
import com.example.sharednotes.data.remote.ApiClient.apiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class NoteRepository(private val noteDao: NoteDao, private val context: Context) {
//    val apiService = ApiClient.create(context)
    val localNotes: Flow<List<Note>> = noteDao.getLocalNotes()

    fun getGuestNotes(): Flow<List<Note>> = noteDao.getGuestNotes()

    fun getNotesByAuthor(authorId: String): Flow<List<Note>> = noteDao.getNotesByAuthor(authorId)

    suspend fun assignGuestNotesToUser(userId: String) = noteDao.assignGuestNotesToUser(userId)

    val publicNotes: Flow<List<Note>> = noteDao.getPublicNotes()

    fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes(query)

    suspend fun insertNote(note: Note): Long = noteDao.insertNote(note)

    suspend fun updateNote(note: Note) = noteDao.updateNote(note)

    suspend fun deleteNote(note: Note) = noteDao.deleteNote(note)

    suspend fun deleteAllLocalNotes() = noteDao.deleteAllLocalNotes()

    suspend fun getUnsyncedNotes(userId: String): List<Note> = withContext(Dispatchers.IO) {
        noteDao.getUnsyncedNotes(userId)
    }

    suspend fun uploadNoteToServer(note: Note, userId: String): Boolean {
        return try {
            val response = if (note.serverId != null) {
                // 已有云端ID，更新
                apiService.updateNote(note.serverId.toString(), note.toNetworkNote())
            } else {
                // 没有云端ID，新增
                apiService.uploadNote(note.toNetworkNote(), userId)
            }
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteNoteOnServer(noteId: Long, token: String): Boolean {
        return try {
            val response = apiService.deleteNote(noteId.toString(), token = token)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteNoteLocally(note: Note) = noteDao.deleteNote(note)

    suspend fun deleteNotePhysically(id: Long) = noteDao.deleteNotePhysically(id)

    suspend fun fetchAndSaveCloudNotes(token: String, userId: String) = withContext(Dispatchers.IO) {
        try {
            Log.d("NoteDebug", "从云端获取笔记")

            val response = apiService.getNotes(userId.toInt())
            Log.d("NoteDebug", "获取到 ${response.size} 条云端笔记")

            // 保存到本地数据库
            for (networkNote in response) {
                saveNoteToLocal(networkNote)
            }

        } catch (e: Exception) {
            Log.e("NoteDebug", "获取云端笔记失败", e)
            throw e
        }
    }

    fun stringToDate(str: String?): Date? {
        if (str.isNullOrEmpty()) return null
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            sdf.parse(str)
        } catch (e: Exception) {
            null
        }
    }

    // 保存云端笔记到本地（本地数据库操作）
    private suspend fun saveNoteToLocal(networkNote: NetworkNote) {
        try {
            // 1. 转换为本地 Note 实体
            val note = networkNote.id?.let {
                Note(
                    id = it.toLong(), // 或者生成本地唯一ID
                    title = networkNote.title,
                    content = networkNote.content,
                    createdAt = stringToDate(networkNote.created_at) ?: Date(),
                    updatedAt = stringToDate(networkNote.updated_at) ?: Date(),
                    isDeleted = false,
                    authorId = networkNote.author_id.toString(),
                    isSynced = true, // 云端下来的肯定是已同步
                    serverId = networkNote.id.toLongOrNull() // 如果有 serverId 字段
                )
            }
            // 2. 保存到本地数据库（插入或更新）
            if (note != null) {
                noteDao.insertOrUpdate(note)
            } // 你需要实现这个方法
            if (note != null) {
                Log.d("NoteDebug", "保存云端笔记到本地: ${note.title}")
            }
        } catch (e: Exception) {
            Log.e("NoteDebug", "保存云端笔记失败", e)
        }
    }

    suspend fun searchNotesOnline(userId: String, keyword: String): List<Note> {
        val networkNotes = apiService.searchNotes(userId.toInt(), keyword)
        // 转换为本地Note类型（如果需要）
        return networkNotes.map { it.toLocalNote() }
    }

}