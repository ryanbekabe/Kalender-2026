package com.hanyajasa.kalender2026

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MonthAdapter(
    months: List<MainActivity.MonthData>,
    private val onMonthClick: (Int) -> Unit
) : RecyclerView.Adapter<MonthAdapter.MonthViewHolder>() {

    // Internal mutable list — bisa diganti penuh saat user ganti tahun
    private val months = months.toMutableList()

    // -1 = tidak ada yang expand (mode accordion default)
    private var expandedPosition = -1

    // true  = semua bulan tampil sekaligus (expand all)
    // false = mode accordion (satu per satu)
    var isExpandAll: Boolean = false
        private set

    // ---------------------------------------------------------------
    // Ganti seluruh data — dipanggil saat user ganti tahun
    // ---------------------------------------------------------------
    fun replaceData(newMonths: List<MainActivity.MonthData>) {
        months.clear()
        months.addAll(newMonths)
        expandedPosition = -1   // reset accordion agar tidak crash
        notifyDataSetChanged()
    }

    // ---------------------------------------------------------------
    // Toggle antara mode "Semua Terbuka" dan "Accordion"
    // ---------------------------------------------------------------
    fun toggleExpandAll() {
        isExpandAll = !isExpandAll
        if (isExpandAll) {
            expandedPosition = -1
        }
        notifyDataSetChanged()
    }

    class MonthViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val headerLayout: LinearLayout      = view.findViewById(R.id.month_header)
        val monthName: TextView             = view.findViewById(R.id.month_name)
        val expandIcon: ImageView           = view.findViewById(R.id.expand_icon)
        val calendarContainer: LinearLayout = view.findViewById(R.id.calendar_container)
        val calendarGrid: RecyclerView      = view.findViewById(R.id.calendar_grid)
        val holidayList: LinearLayout       = view.findViewById(R.id.holiday_list)
        var boundMonthIndex: Int = -1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_month, parent, false)
        return MonthViewHolder(view)
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        val month = months[position]
        holder.monthName.text = month.name

        // ── Visibilitas konten bulan ───────────────────────────────
        val isVisible = isExpandAll || position == expandedPosition
        holder.calendarContainer.visibility = if (isVisible) View.VISIBLE else View.GONE

        // ── Ikon expand ────────────────────────────────────────────
        if (isExpandAll) {
            holder.expandIcon.visibility = View.GONE
        } else {
            holder.expandIcon.visibility = View.VISIBLE
            holder.expandIcon.rotation   = if (position == expandedPosition) 180f else 0f
        }

        // ── Attach grid adapter jika posisi bulan berubah ──────────
        if (holder.boundMonthIndex != position) {
            holder.calendarGrid.layoutManager = GridLayoutManager(holder.itemView.context, 7)
            holder.calendarGrid.adapter       = month.adapter
            holder.boundMonthIndex            = position
        }

        // ── Legend hari libur ──────────────────────────────────────
        if (isVisible) {
            buildHolidayLegend(holder, month.holidayEntries)
        }

        // ── Klik header ────────────────────────────────────────────
        holder.headerLayout.setOnClickListener {
            if (isExpandAll) return@setOnClickListener

            val previousExpanded = expandedPosition
            expandedPosition = if (expandedPosition == position) -1 else position

            if (previousExpanded != -1) notifyItemChanged(previousExpanded)
            notifyItemChanged(position)
            onMonthClick(position)
        }
    }

    // ---------------------------------------------------------------
    // Bangun legend keterangan hari libur di bawah grid kalender
    // ---------------------------------------------------------------
    private fun buildHolidayLegend(
        holder: MonthViewHolder,
        entries: List<MainActivity.HolidayEntry>
    ) {
        val container = holder.holidayList
        container.removeAllViews()

        if (entries.isEmpty()) {
            container.visibility = View.GONE
            return
        }

        val ctx     = holder.itemView.context
        val density = ctx.resources.displayMetrics.density

        // Garis pemisah tipis
        val divider = View(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (1 * density).toInt()
            ).also { it.bottomMargin = (8 * density).toInt() }
            setBackgroundColor(android.graphics.Color.parseColor("#E0E0E0"))
        }
        container.addView(divider)

        for (entry in entries) {
            val row = LinearLayout(ctx).apply {
                orientation  = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = (4 * density).toInt() }
            }

            val tvDay = TextView(ctx).apply {
                text      = entry.day.toString()
                textSize  = 13f
                setTextColor(android.graphics.Color.parseColor("#D32F2F"))
                setTypeface(typeface, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    (32 * density).toInt(),
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val tvName = TextView(ctx).apply {
                text      = entry.name
                textSize  = 13f
                setTextColor(android.graphics.Color.parseColor("#212121"))
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                )
            }

            row.addView(tvDay)
            row.addView(tvName)
            container.addView(row)
        }

        container.visibility = View.VISIBLE
    }

    override fun getItemCount(): Int = months.size
}
