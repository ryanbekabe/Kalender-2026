package com.hanyajasa.kalender2026

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DayAdapter(
    private val days: List<MainActivity.DayItem>
) : RecyclerView.Adapter<DayAdapter.DayViewHolder>() {

    class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dayText: TextView = view.findViewById(R.id.dayText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val dayItem = days[position]
        holder.dayText.text = dayItem.day

        when {
            dayItem.isHoliday -> {
                holder.dayText.setTextColor(android.graphics.Color.RED)
                holder.dayText.setBackgroundResource(R.drawable.bg_holiday)
            }
            dayItem.isSunday -> {
                holder.dayText.setTextColor(android.graphics.Color.RED)
                holder.dayText.background = null
            }
            else -> {
                holder.dayText.setTextColor(android.graphics.Color.BLACK)
                holder.dayText.background = null
            }
        }

        if (dayItem.day.isEmpty()) {
            holder.dayText.text = ""
            holder.dayText.background = null
        }
    }

    override fun getItemCount(): Int = days.size
}