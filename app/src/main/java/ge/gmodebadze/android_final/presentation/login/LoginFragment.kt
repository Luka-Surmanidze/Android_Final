package ge.gmodebadze.android_final.presentation.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import ge.gmodebadze.android_final.R
import ge.gmodebadze.android_final.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.signInButton.setOnClickListener {
            val nickname = binding.nicknameLoginPage.text.toString().trim()
            val password = binding.passwordLoginPage.text.toString().trim()

            if (nickname.isEmpty() || password.isEmpty()) {
                showToast("Fill in all fields")
                return@setOnClickListener
            }

            val email = "$nickname@messenger.app"
            viewModel.login(email, password)
        }

        binding.signUpRedirectButton.setOnClickListener {
            showToast("Navigate to Register Page")
            // TODO GIVI
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            setLoadingState(isLoading)
        }

        viewModel.loginState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LoginViewModel.LoginState.Success -> {
                    showToast("Login successful!")
                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                }
                is LoginViewModel.LoginState.Error -> {
                    showToast(state.message)
                }
                is LoginViewModel.LoginState.Loading -> Unit
                LoginViewModel.LoginState.Idle -> Unit
            }
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.signInButton.isEnabled = !isLoading
        binding.nicknameLoginPage.isEnabled = !isLoading
        binding.passwordLoginPage.isEnabled = !isLoading

        binding.signInButton.text = if (isLoading) "Signing in..." else "SIGN IN"
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
