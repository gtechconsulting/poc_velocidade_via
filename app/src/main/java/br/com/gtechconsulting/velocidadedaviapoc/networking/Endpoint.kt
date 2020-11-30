package br.com.gtechconsulting.velocidadedaviapoc.networking


import br.com.gtechconsulting.velocidadedaviapoc.model.Products
import br.com.gtechconsulting.velocidadedaviapoc.model.SpeedLimit
import retrofit2.Call
import retrofit2.http.GET

interface Endpoint {

    @GET("retriveAll")
    fun retriveAll() : Call<List<SpeedLimit>>

    @GET("retriveAllCarga")
    fun retriveAllCarga() : Call<List<SpeedLimit>>

    @GET("products")
    fun products() : Call<List<Products>>
    
}