package insa.lyon.h4224.ifyoudrive

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.round


class EvaluateRisks : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evaluate_risks)

        val estimationField: TextView = findViewById(R.id.estimation_field)
        val drugsField: TextView = findViewById(R.id.drugs_field)
        val risksAlcoholField: TextView = findViewById(R.id.risks_alcohol)
        val risksDrugsField: TextView = findViewById(R.id.risks_drugs)
        val driveButton: Button = findViewById(R.id.drive_button)
        val infoAlcohol: Button = findViewById(R.id.alcohol_info)
        val infoDrugs: Button = findViewById(R.id.drugs_info)

        val estimation: Double = intent.getDoubleExtra("Estimation", 1.0)
        val drugsEvaluation: Boolean = intent.getBooleanExtra("Drugs", true)
        val threshold: Double = intent.getDoubleExtra("Threshold", 0.0)

        estimationField.text = (round(estimation*100)/100).toString()
        drugsField.text = drugsEvaluation.toString()

        if(estimation <= threshold) {
            risksAlcoholField.text = "Votre alcoolémie est inférieure au seuil autorisée (dans votre cas, ${threshold}."
        }
        else if(estimation > threshold && estimation <= 0.8) {
            risksAlcoholField.text = "Votre alcoolémie est supérieure au seuil autorisé (dans votre cas, ${threshold} g/L dans le sang). Cette contravention fait que vous risquez une amende de 135€ majorable, le retrait de 6 points sur votre permis, et une immobilisation du véhicule."
        }
        else if(estimation > 0.8) {
            risksAlcoholField.text = "Votre alcoolémie est bien supérieur au seuil autorisé. Ce délit fait que vous risquez le retrait de votre permis, une interdiction de conduire pendant 72h, 6 points retirés sur le permis, une amende de 4500€, une peine d'emprisonnement de 2 ans maximum, une suspension voire une interdiction de permis pour une durée de 3 ans maximum.."
        }

        if(drugsEvaluation) {
            risksDrugsField.text = "La conduite sous l'emprise de stupéfiants constitue un délit passible de 4500€ d'amende, d'une peine de 2 ans de prison, ainsi que d'un retrait de 6 points sur le permis de conduire automatique. Il peut aussi entraîner la suspension ou l'annulation du permis de conduire."
        }

        driveButton.isEnabled = (estimation <= threshold) && !drugsEvaluation

        driveButton.setOnClickListener {
            Toast.makeText(this@EvaluateRisks, "YEAH", Toast.LENGTH_LONG).show()
        }

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
    }
}