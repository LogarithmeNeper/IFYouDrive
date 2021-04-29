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
    var listSpeedX : MutableList<Float> = mutableListOf(0F)
    var listSpeedY : MutableList<Float> = mutableListOf(0F)
    var listSpeedZ : MutableList<Float> = mutableListOf(0F)
    var previousTime : Long = 0
    var actualSpeedX : Float = 0F
    var actualSpeedY : Float = 0F
    var actualSpeedZ : Float = 0F
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
        var accX : Float = 0F
        var accY : Float = 0F
        var accZ : Float = 0F
        var actualTime : Long = 0

        if (event?.sensor?.type == Sensor.TYPE_LINEAR_ACCELERATION) {
            accX = event.values[0];
            accY = event.values[1];
            accZ = event.values[2];
            actualTime = System.currentTimeMillis()
            if(previousTime > 0 ) {
                // On intègre l'accélération par rapport au temps (en sec) sur les 3 axes
                if(accX > 0.1) {
                    actualSpeedX += accX * ((actualTime - previousTime) / 1000)
                    listSpeedX.add(actualSpeedX)
                }
                if(accY > 0.1) {
                    actualSpeedY += accY * ((actualTime - previousTime) / 1000)
                    listSpeedY.add(actualSpeedY)
                }
                if(accZ > 0.1) {
                    actualSpeedZ += accZ * ((actualTime - previousTime) / 1000)
                    listSpeedZ.add(actualSpeedZ)
                }

                if (listSpeedX.size > 20) {
                    listSpeedX.removeFirst() // Ainsi on a les 20 dernières vitesses glissantes
                }
                if (listSpeedY.size > 20) {
                    listSpeedY.removeFirst() // Ainsi on a les 20 dernières vitesses glissantes
                }
                if (listSpeedZ.size > 20) {
                    listSpeedZ.removeFirst() // Ainsi on a les 20 dernières vitesses glissantes
                }
            }
            previousTime = actualTime

        }
        if(listSpeedX.isNotEmpty() && listSpeedY.isNotEmpty() && listSpeedZ.isNotEmpty()) {
            var sumSpeedX: Float = 0F
            for (element in listSpeedX) {
                sumSpeedX += element
            }
            var meanSpeedX: Float = sumSpeedX / listSpeedX.size

            var sumSpeedY: Float = 0F
            for (element in listSpeedY) {
                sumSpeedY += element
            }
            var meanSpeedY: Float = sumSpeedY / listSpeedY.size

            var sumSpeedZ: Float = 0F
            for (element in listSpeedZ) {
                sumSpeedZ += element
            }
            var meanSpeedZ: Float = sumSpeedZ / listSpeedZ.size

            var speedMS : Float = sqrt(meanSpeedX*meanSpeedX + meanSpeedY*meanSpeedY + meanSpeedZ*meanSpeedZ)
            var speedKMH : Int = (speedMS*3.6).toInt()

            var textField: TextView = findViewById(R.id.textSpeed)
            textField.text = "actualSpeedX = ${actualSpeedX}, actualSpeedY = ${actualSpeedY}, actualSpeedZ = ${actualSpeedZ}, speed = ${speedKMH}, sizeOfList = ${listSpeedX.size}"

        }
    }
}