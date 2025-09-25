package com.example.sharednotes.data.models

import java.text.SimpleDateFormat
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val isPublic: Boolean = false,
    val authorId: String? = null,
    val isSynced: Boolean = true,
    val isDeleted: Boolean = false,
    val serverId: Long? = null
)

fun Date.toIsoString(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(this)
}

fun Note.toNetworkNote(): NetworkNote = NetworkNote(
    id = serverId?.toString(),                // serverId 可能为 Long?，转为 String?
    title = title,
    content = content,
    author_id = authorId ?: "",               // 保证不为null
    is_public = isPublic,
    created_at = createdAt.toIsoString(),
    updated_at = updatedAt.toIsoString()
)


fun Note.toPublicNote(): PublicNote {
    return PublicNote(
        id = this.id,
        title = this.title,
        content = this.content,
        author = this.authorId ?: "",   // 如果你的Note里是author_id或authorId
        createdAt = this.createdAt.toString()
    )
}