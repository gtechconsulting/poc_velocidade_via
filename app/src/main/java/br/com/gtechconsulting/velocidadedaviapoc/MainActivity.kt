package br.com.gtechconsulting.velocidadedaviapoc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import br.com.gtechconsulting.velocidadedaviapoc.db.DatabaseHandler

class MainActivity : AppCompatActivity() {

    var totalRegistros: Int = 0
    var databaseHandler = DatabaseHandler(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initProcess()
    }

    override fun onResume() {
        super.onResume()

        initProcess()

    }

    private fun initProcess() {

        totalRegistros = databaseHandler.count()
        if (totalRegistros == 0) {
            Toast.makeText(baseContext, "Não existe registros na base. Fazendo carga agora...",Toast.LENGTH_SHORT).show()
        }

    }
}