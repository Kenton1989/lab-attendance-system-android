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
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class SessionListItemAdapter(
    private val context: Context,
    private val onSessionSelected: (s: Session) -> Unit = { }
) : RecyclerView.Adapter<SessionListItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(val binding: ItemSessionListBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    var sessions: List<Session> = listOf()
        private set

    private var selectedSession: Session? = null
    private var lastSelectedHolder: ItemViewHolder? = null

    fun updateSessions(newSessions: List<Session>, initSelectedSession: Session? = null) {
        sessions = newSessions
        updateSelectedSession(initSelectedSession)
        notifySessionDataChanged()
    }


    fun updateSelectedSession(session: Session?) {
        if (selectedSession?.id == session?.id) {
            return
        }

        selectedSession = session
        session?.let { newS ->
            val newPos = sessions.indexOfFirst { it.id == newS.id }
            if (newPos != -1) notifyItemChanged(newPos)
        }
        lastSelectedHolder?.let {
            notifyItemChanged(it.absoluteAdapterPosition)
        }

        if (session != null) {
            onSessionSelected(session)
        }
    }

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

            if (session.id == selectedSession?.id) {
                colorfulBorder.visibility = View.VISIBLE
                lastSelectedHolder = holder
            } else {
                colorfulBorder.visibility = View.GONE
            }
//            sessionCard.isChecked = item.id == viewModel.selectedSession.value?.id

            sessionCard.setOnClickListener {
                updateSelectedSession(session)
            }
        }
    }

    override fun getItemCount(): Int {
        return sessions.size
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun notifySessionDataChanged() {
        notifyDataSetChanged()
    }

    private fun timeStr(t: ZonedDateTime) = t.format(TIME_FORMAT)

    companion object {
        private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm")
    }
}