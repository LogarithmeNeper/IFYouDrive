package insa.lyon.h4224.ifyoudrive

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.round

/**
 * Class to evaluate the risks.
 */
class EvaluateRisks : AppCompatActivity() {
    /**
     * Function used when creating the window at the beginning.
     * Uses the template of the activity as it is defined in ~/res/layout/activity_evaluate_risks
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evaluate_risks)

        // Getting the necessary information
        val estimationField: TextView = findViewById(R.id.estimation_field)
        val drugsField: TextView = findViewById(R.id.drugs_field)
        val risksAlcoholField: TextView = findViewById(R.id.risks_alcohol)
        val risksDrugsField: TextView = findViewById(R.id.risks_drugs)
        val reactivityButton: Button = findViewById(R.id.reactivity_button)
        val skipToDrive: Button = findViewById(R.id.skip_drive_button)
        val infoAlcohol: Button = findViewById(R.id.alcohol_info)
        val infoDrugs: Button = findViewById(R.id.drugs_info)
        val indicButton : Button = findViewById(R.id.skip_indic_button)

        // Getting the information from the intent (data from previous activity)
        val estimation: Double = intent.getDoubleExtra("Estimation", 0.0)
        val drugsEvaluation: Boolean = intent.getBooleanExtra("Drugs", false)
        val threshold: Double = intent.getDoubleExtra("Threshold", 1.0)

        estimationField.text = (round(estimation*100)/100).toString()
        drugsField.text = if(drugsEvaluation) "Présence de drogues" else "Absence de drogues"

        // Displaying according to the estimation and the threshold. Using the French law.
        if(estimation <= threshold) {
            risksAlcoholField.text = "Votre alcoolémie est estimée inférieure au seuil autorisé (dans votre cas, ${threshold}.)"
        }
        else if(estimation > threshold && estimation <= 0.8) {
            risksAlcoholField.text = "Votre alcoolémie est estimée supérieure au seuil autorisé (dans votre cas, ${threshold} g/L dans le sang). " +
                    "Conduire dans cet état constitue une contravention passible d'une amende de 135€ majorable, du retrait de 6 points sur le permis " +
                    "de conduire, et d'une immobilisation du véhicule."
        }
        else if(estimation > 0.8) {
            risksAlcoholField.text = "Votre alcoolémie est estimée très supérieure au seuil autorisé. Conduire dans cet état constitue un " +
                    "délit passible du retrait de 6 points sur le permis de conduire, d'une amende de 4 500€, d'une peine d'emprisonnement " +
                    "de deux ans maximum, d'une suspension, voire d'un retrait, du permis pour une durée de trois ans maximum, etc..."
        }

        if(drugsEvaluation) {
            risksDrugsField.text = "La conduite sous l'emprise de stupéfiants constitue un délit passible de 4500€ d'amende, d'une peine de 2 ans " +
                    "de prison, ainsi que d'un retrait de 6 points sur le permis de conduire. Il peut aussi entraîner la suspension ou l'annulation du permis de conduire."
        }

        // Intenting to test the user reactivity
        reactivityButton.setOnClickListener {
            val intentToReactivity = Intent(this@EvaluateRisks, EvaluateReaction::class.java)
            startActivity(intentToReactivity)
        }

        // Intenting to go straight to the driving activity.
        skipToDrive.setOnClickListener {
            val intentToDrive = Intent(this@EvaluateRisks, Driving::class.java)
            startActivity(intentToDrive)
        }

        // Buttons for external links.
        infoAlcohol.setOnClickListener {
            val uri: Uri = Uri.parse("https://www.securite-routiere.gouv.fr/dangers-de-la-route/lalcool-et-la-conduite")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        infoDrugs.setOnClickListener {
            val uri: Uri = Uri.parse("https://www.securite-routiere.gouv.fr/dangers-de-la-route/la-drogue-et-la-conduite")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        indicButton.setOnClickListener {
            // Intent in order to go the the indications activity
            val intentToIndications = Intent(this@EvaluateRisks, Indications::class.java)
            startActivity(intentToIndications)
        }

    }

    /*private fun listSensor(sensorManager : SensorManager)
    {
        val sensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)
        // La chaîne descriptive de chaque capteur
        val sensorDesc = StringBuffer()
        // pour chaque capteur trouvé, construire sa chaîne descriptive
        for (sensor in sensors) {
            sensorDesc.append("New sensor detected : \r\n")
            sensorDesc.append(
                "	Name: ${sensor.name} \r\n"
            )
            sensorDesc.append(
                "	Type: ${getType(sensor.type)} \r\n"
            )
            sensorDesc.append(
                "   Version: ${sensor.version} \r\n"
            )
            sensorDesc.append(
                "   Resolution (in the sensor unit): ${sensor.resolution} \r\n"
            )
            sensorDesc.append(
                "   Power in mA used by this sensor while in use ${sensor.power} \r\n"
            )
            sensorDesc.append(
                "   Vendor: ${sensor.vendor} \r\n"
            )
            sensorDesc.append(
                "   Maximum range of the sensor in the sensor's unit. ${sensor.maximumRange} \r\n"
            )
            sensorDesc.append(
                "   Minimum delay allowed between two events in microsecond or zero if this sensor only returns a value when the data it's measuring changes ${sensor.minDelay} \r\n"
            )
        }
        var drugsField : TextView = findViewById(R.id.drugs_field)
        drugsField.text = sensorDesc

    }*/

    /*private fun getType(type : Int) : String
    {
        return when (type) {
            Sensor.TYPE_ACCELEROMETER -> "TYPE_ACCELEROMETER"
            Sensor.TYPE_GRAVITY -> "TYPE_GRAVITY"
            Sensor.TYPE_GYROSCOPE -> "TYPE_GYROSCOPE"
            Sensor.TYPE_LIGHT -> "TYPE_LIGHT"
            Sensor.TYPE_LINEAR_ACCELERATION -> "TYPE_LINEAR_ACCELERATION"
            Sensor.TYPE_MAGNETIC_FIELD -> "TYPE_MAGNETIC_FIELD"
            Sensor.TYPE_ORIENTATION -> "TYPE_ORIENTATION"
            Sensor.TYPE_PRESSURE -> "TYPE_PRESSURE"
            Sensor.TYPE_PROXIMITY -> "TYPE_PROXIMITY"
            Sensor.TYPE_ROTATION_VECTOR -> "TYPE_ROTATION_VECTOR"
            Sensor.TYPE_TEMPERATURE -> "TYPE_TEMPERATURE"
            else -> "TYPE_UNKNOW"
        }
    }*/
}

