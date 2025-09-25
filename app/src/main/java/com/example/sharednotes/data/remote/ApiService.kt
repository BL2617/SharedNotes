package com.example.sharednotes.data.remote

import com.example.sharednotes.data.models.NetworkNote
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

data class RegisterRequest(val username: String, val password: String)
data class RegisterResponse(val id: Int, val username: String)
data class LoginRequest(val username: String, val password: String)
data class LoginResponse(
    val access_token: String,
    val token_type: String,
    val user_id: Int,
    val username: String
)



interface ApiService {
    @POST("register")
    fun register(@Body req: RegisterRequest): Call<RegisterResponse>

    @POST("login")
    fun login(@Body req: LoginRequest): Call<LoginResponse>

    @POST("notes")
    suspend fun uploadNote(
        @Body note: NetworkNote,
        @Query("user_id") userId: String
    ): Response<NetworkNote>

    @PUT("notes/{id}")
    suspend fun updateNote(
        @Path("id") id: String,
        @Body note: NetworkNote
    ): Response<NetworkNote>

    @DELETE("notes/{id}")
    suspend fun deleteNote(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): Response<Unit>

    @GET("notes/my")
    suspend fun getMyNotes(@Query("user_id") userId: String): Response<List<NetworkNote>>

    @GET("notes/my")
    suspend fun getNotes(
        @Query("user_id") userId: Int
    ): List<NetworkNote>

    @GET("notes/search")
    suspend fun searchNotes(
        @Query("user_id") userId: Int,
        @Query("keyword") keyword: String
    ): List<NetworkNote>


}