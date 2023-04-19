package com.the_tj.logoai.models

import com.google.gson.annotations.SerializedName

class ResponseLogoMaker (
    @SerializedName("created")
    val created: Int, // 1589478378
    @SerializedName("data")
    val `data`: List<Data>
) {
    data class Data(
        @SerializedName("url")
        val url: String // https://...
    )
}