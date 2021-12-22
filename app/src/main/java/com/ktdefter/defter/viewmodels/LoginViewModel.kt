package com.ktdefter.defter.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

import com.ktdefter.defter.R
import com.ktdefter.defter.data.BookmarksRepository
import com.ktdefter.defter.ui.login.LoggedInUserView
import com.ktdefter.defter.ui.login.LoginFormState
import com.ktdefter.defter.ui.login.LoginResult
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(val bookmarksRepository: BookmarksRepository) :
    ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    private val _signupResult = MutableLiveData<LoginResult>()
    val signupResult: LiveData<LoginResult> = _signupResult

    fun signup(username: String, password: String) {
        Firebase.auth.createUserWithEmailAndPassword(username, password)
            .addOnSuccessListener {
                Timber.d("signing in complete for username: $username, user id: ${Firebase.auth.currentUser!!.uid}")
                _signupResult.value =
                    LoginResult(success = it.user!!.email?.let { it1 ->
                        LoggedInUserView(
                            displayName = it1
                        )
                    })
            }
            .addOnFailureListener {
                Timber.d("signing is failed for username: $username")
                _signupResult.value = LoginResult(error = R.string.signup_failed)
            }
    }

    fun login(username: String, password: String) {
        // can be launched in a separate asynchronous job
        Firebase.auth.signInWithEmailAndPassword(username, password)
            .addOnCompleteListener {
                // save login token for other later
                if (it.isSuccessful) {
                    Timber.d("authentication is complete user id: ${Firebase.auth.currentUser!!.uid}")
                    _loginResult.value =
                        LoginResult(success = it.result.user!!.email?.let { it1 ->
                            LoggedInUserView(
                                displayName = it1
                            )
                        })
//                        )
                } else {
                    Timber.d("authentication is failed")

                    _loginResult.value = LoginResult(error = R.string.login_failed)

                }
            }

    }

    fun logout() {
        Firebase.auth.signOut()
        bookmarksRepository.resetLastSync()
    }


    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains("@")) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}