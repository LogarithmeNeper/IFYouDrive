package insa.lyon.h4224.ifyoudrive

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import kotlin.random.Random
import kotlin.random.Random.*

class EvaluateReaction : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evaluate_reaction)

        val mStartButton: Button = findViewById(R.id.start_button)
        val textEvaluate: TextView = findViewById(R.id.text_test_reactivity)
        val layout_reaction : LinearLayout = findViewById(R.id.layout_reaction)

        mStartButton.setOnClickListener {
            var randomTime = Random.nextInt(2000,10000)
            Thread.sleep(randomTime.toLong());
            layout_reaction.setBackgroundColor(Color.RED);
            textEvaluate.text= "test aaa bbbb ${randomTime}"
        }
    }
}