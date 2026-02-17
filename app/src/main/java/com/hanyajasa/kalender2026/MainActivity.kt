package com.hanyajasa.kalender2026

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class MainActivity : AppCompatActivity() {

    private val monthNames = arrayOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )

    private val holidays2026 = mapOf(
        Pair(0, 1) to "Tahun Baru",
        Pair(1, 17) to "Tahun Baru Imlek",
        Pair(2, 28) to "Nyepi",
        Pair(3, 3) to "Isra Mi'raj",
        Pair(4, 1) to "Hari Buruh",
        Pair(5, 1) to "Hari Lahir Pancasila",
        Pair(5, 26) to "Waisak",
        Pair(5, 27) to "Cuti Bersama",
        Pair(6, 17) to "Idul Fitri",
        Pair(6, 18) to "Idul Fitri",
        Pair(6, 19) to "Idul Fitri",
        Pair(6, 20) to "Cuti Bersama",
        Pair(6, 21) to "Cuti Bersama",
        Pair(7, 17) to "Hari Kemerdekaan RI",
        Pair(8, 24) to "Idul Adha",
        Pair(9, 20) to "Maulid Nabi",
        Pair(11, 25) to "Hari Natal",
        Pair(11, 26) to "Cuti Bersama"
    )

    private val monthsData = mutableListOf<MonthData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupMonths()
        setupRecyclerView()
    }

    private fun setupMonths() {
        val calendar = Calendar.getInstance()
        val currentYear = 2026

        for (monthIndex in 0..11) {
            calendar.set(currentYear, monthIndex, 1)
            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

            val daysList = mutableListOf<DayItem>()

            for (i in 1 until firstDayOfWeek) {
                daysList.add(DayItem("", false, false))
            }

            for (day in 1..daysInMonth) {
                val isHoliday = holidays2026.containsKey(Pair(monthIndex, day))
                val isSunday = (firstDayOfWeek - 1 + day - 1) % 7 == 0
                daysList.add(DayItem(day.toString(), isHoliday, isSunday))
            }

            val adapter = DayAdapter(daysList)
            monthsData.add(MonthData(monthNames[monthIndex], adapter))
        }
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.months_list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = MonthAdapter(monthsData) { position ->
            // Optional: scroll to expanded item
        }
    }

    data class DayItem(
        val day: String,
        val isHoliday: Boolean,
        val isSunday: Boolean
    )

    data class MonthData(
        val name: String,
        val adapter: DayAdapter
    )
}