package com.thelazypeople.scribbl.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.thelazypeople.scribbl.MainActivity
import com.thelazypeople.scribbl.R

/** [AuthActivity.kt] is called to authenticate users with Firebase Authentication. */
class AuthActivity : AppCompatActivity() {

    private val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        /** If user is already logged-in, call [MainActivity.kt]. */
        if (mAuth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            /** Else redirect user to Sign In! */
            supportFragmentManager.beginTransaction()
                .replace(R.id.auth_view, SignInFragment()).commit()
        }
    }
}
