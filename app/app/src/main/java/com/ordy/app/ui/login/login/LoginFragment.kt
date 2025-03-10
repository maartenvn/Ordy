package com.ordy.app.ui.login.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.ordy.app.AppPreferences
import com.ordy.app.MainActivity
import com.ordy.app.R
import com.ordy.app.api.util.ErrorHandler
import com.ordy.app.api.util.InputField
import com.ordy.app.api.util.QueryStatus
import com.ordy.app.databinding.FragmentLoginBinding
import com.ordy.app.ui.login.LoginViewModel
import com.ordy.app.util.SnackbarUtil
import kotlinx.android.synthetic.main.fragment_login.*

class LoginFragment : Fragment() {

    private val viewModel: LoginViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        inflater.inflate(R.layout.fragment_login, container, false)

        // Create binding for the fragment.
        val binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.handlers = LoginHandlers(this, viewModel)
        binding.viewModel = viewModel

        return binding.root
    }

    /**
     * Clears the data of the register-screen
     */
    private fun clearRegister() {
        viewModel.getUserNameData().value = ""
        viewModel.getEmailRegisterData().value = ""
        viewModel.getPasswordRegisterData().value = ""
        viewModel.getPasswordRepeatData().value = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        clearRegister()

        viewModel.getLoginMLD().observe(this, Observer {

            when (it.status) {

                QueryStatus.LOADING -> {
                    SnackbarUtil.openSnackbar(
                        "Attempting to login...",
                        activity
                    )
                }

                QueryStatus.SUCCESS -> {
                    SnackbarUtil.closeSnackbar(requireActivity())

                    val preferences = AppPreferences(requireContext())

                    // Store the given access token.
                    preferences.accessToken = it.requireData().accessToken

                    // Store the given user.
                    preferences.userId = it.requireData().user.id

                    // Open the main activity
                    val intent = Intent(this.context, MainActivity::class.java)
                    startActivity(intent)

                    // Finish the current activity.
                    activity?.finish()
                }

                QueryStatus.ERROR -> {
                    SnackbarUtil.closeSnackbar(requireActivity())

                    ErrorHandler().handle(
                        it.error, activity, listOf(
                            InputField("email", this.input_email),
                            InputField("password", this.input_password)
                        )
                    )
                }

                else -> {
                }
            }
        })
    }
}