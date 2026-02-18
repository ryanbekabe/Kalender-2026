package com.hanyajasa.kalender2026

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
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
    private lateinit var monthAdapter: MonthAdapter
    private lateinit var recyclerView: RecyclerView

    // ── Tanggal hari ini ─────────────────────────────────────────
    private var todayYear  = -1
    private var todayMonth = -1   // 0-based
    private var todayDay   = -1

    // ── Tahun yang sedang ditampilkan ────────────────────────────
    // Default: tahun sistem. Jika tidak ada CSV-nya, tetap tampil
    // kalender kosong (tanpa warna libur).
    private var activeYear = -1

    // ── Rentang tahun yang didukung (bisa diperluas) ─────────────
    private val yearMin = 2025
    private val yearMax = 2030

    // ── View references ──────────────────────────────────────────
    private lateinit var tvYear      : TextView
    private lateinit var tvCsvWarning: TextView
    private lateinit var btnPrevYear : ImageButton
    private lateinit var btnNextYear : ImageButton

    // ─────────────────────────────────────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Ambil tanggal hari ini
        val today  = Calendar.getInstance()
        todayYear  = today.get(Calendar.YEAR)
        todayMonth = today.get(Calendar.MONTH)
        todayDay   = today.get(Calendar.DAY_OF_MONTH)

        // Tahun aktif = tahun hari ini, dibatasi rentang yearMin..yearMax
        activeYear = todayYear.coerceIn(yearMin, yearMax)

        setupToolbar()
        setupYearSelector()
        setupRecyclerView()
        loadYearData(activeYear, scrollToCurrentMonth = true)

        UpdateChecker.check(
            context        = this,
            currentVersion = BuildConfig.VERSION_NAME,
            silent         = true
        )
    }

    // ─────────────────────────────────────────────────────────────
    // Toolbar
    // ─────────────────────────────────────────────────────────────
    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        // Judul dikelola dinamis di updateYearSelectorUI()
        supportActionBar?.title = "Kalender Indonesia"
    }

    // ─────────────────────────────────────────────────────────────
    // Year selector bar — tombol ◀ Tahun ▶
    // ─────────────────────────────────────────────────────────────
    private fun setupYearSelector() {
        tvYear       = findViewById(R.id.tv_year)
        tvCsvWarning = findViewById(R.id.tv_csv_warning)
        btnPrevYear  = findViewById(R.id.btn_prev_year)
        btnNextYear  = findViewById(R.id.btn_next_year)

        btnPrevYear.setOnClickListener {
            if (activeYear > yearMin) {
                activeYear--
                loadYearData(activeYear, scrollToCurrentMonth = false)
            }
        }

        btnNextYear.setOnClickListener {
            if (activeYear < yearMax) {
                activeYear++
                loadYearData(activeYear, scrollToCurrentMonth = false)
            }
        }

        updateYearSelectorUI()
    }

    // Perbarui label tahun dan status tombol panah
    private fun updateYearSelectorUI() {
        tvYear.text          = activeYear.toString()
        btnPrevYear.isEnabled = activeYear > yearMin
        btnPrevYear.alpha     = if (activeYear > yearMin) 1f else 0.3f
        btnNextYear.isEnabled = activeYear < yearMax
        btnNextYear.alpha     = if (activeYear < yearMax) 1f else 0.3f
    }

    // ─────────────────────────────────────────────────────────────
    // Load penuh untuk satu tahun:
    //  1. Baca CSV holidays_<year>.csv
    //  2. Rebuild monthsData
    //  3. Refresh adapter
    //  4. Scroll ke bulan ini (jika diminta & tahun = todayYear)
    // ─────────────────────────────────────────────────────────────
    private fun loadYearData(year: Int, scrollToCurrentMonth: Boolean) {
        updateYearSelectorUI()

        val csvFound = loadHolidaysFromCsv(year)

        // Tampilkan warning banner jika CSV tidak ada
        if (!csvFound) {
            tvCsvWarning.visibility = View.VISIBLE
            tvCsvWarning.text =
                "⚠ File holidays_$year.csv tidak ditemukan — " +
                "upload ke folder assets untuk menampilkan hari libur."
        } else {
            tvCsvWarning.visibility = View.GONE
        }

        // Rebuild data bulan
        monthsData.clear()
        setupMonths(year)

        // Refresh adapter dengan data baru
        monthAdapter.replaceData(monthsData)

        // Scroll ke bulan ini hanya jika tahun yang ditampilkan = tahun hari ini
        if (scrollToCurrentMonth && year == todayYear) {
            scrollToMonth(todayMonth)
        } else if (!scrollToCurrentMonth) {
            // Saat ganti tahun manual, selalu scroll ke atas (bulan Januari)
            recyclerView.scrollToPosition(0)
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Baca assets/holidays_<year>.csv
    // Kembalikan true jika file ditemukan, false jika tidak ada
    // ─────────────────────────────────────────────────────────────
    private fun loadHolidaysFromCsv(year: Int): Boolean {
        holidaysMap.clear()
        val fileName = "holidays_$year.csv"
        return try {
            val reader = BufferedReader(
                InputStreamReader(assets.open(fileName), Charsets.UTF_8)
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
            Log.d("CSV", "Berhasil memuat ${holidaysMap.size} hari libur dari $fileName")
            true
        } catch (e: Exception) {
            Log.w("CSV", "File $fileName tidak ditemukan atau gagal dibaca: ${e.message}")
            false
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Scroll RecyclerView ke bulan tertentu (0-based)
    // Menggunakan scrollToPositionWithOffset agar header bulan
    // terlihat di bagian atas layar, bukan hanya di-scroll ke item
    // ─────────────────────────────────────────────────────────────
    private fun scrollToMonth(monthIndex: Int) {
        if (monthIndex < 0 || monthIndex > 11) return
        recyclerView.post {
            val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
            layoutManager?.scrollToPositionWithOffset(monthIndex, 0)
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Bangun data 12 bulan untuk tahun yang diberikan
    // ─────────────────────────────────────────────────────────────
    private fun setupMonths(year: Int) {
        for (monthIndex in 0..11) {
            val daysList = mutableListOf<DayItem>()

            val calendar = Calendar.getInstance()
            calendar.set(year, monthIndex, 1, 0, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val daysInMonth    = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

            // Sel kosong sebelum tanggal 1
            repeat(firstDayOfWeek - 1) {
                daysList.add(DayItem("", false, false, false, false, ""))
            }

            // Isi tanggal
            for (day in 1..daysInMonth) {
                calendar.set(year, monthIndex, day, 0, 0, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val dayOfWeek   = calendar.get(Calendar.DAY_OF_WEEK)
                val isHoliday   = holidaysMap.containsKey(Pair(monthIndex, day))
                val holidayName = holidaysMap[Pair(monthIndex, day)] ?: ""
                val isToday     = (year        == todayYear)
                               && (monthIndex  == todayMonth)
                               && (day         == todayDay)

                daysList.add(
                    DayItem(
                        day         = day.toString(),
                        isHoliday   = isHoliday,
                        isSunday    = dayOfWeek == Calendar.SUNDAY,
                        isSaturday  = dayOfWeek == Calendar.SATURDAY,
                        isToday     = isToday,
                        holidayName = holidayName
                    )
                )
            }

            val holidayEntries = (1..daysInMonth).mapNotNull { day ->
                holidaysMap[Pair(monthIndex, day)]?.let { name ->
                    HolidayEntry(day, name)
                }
            }

            // Nama bulan: "Januari 2026", "Februari 2026", dst
            monthsData.add(
                MonthData(
                    name           = "${monthNames[monthIndex]} $year",
                    adapter        = DayAdapter(daysList),
                    holidayEntries = holidayEntries
                )
            )
        }
    }

    // ─────────────────────────────────────────────────────────────
    // RecyclerView — inisialisasi sekali, data diganti via replaceData
    // ─────────────────────────────────────────────────────────────
    private fun setupRecyclerView() {
        recyclerView  = findViewById(R.id.months_list)
        monthAdapter  = MonthAdapter(monthsData) { _ -> }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter       = monthAdapter
    }

    // ─────────────────────────────────────────────────────────────
    // Menu toolbar
    // ─────────────────────────────────────────────────────────────
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.action_toggle_view -> {
                monthAdapter.toggleExpandAll()
                if (monthAdapter.isExpandAll) {
                    item.title = "Mode Accordion"
                    item.setIcon(android.R.drawable.ic_menu_close_clear_cancel)
                } else {
                    item.title = "Semua Terbuka"
                    item.setIcon(android.R.drawable.ic_menu_sort_by_size)
                }
                true
            }

            R.id.action_goto_today -> {
                // Kembali ke tahun hari ini dan scroll ke bulan ini
                if (activeYear != todayYear) {
                    activeYear = todayYear.coerceIn(yearMin, yearMax)
                    loadYearData(activeYear, scrollToCurrentMonth = true)
                } else {
                    // Sudah di tahun yang benar, cukup scroll
                    scrollToMonth(todayMonth)
                }
                true
            }

            R.id.action_check_update -> {
                UpdateChecker.check(
                    context        = this,
                    currentVersion = BuildConfig.VERSION_NAME,
                    silent         = false
                )
                true
            }

            R.id.action_about -> {
                showAboutDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Tentang Aplikasi")
            .setMessage(
                "Kalender Indonesia\n" +
                "Versi: ${BuildConfig.VERSION_NAME}\n\n" +
                "Aplikasi kalender multi-tahun untuk Indonesia.\n\n" +
                "Data hari libur dimuat dari:\nassets/holidays_<tahun>.csv\n\n" +
                "Contoh: holidays_2026.csv, holidays_2027.csv\n\n" +
                "Format CSV:\ntanggal,keterangan\nYYYY-MM-DD,Nama Hari Libur\n\n" +
                "Dibuat oleh:\n@ryanbekabe\n\n" +
                "GitHub:\nhttps://github.com/ryanbekabe/Kalender-2026"
            )
            .setPositiveButton("OK", null)
            .show()
    }

    // ─────────────────────────────────────────────────────────────
    // Data classes
    // ─────────────────────────────────────────────────────────────

    data class DayItem(
        val day: String,
        val isHoliday: Boolean,
        val isSunday: Boolean,
        val isSaturday: Boolean = false,
        val isToday: Boolean    = false,
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
