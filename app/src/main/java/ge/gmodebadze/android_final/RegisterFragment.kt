package ge.gmodebadze.android_final

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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

        binding.loginRedirectButton.setOnClickListener {
            showToast("Back to Sign In Page")
        }
    }

    private fun observeViewModel() {
        viewModel.registrationResult.observe(viewLifecycleOwner) { result ->
            showToast(result)
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