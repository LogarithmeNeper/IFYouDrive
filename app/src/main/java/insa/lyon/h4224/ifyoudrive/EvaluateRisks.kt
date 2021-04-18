package insa.lyon.h4224.ifyoudrive

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import org.w3c.dom.Text

class EvaluateRisks : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evaluate_risks)

        val estimationField: TextView = findViewById(R.id.estimation_field)
        val drugsField: TextView = findViewById(R.id.drugs_field)

        estimationField.text = intent.getStringExtra("Estimation")
        drugsField.text = intent.getStringExtra("Drugs")
    }
}