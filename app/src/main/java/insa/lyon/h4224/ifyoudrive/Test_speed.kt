package insa.lyon.h4224.ifyoudrive

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.sqrt


class Test_speed : AppCompatActivity(), SensorEventListener {
    // Le sensor manager (gestionnaire de capteurs)
    var sensorManager: SensorManager? = null

    // L'accéléromètre
    var linearAccelerationSensor: Sensor? = null
    var mSensor : Sensor? = null
    var listSpeed : MutableList<Float> = mutableListOf(0F)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_speed)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager?
        linearAccelerationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        mSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION)

    }

    override fun onPause() {
        // unregister the sensor (désenregistrer le capteur)
        sensorManager!!.unregisterListener(this, linearAccelerationSensor)
        super.onPause()
    }

    override fun onResume() {
        /* Ce qu'en dit Google dans le cas de l'accéléromètre :
         * «  Ce n'est pas nécessaire d'avoir les évènements des capteurs à un rythme trop rapide.
         * En utilisant un rythme moins rapide (SENSOR_DELAY_UI), nous obtenons un filtre
         * automatique de bas niveau qui "extrait" la gravité  de l'accélération.
         * Un autre bénéfice étant que l'on utilise moins d'énergie et de CPU. »
         */
        sensorManager!!.registerListener(this, linearAccelerationSensor, SensorManager.SENSOR_DELAY_UI)
        super.onResume()
    }

    override fun onAccuracyChanged(sensor : Sensor, accuracy : Int) {
        // Rien à faire la plupart du temps
    }

    override fun onSensorChanged(event: SensorEvent?) {
        // Récupérer les valeurs du capteur
        var x : Float = 0F
        var y : Float = 0F
        var z : Float = 0F
        var speed : Float = 0F // actuellement, uniquement vitesse sur x et y
        if (event?.sensor?.type == Sensor.TYPE_LINEAR_ACCELERATION) {
            x = event.values[0];
            y = event.values[1];
            z = event.values[2];
            speed = sqrt(x*x + y*y + z*z)
        }
        listSpeed.add(speed) // Add at the end of the list
        if(listSpeed.size > 50)
        {
            listSpeed.removeFirst() // Ainsi on a les 20 dernières vitesses glissantes
        }
        var sumSpeed : Float = 0F
        for(element in listSpeed)
        {
            sumSpeed += element
        }
        var meanSpeed : Int = (sumSpeed / listSpeed.size).toInt()
        var textField : TextView = findViewById(R.id.textSpeed)
        textField.text = "speed = ${meanSpeed}, sizeOfList = ${listSpeed.size}"
    }
}