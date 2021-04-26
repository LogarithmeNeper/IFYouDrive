package insa.lyon.h4224.ifyoudrive

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.osmdroid.views.MapView


class Driving : AppCompatActivity() {
    private val myOpenMapView: MapView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driving)

        // TODO: as it it deprecated, find a better way to do it
        val myOpenMapView: MapView = findViewById(R.id.mapview)
        myOpenMapView.setBuiltInZoomControls(true)
        myOpenMapView.setClickable(true)
        myOpenMapView.getController().setZoom(15)

    }
}