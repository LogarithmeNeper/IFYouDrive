package insa.lyon.h4224.ifyoudrive

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import org.w3c.dom.Text
import kotlin.random.Random

/**
 * Class to evaluate the user reaction time.
 */
class EvaluateReaction : AppCompatActivity() {
    /**
     * Function used when creating the window at the beginning.
     * Uses the template of the activity as it is defined in ~/res/layout/activity_evaluate_reaction
     */

    var startingTime : Long = System.currentTimeMillis()
    var testStarted : Boolean = false
    var inWaitingTime : Boolean = false
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Using the template.
        setContentView(R.layout.activity_evaluate_reaction)

        // Getting the necessary information.
        val mStartButton: Button = findViewById(R.id.start_button)
        val textEvaluate: TextView = findViewById(R.id.text_test_reactivity)
        val layoutReaction : RelativeLayout = findViewById(R.id.layout_reaction)
        val driveButton : Button = findViewById(R.id.drive_button)
        val indicButton : Button = findViewById(R.id.indication_button)
        val title: TextView = findViewById(R.id.title)
        val line : View = findViewById(R.id.reac_line)
        val indicTest : TextView = findViewById(R.id.indic_text)

        indicTest.text = "Pour obtenir des informations sur le fonctionnement de la navigation, appuyez sur Indications. Pour lancer la navigation, appuyez sur Drive Me."

        // Listener for starting the activity
        mStartButton.setOnClickListener {
            if(!testStarted && !inWaitingTime)
            {
                GlobalScope.launch(Dispatchers.Main) { makeReactivityTest() }

            }

        }
        layoutReaction.setOnTouchListener { v: View, m: MotionEvent ->
            if (testStarted && !inWaitingTime) {
                var endingTime: Long = System.currentTimeMillis()
                var delta = endingTime-startingTime
                // Response according to the delta
                if(delta <= 500)
                {
                    textEvaluate.text = "Votre temps de r??activit?? est de ${delta} ms. C'est un bon temps de r??ponse. \n" +
                            "Si vous le souhaitez, vous pouvez relancer un test en appuyant sur le bouton Nouveau Test."
                    driveButton.setBackgroundColor(resources.getColor(R.color.design_default_color_primary))
                }
                else if(delta in 501..800)
                {
                    textEvaluate.text = "Votre temps de r??activit?? est de ${delta} ms. C'est un temps de r??ponse un peu lent, il pourrait ??tre plus judicieux " +
                            "de ne pas prendre le volant. \nSi vous le souhaitez, vous pouvez relancer un test en appuyant sur le bouton Nouveau Test."
                    driveButton.setBackgroundColor(Color.rgb(0xFF, 0x66, 0x00))
                }
                else
                {
                    textEvaluate.text = "Votre temps de r??ponse est de ${delta} ms. C'est un temps de r??ponse lent, il vous est fortement d??conseill?? de prendre le volant." +
                            "\nSi vous le souhaitez, vous pouvez relancer un test en appuyant sur le bouton Nouveau Test."
                    driveButton.setBackgroundColor(Color.RED)
                }

                // Setting everything visible
                testStarted = false
                textEvaluate.visibility = View.VISIBLE
                title.visibility = View.VISIBLE
                layoutReaction.setBackgroundColor(Color.WHITE)
                driveButton.visibility = View.VISIBLE
                mStartButton.text = "Nouveau test"
                mStartButton.visibility = View.VISIBLE
                indicButton.visibility = View.VISIBLE
                indicTest.visibility = View.VISIBLE
                line.visibility = View.VISIBLE
            }
            true
        }
        driveButton.setOnClickListener {
            if(!testStarted && !inWaitingTime) {
                // Intent in order to go to the driving activity
                val intentToDrive = Intent(this@EvaluateReaction, Driving::class.java)
                startActivity(intentToDrive)
            }
        }

        indicButton.setOnClickListener {
            if(!testStarted && !inWaitingTime) {
                // Intent in order to go the the indications activity
                val intentToIndications = Intent(this@EvaluateReaction, Indications::class.java)
                startActivity(intentToIndications)
            }
        }
    }


    suspend fun makeReactivityTest()
    {
        val mStartButton: Button = findViewById(R.id.start_button)
        val textEvaluate: TextView = findViewById(R.id.text_test_reactivity)
        val layoutReaction : RelativeLayout = findViewById(R.id.layout_reaction)
        val driveButton : Button = findViewById(R.id.drive_button)
        val indicButton : Button = findViewById(R.id.indication_button)
        val title: TextView = findViewById(R.id.title)
        val line : View = findViewById(R.id.reac_line)
        val indicText : TextView = findViewById(R.id.indic_text)
        testStarted = true
        inWaitingTime = true
        // Everything invisible
        driveButton.visibility = View.INVISIBLE
        mStartButton.visibility = View.INVISIBLE
        indicButton.visibility = View.INVISIBLE
        line.visibility = View.INVISIBLE
        indicText.visibility = View.INVISIBLE
        var randomTime = Random.nextInt(2000, 8000)
        val value = GlobalScope.async {
            delay(randomTime.toLong())
        }
        value.await()
        inWaitingTime = false
        // Setting the background color to red.
        layoutReaction.setBackgroundColor(Color.RED)
        startingTime = System.currentTimeMillis()
        textEvaluate.visibility = View.INVISIBLE
        title.visibility = View.INVISIBLE
    }
}