package insa.lyon.h4224.ifyoudrive

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Indications : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use of template.
        setContentView(R.layout.activity_indications)

        val indicText : TextView = findViewById(R.id.indication_text)
        val driveButton : Button = findViewById(R.id.indic_to_drive_button)

        indicText.text = "IFYouDrive vous permet de circuler en toute sécurité. Durant la navigation, notre application :\n\n" +
                "\t•\tVous donne des indications vocales pour vous diriger tout au long de votre itinéraire\n" +
                "\t•\tÉmet un bip sonore quand vous dépassez la vitesse autorisée\n" +
                "\t•\tVous indique vocalement quand vous entrez ou sortez d'une zone accidentogène\n\n" +
                "Pour poursuivre vers la navigation, appuyez sur le bouton Drive Me."

        driveButton.setOnClickListener {
            val intentToDrive = Intent(this@Indications, Driving::class.java)
            startActivity(intentToDrive)
        }
    }
}