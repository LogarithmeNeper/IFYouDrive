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
        val reactivityButton: Button = findViewById(R.id.reactivity_button)
        val skipToDrive: Button = findViewById(R.id.skip_drive_button)
        val infoAlcohol: Button = findViewById(R.id.alcohol_info)
        val infoDrugs: Button = findViewById(R.id.drugs_info)

        val estimation: Double = intent.getDoubleExtra("Estimation", 1.0)
        val drugsEvaluation: Boolean = intent.getBooleanExtra("Drugs", true)
        val threshold: Double = intent.getDoubleExtra("Threshold", 0.0)

        estimationField.text = (round(estimation*100)/100).toString()
        drugsField.text = if(drugsEvaluation) "Présence de drogues" else "Absence de drogues"

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

        reactivityButton.isEnabled = (estimation <= threshold) && !drugsEvaluation
        skipToDrive.isEnabled = (estimation <= threshold) && !drugsEvaluation

        reactivityButton.setOnClickListener {
            val intentToReactivity = Intent(this@EvaluateRisks, EvaluateReaction::class.java)
            startActivity(intentToReactivity)
        }

        skipToDrive.setOnClickListener {
            val intentToDrive = Intent(this@EvaluateRisks, Driving::class.java)
            startActivity(intentToDrive)
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