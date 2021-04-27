package insa.lyon.h4224.ifyoudrive

import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import android.Manifest
import org.osmdroid.util.GeoPoint

class Driving : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
            setContentView(R.layout.activity_driving)

            val map : MapView = findViewById(R.id.mapview)
            map.setTileSource(TileSourceFactory.MAPNIK)

            val mapController = map.controller
            mapController.setZoom(9.5)
            val startPoint = GeoPoint(48.8583, 2.2944)
            mapController.setCenter(startPoint)
    }
}