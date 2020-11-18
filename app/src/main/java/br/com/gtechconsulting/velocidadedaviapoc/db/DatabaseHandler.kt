package br.com.gtechconsulting.velocidadedaviapoc.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import br.com.gtechconsulting.velocidadedaviapoc.model.SpeedLimit

class DatabaseHandler(ctx: Context): SQLiteOpenHelper(ctx,DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_TABLE =
            "CREATE TABLE $TABLE_NAME ($ID INTEGER PRIMARY KEY, $VIA_ID INT, $VIA_NAME TEXT, $LAT DOUBLE, $LONG DOUBLE, $VELO INT, $DIREC TEXT);"
        db?.execSQL(CREATE_TABLE)

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
        db?.execSQL(DROP_TABLE)
        onCreate(db)
    }

    fun addItem(speedLimit: SpeedLimit): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(VIA_ID, speedLimit.viaId)
        values.put(VIA_NAME, speedLimit.viaName)
        values.put(LAT, speedLimit.latitude)
        values.put(LONG, speedLimit.longitude)
        values.put(VELO, speedLimit.speedLimit)
        values.put(DIREC, speedLimit.direction)
        val _success = db.insert(TABLE_NAME,null,values)
        return (("$_success").toInt() != -1)
    }

    fun count(): Int {
        val db = writableDatabase
        val countQuery = "SELECT count(*) as total FROM $TABLE_NAME"
        val cursor = db.rawQuery(countQuery,null)
        cursor?.moveToFirst()
        val total:Int = cursor.getInt(cursor.getColumnIndex("total"))
        cursor.close()
        return total
    }

    fun getSpeedLimit(lat:Double, long: Double, area: Double): SpeedLimit {
        val speedLimit = SpeedLimit()
        val db = writableDatabase
        val selectQuery = "SELECT $ID, $VIA_ID, $VIA_NAME, $LAT, $LONG, $VELO, $DIREC, (6371 * acos(cos( radians($lat) ) * cos( radians( latitude ) ) * cos( radians( longitude ) - radians($long) ) + sin( radians($lat) ) * sin( radians( latitude ))) AS distancia FROM $TABLE_NAME HAVING distancia < $area ORDER BY distancia ASC LIMIT 1;"
        val cursor = db.rawQuery(selectQuery, null)
        cursor?.moveToFirst()
        speedLimit.id = cursor.getInt(cursor.getColumnIndex(ID))
        speedLimit.viaId = cursor.getInt(cursor.getColumnIndex(VIA_ID))
        speedLimit.viaName = cursor.getString(cursor.getColumnIndex(VIA_NAME))
        speedLimit.latitude = cursor.getDouble(cursor.getColumnIndex(LAT))
        speedLimit.longitude = cursor.getDouble(cursor.getColumnIndex(LONG))
        speedLimit.direction = cursor.getString(cursor.getColumnIndex(DIREC))
        cursor.close()

        return speedLimit

    }

    companion object {
        private val DB_VERSION = 1
        private val DB_NAME = "VELOCIDADE_VIA"
        private val TABLE_NAME = "speed_limit"
        private val ID = "id"
        private val VIA_ID = "viaId"
        private val VIA_NAME = "viaName"
        private val LAT = "latitude"
        private val LONG = "longitude"
        private val VELO =  "velocidade"
        private val DIREC = "direction"
    }
}