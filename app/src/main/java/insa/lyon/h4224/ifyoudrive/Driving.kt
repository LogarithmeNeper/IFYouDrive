package insa.lyon.h4224.ifyoudrive

import android.Manifest
import android.app.Activity
import android.app.VoiceInteractor
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import android.speech.tts.TextToSpeech
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
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.osmdroid.bonuspack.routing.GraphHopperRoadManager
import org.osmdroid.bonuspack.routing.Road
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
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.math.*
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sqrt
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.*


/**
 * Class for the driving activity.
 * Displays the map, the speed, a compass and a search field.
 * Using the Openstreetmap API.
 * Working GPS using Graphhopper and Nominatim.
 * Yet to do : alerts, path recompute, erase past line.
 */
class Driving : AppCompatActivity(), TextToSpeech.OnInitListener {
    // In order to get the position
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Global variables
    private var previousLat: Double = 0.0
    private var previousLong: Double = 0.0
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var previousIndV: Int = 0
    private var previousIndH: Int = 0
    private var indexV: Int = 0
    private var indexH: Int = 0
    private var previousDangerZoneState: Int = 0
    private var dangerZoneState: Int = 0
    private var tabSpeed: MutableList<Double> = mutableListOf(0.0)
    private var previousTime: Long = 0L
    private var time: Long = 0L
    private lateinit var mLocationOverlay: MyLocationNewOverlay
    private lateinit var btnCentre: Button
    private var freeCam = false
    private lateinit var route: String
    private var jsonObject: JSONArray? = null
    private lateinit var targetPos: GeoPoint
    private lateinit var txtAddress: EditText
    private lateinit var roadOverlay: Polyline
    private lateinit var btnFin: Button
    private lateinit var map: MapView
    private lateinit var layoutAddress: TextInputLayout
    private lateinit var layoutRouting: LinearLayout
    private lateinit var txtLength: TextView
    private lateinit var txtTime: TextView
    private lateinit var imgRoute: ImageView
    var tts: TextToSpeech? = null

    /**
     * Function used when creating the window at the beginning.
     * Uses the template of the activity as it is defined in ~/res/layout/activity_driving.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Getting the shared preferences
        Configuration.getInstance().load(
            this, androidx.preference.PreferenceManager.getDefaultSharedPreferences(
                this
            )
        )

        // Use of template
        setContentView(R.layout.activity_driving)

        // Creation of the grid for the danger zones
        val cin : InputStream = assets.open("clusterized_accidents_2017_2018_2019_lyon.csv")
        val reader = cin.bufferedReader()

        val accidentsGrid : BooleanArray = BooleanArray(172040)

        // functional for each
        reader.forEachLine {
                line ->
            var splittedline = line.split(",")
            var indV : Int = ((45.832835 - splittedline[0].toDouble())/0.000452).toInt() // Round down
            var indH : Int = ((5.028014 - splittedline[1].toDouble())/0.000642).toInt()  // Round down
            accidentsGrid[460*indV+indH] = true
        }
      
        //tts
        tts = TextToSpeech(this, this)

        // Getting the position.
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        map = findViewById(R.id.mapview)
        btnCentre = findViewById(R.id.btnCentre)
        btnFin = findViewById(R.id.btnFin)
        txtAddress = findViewById(R.id.txtAddress)
        layoutAddress = findViewById(R.id.layoutAddress)
        layoutRouting = findViewById(R.id.layoutRouting)
        txtLength = findViewById(R.id.txtLength)
        txtTime = findViewById(R.id.txtTime)
        imgRoute = findViewById(R.id.imgRoute)


        // Using the Tilesourcefactory of OSM.
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.addMapListener(DelayedMapListener(object : MapListener {
            // When zooming
            override fun onZoom(e: ZoomEvent): Boolean {
                return true
            }

            // When scrolling, liberate the layout and display the button to recenter
            override fun onScroll(e: ScrollEvent): Boolean {
                if (e.x != 0 || e.y != 0) {
                    btnCentre.visibility = View.VISIBLE
                    freeCam = true
                }
                return true
            }
        }, 100))
        map.minZoomLevel =
            5.0 //Limite la possibilité de dézoomer à une échelle qui dépasse la taille du planisphère
        map.maxZoomLevel =
            20.0 //Limite la possibilité de zoomer au point de ne plus pouvoir lire la carte
        map.isVerticalMapRepetitionEnabled = false
        map.setScrollableAreaLimitLatitude(TileSystem.MaxLatitude, -TileSystem.MaxLatitude, 0)
        val mapController = map.controller

        mapController.setZoom(18.0)
        mLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map)
        mLocationOverlay.enableMyLocation()
        mLocationOverlay.setPersonIcon(
            BitmapFactory.decodeResource(
                resources,
                R.drawable.marker_car_on
            )
        ) // pour remplacer le poti bonhomme par une potite voiture
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
        val dm: DisplayMetrics = this.resources.displayMetrics
        val mScaleBarOverlay = ScaleBarOverlay(map)
        mScaleBarOverlay.setCentred(true)
        mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10)
        map.overlays.add(mScaleBarOverlay)

        val request: LocationRequest = LocationRequest.create()
        request.interval = 100
        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        while (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        // Used to get an update on the current location
        fusedLocationClient.requestLocationUpdates(request, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val textField: TextView = findViewById(R.id.textSpeedDriving)

                // Checking if the location permission is granted, otherwise looping
                while (ActivityCompat.checkSelfPermission(
                        this@Driving,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                }
                // When everything goes our way
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        latitude = location.latitude
                        longitude = location.longitude
                        time = System.currentTimeMillis()
                        if (previousLat != 0.0 && previousLong != 0.0 && previousTime != 0L) {
                            // Calculate the distance and deduce the speed
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
                                val maxSpeed = getSpeedLimit(latitude, longitude)
                                textField.text =
                                    "${meanSpeed.toInt()} km/h"
                                //textField.text = "$maxSpeed km/h"
                                if(meanSpeed.toInt() > maxSpeed)
                                {
                                    textField.setTextColor(Color.RED)
                                }
                                else
                                {
                                    textField.setTextColor(Color.BLACK)
                                }
                            }
                        }

                        // Check the position on the accidents grid
                        indexV = ((45.832835 - latitude)/0.000452).toInt() // Round down
                        indexH = ((5.028014 - longitude)/0.000642).toInt()  // Round down
                        if (indexH > previousIndH) {
                            if (indexV < previousIndV) {
                                dangerZoneState = testZone(indexV*460+indexH,3,accidentsGrid)
                            } else if (indexV > previousIndV) {
                                dangerZoneState = testZone(indexV*460+indexH,8,accidentsGrid)
                            } else {
                                dangerZoneState = testZone(indexV*460+indexH,5,accidentsGrid)
                            }
                        } else if (indexH < previousIndH) {
                            if (indexV < previousIndV) {
                                dangerZoneState = testZone(indexV*460+indexH,1,accidentsGrid)
                            } else if (indexV > previousIndV) {
                                dangerZoneState = testZone(indexV*460+indexH,6,accidentsGrid)
                            } else {
                                dangerZoneState = testZone(indexV*460+indexH,4,accidentsGrid)
                            }
                        } else {
                            if (indexV < previousIndV) {
                                dangerZoneState = testZone(indexV*460+indexH,2,accidentsGrid)
                            } else if (indexV > previousIndV) {
                                dangerZoneState = testZone(indexV*460+indexH,7,accidentsGrid)
                            } else {
                                dangerZoneState = previousDangerZoneState
                            }
                        }
                        if (dangerZoneState == 0) {
                            when(previousDangerZoneState) {
                                2 -> { tts!!.speak("Vous sortez d'une zone de danger", TextToSpeech.QUEUE_FLUSH, null) }
                                3 -> { tts!!.speak("Vous sortez d'une zone de danger", TextToSpeech.QUEUE_FLUSH, null) }
                            }
                        } else if (dangerZoneState == 1) {
                            when(previousDangerZoneState) {
                                0 -> { tts!!.speak("Vous approchez d'une zone de danger", TextToSpeech.QUEUE_FLUSH, null) }
                                2 -> dangerZoneState = 3
                            }
                        } else {
                            when (previousDangerZoneState) {
                                0 -> { tts!!.speak("Vous entrez dans une zone de danger", TextToSpeech.QUEUE_FLUSH, null) }
                                1 -> { tts!!.speak("Vous entrez dans une zone de danger", TextToSpeech.QUEUE_FLUSH, null) }
                            }
                        }

                        previousLat = latitude
                        previousLong = longitude
                        previousTime = time
                        previousIndH = indexH
                        previousIndV = indexV
                        previousDangerZoneState = dangerZoneState
                    }

                    if (!freeCam) {
                        mapController.animateTo(
                            GeoPoint(latitude, longitude),
                            map.zoomLevelDouble,
                            100,
                            -compassOverlay.orientation
                        )
                    }
                }
            }
        }, Looper.getMainLooper())

        // Setting the policy and using the GraphHopperRoadManager to build the route
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        // Listen to TextInput address bar
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
                            val obj: JSONObject = jsonObject!!.get(0) as JSONObject
                            val tlat = obj.getString("lat")
                            val tlong = obj.getString("lon")
                            targetPos = GeoPoint(tlat.toDouble(), tlong.toDouble())
                            computeRoute()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                    return true
                }
                return false
            }
        })

    }

    // compute & display route
    fun computeRoute() {
        val roadManager: RoadManager = GraphHopperRoadManager(
            "c61c6759-54c5-4009-9c03-47d4498d97a2",
            true
        )
        doAsync {
            try {
                val waypoints = ArrayList<GeoPoint>()
                while (!this@Driving::targetPos.isInitialized) {
                }
                waypoints.add(GeoPoint(latitude, longitude))
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
                    hideKeyboard(this@Driving)
                    btnFin.visibility = View.VISIBLE
                    layoutAddress.visibility = View.GONE
                    layoutRouting.visibility = View.VISIBLE
                }
                handleRoute(road)
                map.invalidate()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Update informations when reaching a node
     */
    private fun handleRoute(road: Road) {
        var timeup = false
        var reload = false
        for (i in road.mNodes.indices) {
            if (btnFin.visibility != View.VISIBLE || reload)
                break
            when (road.mNodes[i].mManeuverType) {
                // Specific images
                1 -> imgRoute.setImageDrawable(resources.getDrawable(R.drawable.ic_continue))
                6 -> imgRoute.setImageDrawable(resources.getDrawable(R.drawable.ic_slight_right))
                7 -> imgRoute.setImageDrawable(resources.getDrawable(R.drawable.ic_turn_right))
                8 -> imgRoute.setImageDrawable(resources.getDrawable(R.drawable.ic_sharp_right))
                5 -> imgRoute.setImageDrawable(resources.getDrawable(R.drawable.ic_sharp_left))
                4 -> imgRoute.setImageDrawable(resources.getDrawable(R.drawable.ic_turn_left))
                3 -> imgRoute.setImageDrawable(resources.getDrawable(R.drawable.ic_slight_left))
                24 -> imgRoute.setImageDrawable(resources.getDrawable(R.drawable.ic_arrived))
                else -> { imgRoute.setImageDrawable(resources.getDrawable(R.drawable.ic_empty))}
            }
            val rn = Calendar.getInstance()
            var tempsRestant  = 0.0
            for (j in i..road.mNodes.size) {
                tempsRestant += road.mNodes[i].mDuration
            }
            rn.add(Calendar.SECOND, tempsRestant.toInt())
            txtTime.text = java.lang.String.format(
                "%02d",
                rn.get(Calendar.HOUR_OF_DAY)
            ) + ":" + java.lang.String.format(
                "%02d", rn.get(
                    Calendar.MINUTE
                )
            )
            synthese(road.mNodes[i].mInstructions)
            var distanceNode = GeoPoint(latitude, longitude).distanceToAsDouble(road.mNodes[i].mLocation)
            while (btnFin.visibility == View.VISIBLE && road.mNodes[i].mLocation.distanceToAsDouble(GeoPoint(latitude,longitude)) > 20.0) {
                val distance = GeoPoint(latitude, longitude).distanceToAsDouble(road.mNodes[i].mLocation)
                if (distance > 1000)
                    txtLength.text = (distance / 1000).toInt().toString() + "km"
                else
                    txtLength.text = distance.toInt().toString() + "m"
                if (GeoPoint(latitude, longitude).distanceToAsDouble(road.mNodes[i].mLocation) > distanceNode + 30) {
                    computeRoute()
                    reload = true
                    break
                }
            }
        }
    }

    /**
     * Utility function to center the map : no more center button and no more free cam.
     */
    fun onCentrerClick(v: View?) {
        freeCam = false
        btnCentre.visibility = View.INVISIBLE
    }

    /**
     * Called when a route is over or interrupted
     */
    fun routeDone() {
        btnFin.visibility = View.INVISIBLE
        layoutAddress.visibility = View.VISIBLE
        layoutRouting.visibility = View.GONE

        map.overlays.remove(roadOverlay)
        map.invalidate()
        txtAddress.text.clear()
    }

    /**
     * When user want to interrupt a route
     */
    fun onFinClick(v: View?) {
        routeDone()
    }

    /**
     * Function to speak route instructions
     */
    private  fun synthese(voice:String) {
        tts!!.speak(voice,TextToSpeech.QUEUE_FLUSH, null)
    }

    /**
     * Utility function for asyncronous tasks.
     */
    private fun doAsync(f: () -> Unit) {
        Thread { f() }.start()
    }

    private fun hideKeyboard(activity: Activity) {
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

    /**
     * Function to calculate the distance between two points with latitude/longitude.
     */
    fun distance(
        latA: Double,
        longA: Double,
        latB: Double,
        longB: Double
    ): Double {
        val R = 6371000
        val diffLat: Double = latB - latA
        val diffLong: Double = longB - longA
        val a: Double =
            (sin((diffLat / 2.0) * (Math.PI / 180.0))).pow(2) + cos(latA * (Math.PI / 180.0)) * cos(
                latB * (Math.PI / 180.0)
            ) * (sin((diffLong / 2.0) * (Math.PI / 180.0))).pow(2)
        val c: Double = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    /**
     * On initialization for Text To Speech
     */
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.FRANCE)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language specified is not supported!")
            }
        } else {
            Log.e("TTS", "Initialization Failed!")
        }
    }

    /**
     * Performing a POST call using a request URL and the data wanted.
     */
    fun performPostCall(
        requestURL: String?,
        data: String?
    ): String {
        val url: URL
        var response: String = ""
        try {
            url = URL(requestURL)
            val conn =
                url.openConnection() as HttpURLConnection
            conn.readTimeout = 15000
            conn.connectTimeout = 15000
            conn.requestMethod = "POST"
            conn.doInput = true
            conn.doOutput = true
            val os = conn.outputStream
            val writer = BufferedWriter(
                OutputStreamWriter(os, "UTF-8")
            )
            writer.write(data)
            writer.flush()
            writer.close()
            os.close()
            val responseCode = conn.responseCode
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                var line: String?
                val br = BufferedReader(InputStreamReader(conn.inputStream))
                while (br.readLine().also { line = it } != null) {
                    response += line
                }
            } else {
                response = ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return response
    }

    /**
     * Getting speed limit of the current tile, using a query on OSM.
     */
    fun getSpeedLimit (latitude : Double, longitude : Double) : Int
    {
        var maxSpeed = 1000
        var maxSpeedObtained = false
        doAsync {
            val data: String = """
            <query type="way">
                <around radius="10" lat="${latitude}" lon="${longitude}" />
                <has-kv k="maxspeed" />
            </query>

            <!-- added by auto repair -->
            <union>
                <item/>
                <recurse type="down"/>
            </union>
            <!-- end of auto repair -->
            <print/></osm-script>'
            """
            var response = ""
            var speedNotFound = true

            response = performPostCall("http://overpass-api.de/api/interpreter", data)

            while (response == "") {
                Thread.sleep(1)
            }
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val xpp = factory.newPullParser()

            xpp.setInput(StringReader(response))
            var eventType = xpp.eventType
            while (speedNotFound && eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.name == "way") {
                        while (speedNotFound && eventType != XmlPullParser.END_DOCUMENT) {
                            if (eventType == XmlPullParser.START_TAG) {
                                if (xpp.name == "tag") {
                                    if (xpp.getAttributeValue(0) == "maxspeed") {
                                        maxSpeed = (xpp.getAttributeValue(1).toString()).toInt()
                                        speedNotFound = false
                                        maxSpeedObtained = true
                                    }
                                }
                            }
                            eventType = xpp.next()
                        }
                    }
                }
                if (eventType != XmlPullParser.END_DOCUMENT) {
                    eventType = xpp.next()
                }
            }
        }
        while(!maxSpeedObtained)
        {
            Thread.sleep(1)
        }
        return maxSpeed
    }

    /**
     * Testing the dangerosity of the current tile.
     */
    private fun testZone(index : Int,direction : Int,grid : BooleanArray) : Int {
        if ((index >= 172040) or (index < 0)) { //Out of our grid
            return 0
        }
        if (grid[index]) {
            return 2
        } else {
            var dangerNear : Boolean = false
            //Booleans to check if the position is on the border of the grid to avoid out of range indexes
            var borderUp : Boolean = index < 460
            var borderDown : Boolean = index > 171579
            var borderLeft : Boolean = index % 460 == 0
            var borderRight : Boolean = index % 460 == 459
            var almostBorderUp : Boolean = (index < 920) and (index >= 460)
            var almostBorderDown : Boolean = (index > 171119) and (index <= 172580)
            var almostBorderLeft : Boolean = index % 460 == 1
            var almostBorderRight : Boolean = index % 460 == 458
            when (direction) {
                1 -> {
                    dangerNear = (!borderLeft and grid[index-1]) or (!borderLeft and !borderDown and grid[index+459]) or (!borderUp and !almostBorderLeft and grid[index-462]) or (!borderLeft and !borderUp and grid[index-461]) or (!borderUp and grid[index-460]) or (!borderUp and !borderRight and grid[index-459]) or (!borderUp and !almostBorderUp and !borderLeft and grid[index-921]) or (!borderLeft and !almostBorderLeft and !borderUp and !almostBorderUp and grid[index-922])
                }
                2 -> {
                    dangerNear = (!borderLeft and grid[index-1]) or (!borderRight and grid[index+1]) or (!borderLeft and !borderUp and grid[index-461]) or (!borderUp and grid[index-460]) or (!borderRight and !borderUp and grid[index-459]) or (!almostBorderUp and !borderUp and grid[index-920])
                }
                3 -> {
                    dangerNear = (!borderRight and grid[index+1]) or (!borderRight and !almostBorderRight and !borderUp and grid[index-458]) or (!borderRight and !borderUp and grid[index-459]) or (!borderUp and grid[index-460]) or (!borderLeft and !borderUp and grid[index-461]) or (!borderDown and !borderRight and grid[index+461]) or (!borderRight and !borderUp and !almostBorderRight and !almostBorderUp and grid[index-918]) or (!borderUp and !almostBorderUp and !borderRight and grid[index-919])
                }
                4 -> {
                    dangerNear = (!borderRight and grid[index-1]) or (!borderRight and almostBorderRight and grid[index-2]) or (!borderLeft and !borderUp and grid[index-461]) or (!borderUp and grid[index-460]) or (!borderDown and !borderLeft and grid[index+459]) or (!borderDown and grid[index+460])
                }
                5 -> {
                    dangerNear = (!borderRight and grid[index+1]) or (!borderRight and !almostBorderRight and grid[index+2]) or (!borderDown and !borderRight and grid[index+461]) or (!borderDown and grid[index+460]) or (!borderRight and !borderUp and grid[index-459]) or (!borderUp and grid[index-460])
                }
                6 -> {
                    dangerNear = (!borderLeft and grid[index-1]) or (!borderLeft and !almostBorderLeft and !borderDown and grid[index+458]) or (!borderDown and !borderLeft and grid[index+459]) or (!borderDown and grid[index+460]) or (!borderDown and !borderRight and grid[index+461]) or (!borderUp and !borderLeft and grid[index-461]) or (!borderDown and !almostBorderDown and !borderLeft and !almostBorderLeft and grid[index+918]) or (!borderLeft and !borderDown and !almostBorderDown and grid[index+919])
                }
                7 -> {
                    dangerNear = (!borderRight and grid[index+1]) or (!borderLeft and grid[index-1]) or (!borderDown and !borderRight and grid[index+461]) or (!borderDown and grid[index+460]) or (!borderDown and !borderLeft and grid[index+459]) or (!borderDown and !almostBorderDown and grid[index+920])
                }
                8 -> {
                    dangerNear = (!borderRight and grid[index+1]) or (!borderRight and !borderUp and grid[index-459]) or (!borderRight and !almostBorderRight and !borderDown and grid[index+462]) or (!borderDown and !borderRight and grid[index+461]) or (!borderDown and grid[index+460]) or (!borderDown and !borderLeft and grid[index+459]) or (!borderRight and !borderDown and !almostBorderDown and grid[index+921]) or (!borderDown and !almostBorderDown and !borderRight and !almostBorderRight and grid[index+922])
                }
            }
            if (dangerNear) {
                return 1
            }
        }
        return 0
    }
}
