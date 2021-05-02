package insa.lyon.h4224.ifyoudrive

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.preference.PreferenceManager
import android.util.DisplayMetrics
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import org.osmdroid.bonuspack.routing.GraphHopperRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.TileSystem
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import java.lang.Math.pow
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sqrt


class Driving : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var previousLat : Double = 0.0
    private var previousLong : Double = 0.0
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var firstMarker: Marker?=null
    private var init:Boolean = true
    private var tabSpeed : MutableList<Double> = mutableListOf(0.0)
    private var previousTime : Long = 0L
    private var time : Long = 0L

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(
            this, PreferenceManager.getDefaultSharedPreferences(
                this
            )
        )
        setContentView(R.layout.activity_driving)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val map : MapView = findViewById(R.id.mapview)

        map.setTileSource(TileSourceFactory.MAPNIK)
        firstMarker = Marker(map)
      
        map.minZoomLevel = 5.0 //Limite la possibilité de dézoomer à une échelle qui dépasse la taille du planisphère
        map.maxZoomLevel = 20.0 //Limite la possibilité de zoomer au point de ne plus pouvoir lire la carte
        map.isVerticalMapRepetitionEnabled = false;
        map.setScrollableAreaLimitLatitude(TileSystem.MaxLatitude,-TileSystem.MaxLatitude, 0)

        val mapController = map.controller
        mapController.setZoom(15.0)

        val startPoint = GeoPoint(45.7819, 4.8726) // Tour Eiffel
        mapController.setCenter(startPoint)

        // added the possibility to rotate the map

        val mRotationGestureOverlay : RotationGestureOverlay = RotationGestureOverlay(this, map)
        mRotationGestureOverlay.setEnabled(true)
        map.setMultiTouchControls(true)
        map.overlays.add(mRotationGestureOverlay)

        // Added a compass at the top-left of the screen
        val compassOverlay : CompassOverlay = CompassOverlay(this, map)
        compassOverlay.enableCompass()
        map.overlays.add(compassOverlay)

        // Added for a scale bar at the top of the screen
        val dm : DisplayMetrics = this.resources.displayMetrics
        val mScaleBarOverlay : ScaleBarOverlay = ScaleBarOverlay(map)
        mScaleBarOverlay.setCentred(true)
        mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10)
        map.overlays.add(mScaleBarOverlay)

        val request : LocationRequest = LocationRequest()
        request.interval = 1000
        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        while (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        fusedLocationClient.requestLocationUpdates(request, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                var textField: TextView = findViewById(R.id.textSpeedDriving)
                while (ActivityCompat.checkSelfPermission(this@Driving, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                }
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        latitude = if (location.latitude != null) location.latitude else latitude
                        longitude = if (location.longitude != null) location.longitude else longitude
                        time = System.currentTimeMillis()
                        if(previousLat != 0.0 && previousLong != 0.0 && previousTime != 0L)
                        {
                            var distance = distance(previousLat, previousLong, latitude, longitude)
                            var speed = (distance/((time-previousTime)/1000.0
                                    ))*3.6
                            tabSpeed.add(speed)
                            if(tabSpeed.size > 5) // Used to limit the size of the tab to 5
                            {
                                tabSpeed.removeFirst()
                            }
                            var sumSpeed : Double = 0.0
                            for(element in tabSpeed)
                            {
                                sumSpeed += element
                            }
                            if(tabSpeed.size != 0) {
                                var meanSpeed = sumSpeed / tabSpeed.size
                                textField.text =
                                    "${meanSpeed.toInt()} km/h"
                            }
                        }
                        previousLat = latitude
                        previousLong = longitude
                        previousTime = time

                    }
                    firstMarker!!.position = GeoPoint(latitude, longitude)
                    firstMarker!!.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    if (init) {
                        mapController.setCenter(GeoPoint(latitude, longitude))
                        map.overlays.add(firstMarker)
                        init = false
                    }
                }
            }
        }, null)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val roadManager: RoadManager = GraphHopperRoadManager(
            "9db0a28e-4851-433f-86c7-94b8a695fb18",
            true
        )

        doAsync {
            val waypoints = ArrayList<GeoPoint>()
            waypoints.add(GeoPoint(45.78312, 4.87758))
            waypoints.add(GeoPoint(45.76269, 4.86054))
            val road = roadManager.getRoad(waypoints)
            val roadOverlay = RoadManager.buildRoadOverlay(road)
            map.overlays.add(roadOverlay);
            map.invalidate()
        }
    }

    fun doAsync(f: () -> Unit) {
        Thread { f() }.start()
    }

    fun distance(
        latA: Double,
        longA: Double,
        latB: Double,
        longB: Double
    ): Double
    {
        val diffLat: Double = latB - latA
        val diffLong: Double = (longB - longA) * cos((latB + latA) / 2)
        val distDeg: Double = sqrt(diffLong.pow(2.0) + diffLat.pow(2.0))
        return distDeg * 1852 * 60
    }
}