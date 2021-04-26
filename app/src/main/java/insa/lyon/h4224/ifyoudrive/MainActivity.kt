package insa.lyon.h4224.ifyoudrive

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mSexGroup: RadioGroup = findViewById(R.id.sex_group)
        val mWeight: EditText = findViewById(R.id.weight)
        val mBeers: EditText = findViewById(R.id.beers)
        val mWine: EditText = findViewById(R.id.wine)
        val mSpirits: EditText = findViewById(R.id.spirits)
        val mDrugsGroup: RadioGroup = findViewById(R.id.drugs_group)
        val mThresholdGroup: RadioGroup = findViewById(R.id.threshold_group)
        val mEvaluateButton: Button = findViewById(R.id.evaluate_button)
        
        mEvaluateButton.setOnClickListener {
            try {
                val sexId: Int = mSexGroup.checkedRadioButtonId
                var coefDiffusion: Double = 0.0

                val checkedSexButton: RadioButton = findViewById(sexId)
                if ("${checkedSexButton.text}" == "Un homme") coefDiffusion = 0.7 else coefDiffusion =
                    0.6

                val weight: Int = mWeight.text.toString().toInt()
                val nbBeers: Int = mBeers.text.toString().toInt()
                val nbWine: Int = mWine.text.toString().toInt()
                val nbSpirits: Int = mSpirits.text.toString().toInt()

                val totalVolumeAlcool =
                    nbBeers * 250 * 0.07 + nbWine * 100 * 0.12 + nbSpirits * 30 * 0.4

                val drugsId: Int = mDrugsGroup.checkedRadioButtonId
                val checkedDrugsButton: RadioButton = findViewById(drugsId)

                val thresholdId: Int = mThresholdGroup.checkedRadioButtonId
                val checkedThresholdButton: RadioButton = findViewById(thresholdId)
                var threshold: Double = 0.0
                if ("${checkedThresholdButton.text}" == "Oui") threshold = 0.2 else threshold =
                    0.5

                if(weight != 0) {
                    val estimation = (totalVolumeAlcool * 0.8) / (coefDiffusion * weight)

                    val intentToEvaluate = Intent(this@MainActivity, EvaluateRisks::class.java)
                    intentToEvaluate.putExtra("Estimation", estimation)
                    intentToEvaluate.putExtra("Drugs", checkedDrugsButton.text=="Oui")
                    intentToEvaluate.putExtra("Threshold", threshold)
                    startActivity(intentToEvaluate)
                }
                else {
                    Toast.makeText(this@MainActivity, "La masse ne peut pas être nulle !",
                        Toast.LENGTH_SHORT).show()
                }
            }
            catch(e: Exception) {
                Toast.makeText(this@MainActivity, "Merci de remplir tous les champs !",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
}