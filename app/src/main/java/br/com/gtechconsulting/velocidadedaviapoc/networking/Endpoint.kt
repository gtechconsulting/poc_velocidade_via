package br.com.gtechconsulting.velocidadedaviapoc.networking


import br.com.gtechconsulting.velocidadedaviapoc.model.SpeedLimit
import retrofit2.Call
import retrofit2.http.GET

interface Endpoint {

    @GET("retriveAll")
    fun retriveAll() : Call<List<SpeedLimit>>
    
}