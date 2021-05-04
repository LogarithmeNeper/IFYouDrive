package insa.lyon.h4224.ifyoudrive

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class TestSpeedLimit : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_speed_limit)
        val textField: TextView = findViewById(R.id.textSpeedMax)
        val data : String = """
            <query type="way">
                <around radius="10" lat="45.782472" lon="4.889341" />
                <has-kv k="maxspeed" />
            </query>

            <!-- added by auto repair -->
            <union>
                <item/>
                <recurse type="down"/>
            </union>
            <!-- end of auto repair -->
            <print/></osm-script>'
            """
            var response = ""




        doAsync {
            response = performPostCall("http://overpass-api.de/api/interpreter", data)
        }
        while (response == "")
        {
            Thread.sleep(1)
        }
        textField.text = response

    }

    private fun doAsync(f: () -> Unit) {
        Thread { f() }.start()
    }

    fun performPostCall(
        requestURL: String?,
        data: String?
    ): String {
        val url: URL
        var response: String = ""
        try {
            url = URL(requestURL)
            val conn =
                url.openConnection() as HttpURLConnection
            conn.readTimeout = 15000
            conn.connectTimeout = 15000
            conn.requestMethod = "POST"
            conn.doInput = true
            conn.doOutput = true
            val os = conn.outputStream
            val writer = BufferedWriter(
                OutputStreamWriter(os, "UTF-8")
            )
            writer.write(data)
            writer.flush()
            writer.close()
            os.close()
            val responseCode = conn.responseCode
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                var line: String?
                val br = BufferedReader(InputStreamReader(conn.inputStream))
                while (br.readLine().also { line = it } != null) {
                    response += line
                }
            } else {
                response = ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return response
    }
}