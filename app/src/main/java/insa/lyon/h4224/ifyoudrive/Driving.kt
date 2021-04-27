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
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        //if(hasPermissions()) {
            Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
            setContentView(R.layout.activity_driving);

            val map : MapView = findViewById(R.id.mapview)
            map.setTileSource(TileSourceFactory.MAPNIK);

            val mapController = map.controller
            mapController.setZoom(9.5)
            val startPoint = GeoPoint(48.8583, 2.2944);
            mapController.setCenter(startPoint);
        //}
    }

    private fun hasPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) ==
                PackageManager.PERMISSION_GRANTED
    }
}