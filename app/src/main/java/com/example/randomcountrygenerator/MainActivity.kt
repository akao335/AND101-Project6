package com.example.randomcountrygenerator

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.RequestParams
import com.codepath.asynchttpclient.callback.TextHttpResponseHandler
import okhttp3.Headers
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var txtOutput: TextView
    private lateinit var btnGenerate: Button
    private val client = AsyncHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtOutput = findViewById(R.id.txtOutput)
        btnGenerate = findViewById(R.id.btnGenerate)

        btnGenerate.setOnClickListener {
            fetchRandomCountry()
        }
    }

    private fun fetchRandomCountry() {
        val url = "https://restcountries.com/v3.1/all?fields=name,capital,population,languages"

        client.get(url, object : TextHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, responseBody: String) {
                if (responseBody == null) {
                    txtOutput.text = "Empty response from server"
                    return
                }
                try {
                    val countries = JSONArray(responseBody)
                    val index = Random.nextInt(countries.length())
                    val country = countries.getJSONObject(index)

                    val name = country.getJSONObject("name").getString("common")
                    val capital = country.optJSONArray("capital")?.optString(0) ?: "N/A"
                    val population = country.optLong("population", 0)

                    val languagesJson = country.optJSONObject("languages")
                    val languages = if (languagesJson != null) {
                        val keys = languagesJson.keys()
                        val list = mutableListOf<String>()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            list.add(languagesJson.getString(key))
                        }
                        list.joinToString(", ")
                    } else {
                        "N/A"
                    }

                    val output = """
                        Name: $name
                        Capital: $capital
                        Population: $population
                        Languages: $languages
                    """.trimIndent()

                    txtOutput.text = output

                } catch (e: Exception) {
                    txtOutput.text = "Error parsing data"
                    e.printStackTrace()
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                errorResponse: String?,
                throwable: Throwable?
            ) {
                txtOutput.text = "Failed to fetch data. Check your connection."
                Log.e("HTTP_ERROR", "Status: $statusCode")
                Log.e("HTTP_ERROR", "Error body: $errorResponse")
                throwable?.printStackTrace()
            }
        })
    }
}
