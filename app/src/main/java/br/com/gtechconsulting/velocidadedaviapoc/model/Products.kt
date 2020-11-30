package br.com.gtechconsulting.velocidadedaviapoc.model

import com.google.gson.annotations.SerializedName

data class Products(
    @SerializedName("id")
    var id: Int,
    @SerializedName("title")
    var title: String?,
    @SerializedName("description")
    var description: String?

)