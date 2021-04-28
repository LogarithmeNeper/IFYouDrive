package insa.lyon.h4224.ifyoudrive

import android.os.Bundle
import android.preference.PreferenceManager
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider

import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay








class Driving : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(
            this, PreferenceManager.getDefaultSharedPreferences(
                this
            )
        )
        setContentView(R.layout.activity_driving)

        val map : MapView = findViewById(R.id.mapview)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMinZoomLevel(5.0) //Limite la possibilité de dézoomer à une échelle qui dépasse la taille du planisphère
        map.setMaxZoomLevel(20.0) //Limite la possibilité de zoomer au point de ne plus pouvoir lire la carte

        val mapController = map.controller
        mapController.setZoom(18.0)
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
    }
}