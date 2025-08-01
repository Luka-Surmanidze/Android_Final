package ge.gmodebadze.android_final.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import ge.gmodebadze.android_final.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupBottomNavigation()
        setupFloatingActionButton()
    }

    private fun setupRecyclerView() {
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.chatRecyclerView.adapter = ChatAdapter(getDummyChats()) { chatName ->
            Toast.makeText(requireContext(), "Clicked on $chatName", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNavigation() {
        // TODO
    }

    private fun setupFloatingActionButton() {
        // TODO
    }

    private fun getDummyChats(): List<String> {
        return listOf("Alice", "Bob", "Charlie", "Diana", "Eve")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
