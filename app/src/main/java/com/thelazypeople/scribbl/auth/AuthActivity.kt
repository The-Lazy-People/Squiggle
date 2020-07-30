package com.thelazypeople.scribbl.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.thelazypeople.scribbl.MainActivity
import com.thelazypeople.scribbl.R

class AuthActivity : AppCompatActivity() {

    private val mAuth= FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        if (mAuth.currentUser!=null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.auth_view, SignInFragment()).commit()
    }
}