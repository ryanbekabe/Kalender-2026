package com.hanyajasa.kalender2026

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MonthAdapter(
    private val months: List<MainActivity.MonthData>,
    private val onMonthClick: (Int) -> Unit
) : RecyclerView.Adapter<MonthAdapter.MonthViewHolder>() {

    private var expandedPosition = -1

    class MonthViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val headerLayout: LinearLayout = view.findViewById(R.id.month_header)
        val monthName: TextView = view.findViewById(R.id.month_name)
        val expandIcon: ImageView = view.findViewById(R.id.expand_icon)
        val calendarContainer: LinearLayout = view.findViewById(R.id.calendar_container)
        val calendarGrid: RecyclerView = view.findViewById(R.id.calendar_grid)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_month, parent, false)
        return MonthViewHolder(view)
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        val month = months[position]
        holder.monthName.text = month.name

        val isExpanded = position == expandedPosition
        holder.calendarContainer.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.expandIcon.rotation = if (isExpanded) 180f else 0f

        holder.headerLayout.setOnClickListener {
            val previousExpanded = expandedPosition
            expandedPosition = if (expandedPosition == position) -1 else position
            
            if (previousExpanded != -1) {
                notifyItemChanged(previousExpanded)
            }
            notifyItemChanged(position)
            
            onMonthClick(position)
        }

        holder.calendarGrid.layoutManager = GridLayoutManager(holder.itemView.context, 7)
        holder.calendarGrid.adapter = month.adapter
    }

    override fun getItemCount(): Int = months.size

    fun collapseAll() {
        val previousExpanded = expandedPosition
        expandedPosition = -1
        if (previousExpanded != -1) {
            notifyItemChanged(previousExpanded)
        }
    }
}