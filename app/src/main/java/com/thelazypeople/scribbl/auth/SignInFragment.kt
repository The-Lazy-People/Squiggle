package com.thelazypeople.scribbl.auth

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.thelazypeople.scribbl.MainActivity
import com.thelazypeople.scribbl.R
import kotlinx.android.synthetic.main.fragment_sign_in.*

/** [SignInFragment] is called to Sign-In users by verifying authentication from Firebase Authentication. */
class SignInFragment : Fragment() {

    lateinit var frameView: FrameLayout
    private val auth = Firebase.auth
    private lateinit var prefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        frameView = activity?.findViewById(R.id.auth_view)!!
        return inflater.inflate(R.layout.fragment_sign_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /** [SignUpFragment] is called to switch to Sign Up users. */
        sign_in_signup.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(frameView.id, SignUpFragment())
                ?.commit()
        }
        prefs = context!!.getSharedPreferences(
            getString(R.string.packageName), Context.MODE_PRIVATE
        )

        val userName: String? =
            prefs.getString(getString(R.string.userName), getString(R.string.EMPTY))
        if (userName != getString(R.string.EMPTY)) {
            name_login.setText(userName)
        }

        /** Called when user press on Sign In button.
         *  First the fields are validated and then authenticated with Firebase and if it matches, User is Logged In. */
        login_btn.setOnClickListener {
            val email = email_login.text.toString().trim()
            val password = pass_login.text.toString().trim()

            if (TextUtils.isEmpty(name_login.text.toString().trim())) {
                name_login.error = getString(R.string.enterNickname)
            } else if (TextUtils.isEmpty(email)) {
                email_login.error = getString(R.string.enterEmail)
            } else if (TextUtils.isEmpty(password) || password.length < 5) {
                pass_login.error = getString(R.string.passwordLengthSmall)
            } else {
                login_btn.isEnabled = false
                login_btn.isClickable = false
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task: Task<AuthResult> ->
                        if (task.isSuccessful) {
                            // Sign In successful. Store the USERNAME and UUID in local preference and then user is logged In.
                            prefs.edit().putString(
                                getString(R.string.userName),
                                name_login.text.toString().trim()
                            ).apply()
                            prefs.edit().putString(
                                getString(R.string.userId),
                                FirebaseAuth.getInstance().currentUser?.uid.toString()
                            ).apply()
                            startActivity(Intent(context, MainActivity::class.java))
                            activity?.finish()

                        } else {
                            // Sign In failed. Error message displayed.
                            Toast.makeText(
                                context, getString(R.string.authFailed),
                                Toast.LENGTH_SHORT
                            ).show()
                            login_btn.isEnabled = true
                            login_btn.isClickable = true

                        }
                    }
            }
        }
    }

}
