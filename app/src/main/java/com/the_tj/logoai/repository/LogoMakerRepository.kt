package com.the_tj.logoai.repository

import com.the_tj.logoai.api.LogoMakerApiServices
import com.the_tj.logoai.models.PostModel
import javax.inject.Inject

class LogoMakerRepository @Inject constructor(private val apiServices: LogoMakerApiServices) {
    suspend fun sendPrompt(postModel: PostModel)=apiServices.getPhoto(postModel)
}