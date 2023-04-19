package com.the_tj.logoai.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.the_tj.logoai.models.PostModel
import com.the_tj.logoai.models.ResponseLogoMaker
import com.the_tj.logoai.repository.LogoMakerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogoMakerViewModel @Inject constructor(private val respository: LogoMakerRepository) : ViewModel() {
    val successResult = MutableLiveData<Boolean>()
    val botMessage = MutableLiveData<ResponseLogoMaker>()

    fun sendPrompt(postModel: PostModel)=viewModelScope.launch {
        val response =respository.sendPrompt(postModel)
        if (response.isSuccessful) {
            botMessage.postValue(response.body())
            successResult.postValue(true)
        }else{
            successResult.postValue(false)

        }
    }

}