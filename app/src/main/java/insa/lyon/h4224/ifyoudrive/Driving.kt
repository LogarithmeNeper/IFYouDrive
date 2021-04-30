package insa.lyon.h4224.ifyoudrive

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.preference.PreferenceManager
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
        Configuration.getInstance().load(
            this, PreferenceManager.getDefaultSharedPreferences(
                this
            )
        )
        setContentView(R.layout.activity_driving)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);

        val map : MapView = findViewById(R.id.mapview)
        map.setTileSource(TileSourceFactory.MAPNIK);
        firstMarker = Marker(map)

        val mapController = map.controller
        mapController.setZoom(15.0)
      
        // Added the possibility to rotate the map
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
                while (ActivityCompat.checkSelfPermission(
                        this@Driving,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                }
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        latitude = if (location.latitude != null) location.latitude else latitude
                        longitude =
                            if (location.longitude != null) location.longitude else longitude
                    }
                    firstMarker!!.position = GeoPoint(latitude, longitude)
                    firstMarker!!.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    if (init) {
                        //mapController.setCenter(GeoPoint(latitude, longitude))
                        mapController.setCenter(GeoPoint(45.78312, 4.87758))
                        map.overlays.add(firstMarker)
                        init = false
                    }
                    mapController.animateTo(firstMarker!!.position, 15.0, 100, compassOverlay.orientation)
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
            roadOverlay.outlinePaint.color = Color.RED
            roadOverlay.outlinePaint.strokeWidth = 15.0F
            map.overlays.add(roadOverlay);
            map.invalidate()
        }
    }

    fun doAsync(f: () -> Unit) {
        Thread { f() }.start()
    }

    fun onSensorChanged(event: SensorEvent) {
        Log.d("TAG","cc")
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) mGravity = event.values
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) mGeomagnetic = event.values
        if (mGravity != null && mGeomagnetic != null) {
            val R = FloatArray(9)
            val I = FloatArray(9)
            if (SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic)) {

                // orientation contains azimut, pitch and roll
                val orientation = FloatArray(3)
                SensorManager.getOrientation(R, orientation)
                //azimut = orientation[0]
            }
        }
    }
}

