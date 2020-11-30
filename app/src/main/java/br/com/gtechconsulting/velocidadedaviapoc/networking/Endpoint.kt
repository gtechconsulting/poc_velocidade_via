package br.com.gtechconsulting.velocidadedaviapoc.networking


import br.com.gtechconsulting.velocidadedaviapoc.model.Products
import br.com.gtechconsulting.velocidadedaviapoc.model.SpeedLimit
import br.com.gtechconsulting.velocidadedaviapoc.model.SpeedLimitList
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface Endpoint {

    @GET("retriveAll")
    fun retriveAll() : Call<List<SpeedLimit>>

    @GET("retriveAllPage")
    fun retriveAllPage(@Query("page") page:Int, @Query("qt") qt:Int) : Call<SpeedLimitList>

    @GET("retriveAllCarga")
    fun retriveAllCarga() : Call<List<SpeedLimit>>

    @GET("products")
    fun products() : Call<List<Products>>
    
}