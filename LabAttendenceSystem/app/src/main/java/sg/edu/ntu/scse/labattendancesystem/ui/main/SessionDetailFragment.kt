package sg.edu.ntu.scse.labattendancesystem.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import sg.edu.ntu.scse.labattendancesystem.R
import sg.edu.ntu.scse.labattendancesystem.databinding.FragmentSessionDetailBinding
import sg.edu.ntu.scse.labattendancesystem.viewmodels.ViewModelFactory
import sg.edu.ntu.scse.labattendancesystem.viewmodels.main.MainViewModel

class SessionDetailFragment : Fragment() {
    private var _binding: FragmentSessionDetailBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels {
        ViewModelFactory(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_session_detail, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.selectedSession.observe(viewLifecycleOwner) { session ->
            if (session == null) {
                binding.title.text = getString(R.string.no_session_detail_title)
                binding.mainScrollView.visibility = View.GONE
            } else {
                binding.title.text = getString(
                    R.string.session_detail_title,
                    session.group.name,
                    session.group.course.code,
                    session.group.course.title,
                )
                binding.mainScrollView.visibility = View.VISIBLE
            }
            binding.mainScrollView.smoothScrollTo(0, 0)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    companion object {
        private const val MAX_TITLE_LENGTH = 45
    }
}