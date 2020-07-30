package com.thelazypeople.scribbl.auth

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
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
import kotlinx.android.synthetic.main.fragment_sign_up.*


class SignUpFragment : Fragment() {

    lateinit var frameView: FrameLayout
    private val mAuth= Firebase.auth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        frameView = activity?.findViewById<FrameLayout>(R.id.auth_view)!!
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sign_up_signin.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()
                    ?.replace(frameView.id, SignInFragment())
                    ?.commit()
        }

        register_btn.setOnClickListener {
            val email = email_register.text.toString().trim()
            val password = pass_register.text.toString().trim()

            if(TextUtils.isEmpty(email)){
                email_register.error = "Please enter the Email"
            }
           else if(TextUtils.isEmpty(password) || password.length < 8){
                pass_register.error = "Length greater than 8"
            }
            else {
                register_btn.isEnabled = false
                register_btn.isClickable = false
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task: Task<AuthResult> ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            startActivity(Intent(context, MainActivity::class.java))
                            activity?.finish()
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(
                                context, "Authentication failed.",
                                Toast.LENGTH_SHORT
                            ).show()
                            register_btn.isEnabled = true
                            register_btn.isClickable = true
                        }
                    }
            }
        }
    }
}