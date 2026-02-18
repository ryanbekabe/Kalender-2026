package com.hanyajasa.kalender2026

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

/**
 * AboutDialog — dialog "Tentang Aplikasi" dengan layout custom.
 *
 * Menampilkan:
 *  - Nama aplikasi & versi
 *  - Deskripsi singkat
 *  - Website, Email, GitHub (bisa diklik)
 *  - Gambar QRIS dari assets/QRISHanyaJasaCom.jpg
 *  - Daftar rekening bank donasi
 */
object AboutDialog {

    // ── Data rekening bank donasi ─────────────────────────────────
    private val bankAccounts = listOf(
        Pair("BSI",      "9692999170"),
        Pair("BCA",      "8600432053"),
        Pair("BRI",      "4543-01-020754-53-0"),
        Pair("Mandiri",  "159-00-0068323-4"),
        Pair("Muamalat", "6310042068"),
        Pair("Dana",     "082254205110"),
        Pair("Jenius",   "90110062490"),
        Pair("Jago",     "101396991206"),
        Pair("Seabank",  "901899706783")
    )

    fun show(context: Context, versionName: String) {
        val inflater = LayoutInflater.from(context)
        val view     = inflater.inflate(R.layout.dialog_about, null)

        // ── Isi nama & versi ──────────────────────────────────────
        view.findViewById<TextView>(R.id.tv_app_name).text    = "Kalender Nusantara"
        view.findViewById<TextView>(R.id.tv_app_version).text = "Versi $versionName"

        // ── Link Website ──────────────────────────────────────────
        view.findViewById<TextView>(R.id.tv_website).apply {
            setOnClickListener {
                openUrl(context, "https://hanyajasa.com")
            }
        }

        // ── Link Email ────────────────────────────────────────────
        view.findViewById<TextView>(R.id.tv_email).apply {
            setOnClickListener {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:hanyajasa@gmail.com")
                }
                context.startActivity(Intent.createChooser(intent, "Kirim Email"))
            }
        }

        // ── Link GitHub ───────────────────────────────────────────
        view.findViewById<TextView>(R.id.tv_github).apply {
            setOnClickListener {
                openUrl(context, "https://github.com/ryanbekabe/Kalender-2026")
            }
        }

        // ── Gambar QRIS dari assets ───────────────────────────────
        loadQrisImage(context, view.findViewById(R.id.img_qris))

        // ── Daftar rekening bank ──────────────────────────────────
        buildBankList(context, view.findViewById(R.id.ll_bank_accounts))

        // ── Tampilkan dialog ──────────────────────────────────────
        AlertDialog.Builder(context)
            .setTitle("Tentang Aplikasi")
            .setView(view)
            .setPositiveButton("Tutup", null)
            .show()
    }

    // ─────────────────────────────────────────────────────────────
    // Muat gambar QRIS dari assets/QRISHanyaJasaCom.jpg
    // Jika file tidak ditemukan, sembunyikan ImageView
    // ─────────────────────────────────────────────────────────────
    private fun loadQrisImage(context: Context, imageView: ImageView) {
        try {
            val inputStream = context.assets.open("QRISHanyaJasaCom.jpg")
            val bitmap      = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            imageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            // File tidak ada di assets — sembunyikan ImageView
            imageView.visibility = View.GONE
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Bangun baris rekening bank secara programatik
    // Format: [Nama Bank]   [Nomor Rekening]
    // ─────────────────────────────────────────────────────────────
    private fun buildBankList(context: Context, container: LinearLayout) {
        val density = context.resources.displayMetrics.density

        for ((bankName, accountNumber) in bankAccounts) {
            val row = LinearLayout(context).apply {
                orientation  = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = (4 * density).toInt() }
            }

            // Nama bank — lebar tetap 72dp, tebal
            val tvBank = TextView(context).apply {
                text      = bankName
                textSize  = 12f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(android.graphics.Color.parseColor("#424242"))
                layoutParams = LinearLayout.LayoutParams(
                    (72 * density).toInt(),
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            // Separator " : "
            val tvSep = TextView(context).apply {
                text      = ": "
                textSize  = 12f
                setTextColor(android.graphics.Color.parseColor("#757575"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            // Nomor rekening — bisa diseleksi untuk copy-paste
            val tvNumber = TextView(context).apply {
                text      = accountNumber
                textSize  = 12f
                setTextIsSelectable(true)   // method yang benar di Kotlin
                setTextColor(android.graphics.Color.parseColor("#212121"))
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            row.addView(tvBank)
            row.addView(tvSep)
            row.addView(tvNumber)
            container.addView(row)
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Buka URL di browser
    // ─────────────────────────────────────────────────────────────
    private fun openUrl(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }
}
