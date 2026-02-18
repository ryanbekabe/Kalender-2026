package com.hanyajasa.kalender2026

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

/**
 * HolidayUpdater — mengunduh file CSV hari libur dari GitHub Raw
 * dan menyimpannya di internal storage perangkat.
 *
 * ── Prioritas sumber data (dari tertinggi ke terendah): ──────────
 *  1. Internal storage  → holidays_<year>.csv yang sudah diunduh
 *  2. Assets APK        → holidays_<year>.csv bawaan APK
 *  3. Tidak ada data    → kalender kosong tanpa warna libur
 *
 * ── Kapan unduh dilakukan: ───────────────────────────────────────
 *  - Saat loadAndRefresh() dipanggil dan ada koneksi internet
 *  - Unduhan berjalan di background thread (tidak memblokir UI)
 *  - Setelah unduhan selesai, kalender di-refresh otomatis
 *  - Setiap tahun hanya diunduh ulang maksimal sekali per hari
 *    (dibatasi oleh REFRESH_INTERVAL_MS) agar tidak boros data
 *
 * ── URL sumber CSV: ──────────────────────────────────────────────
 *  https://raw.githubusercontent.com/ryanbekabe/Kalender-2026/main/
 *      app/src/main/assets/holidays_<year>.csv
 */
object HolidayUpdater {

    private const val TAG = "HolidayUpdater"

    // URL dasar GitHub Raw — sesuaikan jika struktur folder berubah
    private const val GITHUB_RAW_BASE =
        "https://raw.githubusercontent.com/ryanbekabe/Kalender-2026/main/" +
        "app/src/main/assets"

    // Interval minimum antar unduhan ulang untuk satu tahun: 24 jam
    private const val REFRESH_INTERVAL_MS = 24 * 60 * 60 * 1000L

    private const val PREFS_NAME      = "holiday_updater_prefs"
    private const val PREFS_KEY_PREFIX = "last_fetch_"   // + year

    private const val CONNECT_TIMEOUT = 10_000
    private const val READ_TIMEOUT    = 15_000

    // ─────────────────────────────────────────────────────────────
    // Fungsi utama — dipanggil dari MainActivity.
    //
    // Alur:
    //  1. Coba unduh CSV dari GitHub di background thread
    //  2. Jika berhasil → simpan ke internal storage → panggil onRefresh
    //  3. Jika gagal / belum waktunya → tidak ada perubahan
    //
    // @param context     Application context
    // @param year        Tahun yang akan diperbarui
    // @param onRefresh   Callback dipanggil di MAIN THREAD setelah
    //                    CSV baru berhasil disimpan → rebuild kalender
    // ─────────────────────────────────────────────────────────────
    fun loadAndRefresh(context: Context, year: Int, onRefresh: () -> Unit) {
        val executor    = Executors.newSingleThreadExecutor()
        val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())

        executor.execute {
            // Cek apakah sudah waktunya unduh ulang
            if (!shouldFetch(context, year)) {
                Log.d(TAG, "CSV $year masih fresh, skip unduhan")
                return@execute
            }

            val csvUrl  = "$GITHUB_RAW_BASE/holidays_$year.csv"
            val content = downloadCsv(csvUrl)

            if (content != null && isValidCsv(content)) {
                // Simpan ke internal storage
                saveToCacheFile(context, year, content)
                // Catat waktu unduhan terakhir
                saveLastFetchTime(context, year)
                Log.d(TAG, "CSV $year berhasil diunduh dan disimpan")

                // Panggil callback di main thread untuk refresh UI
                mainHandler.post { onRefresh() }
            } else {
                Log.w(TAG, "Unduhan CSV $year gagal atau konten tidak valid")
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Baca CSV untuk tahun tertentu.
    // Prioritas: internal storage → assets APK → null
    //
    // Mengembalikan isi CSV sebagai String, atau null jika tidak ada.
    // ─────────────────────────────────────────────────────────────
    fun readCsv(context: Context, year: Int): String? {
        // 1. Coba baca dari internal storage (versi terbaru dari GitHub)
        val cacheFile = getCacheFile(context, year)
        if (cacheFile.exists()) {
            try {
                val content = cacheFile.readText(Charsets.UTF_8)
                if (isValidCsv(content)) {
                    Log.d(TAG, "Membaca CSV $year dari cache (${cacheFile.length()} bytes)")
                    return content
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gagal baca cache $year: ${e.message}")
            }
        }

        // 2. Fallback: baca dari assets APK
        try {
            val content = context.assets
                .open("holidays_$year.csv")
                .bufferedReader(Charsets.UTF_8)
                .readText()
            Log.d(TAG, "Membaca CSV $year dari assets APK")
            return content
        } catch (e: Exception) {
            Log.d(TAG, "CSV $year tidak ada di assets APK")
        }

        return null
    }

    // ─────────────────────────────────────────────────────────────
    // Informasi sumber data yang sedang dipakai — untuk ditampilkan
    // di UI (banner info di bawah year selector)
    // ─────────────────────────────────────────────────────────────
    fun getDataSource(context: Context, year: Int): DataSource {
        val cacheFile = getCacheFile(context, year)
        if (cacheFile.exists()) {
            val lastFetch = getLastFetchTime(context, year)
            val dateStr   = if (lastFetch > 0) {
                val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("id", "ID"))
                sdf.format(Date(lastFetch))
            } else "tidak diketahui"
            return DataSource.Online(dateStr)
        }

        return try {
            context.assets.open("holidays_$year.csv").close()
            DataSource.Bundled
        } catch (e: Exception) {
            DataSource.NotFound
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Hapus cache untuk satu tahun — untuk force refresh manual
    // ─────────────────────────────────────────────────────────────
    fun clearCache(context: Context, year: Int) {
        getCacheFile(context, year).delete()
        getPrefs(context).edit().remove("$PREFS_KEY_PREFIX$year").apply()
        Log.d(TAG, "Cache CSV $year dihapus")
    }

    // ─────────────────────────────────────────────────────────────
    // Internal helpers
    // ─────────────────────────────────────────────────────────────

    private fun downloadCsv(urlString: String): String? {
        return try {
            val url  = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.apply {
                requestMethod  = "GET"
                connectTimeout = CONNECT_TIMEOUT
                readTimeout    = READ_TIMEOUT
                setRequestProperty("Cache-Control", "no-cache")
            }
            val code = conn.responseCode
            if (code != HttpURLConnection.HTTP_OK) {
                Log.w(TAG, "HTTP $code dari $urlString")
                conn.disconnect()
                return null
            }
            val content = conn.inputStream.bufferedReader(Charsets.UTF_8).readText()
            conn.disconnect()
            content
        } catch (e: Exception) {
            Log.w(TAG, "Gagal unduh $urlString: ${e.message}")
            null
        }
    }

    // Validasi minimal: harus ada header "tanggal,keterangan"
    // dan minimal satu baris data berformat YYYY-MM-DD
    private fun isValidCsv(content: String): Boolean {
        val lines = content.trim().lines()
        if (lines.size < 2) return false
        if (!lines[0].contains("tanggal", ignoreCase = true)) return false
        // Cek minimal satu baris data berformat tanggal valid
        return lines.drop(1).any { line ->
            line.trim().matches(Regex("\\d{4}-\\d{2}-\\d{2},.+"))
        }
    }

    private fun getCacheFile(context: Context, year: Int): File =
        File(context.filesDir, "holidays_$year.csv")

    private fun saveToCacheFile(context: Context, year: Int, content: String) {
        getCacheFile(context, year).writeText(content, Charsets.UTF_8)
    }

    private fun shouldFetch(context: Context, year: Int): Boolean {
        val lastFetch = getLastFetchTime(context, year)
        val now       = System.currentTimeMillis()
        return (now - lastFetch) >= REFRESH_INTERVAL_MS
    }

    private fun getLastFetchTime(context: Context, year: Int): Long =
        getPrefs(context).getLong("$PREFS_KEY_PREFIX$year", 0L)

    private fun saveLastFetchTime(context: Context, year: Int) {
        getPrefs(context).edit()
            .putLong("$PREFS_KEY_PREFIX$year", System.currentTimeMillis())
            .apply()
    }

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ─────────────────────────────────────────────────────────────
    // Sealed class untuk status sumber data
    // ─────────────────────────────────────────────────────────────
    sealed class DataSource {
        /** Data diunduh dari GitHub, terakhir diperbarui pada [date] */
        data class Online(val date: String) : DataSource()
        /** Data bawaan APK (belum pernah diunduh) */
        object Bundled : DataSource()
        /** Tidak ada data sama sekali */
        object NotFound : DataSource()
    }
}
