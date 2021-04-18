package insa.lyon.h4224.ifyoudrive

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import org.w3c.dom.Text
import kotlin.math.round

class EvaluateRisks : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evaluate_risks)

        val estimationField: TextView = findViewById(R.id.estimation_field)
        val drugsField: TextView = findViewById(R.id.drugs_field)
        val risksField: TextView = findViewById(R.id.risks)
        val driveButton: Button = findViewById(R.id.drive_button)

        val estimation: Double = intent.getDoubleExtra("Estimation", 1.0)
        val drugsEvaluation: Boolean = intent.getBooleanExtra("Drugs", true)
        val threshold: Double = intent.getDoubleExtra("Threshold", 0.0)

        estimationField.text = (round(estimation*100)/100).toString()
        drugsField.text = drugsEvaluation.toString()

        if(estimation > threshold && estimation <= 0.8) {
            risksField.text = "Votre alcoolémie est supérieure au seuil autorisé (dans votre cas, ${threshold} g/L dans le sang). Cette contravention fait que vous risquez une amende de 135€ majorable, le retrait de 6 points sur votre permis, et une immobilisation du véhicule."
        }
        if(estimation > 0.8) {
            risksField.text = "Votre alcoolémie est bien supérieur au seuil autorisé. Ce délit fait que vous risquez le retrait de votre permis, une interdiction de conduire pendant 72h, 6 points retirés sur le permis, une amende de 4500€, une peine d'emprisonnement de 2 ans maximum, une suspension voire une interdiction de permis pour une durée de 3 ans maximum.."
        }
        if(drugsEvaluation) {
            risksField.text = "La conduite sous l'emprise de stupéfiants constitue un délit passible de 4500€ d'amende, d'une peine de 2 ans de prison, ainsi que d'un retrait de 6 points sur le permis de conduire automatique. Il peut aussi entraîner la suspension ou l'annulation du permis de conduire."
        }

        driveButton.isEnabled = (estimation <= threshold) && !drugsEvaluation
        // TODO : Ajouter un bouton "+ d'infos" dans une optique de réduction des risques
        // TODO : Faire en sorte de concaténer les deux strings plutôt que de remplacer

        driveButton.setOnClickListener {
            Toast.makeText(this@EvaluateRisks, "YEAH", Toast.LENGTH_LONG).show()
        }
    }
}