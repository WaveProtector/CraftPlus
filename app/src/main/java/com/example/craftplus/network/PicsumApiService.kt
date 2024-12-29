//package com.example.marsphotos.network
//
//import retrofit2.Retrofit
//import retrofit2.http.GET
//import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
//import kotlinx.serialization.json.Json
//import okhttp3.MediaType.Companion.toMediaType
//import retrofit2.http.Path
//
//
//private const val BASE_URL = "https://picsum.photos"
//
//
//private val retrofit = Retrofit.Builder()
//    .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
//    .baseUrl(BASE_URL)
//    .build()
//
//
//interface PicsumApiService {
//
//    @GET("/v2/list?page=5&limit=100")
//    suspend fun getListPicsumPhotos(): List<PicsumPhoto>
//
//    @GET("/v2/list?page=4")
//    suspend fun getListPicsumPhotosPage4(): List<PicsumPhoto>
//
////    @GET("/id/{id}/{width}/{height}/?blur")
////    suspend fun getBlurPhoto(@Path("id") id: String, @Path("width") width: Int, @Path("height") height: Int): PicsumPhoto
////
////    @GET("/id/{id}/?grayscale")
////    suspend fun getGrayPhoto(@Path("id") id: String): PicsumPhoto
//
//    @GET("/id/{id}/info")
//    suspend fun getPhotoById(@Path("id") id: String): PicsumPhoto
//}
//
//
//object PicsumApi {
//    val retrofitService : PicsumApiService by lazy {
//        retrofit.create(PicsumApiService::class.java)
//    }
//}