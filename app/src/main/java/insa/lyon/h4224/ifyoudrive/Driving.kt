package insa.lyon.h4224.ifyoudrive

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.StrictMode
import android.util.DisplayMetrics
import android.util.Log
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


class Driving : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var firstMarker: Marker?=null
    private var init:Boolean = true
    var mGravity: FloatArray? = null
    var mGeomagnetic: FloatArray? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(this, androidx.preference.PreferenceManager.getDefaultSharedPreferences(this))
        setContentView(R.layout.activity_driving)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val map : MapView = findViewById(R.id.mapview)

        map.setTileSource(TileSourceFactory.MAPNIK)
        firstMarker = Marker(map)
      
        map.minZoomLevel = 5.0 //Limite la possibilité de dézoomer à une échelle qui dépasse la taille du planisphère
        map.maxZoomLevel = 20.0 //Limite la possibilité de zoomer au point de ne plus pouvoir lire la carte
        map.isVerticalMapRepetitionEnabled = false
        map.setScrollableAreaLimitLatitude(TileSystem.MaxLatitude,-TileSystem.MaxLatitude, 0)
        val mapController = map.controller
        mapController.setZoom(18.0)

        // added the possibility to rotate the map
        val mRotationGestureOverlay = RotationGestureOverlay(map)
        mRotationGestureOverlay.isEnabled = true
        map.setMultiTouchControls(true)
        map.overlays.add(mRotationGestureOverlay)

        // Added a compass at the top-left of the screen
        val compassOverlay = CompassOverlay(this, map)
        compassOverlay.enableCompass()
        map.overlays.add(compassOverlay)

        // Added for a scale bar at the top of the screen
        val dm : DisplayMetrics = this.resources.displayMetrics
        val mScaleBarOverlay = ScaleBarOverlay(map)
        mScaleBarOverlay.setCentred(true)
        mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10)
        map.overlays.add(mScaleBarOverlay)

        val request : LocationRequest = LocationRequest.create()
        request.interval = 1000
        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        while (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        fusedLocationClient.requestLocationUpdates(request, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                while (ActivityCompat.checkSelfPermission(this@Driving, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                }
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        latitude = location.latitude
                        longitude = location.longitude
                    }
                    firstMarker!!.position = GeoPoint(latitude, longitude)
                    firstMarker!!.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    if (init) {
                        //mapController.setCenter(GeoPoint(latitude, longitude))
                        //mapController.setCenter(GeoPoint(45.78312, 4.87758))
                        map.overlays.add(firstMarker)
                        init = false
                    }
                    mapController.animateTo(firstMarker!!.position, map.zoomLevelDouble, 100, -compassOverlay.orientation)
                }
            }
        }, Looper.getMainLooper())

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        val roadManager: RoadManager = GraphHopperRoadManager("c61c6759-54c5-4009-9c03-47d4498d97a2", true)

        doAsync {
            val waypoints = ArrayList<GeoPoint>()
            while (init) {} // y a surement mieux
            waypoints.add(firstMarker!!.position)
            waypoints.add(GeoPoint(45.76269, 4.86054))
            val road = roadManager.getRoad(waypoints)
            val roadOverlay = RoadManager.buildRoadOverlay(road)
            roadOverlay.outlinePaint.color = Color.RED
            roadOverlay.outlinePaint.strokeWidth = 15.0F
            map.overlays.add(roadOverlay)
            map.invalidate()
        }
    }

    private fun doAsync(f: () -> Unit) {
        Thread { f() }.start()
    }
}

