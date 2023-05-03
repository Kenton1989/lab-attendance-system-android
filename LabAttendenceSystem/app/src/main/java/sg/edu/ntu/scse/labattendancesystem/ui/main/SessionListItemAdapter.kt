package sg.edu.ntu.scse.labattendancesystem.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import sg.edu.ntu.scse.labattendancesystem.R
import sg.edu.ntu.scse.labattendancesystem.databinding.ItemSessionListBinding
import sg.edu.ntu.scse.labattendancesystem.domain.models.Session
import sg.edu.ntu.scse.labattendancesystem.viewmodels.main.MainViewModel
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class SessionListItemAdapter(
    private val context: Context,
    private val viewModel: MainViewModel,
    private val onSessionSelected: (s: Session) -> Unit = { }
) : RecyclerView.Adapter<SessionListItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(val binding: ItemSessionListBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    var sessions: List<Session> = listOf()
        @SuppressLint("NotifyDataSetChanged") set(value) {
            field = value
            resetClickedItem()
            notifyDataSetChanged()
        }

    private var lastSelectedHolder: ItemViewHolder? = null

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
        val session = sessions[position]
        if (position == 0 && lastSelectedHolder == null) {
            updateSelectedSession(holder, session)
        }
        holder.binding.apply {
            sessionTitle.text = context.resources.getString(
                R.string.group_course_tag,
                session.group.name,
                session.group.course.code,
                if (session.isCompulsory) "" else "(optional)"
            )
            sessionTime.text = context.resources.getString(
                R.string.time_range,
                timeStr(session.startTime),
                timeStr(session.endTime),
            )

            if (session.id == viewModel.selectedSession.value?.id) {
                colorfulBorder.visibility = View.VISIBLE
                lastSelectedHolder = holder
            } else {
                colorfulBorder.visibility = View.GONE
            }
//            sessionCard.isChecked = item.id == viewModel.selectedSession.value?.id

            sessionCard.setOnClickListener {
                onItemClicked(holder, session)
            }
        }
    }

    override fun getItemCount(): Int {
        return sessions.size
    }

    private fun onItemClicked(holder: ItemViewHolder, session: Session) {
        notifyItemChanged(holder.layoutPosition)
        lastSelectedHolder?.let { notifyItemChanged(it.layoutPosition) }
        updateSelectedSession(holder, session)
    }

    private fun updateSelectedSession(holder: ItemViewHolder, session: Session) {
        viewModel.updateSelectedSession(session.id)
        onSessionSelected(session)
        lastSelectedHolder = holder
    }

    private fun resetClickedItem() {
        lastSelectedHolder = null
    }

    private fun timeStr(t: ZonedDateTime) = t.format(TIME_FORMAT)

    companion object {
        private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm")
    }
}