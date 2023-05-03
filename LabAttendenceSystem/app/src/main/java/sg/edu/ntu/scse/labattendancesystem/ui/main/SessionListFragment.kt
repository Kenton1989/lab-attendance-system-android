package sg.edu.ntu.scse.labattendancesystem.ui.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import sg.edu.ntu.scse.labattendancesystem.R
import sg.edu.ntu.scse.labattendancesystem.databinding.FragmentSessionListBinding
import sg.edu.ntu.scse.labattendancesystem.domain.models.Session
import sg.edu.ntu.scse.labattendancesystem.viewmodels.ViewModelFactory
import sg.edu.ntu.scse.labattendancesystem.viewmodels.main.MainViewModel

class SessionListFragment : Fragment() {
    companion object {
        private val TAG: String = SessionListFragment::class.java.simpleName
    }

    private var _binding: FragmentSessionListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels {
        ViewModelFactory(requireActivity().application)
    }

    private lateinit var sessionItemAdapter: SessionListItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_session_list, container, false
        )
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionItemAdapter = SessionListItemAdapter(
            requireContext(),
            viewModel,
            ::onSessionSelected,
        )
        binding.sessionListRecycler.adapter = sessionItemAdapter

        Log.d(TAG, "Watching activeSessionList")
        viewModel.activeSessionList.observe(viewLifecycleOwner) {
            Log.d(TAG, if (it != null) "loaded: ${it.size}" else "loading session list")

            sessionItemAdapter.sessions = it ?: listOf()

            binding.loadingActiveSessionHint.visibility = View.GONE
            if (sessionItemAdapter.sessions.isEmpty()) {
                binding.noActiveSessionHint.visibility = View.VISIBLE
                binding.sessionListRecycler.visibility = View.GONE
            } else {
                binding.noActiveSessionHint.visibility = View.GONE
                binding.sessionListRecycler.visibility = View.VISIBLE
            }
        }
    }


    private fun onSessionSelected(session: Session) {
        val msg = with(session) {
            "selected $id ${group.course.code} ${group.name}"
        }
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}