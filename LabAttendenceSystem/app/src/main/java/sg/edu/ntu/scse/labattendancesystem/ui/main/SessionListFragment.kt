package sg.edu.ntu.scse.labattendancesystem.ui.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
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

    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory(requireActivity().application)
    }

    private lateinit var sessionItemAdapter: SessionListItemAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.activeSessionList.observe(viewLifecycleOwner) {
            Log.d(TAG, if (it != null) "loaded: ${it.size}" else "loading session list")
            sessionItemAdapter.sessions = it ?: listOf()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_session_list,
            container,
            false
        )
        binding.lifecycleOwner = viewLifecycleOwner

        sessionItemAdapter = SessionListItemAdapter(
            requireContext(),
            ::onSessionSelected,
        )

        binding.sessionListRecycler.adapter = sessionItemAdapter

        return binding.root
    }

    private fun onSessionSelected(session: Session) {
        viewModel.updateCurrentSession(session)
        Toast.makeText(requireContext(), "selected ${session.id}", Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}