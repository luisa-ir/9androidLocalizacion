package com.example.a9androidlocalizacion

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.IOException
import java.util.Locale

class MainActivity : AppCompatActivity(), LocationListener {

    private lateinit var contenedorMenuPrincipal: LinearLayout
    private lateinit var contenedorDetalleActividad: LinearLayout

    private lateinit var salida: TextView
    private lateinit var txtActividadActiva: TextView
    private lateinit var panelActividad5: LinearLayout

    private lateinit var btnVolverMenu: Button

    private lateinit var btnActividad1: Button
    private lateinit var btnActividad2: Button
    private lateinit var btnActividad3: Button
    private lateinit var btnActividad4: Button
    private lateinit var btnActividad5: Button

    private lateinit var btnAltaPrecision: Button
    private lateinit var btnAhorroBateria: Button

    private lateinit var manejadorLoc: LocationManager
    private lateinit var proveedor: String
    private val PERMISO_LOCALIZACION = 1001


    private var actividadSeleccionada: Int = 0

    private var ultimaUbicacion: Location? = null
    private var distanciaTotalMetros: Double = 0.0

    private val destinoLat = 4.60971
    private val destinoLon = -74.08175
    private var notificacionEnviada = false
    private val CHANNEL_ID = "canal_destino"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        contenedorMenuPrincipal = findViewById(R.id.contenedorMenuPrincipal)
        contenedorDetalleActividad = findViewById(R.id.contenedorDetalleActividad)

        salida = findViewById(R.id.salida)
        txtActividadActiva = findViewById(R.id.txtActividadActiva)
        panelActividad5 = findViewById(R.id.panelActividad5)
        btnVolverMenu = findViewById(R.id.btnVolverMenu)

        btnActividad1 = findViewById(R.id.btnActividad1)
        btnActividad2 = findViewById(R.id.btnActividad2)
        btnActividad3 = findViewById(R.id.btnActividad3)
        btnActividad4 = findViewById(R.id.btnActividad4)
        btnActividad5 = findViewById(R.id.btnActividad5)

        btnAltaPrecision = findViewById(R.id.btnAltaPrecision)
        btnAhorroBateria = findViewById(R.id.btnAhorroBateria)

        manejadorLoc = getSystemService(LOCATION_SERVICE) as LocationManager

        crearCanalNotificacion()


        btnActividad1.setOnClickListener { seleccionarActividad(1) }
        btnActividad2.setOnClickListener { seleccionarActividad(2) }
        btnActividad3.setOnClickListener { seleccionarActividad(3) }
        btnActividad4.setOnClickListener { seleccionarActividad(4) }
        btnActividad5.setOnClickListener { seleccionarActividad(5) }


        btnVolverMenu.setOnClickListener { volverAlMenuPrincipal() }


        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (actividadSeleccionada != 0) {
                    volverAlMenuPrincipal()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        // Sub-panel Actividad 5
        btnAltaPrecision.setOnClickListener {
            cambiarModoPrecision(LocationManager.GPS_PROVIDER, "Alta Precisión (GPS)")
        }
        btnAhorroBateria.setOnClickListener {
            cambiarModoPrecision(LocationManager.NETWORK_PROVIDER, "Ahorro de Batería (Red)")
        }

        // Verificar permisos al iniciar
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                PERMISO_LOCALIZACION
            )
        } else {
            iniciarLocalizacionBase()
        }
    }

    private fun iniciarLocalizacionBase() {
        proveedor = when {
            manejadorLoc.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            manejadorLoc.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> {
                return
            }
        }
    }

    private fun seleccionarActividad(numero: Int) {
        actividadSeleccionada = numero
        salida.text = ""

        // Cambiar visibilidad de las pantallas
        contenedorMenuPrincipal.visibility = View.GONE
        contenedorDetalleActividad.visibility = View.VISIBLE
        panelActividad5.visibility = if (numero == 5) View.VISIBLE else View.GONE

        when (numero) {
            1 -> {
                txtActividadActiva.text = "Actividad 1: Rastreo cada 5s y Distancia Haversine"
                log("=== ACTIVIDAD 1: RASTREO Y DISTANCIA RECORRIDA ===")
                log("Actualizaciones programadas cada 5000 ms (5 segundos).")
                solicitarActualizaciones(5000L)
            }
            2 -> {
                txtActividadActiva.text = "Actividad 2: Notificación a menos de 50m"
                log("=== ACTIVIDAD 2: NOTIFICACIÓN DE LLEGADA A DESTINO ===")
                log("Punto de destino objetivo: Lat: $destinoLat, Lon: $destinoLon")
                notificacionEnviada = false
                solicitarActualizaciones(3000L)
            }
            3 -> {
                txtActividadActiva.text = "Actividad 3: Simulación de Movimiento GPX"
                log("=== ACTIVIDAD 3: REGISTRO DE RUTA SIMULADA GPX ===")
                log("Instrucciones: En el Emulador abre Extended Controls (...) " +
                        "-> Location -> Routes, carga el archivo GPX y haz clic en PLAY.")
                solicitarActualizaciones(2000L)
            }
            4 -> {
                txtActividadActiva.text = "Actividad 4: Geocodificación Inversa (Dirección)"
                log("=== ACTIVIDAD 4: CONVERSIÓN DE COORDENADAS A DIRECCIÓN ===")
                log("Se consultará el Geocoder en cada cambio de posición.")
                solicitarActualizaciones(3000L)
            }
            5 -> {
                txtActividadActiva.text = "Actividad 5: Configurar Precisión (GPS vs Red)"
                log("=== ACTIVIDAD 5: CONFIGURACIÓN DE PRECISIÓN DE LOCALIZACIÓN ===")
                log("Proveedor actual: $proveedor")
                log("Usa los botones superiores para cambiar entre Alta Precisión (GPS) y Ahorro de Batería (Red).")
                solicitarActualizaciones(4000L)
            }
        }
    }

    private fun volverAlMenuPrincipal() {
        actividadSeleccionada = 0
        salida.text = ""
        if (::manejadorLoc.isInitialized) {
            manejadorLoc.removeUpdates(this)
        }
        contenedorDetalleActividad.visibility = View.GONE
        contenedorMenuPrincipal.visibility = View.VISIBLE
    }

    private fun solicitarActualizaciones(intervaloMs: Long) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        manejadorLoc.removeUpdates(this)
        val ultima = manejadorLoc.getLastKnownLocation(proveedor)
        if (ultima != null) {
            log("Última ubicación conocida:")
            muestraLocaliz(ultima)
        }

        manejadorLoc.requestLocationUpdates(proveedor, intervaloMs, 0f, this)
    }

    private fun cambiarModoPrecision(nuevoProveedor: String, nombreModo: String) {
        if (manejadorLoc.isProviderEnabled(nuevoProveedor)) {
            proveedor = nuevoProveedor
            log("\n---> Proveedor cambiado a: $nombreModo <---")
            solicitarActualizaciones(4000L)
        } else {
            log("\nEl proveedor $nuevoProveedor no está habilitado en este dispositivo.")
        }
    }

    override fun onLocationChanged(location: Location) {
        if (actividadSeleccionada == 0) return

        log("\n--- Nueva posición recibida ---")
        muestraLocaliz(location)

        when (actividadSeleccionada) {
            1 -> ejecutarActividad1(location)
            2 -> ejecutarActividad2(location)
            3 -> ejecutarActividad3(location)
            4 -> ejecutarActividad4(location)
            5 -> ejecutarActividad5(location)
        }
    }

    private fun muestraLocaliz(loc: Location) {
        log("Lat: ${loc.latitude}, Lon: ${loc.longitude}")
    }

    private fun ejecutarActividad1(location: Location) {
        val velocidadKmh = location.speed * 3.6f
        log("Velocidad: ${location.speed} m/s (%.2f km/h)".format(velocidadKmh))

        ultimaUbicacion?.let { prev ->
            val d = haversine(prev.latitude, prev.longitude, location.latitude, location.longitude)
            distanciaTotalMetros += d
        }
        ultimaUbicacion = location
        log("Distancia total recorrida (Haversine): %.2f metros".format(distanciaTotalMetros))
    }

    private fun ejecutarActividad2(location: Location) {
        val distanciaAlDestino = haversine(location.latitude, location.longitude, destinoLat, destinoLon)
        log("Distancia actual al destino: %.2f metros".format(distanciaAlDestino))

        if (distanciaAlDestino < 50.0) {
            log("¡HAS LLEGADO A TU DESTINO!")
            if (!notificacionEnviada) {
                enviarNotificacionDestino()
                notificacionEnviada = true
            }
        }
    }

    private fun ejecutarActividad3(location: Location) {
        log("[Punto de Ruta GPX] Altitud: ${location.altitude}m | Precisión: ${location.accuracy}m")
    }

    private fun ejecutarActividad4(location: Location) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val direcciones = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (!direcciones.isNullOrEmpty()) {
                val dir = direcciones[0]
                val calle = dir.thoroughfare ?: dir.getAddressLine(0) ?: "Calle desconocida"
                val ciudad = dir.locality ?: dir.subAdminArea ?: "Ciudad desconocida"
                val pais = dir.countryName ?: "País desconocido"
                log("Dirección obtenida: $calle, $ciudad, $pais")
            } else {
                log("Dirección no encontrada para estas coordenadas.")
            }
        } catch (e: IOException) {
            log("Error en Geocoder: ${e.message}")
        }
    }

    private fun ejecutarActividad5(location: Location) {
        log("Modo activo: $proveedor | Precisión: ${location.accuracy} metros")
    }

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }

    private fun crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Canal Destino"
            val descriptionText = "Notificaciones de llegada a destino"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun enviarNotificacionDestino() {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Llegada a destino")
            .setContentText("Has llegado a tu destino")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, builder.build())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISO_LOCALIZACION &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            iniciarLocalizacionBase()
        } else {
            log("Permiso de ubicación denegado.")
        }
    }

    override fun onPause() {
        super.onPause()
        if (::manejadorLoc.isInitialized) {
            manejadorLoc.removeUpdates(this)
        }
    }

    override fun onResume() {
        super.onResume()
        if (::manejadorLoc.isInitialized && ::proveedor.isInitialized && actividadSeleccionada != 0) {
            solicitarActualizaciones(5000L)
        }
    }

    private fun log(texto: String) {
        salida.append("$texto\n")
    }
}