package com.prathamesh.womensafetyapp

import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.database.*

class CrimeStatsActivity : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference
    private val crimeData = mutableMapOf<String, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crime_stats)

        val autoCompleteArea = findViewById<AutoCompleteTextView>(R.id.autoCompleteArea)
        val resultText = findViewById<TextView>(R.id.txtCrimeResult)
        val pieChart = findViewById<PieChart>(R.id.pieChart)

        dbRef = FirebaseDatabase.getInstance().reference.child("crimeData")


        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    resultText.text = "⚠️ No data found in Firebase!"
                    return
                }

                crimeData.clear()
                val stateList = mutableListOf<String>()

                for (child in snapshot.children) {
                    val name = child.key?.trim()?.lowercase()
                    val percentage = child.getValue(Long::class.java)?.toInt()

                    if (name != null && percentage != null) {
                        crimeData[name] = percentage
                        // Show nicely formatted name in dropdown
                        stateList.add(name.replaceFirstChar { it.uppercase() })
                    }
                }


                val adapter = ArrayAdapter(
                    this@CrimeStatsActivity,
                    android.R.layout.simple_dropdown_item_1line,
                    stateList
                )
                autoCompleteArea.setAdapter(adapter)


                autoCompleteArea.setOnItemClickListener { _, _, position, _ ->
                    val selectedState = adapter.getItem(position)?.lowercase()?.trim()
                    val percentage = crimeData[selectedState]

                    if (percentage != null) {
                        resultText.text =
                            "📍 ${selectedState?.replaceFirstChar { it.uppercase() }} → $percentage% crime rate"
                        showPieChart(pieChart, selectedState!!, percentage)
                    } else {
                        resultText.text = "⚠️ Data not found for $selectedState"
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                resultText.text = "❌ Failed to load data: ${error.message}"
            }
        })
    }

    private fun showPieChart(pieChart: PieChart, state: String, percentage: Int) {
        val entries = listOf(
            PieEntry(percentage.toFloat(), "Crime"),
            PieEntry((100 - percentage).toFloat(), "Safe")
        )

        val dataSet = PieDataSet(entries, "Crime Stats").apply {
            colors = listOf(Color.RED, Color.GREEN)
            valueTextSize = 14f
        }

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.centerText = state.uppercase()
        pieChart.setCenterTextSize(16f)
        pieChart.animateY(1000)
        pieChart.invalidate()
    }
}
