package insa.lyon.h4224.ifyoudrive

import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import java.io.FileReader

class testCSV : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_c_s_v)
        val textCSV: TextView = findViewById(R.id.textCSV)

        var text : String = ""
        val csvReader = CSVReaderBuilder(FileReader("app/src/main/assets/clusterized_accidents_2017_2018_2019_lyon.csv"))
            .withCSVParser(CSVParserBuilder().withSeparator(';').build())
            .build()

// Maybe do something with the header if there is one
        /*val header = csvReader.readNext()

// Read the rest
        var line: Array<String>? = csvReader.readNext()
        while (line != null) {
            // Do something with the data
            text += line[0]

            line = csvReader.readNext()
        }*/
        textCSV.text = text
    }
}