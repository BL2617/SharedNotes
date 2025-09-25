package com.example.sharednotes.data.models

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class NetworkNote(
    val id: String? = null,           // 笔记唯一ID，新增时为null，更新/删除时有值
    val title: String,
    val content: String,
    val author_id: String,            // 注意：字段名与后端一致
    val is_public: Boolean = false,
    val created_at: String? = null,   // ISO8601字符串，建议用String，后端可自动生成
    val updated_at: String? = null    // 同上
)

fun String?.toDateOrNow(): Date {
    return try {
        if (this == null) Date() else {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            sdf.parse(this) ?: Date()
        }
    } catch (e: Exception) {
        Date()
    }
}

fun NetworkNote.toLocalNote(): Note {
    return Note(
        // id 本地自增，拉取云端时应为0
        id = 0L,
        title = this.title,
        content = this.content,
        createdAt = this.created_at.toDateOrNow(),
        updatedAt = this.updated_at.toDateOrNow(),
        isPublic = this.is_public,
        authorId = this.author_id,
        isSynced = true, // 拉取自云端的笔记默认已同步
        isDeleted = false,
        serverId = this.id?.toLongOrNull() // 如果本地serverId是String类型，直接用this.id
    )
}

//fun NetworkNote.toLocalNote(): Note {
//    return Note(
//        id = this.id?.toLongOrNull() ?: 0,
//        title = this.title,
//        content = this.content,
//        createdAt = this.created_at.toDateOrNow(),
//        updatedAt = this.updated_at.toDateOrNow(),
//        isDeleted = false,
//        authorId = this.author_id,
//        isSynced = true,
//        serverId = this.id?.toLongOrNull()
//    )
//}






