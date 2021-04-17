package insa.lyon.h4224.ifyoudrive

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.View
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
        val mSubmitButton: Button = findViewById(R.id.submit_button)

        mSubmitButton.setOnClickListener {
            try {
                val sexId: Int = mSexGroup.checkedRadioButtonId
                var coefDiffusion: Double = 0.0

                val checkedButton: RadioButton = findViewById(sexId)
                if ("${checkedButton.text}" == "Un homme") coefDiffusion = 0.7 else coefDiffusion =
                    0.6

                val weight: Int = mWeight.text.toString().toInt()
                val nbBeers: Int = mBeers.text.toString().toInt()
                val nbWine: Int = mWine.text.toString().toInt()
                val nbSpirits: Int = mSpirits.text.toString().toInt()

                val totalVolumeAlcool =
                    nbBeers * 250 * 0.07 + nbWine * 100 * 0.12 + nbSpirits * 30 * 0.4
                val estimation = (totalVolumeAlcool * 0.8) / (coefDiffusion * weight)

                Toast.makeText(this@MainActivity, "Estimation : $estimation",
                    Toast.LENGTH_SHORT).show()
            }
            catch(e: Exception) {
                Toast.makeText(this@MainActivity, "Merci de remplir tous les champs !",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
}