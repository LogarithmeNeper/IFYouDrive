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
class TitlePage : AppCompatActivity() {
    /**
     * Function used when creating the window at the beginning.
     * Uses the template of the activity as it is defined in ~/res/layout/activity_alcohol_form
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use of template.
        setContentView(R.layout.activity_title_page)

        //Thread.sleep(2000)
        val mTitleButton: Button = findViewById(R.id.title_button)

        mTitleButton.setOnClickListener {
            // Intent in order to go to the driving activity
            val intentToTest = Intent(this@TitlePage, AlcoholForm::class.java)
            startActivity(intentToTest)
        }
    }
}