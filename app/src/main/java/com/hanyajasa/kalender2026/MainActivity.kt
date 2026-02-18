package com.hanyajasa.kalender2026

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

class MainActivity : AppCompatActivity() {

    private val monthNames = arrayOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )

    private val holidaysMap = mutableMapOf<Pair<Int, Int>, String>()
    private val monthsData  = mutableListOf<MonthData>()

    // PERBAIKAN WARNING #3: hapus menuItemToggle yang tidak pernah dipakai
    private lateinit var monthAdapter: MonthAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()
        loadHolidaysFromCsv()
        setupMonths()
        setupRecyclerView()
    }

    // ---------------------------------------------------------------
    // Baca file assets/holidays.csv
    // Format: tanggal,keterangan  (YYYY-MM-DD)
    // Baris pertama (header) dilewati otomatis
    // ---------------------------------------------------------------
    private fun loadHolidaysFromCsv() {
        holidaysMap.clear()
        try {
            val reader = BufferedReader(
                InputStreamReader(assets.open("holidays.csv"), Charsets.UTF_8)
            )
            var isFirstLine = true
            reader.forEachLine { line ->
                if (isFirstLine) { isFirstLine = false; return@forEachLine }
                val trimmed = line.trim()
                if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEachLine

                val parts = trimmed.split(",", limit = 2)
                if (parts.size < 2) return@forEachLine

                val dateStr    = parts[0].trim()
                val keterangan = parts[1].trim()
                val dateParts  = dateStr.split("-")
                if (dateParts.size != 3) return@forEachLine

                val month = dateParts[1].toIntOrNull() ?: return@forEachLine
                val day   = dateParts[2].toIntOrNull() ?: return@forEachLine
                holidaysMap[Pair(month - 1, day)] = keterangan
            }
            reader.close()
            Log.d("CSV", "Berhasil memuat ${holidaysMap.size} hari libur dari holidays.csv")
        } catch (e: Exception) {
            Log.e("CSV", "Gagal membaca holidays.csv: ${e.message}")
        }
    }

    // ---------------------------------------------------------------
    // Menu toolbar
    // ---------------------------------------------------------------
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.action_toggle_view -> {
                monthAdapter.toggleExpandAll()
                // Update label & ikon tombol sesuai state terkini
                if (monthAdapter.isExpandAll) {
                    item.title = "Mode Accordion"
                    item.setIcon(android.R.drawable.ic_menu_close_clear_cancel)
                } else {
                    item.title = "Semua Terbuka"
                    item.setIcon(android.R.drawable.ic_menu_sort_by_size)
                }
                true
            }

            R.id.action_about -> {
                showAboutDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Tentang Aplikasi")
            .setMessage(
                "Kalender 2026\n\n" +
                "Aplikasi kalender tahun 2026 untuk Indonesia.\n\n" +
                "Data hari libur dimuat dari:\nassets/holidays.csv\n\n" +
                "Format CSV:\ntanggal,keterangan\nYYYY-MM-DD,Nama Hari Libur\n\n" +
                "Dibuat oleh:\n@ryanbekabe\n\n" +
                "GitHub:\nhttps://github.com/ryanbekabe/Kalender-2026"
            )
            .setPositiveButton("OK", null)
            .show()
    }

    // ---------------------------------------------------------------
    // Bangun data setiap bulan
    // ---------------------------------------------------------------
    private fun setupMonths() {
        val currentYear = 2026

        for (monthIndex in 0..11) {
            val daysList = mutableListOf<DayItem>()

            val calendar = Calendar.getInstance()
            calendar.set(currentYear, monthIndex, 1, 0, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val daysInMonth    = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

            // Sel kosong sebelum tanggal 1
            repeat(firstDayOfWeek - 1) {
                daysList.add(DayItem("", false, false, false, ""))
            }

            // Isi tanggal
            for (day in 1..daysInMonth) {
                calendar.set(currentYear, monthIndex, day, 0, 0, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val dayOfWeek   = calendar.get(Calendar.DAY_OF_WEEK)
                val isHoliday   = holidaysMap.containsKey(Pair(monthIndex, day))
                val holidayName = holidaysMap[Pair(monthIndex, day)] ?: ""
                daysList.add(
                    DayItem(
                        day         = day.toString(),
                        isHoliday   = isHoliday,
                        isSunday    = dayOfWeek == Calendar.SUNDAY,
                        isSaturday  = dayOfWeek == Calendar.SATURDAY,
                        holidayName = holidayName
                    )
                )
            }

            val holidayEntries = (1..daysInMonth).mapNotNull { day ->
                holidaysMap[Pair(monthIndex, day)]?.let { name ->
                    HolidayEntry(day, name)
                }
            }

            monthsData.add(
                MonthData(
                    name           = monthNames[monthIndex],
                    adapter        = DayAdapter(daysList),
                    holidayEntries = holidayEntries
                )
            )
        }
    }

    private fun setupRecyclerView() {
        monthAdapter = MonthAdapter(monthsData) { _ -> }
        val recyclerView = findViewById<RecyclerView>(R.id.months_list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter       = monthAdapter
    }

    // ---------------------------------------------------------------
    // Data classes
    // ---------------------------------------------------------------

    data class DayItem(
        val day: String,
        val isHoliday: Boolean,
        val isSunday: Boolean,
        val isSaturday: Boolean = false,
        val holidayName: String = ""
    )

    data class HolidayEntry(
        val day: Int,
        val name: String
    )

    data class MonthData(
        val name: String,
        val adapter: DayAdapter,
        val holidayEntries: List<HolidayEntry> = emptyList()
    )
}
