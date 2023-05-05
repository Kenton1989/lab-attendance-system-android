package sg.edu.ntu.scse.labattendancesystem.ui.main

import android.app.AlertDialog
import android.util.Log
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
    private val highlightCompulsory: Boolean = true,
    private val onUndoCheckIn: ((a: Attendance) -> Any)? = null,
    private val onCheckIn: (a: Attendance) -> Any,
) {
    companion object {
        val TAG: String = CheckInTable::class.java.simpleName
    }

    init {
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        val adapter = CheckInTableItemAdapter(
            parent.requireContext(), hideSeat, highlightCompulsory, ::onRowClicked
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
        Log.d(TAG, "checking in")
        val builder = AlertDialog.Builder(parent.requireContext())
        val username = attendance.attender.username
        val name = attendance.attender.displayName
        builder.setMessage("Check-in for:\n$name\n(username: $username)?").setTitle("Confirm?")
            .setPositiveButton("YES") { _, _ -> onCheckIn(attendance) }
            .setNegativeButton("NO") { _, _ -> }
        val dialog = builder.create()
        dialog.show()
        freezeDialogPositiveButton(dialog)
    }

    private fun handleUndoCheckIn(attendance: Attendance) {
        Log.d(TAG, "undo checking in")
        if (onUndoCheckIn == null) return

        val builder = AlertDialog.Builder(parent.requireContext())
        val username = attendance.attender.username
        val name = attendance.attender.displayName
        builder.setTitle("Confirm?")
            .setMessage("Undo check-in for:\n$name\n(username: $username)?")
            .setPositiveButton("YES") { _, _ -> onUndoCheckIn.invoke(attendance) }
            .setNegativeButton("NO") { _, _ -> }
        val dialog = builder.create()
        dialog.show()
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
            button.text = oldText
            button.isEnabled = true
        }
    }
}