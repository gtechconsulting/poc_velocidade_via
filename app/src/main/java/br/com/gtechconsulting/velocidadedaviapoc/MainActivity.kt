package br.com.gtechconsulting.velocidadedaviapoc

import android.graphics.PointF
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.Toast
import androidx.core.view.isVisible
import br.com.gtechconsulting.velocidadedaviapoc.db.DatabaseHandler
import br.com.gtechconsulting.velocidadedaviapoc.model.SpeedLimit
import br.com.gtechconsulting.velocidadedaviapoc.networking.Endpoint
import br.com.gtechconsulting.velocidadedaviapoc.networking.NetworkUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class MainActivity : AppCompatActivity() {

    var totalRegistros: Int = 0
    var databaseHandler = DatabaseHandler(this)
    lateinit var mTTS: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mTTS = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR){
                mTTS.language = Locale.ROOT
            }
        })

        supportActionBar?.hide()

        initProcess()
    }

    override fun onResume() {
        super.onResume()

    }

    private fun initProcess() {

        totalRegistros = databaseHandler.count()
        textView.text = totalRegistros.toString() + " registros na base"

        var text = "Não existem registros na base. Realizando carga inicial."
        textAux.text = text

        mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null)
        if (totalRegistros == 0) {
            getData()
        } else {
            textAux.text = ""
            getSpeedLimitByLatLong()
        }

    }

    private fun getData() {
        val retrofitClient = NetworkUtils.getRetrofitInstance("http://40.117.187.59:9090/api/poc-speed-limitt-service/retrieveSpeedLimit/")
        val endpoint = retrofitClient.create(Endpoint::class.java)
        val callback = endpoint.retriveAll()

        callback.enqueue(object : Callback<List<SpeedLimit>> {
            override fun onFailure(call: Call<List<SpeedLimit>>, t: Throwable) {
                Toast.makeText(baseContext, t.message, Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<List<SpeedLimit>>, response: Response<List<SpeedLimit>>) {
                response.body()?.forEach {it ->
                    val speedLimit = SpeedLimit(
                        it.id,
                        it.viaId,
                        it.viaName,
                        it.latitude,
                        it.longitude,
                        it.speedLimit,
                        it.direction
                    )
                    print(speedLimit.viaId)
                    databaseHandler.insert(speedLimit)
                }
                textView.text = response.body()?.size.toString()  + " registros na base"
                textAux.text = ""

                getSpeedLimitByLatLong()
            }
        })
    }

    fun getSpeedLimitByLatLong() {
        val latitude: Double = -20.37777
        val longitude: Double = -49.78142
        val area: Double = 1.0

        val center:PointF = PointF(latitude.toFloat(), longitude.toFloat())
        val mult:Double = 1.1

        val p1:PointF =  calculateDerivedPosition(center,mult*area,0.0)
        val p2:PointF =  calculateDerivedPosition(center,mult*area,90.0)
        val p3:PointF =  calculateDerivedPosition(center,mult*area,180.0)
        val p4:PointF =  calculateDerivedPosition(center,mult*area,270.0)

        //val via:SpeedLimit = databaseHandler.getSpeedLimit(latitude,longitude, area)
        val via:SpeedLimit = databaseHandler.getSpeedLimit(p1,p2, p3, p4)

        textAux.text = via.viaName + " limite de velocidade: " + via.speedLimit + "km/h"
    }

    fun calculateDerivedPosition(pointF: PointF, range:Double, bearing:Double): PointF {

        val earthRadius:Int = 6371000

        val latA:Double = Math.toRadians(pointF.x.toDouble())
        val lonA:Double = Math.toRadians(pointF.y.toDouble())
        val angularDistance:Double = range/earthRadius
        val trueCourse:Double = Math.toRadians(bearing)

        var lat:Double = Math.asin(Math.sin(latA) * Math.cos(angularDistance) + Math.cos(latA) * Math.sin(angularDistance) * Math.cos(trueCourse))

         val dlon:Double = Math.atan2(Math.sin(trueCourse) * Math.sin(angularDistance) * Math.cos(latA), Math.cos(angularDistance) - Math.sin(latA) * Math.sin(lat))

         var lon:Double = ((lonA  + dlon  + Math.PI) % (Math.PI * 2)) - Math.PI

        lat = Math.toDegrees(lat)
        lon = Math.toDegrees(lon)

        val newPoint:PointF = PointF( lat.toFloat(), lon.toFloat());

        return newPoint;

    }
}