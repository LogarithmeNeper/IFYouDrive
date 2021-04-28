package insa.lyon.h4224.ifyoudrive

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class EvaluateReaction : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evaluate_reaction)

        val mStartButton: Button = findViewById(R.id.start_button)
        val textEvaluate: TextView = findViewById(R.id.text_test_reactivity)
        val layoutReaction : LinearLayout = findViewById(R.id.layout_reaction)
        val driveButton : Button = findViewById(R.id.drive_button)
        val title: TextView = findViewById(R.id.title)

        var startingTime : Long = System.currentTimeMillis()
        var testStarted : Boolean = false

        mStartButton.setOnClickListener {
            driveButton.visibility = View.INVISIBLE
            mStartButton.visibility = View.INVISIBLE
            var randomTime = Random.nextInt(2000,8000)
            Thread.sleep(randomTime.toLong());
            layoutReaction.setBackgroundColor(Color.RED);
            textEvaluate.visibility = View.INVISIBLE
            title.visibility = View.INVISIBLE
            testStarted = true
            startingTime = System.currentTimeMillis()

        }
        layoutReaction.setOnTouchListener { v: View, m: MotionEvent ->
            if (testStarted) {
                var endingTime: Long = System.currentTimeMillis()
                var delta = endingTime-startingTime
                if(delta < 500)
                {
                    layoutReaction.setBackgroundColor(Color.GREEN)
                    textEvaluate.text = "Votre temps de réactivité est de ${delta} ms ! C'est un bon temps de réponse. \n" +
                            "Vous pouvez relancer un test en appuyant sur le bouton Nouveau Test ou lancer la navigation en appuyant sur le bouton Drive Me."
                }
                else
                {
                    layoutReaction.setBackgroundColor(Color.YELLOW)
                    textEvaluate.text = "Votre temps de réactivité est de ${delta} ms ! C'est un temps de réponse un peu long, il ne vous est pas conseillé de conduire \n" +
                            "Vous pouvez relancer un test en appuyant sur le bouton Nouveau Test ou lancer la navigation en appuyant sur le bouton Drive Me."
                }

                //textEvaluate.text = "starting time : ${startingTime}, in ms : ${startingTimeInMs}, ending time : ${endingTime}, in ms : ${endingTimeInMs}"
                testStarted = false
                textEvaluate.visibility = View.VISIBLE
                title.visibility = View.VISIBLE
                layoutReaction.setBackgroundColor(Color.WHITE)
                driveButton.visibility = View.VISIBLE
                mStartButton.text = "Nouveau test"
                mStartButton.visibility = View.VISIBLE
            }
            true
        }
        driveButton.setOnClickListener {
            val intentToDrive = Intent(this@EvaluateReaction, Driving::class.java)
            startActivity(intentToDrive)
        }
    }
}