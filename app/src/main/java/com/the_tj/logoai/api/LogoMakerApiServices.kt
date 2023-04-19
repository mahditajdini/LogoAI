package com.the_tj.logoai.api

import com.the_tj.logoai.models.PostModel
import com.the_tj.logoai.models.ResponseLogoMaker
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LogoMakerApiServices {

    @POST("generations")
    suspend fun getPhoto(@Body prompt: PostModel): Response<ResponseLogoMaker>

}