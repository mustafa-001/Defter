package com.ktdefter.defter.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ktdefter.defter.data.LoginRepository
import com.ktdefter.defter.data.Result

import com.ktdefter.defter.R
import com.ktdefter.defter.data.FirestoreSync
import timber.log.Timber
import java.util.*

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(username: String, password: String) {
        // can be launched in a separate asynchronous job
        val result = loginRepository.login(username, password)

        Firebase.auth.signInWithEmailAndPassword("mustafaalimutlu@googlemail.com", "qwerty")
        .addOnCompleteListener {
            // save login token for other later
//            if (it.isSuccessful) {
//                Timber.d("authentication is complete user id: ${Firebase.auth.currentUser!!.uid}")
//                val firestoreSync = FirestoreSync(
//                    this,
//                    Firebase.firestore,
//                    Firebase.auth.currentUser!!
//                )
//                firestoreSync.sync(
//                    Date(
//                        context.getSharedPreferences("SyncSettings", 0).getLong(
//                            "lastModificationTime",
//                            0
//                        )
//                    )
//                )
//
//                context.getSharedPreferences(
//                    "SyncSettings", 0
//                ).edit()
//                    .putLong("lastModificationTime", Date().time).apply()
            } else {
                Timber.d("authentication is failed")

            }
        }
        irebase.auth.signInWithEmailAndPassword("mustafaalimutlu@googlemail.com", "qwerty")

        if (result is Result.Success) {
            _loginResult.value =
                LoginResult(success = LoggedInUserView(displayName = result.data.displayName))
        } else {
            _loginResult.value = LoginResult(error = R.string.login_failed)
        }
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