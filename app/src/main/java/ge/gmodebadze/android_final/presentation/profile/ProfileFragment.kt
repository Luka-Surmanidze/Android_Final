package ge.gmodebadze.android_final.presentation.profile

import ProfileViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import ge.gmodebadze.android_final.R
import ge.gmodebadze.android_final.databinding.FragmentProfileBinding
import ge.gmodebadze.android_final.domain.model.User
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        setupClickListeners()
        observeViewModel()
        setupBottomNavigation()
        viewModel.loadUserProfile()
    }

    private fun setupBottomNavigation() {
        binding.bottomBar.bottomNav.setOnItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                R.id.menu_home -> {
                    findNavController().navigate(R.id.action_profileFragment_to_homeFragment)
                    true
                }
                R.id.menu_profile -> {
                    true
                }
                else -> false
            }
        }

        binding.bottomBar.bottomNav.selectedItemId = R.id.menu_profile
    }

    private fun setupClickListeners() {
        binding.btnUpdateProfile.setOnClickListener {
            val nickname = binding.etNickname.text.toString().trim()
            val profession = binding.etProfession.text.toString().trim()

            when {
                nickname.isEmpty() -> {
                    showToast("Please enter nickname")
                }
                profession.isEmpty() -> {
                    showToast("Please enter profession")
                }
                else -> {
                    viewModel.updateProfile(nickname, profession)
                }
            }
        }

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
        }

        binding.ivProfileImage.setOnClickListener {
            showToast("Image picker not implemented yet")
            // TODO: Implement image picker
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            setLoadingState(isLoading)
        }

        viewModel.profileState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ProfileViewModel.ProfileState.Success -> {
                    binding.contentLayout.visibility = View.VISIBLE
                    bindUserData(state.user)
                }
                is ProfileViewModel.ProfileState.Error -> {
                    binding.contentLayout.visibility = View.VISIBLE
                    showToast(state.message)
                }
                is ProfileViewModel.ProfileState.Loading -> {
                    binding.contentLayout.visibility = View.GONE
                }
                ProfileViewModel.ProfileState.Idle -> {
                    // Initial state, do nothing
                }

                else -> {}
            }
        }

        viewModel.updateState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ProfileViewModel.UpdateState.Success -> {
                    showToast("Profile updated successfully!")
                }
                is ProfileViewModel.UpdateState.Error -> {
                    showToast(state.message)
                }
                is ProfileViewModel.UpdateState.Loading -> {
                    // Loading state handled by isLoading observer
                }
                ProfileViewModel.UpdateState.Idle -> {
                    // Initial state, do nothing
                }

                else -> {}
            }
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.btnUpdateProfile.isEnabled = !isLoading
        binding.etNickname.isEnabled = !isLoading
        binding.etProfession.isEnabled = !isLoading
        binding.btnLogout.isEnabled = !isLoading

        binding.btnUpdateProfile.text = if (isLoading) "Updating..." else "Update Profile"

        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun bindUserData(user: User) {
        binding.apply {
            etNickname.setText(user.nickname)
            etProfession.setText(user.profession)

            // Load profile image with Glide
            Glide.with(requireContext())
                .load(user.profileImageUrl.ifEmpty { null })
                .placeholder(R.drawable.avatar_image_placeholder)
                .error(R.drawable.avatar_image_placeholder)
                .circleCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(ivProfileImage)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}