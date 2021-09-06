package com.reply.irisstandbyduty.ui.calendar.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.reply.irisstandbyduty.R
import com.reply.irisstandbyduty.databinding.ItemCalendarDayBinding
import com.reply.irisstandbyduty.model.InterventionType
import com.reply.irisstandbyduty.model.Shift
import java.util.*

/**
 * Created by Reply on 06/09/21.
 */
class CalendarAdapter :
    ListAdapter<Shift, ShiftViewHolder>(ShiftDiff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShiftViewHolder {
        return ShiftViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_day, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ShiftViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.item_calendar_day
    }
}

class ShiftViewHolder(
    private val view: View
) : RecyclerView.ViewHolder(view) {

    private val binding = ItemCalendarDayBinding.bind(view)

    fun bind(shift: Shift) {
        val cal: Calendar = Calendar.getInstance()
        cal.time = shift.date
        val dayOfWeek: Int = cal.get(Calendar.DAY_OF_WEEK)
        val day = cal.get(Calendar.DAY_OF_MONTH)

        binding.dayOfWeekTextView.text =
            view.context.resources.getStringArray(R.array.daysOfWeek)[dayOfWeek-1]
        binding.dayNumberTextView.text = day.toString()
        binding.nameTextView.text = shift.employee.name

        if (shift.interventionType != InterventionType.R) {
            binding.overtimeStatus.visibility = View.VISIBLE
            binding.overtimeStatus.text = shift.interventionType.name
        } else {
            binding.overtimeStatus.visibility = View.GONE
        }
    }

}

object ShiftDiff : DiffUtil.ItemCallback<Shift>() {
    override fun areItemsTheSame(oldItem: Shift, newItem: Shift): Boolean {
        return oldItem.date == newItem.date
    }

    override fun areContentsTheSame(oldItem: Shift, newItem: Shift) = oldItem == newItem
}