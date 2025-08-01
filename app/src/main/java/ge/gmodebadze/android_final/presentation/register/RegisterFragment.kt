package ge.gmodebadze.android_final.presentation.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import ge.gmodebadze.android_final.R
import ge.gmodebadze.android_final.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RegisterViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[RegisterViewModel::class.java]

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.signUpButton.setOnClickListener {
            val nickname = binding.nicknameRegisterPage.text.toString().trim()
            val password = binding.passwordRegisterPage.text.toString().trim()
            val profession = binding.whatIDoRegisterPage.text.toString().trim()

            when {
                nickname.isEmpty() -> {
                    showToast("Please Enter Nickname")
                }
                password.isEmpty() -> {
                    showToast("Please Enter Password")
                }
                profession.isEmpty() -> {
                    showToast("Please Enter Profession")
                }
                password.length < 6 -> {
                    showToast("Password must be at least 6 characters long")
                }
                else -> {
                    viewModel.registerUser(nickname, password, profession)
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            setLoadingState(isLoading)
        }

        viewModel.registrationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RegisterViewModel.RegistrationState.Success -> {
                    showToast("Registration successful!")
                    findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
                }
                is RegisterViewModel.RegistrationState.Error -> {
                    showToast(state.message)
                }
                is RegisterViewModel.RegistrationState.Loading -> {
                    // Loading state handled by isLoading observer
                }
                RegisterViewModel.RegistrationState.Idle -> {
                    // Initial state, do nothing
                }
            }
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.signUpButton.isEnabled = !isLoading
        binding.nicknameRegisterPage.isEnabled = !isLoading
        binding.passwordRegisterPage.isEnabled = !isLoading
        binding.whatIDoRegisterPage.isEnabled = !isLoading

        binding.signUpButton.text = if (isLoading) "Registering..." else "SIGN UP"
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
