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
import java.io.StringReader
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
    private var activeYear = -1

    // ── Rentang tahun yang didukung ──────────────────────────────
    private val yearMin = 2025
    private val yearMax = 2030

    // ── View references ──────────────────────────────────────────
    private lateinit var tvYear       : TextView
    private lateinit var tvCsvWarning : TextView
    private lateinit var btnPrevYear  : ImageButton
    private lateinit var btnNextYear  : ImageButton

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

        val today  = Calendar.getInstance()
        todayYear  = today.get(Calendar.YEAR)
        todayMonth = today.get(Calendar.MONTH)
        todayDay   = today.get(Calendar.DAY_OF_MONTH)

        activeYear = todayYear.coerceIn(yearMin, yearMax)

        setupToolbar()
        setupYearSelector()
        setupRecyclerView()

        // Tampilkan kalender dari data lokal (assets/cache) dulu,
        // lalu unduh update di background secara diam-diam
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
        supportActionBar?.title = "Kalender Nusantara"
    }

    // ─────────────────────────────────────────────────────────────
    // Year selector bar ◀ Tahun ▶
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

    private fun updateYearSelectorUI() {
        tvYear.text           = activeYear.toString()
        btnPrevYear.isEnabled = activeYear > yearMin
        btnPrevYear.alpha     = if (activeYear > yearMin) 1f else 0.3f
        btnNextYear.isEnabled = activeYear < yearMax
        btnNextYear.alpha     = if (activeYear < yearMax) 1f else 0.3f
    }

    // ─────────────────────────────────────────────────────────────
    // Load penuh untuk satu tahun — inti dari semua perubahan data
    //
    // Alur:
    //  1. Baca CSV dari cache/assets via HolidayUpdater.readCsv()
    //  2. Rebuild 12 bulan & refresh adapter
    //  3. Tampilkan banner status sumber data
    //  4. Jalankan unduhan CSV terbaru di background (silent)
    //     → Jika ada CSV baru: rebuild ulang kalender otomatis
    // ─────────────────────────────────────────────────────────────
    private fun loadYearData(year: Int, scrollToCurrentMonth: Boolean) {
        updateYearSelectorUI()

        // ── Langkah 1 & 2: Baca data lokal & tampilkan kalender ──
        val csvContent = HolidayUpdater.readCsv(this, year)
        if (csvContent != null) {
            parseCsvContent(csvContent)
        } else {
            holidaysMap.clear()
        }

        monthsData.clear()
        setupMonths(year)
        monthAdapter.replaceData(monthsData)

        // ── Langkah 3: Tampilkan banner status sumber data ────────
        updateDataSourceBanner(year)

        // ── Langkah 4: Scroll posisi ──────────────────────────────
        if (scrollToCurrentMonth && year == todayYear) {
            scrollToMonth(todayMonth)
        } else if (!scrollToCurrentMonth) {
            recyclerView.scrollToPosition(0)
        }

        // ── Langkah 5: Unduh CSV terbaru di background ────────────
        // Callback onRefresh hanya dipanggil jika ada file baru yang
        // berhasil disimpan — kalender di-rebuild ulang secara otomatis
        HolidayUpdater.loadAndRefresh(this, year) {
            // Callback ini sudah dijalankan di main thread
            Log.d("MainActivity", "CSV $year diperbarui dari GitHub, rebuild kalender")
            val newContent = HolidayUpdater.readCsv(this, year)
            if (newContent != null) parseCsvContent(newContent) else holidaysMap.clear()

            monthsData.clear()
            setupMonths(year)
            monthAdapter.replaceData(monthsData)
            updateDataSourceBanner(year)

            // Pertahankan posisi scroll yang sedang aktif
            if (year == todayYear) scrollToMonth(todayMonth)
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Banner info sumber data di bawah year selector
    // Warna & teks berbeda tergantung sumber:
    //  🟢 Hijau  = data dari GitHub (online, ada tanggal update)
    //  🔵 Biru   = data bawaan APK (bundled)
    //  🟠 Oranye = tidak ada data sama sekali
    // ─────────────────────────────────────────────────────────────
    private fun updateDataSourceBanner(year: Int) {
        when (val source = HolidayUpdater.getDataSource(this, year)) {
            is HolidayUpdater.DataSource.Online -> {
                tvCsvWarning.visibility = View.VISIBLE
                tvCsvWarning.text       = "✅ Data diperbarui dari GitHub · ${source.date}"
                tvCsvWarning.setBackgroundColor(
                    android.graphics.Color.parseColor("#2E7D32")  // hijau gelap
                )
            }
            is HolidayUpdater.DataSource.Bundled -> {
                tvCsvWarning.visibility = View.VISIBLE
                tvCsvWarning.text       = "📦 Data bawaan APK · Akan diperbarui saat online"
                tvCsvWarning.setBackgroundColor(
                    android.graphics.Color.parseColor("#1565C0")  // biru gelap
                )
            }
            is HolidayUpdater.DataSource.NotFound -> {
                tvCsvWarning.visibility = View.VISIBLE
                tvCsvWarning.text       = "⚠ Tidak ada data hari libur untuk tahun $year"
                tvCsvWarning.setBackgroundColor(
                    android.graphics.Color.parseColor("#E65100")  // oranye gelap
                )
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Parse isi CSV (String) → isi holidaysMap
    // Memisahkan logika parse dari sumber data (assets vs cache)
    // ─────────────────────────────────────────────────────────────
    private fun parseCsvContent(csvContent: String) {
        holidaysMap.clear()
        var isFirstLine = true
        BufferedReader(StringReader(csvContent)).forEachLine { line ->
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
        Log.d("CSV", "Parsed ${holidaysMap.size} hari libur")
    }

    // ─────────────────────────────────────────────────────────────
    // Bangun data 12 bulan untuk tahun tertentu
    // ─────────────────────────────────────────────────────────────
    private fun setupMonths(year: Int) {
        for (monthIndex in 0..11) {
            val daysList = mutableListOf<DayItem>()
            val calendar = Calendar.getInstance()
            calendar.set(year, monthIndex, 1, 0, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val daysInMonth    = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

            repeat(firstDayOfWeek - 1) {
                daysList.add(DayItem("", false, false, false, false, ""))
            }

            for (day in 1..daysInMonth) {
                calendar.set(year, monthIndex, day, 0, 0, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val dayOfWeek   = calendar.get(Calendar.DAY_OF_WEEK)
                val isHoliday   = holidaysMap.containsKey(Pair(monthIndex, day))
                val holidayName = holidaysMap[Pair(monthIndex, day)] ?: ""
                val isToday     = (year       == todayYear)
                               && (monthIndex == todayMonth)
                               && (day        == todayDay)

                daysList.add(DayItem(
                    day         = day.toString(),
                    isHoliday   = isHoliday,
                    isSunday    = dayOfWeek == Calendar.SUNDAY,
                    isSaturday  = dayOfWeek == Calendar.SATURDAY,
                    isToday     = isToday,
                    holidayName = holidayName
                ))
            }

            val holidayEntries = (1..daysInMonth).mapNotNull { day ->
                holidaysMap[Pair(monthIndex, day)]?.let { HolidayEntry(day, it) }
            }

            monthsData.add(MonthData(
                name           = "${monthNames[monthIndex]} $year",
                adapter        = DayAdapter(daysList),
                holidayEntries = holidayEntries
            ))
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Scroll ke bulan tertentu (0-based index)
    // ─────────────────────────────────────────────────────────────
    private fun scrollToMonth(monthIndex: Int) {
        if (monthIndex < 0 || monthIndex > 11) return
        recyclerView.post {
            (recyclerView.layoutManager as? LinearLayoutManager)
                ?.scrollToPositionWithOffset(monthIndex, 0)
        }
    }

    // ─────────────────────────────────────────────────────────────
    // RecyclerView
    // ─────────────────────────────────────────────────────────────
    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.months_list)
        monthAdapter = MonthAdapter(monthsData) { _ -> }
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
                if (activeYear != todayYear) {
                    activeYear = todayYear.coerceIn(yearMin, yearMax)
                    loadYearData(activeYear, scrollToCurrentMonth = true)
                } else {
                    scrollToMonth(todayMonth)
                }
                true
            }

            R.id.action_refresh_holidays -> {
                // Force refresh: hapus cache lalu unduh ulang dari GitHub
                showForceRefreshDialog()
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

    // ─────────────────────────────────────────────────────────────
    // Dialog konfirmasi force refresh data hari libur
    // ─────────────────────────────────────────────────────────────
    private fun showForceRefreshDialog() {
        AlertDialog.Builder(this)
            .setTitle("Perbarui Data Hari Libur")
            .setMessage(
                "Mengunduh ulang data hari libur $activeYear dari GitHub https://raw.githubusercontent.com/ryanbekabe/Kalender-2026/main/app/src/main/assets/holidays_$activeYear.csv.\n\n" +
                "Kalender akan diperbarui otomatis setelah unduhan selesai."
            )
            .setPositiveButton("Perbarui") { _, _ ->
                // Hapus cache agar batas waktu 24 jam diabaikan
                HolidayUpdater.clearCache(this, activeYear)
                // Tampilkan banner loading sementara
                tvCsvWarning.visibility = View.VISIBLE
                tvCsvWarning.text       = "⏳ Mengunduh data hari libur $activeYear..."
                tvCsvWarning.setBackgroundColor(
                    android.graphics.Color.parseColor("#4A148C")  // ungu gelap
                )
                // Jalankan unduhan
                HolidayUpdater.loadAndRefresh(this, activeYear) {
                    val newContent = HolidayUpdater.readCsv(this, activeYear)
                    if (newContent != null) parseCsvContent(newContent) else holidaysMap.clear()
                    monthsData.clear()
                    setupMonths(activeYear)
                    monthAdapter.replaceData(monthsData)
                    updateDataSourceBanner(activeYear)
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showAboutDialog() {
        AboutDialog.show(this, BuildConfig.VERSION_NAME)
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

    data class HolidayEntry(val day: Int, val name: String)

    data class MonthData(
        val name: String,
        val adapter: DayAdapter,
        val holidayEntries: List<HolidayEntry> = emptyList()
    )
}
