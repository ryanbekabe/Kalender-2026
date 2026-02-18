package com.hanyajasa.kalender2026

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DayAdapter(
    private val days: List<MainActivity.DayItem>
) : RecyclerView.Adapter<DayAdapter.DayViewHolder>() {

    class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dayContainer: LinearLayout = view.findViewById(R.id.day_container)
        val dayText: TextView          = view.findViewById(R.id.dayText)
        val holidayText: TextView      = view.findViewById(R.id.holidayText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val d = days[position]

        // Sel kosong — reset semua lalu keluar
        if (d.day.isEmpty()) {
            holder.dayContainer.visibility = View.INVISIBLE
            holder.dayContainer.background = null
            return
        }

        holder.holidayText.visibility  = View.GONE
        holder.dayContainer.visibility = View.VISIBLE
        holder.dayText.text            = d.day

        // ── Prioritas tampilan (dari tertinggi ke terendah): ────────
        //
        //  1. isToday + isHoliday  → lingkaran biru tua, teks PUTIH
        //                            (hari ini sekaligus libur nasional)
        //  2. isToday + isSunday   → lingkaran biru tua, teks PUTIH
        //                            (hari ini sekaligus Minggu)
        //  3. isToday (biasa)      → lingkaran biru tua, teks PUTIH
        //  4. isHoliday            → lingkaran merah muda, teks merah
        //  5. isSunday             → teks merah, tanpa background
        //  6. isSaturday           → teks biru, tanpa background
        //  7. hari biasa           → teks hitam, tanpa background
        // ────────────────────────────────────────────────────────────

        when {
            d.isToday -> {
                // Hari ini selalu mendapat lingkaran biru tua + teks putih,
                // apapun jenis hari lainnya (libur, Minggu, Sabtu, biasa)
                holder.dayContainer.setBackgroundResource(R.drawable.bg_today)
                holder.dayText.setTextColor(android.graphics.Color.WHITE)
            }
            d.isHoliday -> {
                holder.dayContainer.setBackgroundResource(R.drawable.bg_holiday)
                holder.dayText.setTextColor(android.graphics.Color.parseColor("#D32F2F"))
            }
            d.isSunday -> {
                holder.dayContainer.background = null
                holder.dayText.setTextColor(android.graphics.Color.parseColor("#D32F2F"))
            }
            d.isSaturday -> {
                holder.dayContainer.background = null
                holder.dayText.setTextColor(android.graphics.Color.parseColor("#1976D2"))
            }
            else -> {
                holder.dayContainer.background = null
                holder.dayText.setTextColor(android.graphics.Color.parseColor("#212121"))
            }
        }
    }

    override fun getItemCount(): Int = days.size
}
