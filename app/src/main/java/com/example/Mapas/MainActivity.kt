@file:Suppress("DEPRECATION")

package com.example.Mapas

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.*

class MainActivity : AppCompatActivity() {
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var btnruta: Button
    private var map: MapView? = null
    private var line = Polyline()
    private var start: String = ""
    private var end: String = ""
    private val apiService: ApiService by lazy {
        Directions.apiService
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ctx: Context = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx)) // biblioteca

        setContentView(R.layout.activity_main)
        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this)
        map = findViewById<View>(R.id.map) as MapView
        map!!.setTileSource(TileSourceFactory.OpenTopo) // mosaico


        val Controladordemapa = map!!.controller
        Controladordemapa.setZoom(14)
        val puntopartida =GeoPoint(20.2135153, -101.1281619)
        Controladordemapa.setCenter(puntopartida)


        val puntoOrigen = Marker(map)
        puntoOrigen.isDraggable = true //arrastrar
        puntoOrigen.subDescription="Este es el punto de origen"
        puntoOrigen.position = GeoPoint(20.2135153, -101.1281619)
        puntoOrigen.title = "Origen"
        map!!.overlays.add(puntoOrigen)

        val puntoDestino = Marker(map)
        puntoDestino.isDraggable = true
        puntoDestino.subDescription="Este es el punto de destino"
        puntoDestino.position = GeoPoint(20.139398208378335, -101.15073143396242)
        puntoDestino.title = "DESTINO"
        map?.overlays?.add(puntoDestino)


        btnruta = findViewById(R.id.btnCalcularRuta)
        var aux = false
        var msj = true

        btnruta.setOnClickListener{
            caracteristicasdelocalizacion()
            if (!aux) {
                line = Polyline()
                dibujarRuta(puntoOrigen.position, puntoDestino.position)
                btnruta.text = "Eliminar"
                aux=true

            } else {
                btnruta.text = "Ruta"
                map?.overlays?.remove(line)
                aux =false
                if (msj) {
                    Toast.makeText(
                        ctx, "Arrastre los puntos de ubicación", Toast.LENGTH_LONG
                    ).show()
                    msj = false
                }
            }
        }

        map?.invalidate()
    }

    private fun caracteristicasdelocalizacion() {
        val task= fusedLocationProviderClient.lastLocation // interactuar con la ubicacion
       if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)!=
               PackageManager.PERMISSION_GRANTED &&
           ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION)!=
               PackageManager.PERMISSION_GRANTED){
           ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),101)
           return
       }
        task.addOnSuccessListener {
            if(it!=null){
                Toast.makeText(applicationContext, "Su ubicacion de origen es: ${it.latitude} ${it.longitude}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun dibujarRuta(Puntoorigen: GeoPoint, Puntodestino: GeoPoint){
        CoroutineScope(Dispatchers.IO).launch {
            start = "${Puntoorigen.longitude},${Puntoorigen.latitude}"
            end = "${Puntodestino.longitude},${Puntodestino.latitude}"


            val points = apiService.getRoute("5b3ce3597851110001cf62488d38aa048bea4519ae3177df424c06de", start, end)
            val caracteristicas = points.features

            for (feature in caracteristicas) {
                val geometry = feature.geometry
                val coordinates = geometry.coordinates
                for (coordinate in coordinates) {
                    val point = GeoPoint(coordinate[1], coordinate[0])
                    line.addPoint(point)
                }
                map?.overlays?.add(line)

            }
        }
    }

}