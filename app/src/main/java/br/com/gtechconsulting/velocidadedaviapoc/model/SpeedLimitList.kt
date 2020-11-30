package br.com.gtechconsulting.velocidadedaviapoc.model

import com.google.gson.annotations.SerializedName

data class SpeedLimitList(
    @SerializedName("speedLimit")
    var speedLimits: List<SpeedLimit>,

    @SerializedName("page")
    var page: Int,

    @SerializedName("totalPage")
    var totalPage: Int


)