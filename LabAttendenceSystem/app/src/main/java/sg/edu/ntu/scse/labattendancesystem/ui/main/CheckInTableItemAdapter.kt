package sg.edu.ntu.scse.labattendancesystem.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import sg.edu.ntu.scse.labattendancesystem.R
import sg.edu.ntu.scse.labattendancesystem.databinding.ItemCheckInTableBinding
import sg.edu.ntu.scse.labattendancesystem.domain.models.Attendance


class CheckInTableItemAdapter(
    private val context: Context,
    private val hideSeat: Boolean = false,
    private val onRowClicked: (a: Attendance) -> Unit = { }
) : RecyclerView.Adapter<CheckInTableItemAdapter.ItemViewHolder>() {
    class ItemViewHolder(val binding: ItemCheckInTableBinding) :
        RecyclerView.ViewHolder(binding.root) {}


    var attendances: List<Attendance> = listOf()
        @SuppressLint("NotifyDataSetChanged") set(value) {
            val sortedList = value.sortedBy { a -> a.attender.username }
            field = sortedList
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding: ItemCheckInTableBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_check_in_table,
            parent,
            false,
        )
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val attendance = attendances[position]
        holder.binding.apply {
            @SuppressLint("SetTextI18n")
            rowNumber.text = "${position + 1}"
            username.text = blurUsername(attendance.attender.username)
            fullName.text = attendance.attender.displayName
            if (hideSeat) {
                seat.visibility = View.GONE
            } else {
                seat.visibility = View.VISIBLE
                seat.text = attendance.seat ?: getString(R.string.empty_cell)
            }
            tableRow.setOnClickListener {
                onItemClicked(holder, attendance)
            }
            
            renderCheckInState(holder, attendance)
        }
    }

    private fun renderCheckInState(holder: ItemViewHolder, attendance: Attendance) {
        holder.binding.apply {
            if (attendance.checkInState.isAbsent()) {
                checkedIn.text = getString(R.string.absent_mark)
                if (attendance.session!!.isCompulsory)
                    tableRow.setBackgroundColor(getColor(R.color.warning_background))
                else
                    tableRow.setBackgroundColor(getColor(R.color.no_background))
            } else {
                checkedIn.text = getString(R.string.attended_mark)
                tableRow.setBackgroundColor(getColor(R.color.success_background))
            }
        }
    }

    override fun getItemCount(): Int {
        return attendances.size
    }

    private fun onItemClicked(holder: ItemViewHolder, attendance: Attendance) {
        onRowClicked(attendance)
        notifyItemChanged(holder.layoutPosition)
    }

    private fun blurUsername(s: String): String {
        val prefix = s.substring(0, 2)
        val suffix = s.substring(s.length - 2)
        val middle = "*".repeat(s.length - prefix.length - suffix.length)
        return prefix + middle + suffix
    }

    private fun getString(@StringRes id: Int, vararg v: Any) =
        context.resources.getString(id, *v)

    private fun getColor(@ColorRes id: Int) = ContextCompat.getColor(context, id)
}
