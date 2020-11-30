package br.com.gtechconsulting.velocidadedaviapoc

import android.content.Context
import android.graphics.PointF
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Chronometer
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import br.com.gtechconsulting.velocidadedaviapoc.Utils.CountUpTimer
import br.com.gtechconsulting.velocidadedaviapoc.db.DatabaseHandler
import br.com.gtechconsulting.velocidadedaviapoc.model.Products
import br.com.gtechconsulting.velocidadedaviapoc.model.SpeedLimit
import br.com.gtechconsulting.velocidadedaviapoc.model.SpeedLimitList
import br.com.gtechconsulting.velocidadedaviapoc.networking.Endpoint
import br.com.gtechconsulting.velocidadedaviapoc.networking.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class MainActivity : AppCompatActivity() {

    var totalRegistros: Int = 0
    var databaseHandler = DatabaseHandler(this)
    lateinit var counter: CountUpTimer
    lateinit var saveCounter: CountUpTimer
    lateinit var mTTS: TextToSpeech
    lateinit var dialogSave: AlertDialog

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val cm = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nc = cm.getNetworkCapabilities(cm.activeNetwork)

        val downSpeed = (nc?.linkDownstreamBandwidthKbps)?.div(1000)

        val upSpeed = (nc?.linkUpstreamBandwidthKbps)?.div(1000)

        interntInfo.text = "Velocidade download: $downSpeed Mbps \nVelocidade upload: $upSpeed Mbps"

        counter = object: CountUpTimer(3000, 1){

            var seg:Int = 0

            override fun onCount(count: Int) {
                seg = count
                dataTime.text = "Tempo para buscar dados: $count segs"
            }

            override fun onFinish() {
                Log.i("Counter", "Counting done")
            }

            override fun timeOut(){
                cancel()
                dataTime.text = "Timeout em: $seg segs"
            }
        }

        saveCounter = object: CountUpTimer(30000, 1){

            var seg:Int = 0
            override fun onCount(count: Int) {
                seg = count
                Log.i("Counter", "Counting: $count")
                dataBaseTime.text = "Tempo para salvar dados: $count segs"
            }

            override fun onFinish() {
                Log.i("Counter", "Counting done")
            }

            override fun timeOut(){
                cancel()
                dataBaseTime.text = "Timeout em: $seg segs"
            }
        }

        mTTS = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR){
                mTTS.language = Locale.ROOT
            }
        })

        supportActionBar?.hide()
        btnReload.isEnabled = false

        btnReload.setOnClickListener {
            getData()
        }

        initProcess()
    }

    override fun onResume() {
        super.onResume()

    }


    private fun initProcess() {

        totalRegistros = databaseHandler.count()
        textView.text = totalRegistros.toString() + " registros na base"

        if (totalRegistros == 0) {
            dialog()
        } else {
            speedInfo.text = ""
            getSpeedLimitByLatLong()
        }

    }

    private fun dialog(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Sem registros na base.")
        builder.setMessage("Não existem registros na base. Realizar carga agora?")
        builder.setPositiveButton("Sim"){dialog, which ->
            getData()
        }
        builder.setNeutralButton("Cancelar"){_,_ ->
            btnReload.isEnabled = true
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun dialogSaveDataBase(response:  Response<SpeedLimitList>){
        val total = response.body()?.speedLimits?.size
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Retorno API.")
        builder.setMessage("$total registros serão salvos. Proseguir?")
        builder.setPositiveButton("Sim"){dialog, which ->
            
            GlobalScope.launch(context = Dispatchers.Main) {
                dialogSave.dismiss()
                saveCounter.start()

                response.body()?.speedLimits?.forEach {it ->
                    val speedLimit = SpeedLimit(
                        it.id,
                        it.viaId,
                        it.viaName,
                        it.latitude,
                        it.longitude,
                        it.speedLimit,
                        it.direction
                    )
                    databaseHandler.insert(speedLimit)

                }

                textView.text = response.body()?.speedLimits?.size.toString()  + " registros na base"
                saveCounter.cancel()
                progressBar.visibility = View.GONE
                getSpeedLimitByLatLong()
            }

        }
        builder.setNeutralButton("Cancelar"){_,_ ->
            btnReload.isEnabled = true
        }
        dialogSave = builder.create()
        dialogSave.show()
    }

    private fun getData() {
        counter.start()
        progressBar.visibility = View.VISIBLE
        val path = "http://40.117.187.59:9090/api/poc-speed-limitt-service/retrieveSpeedLimit/"
        val retrofitClient = NetworkUtils.getRetrofitInstance(path)
        val endpoint = retrofitClient.create(Endpoint::class.java)
        val callback = endpoint.retriveAllPage(0, 5000)
        //val callback = endpoint.retriveAllCarga()

        callback.enqueue(object : Callback<SpeedLimitList> {
            override fun onFailure(call: Call<SpeedLimitList>, t: Throwable) {
                Log.i("ERROR - ", t.message.toString())
                counter.timeOut()
                progressBar.visibility = View.GONE
                Toast.makeText(baseContext, t.message, Toast.LENGTH_LONG).show()
                btnReload.isEnabled = true
            }

            override fun onResponse(call: Call<SpeedLimitList>, response:  Response<SpeedLimitList>) {
                counter.cancel()
                this@MainActivity.runOnUiThread(java.lang.Runnable {
                    dialogSaveDataBase(response)
                })
            }
        })
    }


    private fun getProducts() {
        counter.start()
        progressBar.visibility = View.VISIBLE
        val path = "http://vps13132.publiccloud.com.br:3002/"
        val retrofitClient = NetworkUtils.getRetrofitInstance(path)
        val endpoint = retrofitClient.create(Endpoint::class.java)
        val callback = endpoint.products()

        callback.enqueue(object : Callback<List<Products>> {
            override fun onFailure(call: Call<List<Products>>, t: Throwable) {
                counter.cancel()
                progressBar.visibility = View.GONE
                Toast.makeText(baseContext, t.message, Toast.LENGTH_LONG).show()
                btnReload.isEnabled = true
            }

            override fun onResponse(call: Call<List<Products>>, response: Response<List<Products>>) {
                counter.cancel()
                progressBar.visibility = View.GONE
                getSpeedLimitByLatLong()
            }
        })
    }

    fun getSpeedLimitByLatLong() {
        val latitude: Double = -23.125999
        val longitude: Double = -46.56160
        val area = 1000.0

        val center = PointF(latitude.toFloat(), longitude.toFloat())
        val mult = 1.1

        val p1:PointF =  calculateDerivedPosition(center,mult*area,0.0)
        val p2:PointF =  calculateDerivedPosition(center,mult*area,90.0)
        val p3:PointF =  calculateDerivedPosition(center,mult*area,180.0)
        val p4:PointF =  calculateDerivedPosition(center,mult*area,270.0)

        val via:SpeedLimit = databaseHandler.getSpeedLimit(p1,p2, p3, p4)

        if(via.id != null) {
            val text:String = via.viaName + " limite de velocidade: " + via.speedLimit + "km/h"
            speedInfo.text = text

            textLatLong.text = "latitude: " + via.latitude + " longitude: " + via.longitude

            mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null)
        } else {
            speedInfo.text = "localização não encontrada."
            textLatLong.text = ""
        }
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