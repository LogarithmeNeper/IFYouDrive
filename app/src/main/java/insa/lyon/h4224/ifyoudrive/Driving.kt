package insa.lyon.h4224.ifyoudrive

import android.Manifest
import android.app.Activity
import android.app.VoiceInteractor
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.StrictMode
import android.util.DisplayMetrics
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.view.textclassifier.TextLinks
import android.view.textclassifier.TextSelection
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.osmdroid.bonuspack.routing.GraphHopperRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.events.DelayedMapListener
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.TileSystem
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.math.*
import java.net.URL
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sqrt


/*
1. Barre de recherche navigation
2. Bouton de fin de navigation
3. affichage instructions
4. affichage vitesse -- ok
5. bouton fin de liberté pour la carte -- ok

6. effacer le chemin déjà parcouru ?
 */
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
    private lateinit var mLocationOverlay : MyLocationNewOverlay
    private lateinit var btnCentre : Button
    private var freeCam = false
    private lateinit var route : String
    private var jsonObject: JSONArray? = null
    private lateinit var targetPos : GeoPoint
    private lateinit var txtAddress : EditText
    private lateinit var roadOverlay : Polyline
    private  lateinit var btnFin : Button
    private lateinit var map : MapView

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(
            this, androidx.preference.PreferenceManager.getDefaultSharedPreferences(
                this
            )
        )
        setContentView(R.layout.activity_driving)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        map = findViewById(R.id.mapview)

        doAsync {

        }
        btnCentre = findViewById(R.id.btnCentre)
        btnFin = findViewById(R.id.btnFin)
        txtAddress = findViewById(R.id.txtAddress)

        map.setTileSource(TileSourceFactory.MAPNIK)
        firstMarker = Marker(map)

        map.addMapListener(DelayedMapListener(object : MapListener {
            override fun onZoom(e: ZoomEvent): Boolean {
                //do something
                return true
            }

            override fun onScroll(e: ScrollEvent): Boolean {
                if (e.x != 0 || e.y != 0) {
                    btnCentre.visibility = View.VISIBLE
                    freeCam = true
                }
                return true
            }
        }, 100))
        map.minZoomLevel = 5.0 //Limite la possibilité de dézoomer à une échelle qui dépasse la taille du planisphère
        map.maxZoomLevel = 20.0 //Limite la possibilité de zoomer au point de ne plus pouvoir lire la carte
        map.isVerticalMapRepetitionEnabled = false
        map.setScrollableAreaLimitLatitude(TileSystem.MaxLatitude, -TileSystem.MaxLatitude, 0)
        val mapController = map.controller

        mapController.setZoom(18.0)
        mLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map)
        mLocationOverlay.enableMyLocation()
        //mLocationOverlay.setPersonIcon() // pour remplacer le poti bonhomme par une potite voiture
        map.overlays.add(this.mLocationOverlay)

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
                val textField: TextView = findViewById(R.id.textSpeedDriving)


                // Test getting speed limits

                // End of test

                while (ActivityCompat.checkSelfPermission(
                        this@Driving,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                }
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        latitude = location.latitude
                        longitude = location.longitude
                        time = System.currentTimeMillis()
                        if (previousLat != 0.0 && previousLong != 0.0 && previousTime != 0L) {
                            val distance = distance(previousLat, previousLong, latitude, longitude)
                            val speed = (distance / ((time - previousTime) / 1000.0
                                    )) * 3.6
                            tabSpeed.add(speed)
                            if (tabSpeed.size > 5) // Used to limit the size of the tab to 5
                            {
                                tabSpeed.removeFirst()
                            }
                            var sumSpeed = 0.0
                            for (element in tabSpeed) {
                                sumSpeed += element
                            }
                            if (tabSpeed.size != 0) {
                                val meanSpeed = sumSpeed / tabSpeed.size
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
                        //mapController.setCenter(GeoPoint(latitude, longitude))
                        //mapController.setCenter(GeoPoint(45.78312, 4.87758))
                        //map.overlays.add(firstMarker)
                        init = false
                    }
                    if (!freeCam) {
                        mapController.animateTo(
                            firstMarker!!.position,
                            map.zoomLevelDouble,
                            100,
                            -compassOverlay.orientation
                        )
                    }
                }
            }
        }, Looper.getMainLooper())

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        val roadManager: RoadManager = GraphHopperRoadManager(
            "c61c6759-54c5-4009-9c03-47d4498d97a2",
            true
        )

        txtAddress.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
                // if the event is a key down event on the enter button
                if (event.action == KeyEvent.ACTION_DOWN ||
                    keyCode == KeyEvent.KEYCODE_ENTER
                ) {
                    // perform action on key press
                    doAsync {
                        route =
                            URL("https://nominatim.openstreetmap.org/search.php?q=" + txtAddress.text + "&format=json").readText()
                        try {
                            jsonObject = JSONArray(route)
                            Log.d("TAG", "ok")
                            val obj: JSONObject = jsonObject!!.get(0) as JSONObject
                            val tlat = obj.getString("lat")
                            val tlong = obj.getString("lon")
                            targetPos = GeoPoint(tlat.toDouble(), tlong.toDouble())

                            val waypoints = ArrayList<GeoPoint>()
                            while (init) {
                            } // y a surement mieux
                            //while (!this::targetPos.isInitialized) {}
                            waypoints.add(firstMarker!!.position)
                            waypoints.add(targetPos)
                            val road = roadManager.getRoad(waypoints)
                            if (this@Driving::roadOverlay.isInitialized) {
                                map.overlays.remove(roadOverlay)
                            }
                            roadOverlay = RoadManager.buildRoadOverlay(road)
                            roadOverlay.outlinePaint.color = Color.rgb(250, 0, 255)
                            roadOverlay.outlinePaint.strokeWidth = 15.0F
                            map.overlays.add(roadOverlay)
                            runOnUiThread {
                                btnFin.visibility = View.VISIBLE
                                hideKeyboard(this@Driving)
                            }
                            map.invalidate()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }

                    // hide soft keyboard programmatically
                    //hideSoftKeyboard()

                    // clear focus and hide cursor from edit text
                    //editText.clearFocus()
                    //editText.isCursorVisible = false

                    return true
                }
                return false
            }
        })

    }

    fun onCentrerClick(v: View?) {
        freeCam = false
        btnCentre.visibility = View.INVISIBLE
    }

    fun onFinClick(v: View?) {
        btnFin.visibility = View.INVISIBLE
        map.overlays.remove(roadOverlay)
        map.invalidate()
        txtAddress.text.clear()
    }
    private fun doAsync(f: () -> Unit) {
        Thread { f() }.start()
    }

    fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager =
            activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun distance(
        latA: Double,
        longA: Double,
        latB: Double,
        longB: Double
    ): Double
    {
        val R = 6371000
        val diffLat: Double = latB - latA
        val diffLong: Double = longB - longA
        val a: Double = (sin((diffLat/2.0)*(Math.PI/180.0))).pow(2) + cos(latA*(Math.PI/180.0)) * cos(latB*(Math.PI/180.0)) * (sin((diffLong/2.0)*(Math.PI/180.0))).pow(2)
        val c: Double = 2 * atan2(sqrt(a), sqrt(1-a))
        return R*c
    }
}
