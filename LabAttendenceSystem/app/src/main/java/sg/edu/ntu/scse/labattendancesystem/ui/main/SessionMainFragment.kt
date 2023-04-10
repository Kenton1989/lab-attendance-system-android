package sg.edu.ntu.scse.labattendancesystem.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import sg.edu.ntu.scse.labattendancesystem.databinding.FragmentSessionMainBinding

class SessionMainFragment : Fragment() {

    private var _binding: FragmentSessionMainBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSessionMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val slidingPaneLayout = binding.slidingPaneLayout

        slidingPaneLayout.lockMode = SlidingPaneLayout.LOCK_MODE_LOCKED
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            SessionListOnBackPressedCallback(slidingPaneLayout)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}