package com.example.map_umkm.model

import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @FormUrlEncoded
    @POST("login.php")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("register.php")
    fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<GenericResponse>   // ✅ harus GenericResponse, bukan LoginResponse

    @GET("get_menu.php")
    fun getMenu(): Call<List<MenuItem>>
}
