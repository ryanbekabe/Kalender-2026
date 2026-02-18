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
        val dayItem = days[position]

        // PERBAIKAN BUG #2: reset background dan visibility SEBELUM return,
        // agar ViewHolder yang di-recycle dari sel libur tidak mewarisi
        // background bg_holiday ke sel kosong berikutnya.
        if (dayItem.day.isEmpty()) {
            holder.dayContainer.visibility = View.INVISIBLE
            holder.dayContainer.background = null   // ← reset background
            return
        }

        // holidayText selalu disembunyikan — keterangan libur ada di holiday_list
        holder.holidayText.visibility  = View.GONE
        holder.dayContainer.visibility = View.VISIBLE
        holder.dayText.text            = dayItem.day

        // Warna angka tanggal + background
        when {
            dayItem.isHoliday -> {
                holder.dayText.setTextColor(android.graphics.Color.parseColor("#D32F2F"))
                holder.dayContainer.setBackgroundResource(R.drawable.bg_holiday)
            }
            dayItem.isSunday -> {
                holder.dayText.setTextColor(android.graphics.Color.parseColor("#D32F2F"))
                holder.dayContainer.background = null
            }
            dayItem.isSaturday -> {
                holder.dayText.setTextColor(android.graphics.Color.parseColor("#1976D2"))
                holder.dayContainer.background = null
            }
            else -> {
                holder.dayText.setTextColor(android.graphics.Color.parseColor("#212121"))
                holder.dayContainer.background = null
            }
        }
    }

    override fun getItemCount(): Int = days.size
}
