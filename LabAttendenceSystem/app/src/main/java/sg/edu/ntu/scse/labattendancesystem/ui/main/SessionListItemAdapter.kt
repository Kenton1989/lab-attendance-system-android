package sg.edu.ntu.scse.labattendancesystem.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import sg.edu.ntu.scse.labattendancesystem.R
import sg.edu.ntu.scse.labattendancesystem.databinding.ItemSessionListBinding
import sg.edu.ntu.scse.labattendancesystem.domain.models.Session
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class SessionListItemAdapter(
    private val context: Context,
    private val onSessionSelected: (s: Session) -> Any = { }
) : RecyclerView.Adapter<SessionListItemAdapter.ItemViewHolder>() {

    var sessions: List<Session> = listOf()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class ItemViewHolder(val binding: ItemSessionListBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding: ItemSessionListBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_session_list,
            parent,
            false,
        )
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = sessions[position]
        holder.binding.apply {
            sessionTitle.text = context.resources.getString(
                R.string.course_group,
                item.group.course.code,
                item.group.name,
            )
            sessionTime.text = context.resources.getString(
                R.string.time_range,
                timeStr(item.startTime),
                timeStr(item.endTime),
            )
            clickableOverlay.setOnClickListener {
                onSessionSelected(item)
            }
        }

    }

    override fun getItemCount(): Int {
        return sessions.size
    }

    private fun timeStr(t: OffsetDateTime) =
        t.format(TIME_FORMAT)

    companion object {
        private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm")
    }
}