package com.ktdefter.defter.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.ktdefter.defter.R
import com.ktdefter.defter.databinding.FragmentSignupBinding
import com.ktdefter.defter.ui.login.LoggedInUserView
import com.ktdefter.defter.viewmodels.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.app_bar_main.*

@AndroidEntryPoint
class SignupFragment : Fragment() {

    private val loginViewModel: LoginViewModel by activityViewModels()
    private var _binding: FragmentSignupBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        requireActivity().fab.hide()
        setHasOptionsMenu(true)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usernameEditText = binding.username
        val loadingProgressBar = binding.loading

        loginViewModel.loginFormState.observe(viewLifecycleOwner,
            Observer { loginFormState ->
                if (loginFormState == null) {
                    return@Observer
                }
                binding.signup.isEnabled = loginFormState.isDataValid
                loginFormState.usernameError?.let {
                    usernameEditText.error = getString(it)
                }
                loginFormState.passwordError?.let {
                    binding.passwordSignup.error = getString(it)
                    binding.passwordRepeatS.error = getString(it)
                }
            })
        loginViewModel.signupResult.observe(viewLifecycleOwner,
            Observer { signupResult ->
                signupResult.error?.let {
                    showSignupFailed(it)
                }
                signupResult.success?.let {
                    updateUiWithUser(it)
                }
            })

        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // ignore
            }

            override fun afterTextChanged(s: Editable) {
                loginViewModel.registerDataChanged(
                    usernameEditText.text.toString(),
                    binding.passwordSignup.text.toString(),
                    binding.passwordRepeatS.text.toString()

                )
            }
        }
        usernameEditText.addTextChangedListener(afterTextChangedListener)
        binding.passwordSignup.addTextChangedListener(afterTextChangedListener)
        binding.passwordRepeatS.addTextChangedListener(afterTextChangedListener)


        binding.signup.setOnClickListener {
            loadingProgressBar.visibility = View.VISIBLE
            loginViewModel.signup(
                usernameEditText.text.toString(),
                binding.passwordSignup.text.toString()
            )
        }
        binding.signupToLogin.setOnClickListener {
            findNavController().navigate(R.id.login_fragment)
        }
    }

    private fun showSignupFailed(errorString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()

    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = "Registered with account name: " + model.displayName
        // TODO : initiate successful logged in experience
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, welcome, Toast.LENGTH_LONG).show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}