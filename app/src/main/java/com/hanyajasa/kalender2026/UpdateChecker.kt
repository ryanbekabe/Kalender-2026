package com.hanyajasa.kalender2026

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AlertDialog
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

/**
 * UpdateChecker — mengecek rilis terbaru dari GitHub Releases API.
 *
 * Alur kerja:
 *  1. Jalankan HTTP GET ke GitHub API di background thread
 *  2. Parse JSON → ambil tag_name dan body (release notes)
 *  3. Bandingkan tag_name dengan versionName APK saat ini
 *  4. Jika berbeda → tampilkan dialog update di main thread
 *  5. Tombol "Download" membuka browser ke halaman release GitHub
 *
 * Format tag GitHub Release yang diharapkan: "v1.0", "v1.1", dst.
 * Format versionName di build.gradle: "1.0", "1.1", dst.
 */
object UpdateChecker {

    private const val TAG             = "UpdateChecker"
    private const val GITHUB_API_URL  =
        "https://api.github.com/repos/ryanbekabe/Kalender-2026/releases/latest"
    private const val GITHUB_RELEASES =
        "https://github.com/ryanbekabe/Kalender-2026/releases/latest"
    private const val CONNECT_TIMEOUT = 10_000   // 10 detik
    private const val READ_TIMEOUT    = 10_000

    /**
     * Cek update dari GitHub.
     * @param context      Activity context untuk menampilkan dialog
     * @param currentVersion versionName APK saat ini (misal "1.0")
     * @param silent       true = tidak tampilkan dialog jika sudah up-to-date
     *                     false = selalu tampilkan hasil (untuk cek manual)
     */
    fun check(context: Context, currentVersion: String, silent: Boolean = true) {
        val executor    = Executors.newSingleThreadExecutor()
        val mainHandler = Handler(Looper.getMainLooper())

        executor.execute {
            val result = fetchLatestRelease()

            mainHandler.post {
                when (result) {
                    is Result.Success -> {
                        val latestTag     = result.tagName.trimStart('v', 'V')
                        val latestVersion = latestTag
                        val releaseNotes  = result.body.ifBlank { "Tidak ada catatan rilis." }
                        val downloadUrl   = result.downloadUrl.ifBlank { GITHUB_RELEASES }

                        if (isNewerVersion(latestVersion, currentVersion)) {
                            // Ada update — selalu tampilkan dialog
                            showUpdateDialog(
                                context        = context,
                                currentVersion = currentVersion,
                                latestVersion  = latestVersion,
                                releaseNotes   = releaseNotes,
                                downloadUrl    = downloadUrl
                            )
                        } else {
                            // Sudah up-to-date
                            if (!silent) {
                                showUpToDateDialog(context, currentVersion)
                            }
                        }
                    }
                    is Result.Error -> {
                        Log.e(TAG, "Gagal cek update: ${result.message}")
                        if (!silent) {
                            showErrorDialog(context, result.message)
                        }
                    }
                }
            }
        }
    }

    // ---------------------------------------------------------------
    // HTTP request ke GitHub API
    // ---------------------------------------------------------------
    private fun fetchLatestRelease(): Result {
        return try {
            val url  = URL(GITHUB_API_URL)
            val conn = url.openConnection() as HttpURLConnection
            conn.apply {
                requestMethod  = "GET"
                connectTimeout = CONNECT_TIMEOUT
                readTimeout    = READ_TIMEOUT
                setRequestProperty("Accept", "application/vnd.github+json")
            }

            val responseCode = conn.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return Result.Error("Server merespons dengan kode $responseCode")
            }

            val json      = conn.inputStream.bufferedReader().readText()
            conn.disconnect()

            val obj         = JSONObject(json)
            val tagName     = obj.optString("tag_name", "")
            val body        = obj.optString("body", "")

            // Coba ambil URL download APK dari assets release
            var downloadUrl = GITHUB_RELEASES
            val assets = obj.optJSONArray("assets")
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name  = asset.optString("name", "")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        downloadUrl = asset.optString("browser_download_url", GITHUB_RELEASES)
                        break
                    }
                }
            }

            if (tagName.isEmpty()) {
                Result.Error("Tag versi tidak ditemukan di GitHub")
            } else {
                Result.Success(tagName, body, downloadUrl)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception saat fetch: ${e.message}")
            Result.Error("Tidak dapat terhubung ke server.\nPeriksa koneksi internet Anda.")
        }
    }

    // ---------------------------------------------------------------
    // Bandingkan versi: apakah latestVersion > currentVersion?
    // Mendukung format semantic version: "1.0", "1.2.3", dst.
    // ---------------------------------------------------------------
    private fun isNewerVersion(latest: String, current: String): Boolean {
        return try {
            val latestParts  = latest.split(".").map { it.trim().toIntOrNull() ?: 0 }
            val currentParts = current.split(".").map { it.trim().toIntOrNull() ?: 0 }
            val maxLen       = maxOf(latestParts.size, currentParts.size)

            for (i in 0 until maxLen) {
                val l = latestParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (l > c) return true
                if (l < c) return false
            }
            false   // sama persis
        } catch (e: Exception) {
            false
        }
    }

    // ---------------------------------------------------------------
    // Dialog: ada update tersedia
    // ---------------------------------------------------------------
    private fun showUpdateDialog(
        context: Context,
        currentVersion: String,
        latestVersion: String,
        releaseNotes: String,
        downloadUrl: String
    ) {
        val message = buildString {
            append("Versi terbaru: $latestVersion\n")
            append("Versi Anda   : $currentVersion\n\n")
            append("Catatan Rilis:\n")
            // Batasi panjang release notes agar dialog tidak terlalu panjang
            val notes = if (releaseNotes.length > 300)
                releaseNotes.take(300) + "…" else releaseNotes
            append(notes)
        }

        AlertDialog.Builder(context)
            .setTitle("🔔 Update Tersedia!")
            .setMessage(message)
            .setPositiveButton("Download") { _, _ ->
                // Buka browser ke halaman download / release APK
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                context.startActivity(intent)
            }
            .setNegativeButton("Nanti", null)
            .setCancelable(true)
            .show()
    }

    // ---------------------------------------------------------------
    // Dialog: sudah versi terbaru (untuk cek manual)
    // ---------------------------------------------------------------
    private fun showUpToDateDialog(context: Context, currentVersion: String) {
        AlertDialog.Builder(context)
            .setTitle("✅ Sudah Terbaru")
            .setMessage("Anda menggunakan versi terbaru ($currentVersion).")
            .setPositiveButton("OK", null)
            .show()
    }

    // ---------------------------------------------------------------
    // Dialog: gagal cek update (untuk cek manual)
    // ---------------------------------------------------------------
    private fun showErrorDialog(context: Context, message: String) {
        AlertDialog.Builder(context)
            .setTitle("⚠️ Gagal Cek Update")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    // ---------------------------------------------------------------
    // Sealed class hasil fetch
    // ---------------------------------------------------------------
    private sealed class Result {
        data class Success(
            val tagName: String,
            val body: String,
            val downloadUrl: String
        ) : Result()

        data class Error(val message: String) : Result()
    }
}
