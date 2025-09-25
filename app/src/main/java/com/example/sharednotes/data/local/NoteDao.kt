package com.example.sharednotes.data.local

import androidx.room.*
import com.example.sharednotes.data.models.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE isSynced = 0 AND authorId = :authorId")
    fun getUnsyncedNotes(authorId: String): List<Note>

    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun getLocalNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND authorId IS NULL ORDER BY updatedAt DESC")
    fun getGuestNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND authorId = :authorId ORDER BY updatedAt DESC")
    fun getNotesByAuthor(authorId: String): Flow<List<Note>>

    @Query("UPDATE notes SET authorId = :userId WHERE authorId IS NULL")
    suspend fun assignGuestNotesToUser(userId: String)

    @Query("SELECT * FROM notes WHERE isPublic = 1 ORDER BY updatedAt DESC")
    fun getPublicNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'")
    fun searchNotes(query: String): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("DELETE FROM notes WHERE authorId IS NULL")
    suspend fun deleteAllLocalNotes()

    @Query("UPDATE notes SET isDeleted = 1, isSynced = 0 WHERE id = :id")
    suspend fun softDeleteNote(id: Long)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNotePhysically(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(note: Note)



}