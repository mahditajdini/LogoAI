package com.the_tj.logoai.models

import com.google.gson.annotations.SerializedName

class PostModel(
    @SerializedName("n")
    val n: Int, // 2
    @SerializedName("prompt")
    val prompt: String, // A cute baby sea otter
    @SerializedName("size")
    val size: String // 1024x1024
)