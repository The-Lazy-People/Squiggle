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


class SignInFragment : Fragment() {

    lateinit var frameView:FrameLayout
    private val auth= Firebase.auth

    private lateinit var prefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        frameView = activity?.findViewById<FrameLayout>(R.id.auth_view)!!
        return inflater.inflate(R.layout.fragment_sign_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = context!!.getSharedPreferences(
            getString(R.string.packageName), Context.MODE_PRIVATE
        )

       sign_in_signup.setOnClickListener {
           activity?.supportFragmentManager?.beginTransaction()
                   ?.replace(frameView.id, SignUpFragment())
                   ?.commit()
        }
        login_btn.setOnClickListener {
            val email = email_login.text.toString().trim()
            val password = pass_login.text.toString().trim()
            if(TextUtils.isEmpty(email)){
                email_login.error = "Please enter the Email"
            }
            else if(TextUtils.isEmpty(password) || password.length < 8){
                pass_login.error = "Length greater than 8"
            }
            else {
                login_btn.isEnabled = false
                login_btn.isClickable = false
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task: Task<AuthResult> ->
                        if (task.isSuccessful) {
                            startActivity(Intent(context, MainActivity::class.java))
                            activity?.finish()
                            prefs.edit().putString(getString(R.string.userId), FirebaseAuth.getInstance().currentUser?.uid.toString()).apply();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(
                                context, "Authentication failed.",
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