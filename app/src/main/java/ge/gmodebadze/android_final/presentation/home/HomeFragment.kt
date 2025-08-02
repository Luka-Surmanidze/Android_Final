package ge.gmodebadze.android_final.presentation.home

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ge.gmodebadze.android_final.R
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
        setupScrollBehavior()
    }

    private fun setupRecyclerView() {
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.chatRecyclerView.adapter = ChatAdapter(getDummyChats()) { chatName ->
            Toast.makeText(requireContext(), "Clicked on $chatName", Toast.LENGTH_SHORT).show()
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun setupScrollBehavior() {
        var isBottomBarVisible = true

        binding.chatRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var totalDy = 0

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                totalDy += dy

                val currentHeight = binding.topBar.root.layoutParams.height
                val newHeight = (currentHeight - dy / 3).coerceIn(dpToPx(100), dpToPx(160))

                val layoutParams = binding.topBar.root.layoutParams
                layoutParams.height = newHeight
                binding.topBar.root.layoutParams = layoutParams

                if (dy > 10 && isBottomBarVisible) {
                    hideBottomBar()
                    isBottomBarVisible = false
                } else if (dy < -10 && !isBottomBarVisible) {
                    showBottomBar()
                    isBottomBarVisible = true
                }
            }
        })
    }

    private fun hideBottomBar() {
        binding.bottomBar.bottomNav.animate()
            .translationY(binding.bottomBar.bottomNav.height.toFloat())
            .setDuration(200)
            .start()

        binding.bottomBar.fabUsers.animate()
            .translationY(40f)
            .setDuration(200).start()
    }

    private fun showBottomBar() {
        binding.bottomBar.bottomNav.animate()
            .translationY(0f)
            .setDuration(200)
            .start()

        binding.bottomBar.fabUsers.animate()
            .translationY(0f)
            .setDuration(200).start()
    }

    private fun setupBottomNavigation() {
        binding.bottomBar.bottomNav.setOnItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                R.id.menu_home -> {
                    true
                }
                R.id.menu_profile -> {
                    findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
                    true
                }
                else -> false
            }

        }

        binding.bottomBar.bottomNav.selectedItemId = R.id.menu_home
    }

    private fun setupFloatingActionButton() {
        binding.bottomBar.fabUsers.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_userSearchFragment)
        }
    }

    private fun getDummyChats(): List<ChatItem> {
        val now = System.currentTimeMillis()
        return listOf(
            ChatItem("Givi Modebadze", "Working on the homework", now - 3 * 60 * 1000),
            ChatItem("Ana", "Check this out!", now - 2 * 60 * 60 * 1000),
            ChatItem("Luka", "😂😂😂", now - 25 * 60 * 60 * 1000),
            ChatItem("Nino", "Done with the task!", now - 10 * 60 * 1000),
            ChatItem("Saba", "See you soon", now - 4 * 24 * 60 * 60 * 1000),
        )
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
