package ge.gmodebadze.android_final.presentation.profile

import ProfileViewModel
import android.app.AlertDialog
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import ge.gmodebadze.android_final.R
import ge.gmodebadze.android_final.databinding.FragmentProfileBinding
import ge.gmodebadze.android_final.domain.model.User
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.RequestListener

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ProfileViewModel
    private var currentProfileImageUrl: String = ""

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
                    viewModel.updateProfile(nickname, profession, currentProfileImageUrl)
                }
            }
        }

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }

        binding.ivProfileImage.setOnClickListener {
//            showToast("Image picker not implemented yet")
            showProfileImageUrlDialog()
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

    private fun showProfileImageUrlDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Profile Picture URL")

        val input = EditText(requireContext())
        input.hint = "Enter image URL"
        input.setText(currentProfileImageUrl)
        builder.setView(input)

        builder.setPositiveButton("OK") { _, _ ->
            val imageUrl = input.text.toString().trim()
            currentProfileImageUrl = imageUrl
            loadProfileImage(imageUrl)
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.setNeutralButton("Remove") { _, _ ->
            currentProfileImageUrl = ""
            loadProfileImage("")
        }

        builder.show()
    }

    private fun loadProfileImage(imageUrl: String) {
        Glide.with(requireContext())
            .load(imageUrl)
            .placeholder(R.drawable.avatar_image_placeholder)
            .error(R.drawable.avatar_image_placeholder)
            .circleCrop()
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.e("Glide", "Load failed", e)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.d("Glide", "Image loaded successfully")
                    return false
                }
            })
            .into(binding.ivProfileImage)


        Log.d("DEBUG", "Loading image from: $imageUrl")
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.btnUpdateProfile.isEnabled = !isLoading
        binding.etNickname.isEnabled = !isLoading
        binding.etProfession.isEnabled = !isLoading
        binding.btnLogout.isEnabled = !isLoading

        binding.ivProfileImage.isEnabled = !isLoading

        binding.btnUpdateProfile.text = if (isLoading) "Updating..." else "Update Profile"

        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun bindUserData(user: User) {
        binding.apply {
            etNickname.setText(user.nickname)
            etProfession.setText(user.profession)
            currentProfileImageUrl = user.profileImageUrl

            loadProfileImage(user.profileImageUrl)
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