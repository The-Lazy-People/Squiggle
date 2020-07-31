package com.thelazypeople.scribbl

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.thelazypeople.scribbl.auth.AuthActivity
import com.thelazypeople.scribbl.joinRoom.RoomListActivity
import com.thelazypeople.scribbl.model.playerInfo
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    val auth = Firebase.auth
    private lateinit var prefs: SharedPreferences
    private lateinit var database: FirebaseDatabase
    private lateinit var roomReference: DatabaseReference
    private lateinit var playerReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        logout.setOnClickListener {
            Firebase.auth.signOut()
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }

        database = Firebase.database
        roomReference = database.reference.child("rooms")
        prefs = this.getSharedPreferences(
            getString(R.string.packageName), Context.MODE_PRIVATE
        )
        playerReference=database.reference.child("players")
        val playerInfoKeeper=playerInfo(prefs.getString(getString(R.string.userName), "EMPTY"),prefs.getString(getString(R.string.userId), "EMPTY"))
        playerReference.child(prefs.getString(getString(R.string.userId), "EMPTY")!!).setValue(playerInfoKeeper)
        passwordSwitchButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                closedRoomPassword.visibility = View.VISIBLE
                passwordSwitchInfo.text = getString(R.string.closedRoomText)
            } else {
                closedRoomPassword.visibility = View.GONE
                passwordSwitchInfo.text = getString(R.string.openRoomText)
            }
        }

        joinRoom.setOnClickListener {
            val intent = Intent(this, RoomListActivity::class.java)
            startActivity(intent)
        }

        createRoom.setOnClickListener {
            if (roomName.text.isNullOrEmpty() || passwordSwitchButton.isChecked && closedRoomPassword.text.isNullOrEmpty()) {
                Toast.makeText(this, getString(R.string.fieldEmpty), Toast.LENGTH_LONG).show()
            } else {
                val userId : String? = prefs.getString(getString(R.string.userId), "EMPTY")
                val userName : String? = prefs.getString(getString(R.string.userName), "EMPTY")
                if(userId != "EMPTY") {
                    roomReference.child(userId.toString()).child("roomname").setValue(roomName.text.toString())
                    roomReference.child(userId.toString()).child("reference").setValue(userId.toString())
                    roomReference.child(userId.toString()).child("Players").child(userId.toString()).setValue(playerInfo(userName,userId))
                    if (passwordSwitchButton.isChecked) {
                        roomReference.child(userId.toString()).child("password")
                            .setValue(closedRoomPassword.text.toString())
                    } else {
                        roomReference.child(userId.toString()).child("password").setValue(getString(R.string.NO))
                    }
                    createAndJoinRoom(userId.toString())
                }else{
                    Toast.makeText(this, getString(R.string.userNotFoundError), Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun createAndJoinRoom(userId : String){
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("reference", userId)
        startActivity(intent)
        Toast.makeText(this, "Room created", Toast.LENGTH_SHORT).show()
    }
}