package sg.edu.ntu.scse.labattendancesystem.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import sg.edu.ntu.scse.labattendancesystem.R
import sg.edu.ntu.scse.labattendancesystem.databinding.FragmentSessionDetailBinding

class SessionDetailFragment : Fragment() {
    private var _binding: FragmentSessionDetailBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_session_detail,
            container,
            false)
        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}