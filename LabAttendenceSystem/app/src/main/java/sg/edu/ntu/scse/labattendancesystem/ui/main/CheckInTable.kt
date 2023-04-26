package sg.edu.ntu.scse.labattendancesystem.ui.main

import android.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sg.edu.ntu.scse.labattendancesystem.domain.models.Attendance
import kotlin.math.min


class CheckInTable(
    private val parent: Fragment,
    private val tableBodyRecyclerView: RecyclerView,
    private val attendances: LiveData<List<Attendance>>,
    private val hideSeat: Boolean = false,
    private val onUndoCheckIn: (a: Attendance) -> Any = { TODO("Undo not Allowed") },
    private val onCheckIn: (a: Attendance) -> Any,
) {
    init {
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        val adapter = CheckInTableItemAdapter(
            parent.requireContext(), hideSeat, ::onRowClicked
        )

        attendances.observe(parent.viewLifecycleOwner) {
            it?.let { adapter.attendances = it }
        }

        tableBodyRecyclerView.adapter = adapter

        val dividerItemDecoration = DividerItemDecoration(
            tableBodyRecyclerView.context,
            (tableBodyRecyclerView.layoutManager as LinearLayoutManager).orientation
        )
        tableBodyRecyclerView.addItemDecoration(dividerItemDecoration)
    }

    private fun onRowClicked(attendance: Attendance) {
        if (attendance.checkInState.isAbsent()) {
            handleCheckIn(attendance)
        } else {
            handleUndoCheckIn(attendance)
        }
    }

    private fun handleCheckIn(attendance: Attendance) {
        val builder = AlertDialog.Builder(parent.requireContext())
        val username = attendance.attender.username
        val name = attendance.attender.displayName
        builder.setMessage("Confirm check-in for:\n$name\n(username: $username)?")
            .setTitle("Confirm?")
            .setPositiveButton("YES") { _, _ -> onCheckIn(attendance) }
            .setNegativeButton("NO") { _, _ -> }
        val dialog = builder.create()
        freezeDialogPositiveButton(dialog)
    }

    private fun handleUndoCheckIn(attendance: Attendance) {
        val builder = AlertDialog.Builder(parent.requireContext())
        val username = attendance.attender.username
        val name = attendance.attender.displayName
        builder.setMessage("Confirm undo check-in for:\n$name\n(username: $username)?")
            .setTitle("Confirm?")
            .setPositiveButton("YES") { _, _ -> onUndoCheckIn(attendance) }
            .setNegativeButton("NO") { _, _ -> }
        val dialog = builder.create()
        freezeDialogPositiveButton(dialog)
    }

    private fun freezeDialogPositiveButton(dialog: AlertDialog, freezeDurationMillis: Long = 3000) {
        val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        val oldText = button.text

        button.isEnabled = false
        var remainMills = freezeDurationMillis;
        parent.lifecycleScope.launch {
            while (remainMills > 0) {
                val remainSecs = (remainMills + 500) / 1000
                button.text = "$oldText ($remainSecs)"
                val delayDuration = min(1000, remainMills)
                delay(delayDuration)
                remainMills -= delayDuration
            }
            button.isEnabled = false
        }
    }
}