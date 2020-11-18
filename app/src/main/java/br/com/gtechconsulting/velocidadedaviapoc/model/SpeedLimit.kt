package br.com.gtechconsulting.velocidadedaviapoc.model

data class SpeedLimit(
    var id: Int = 0,
    var viaId: Int = 0,
    var viaName: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var speedLimit: Int = 0,
    var direction: String = ""
)