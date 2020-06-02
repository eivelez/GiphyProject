package com.abstractchile.clase10.networking


import com.google.gson.JsonArray
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.*


interface CatApi {
    //@GET("breeds")
    //fun getCats(@Header("Authorization") key: String?): Call<List<Cat>>

    @GET("trending")
    fun getTrend(@Query("api_key") apiKey:String?,@Query("limit") limit:String?):Call<JsonObject>

    @GET("search")
    fun getSearch(@Query("api_key") apiKey:String?,@Query("limit")limit:String? ,@Query("q") s:String?):Call<JsonObject>

    @GET("random")
    fun getRandom(@Query("api_key") apiKey:String?):Call<JsonObject>

    @GET("categories")
    fun getCategories():Call<JsonObject>

    //@GET("images/search")
    //fun getImage(@Header("Authorization") key: String?,@Query("breed_id") breedId: String? ): Call<JsonArray>

    //@POST("votes")
    //@Headers("Content-Type: application/json")
    //fun postVote(@Body body: String,
    //             @Header("Authorization") key: String?
    //): Call<JsonObject>

}

