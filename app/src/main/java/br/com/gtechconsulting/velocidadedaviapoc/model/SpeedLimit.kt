package br.com.gtechconsulting.velocidadedaviapoc.model

import com.google.gson.annotations.SerializedName

data class SpeedLimit(
    @SerializedName("id")
    var id: Int,
    @SerializedName("viaId")
    var viaId: Int?,
    @SerializedName("viaName")
    var viaName: String?,
    @SerializedName("latitude")
    var latitude: Double?,
    @SerializedName("longitude")
    var longitude: Double?,
    @SerializedName("speedLimit")
    var speedLimit: Int?,
    @SerializedName("direction")
    var direction: String?
)