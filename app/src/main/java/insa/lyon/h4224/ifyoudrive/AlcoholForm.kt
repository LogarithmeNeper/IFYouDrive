package insa.lyon.h4224.ifyoudrive

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.*

/**
 * Class for the form checking alcohol and drugs.
 * For now, it is the entry point of the application, and it is necessary to further proceed.
 */
class AlcoholForm : AppCompatActivity() {
    /**
     * Function used when creating the window at the beginning.
     * Uses the template of the activity as it is defined in ~/res/layout/activity_alcohol_form
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use of template.
        setContentView(R.layout.activity_alcohol_form)

        // Obtain all the necessary elements in the layout.
        val mSexGroup: RadioGroup = findViewById(R.id.sex_group)
        val mWeight: EditText = findViewById(R.id.weight)
        val mBeers: EditText = findViewById(R.id.beers)
        val mWine: EditText = findViewById(R.id.wine)
        val mSpirits: EditText = findViewById(R.id.spirits)
        val mDrugsGroup: RadioGroup = findViewById(R.id.drugs_group)
        val mThresholdGroup: RadioGroup = findViewById(R.id.threshold_group)
        val mEvaluateButton: Button = findViewById(R.id.evaluate_button)

        // Evaluation when pressing the Evaluation Button.
        mEvaluateButton.setOnClickListener {
            // Wrapped in a try/catch in order to check if all fields are set.
            try {
                val sexId: Int = mSexGroup.checkedRadioButtonId
                var coefDiffusion: Double

                val checkedSexButton: RadioButton = findViewById(sexId)
                if ("${checkedSexButton.text}" == "Un homme") coefDiffusion = 0.7 else coefDiffusion =
                    0.6

                val weight: Int = mWeight.text.toString().toInt()

                // User-friendly null check (not set --> 0)
                if (TextUtils.isEmpty(mBeers.text)) {mBeers.setText("0")}
                if (TextUtils.isEmpty(mWine.text)) {mWine.setText("0")}
                if (TextUtils.isEmpty(mSpirits.text)) {mSpirits.setText("0")}
                val nbBeers: Int = mBeers.text.toString().toInt()
                val nbWine: Int = mWine.text.toString().toInt()
                val nbSpirits: Int = mSpirits.text.toString().toInt()

                val totalVolumeAlcool =
                    nbBeers * 250 * 0.07 + nbWine * 100 * 0.12 + nbSpirits * 30 * 0.4

                val drugsId: Int = mDrugsGroup.checkedRadioButtonId
                val checkedDrugsButton: RadioButton = findViewById(drugsId)

                val thresholdId: Int = mThresholdGroup.checkedRadioButtonId
                val checkedThresholdButton: RadioButton = findViewById(thresholdId)
                var threshold: Double
                // Relative to French law
                if ("${checkedThresholdButton.text}" == "Oui") threshold = 0.2 else threshold =
                    0.5

                if(weight != 0) {
                    // Estimating the alcohol %
                    val estimation = (totalVolumeAlcool * 0.8) / (coefDiffusion * weight)

                    // Starting the intent...
                    val intentToEvaluate = Intent(this@AlcoholForm, EvaluateRisks::class.java)
                    // ... giving the estimation, if presence of drugs, and the corresponding threshold
                    intentToEvaluate.putExtra("Estimation", estimation)
                    intentToEvaluate.putExtra("Drugs", checkedDrugsButton.text=="Oui")
                    intentToEvaluate.putExtra("Threshold", threshold)
                    startActivity(intentToEvaluate)
                }
                else {
                    // Notification if we divide by 0
                    Toast.makeText(this@AlcoholForm, "La masse ne peut pas être nulle !",
                        Toast.LENGTH_SHORT).show()
                }
            }
            catch(e: Exception) {
                // Notification if we catch an nullptr exception
                Toast.makeText(this@AlcoholForm, "Merci de remplir tous les champs !",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
}