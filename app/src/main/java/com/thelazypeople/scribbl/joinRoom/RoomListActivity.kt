package com.thelazypeople.scribbl.joinRoom

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.thelazypeople.scribbl.GameActivity
import com.thelazypeople.scribbl.R
import com.thelazypeople.scribbl.model.playerInfo
import com.thelazypeople.scribbl.model.roomInfo
import kotlinx.android.synthetic.main.activity_room_list.*

class RoomListActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var roomReference: DatabaseReference
    private lateinit var roomList: ArrayList<roomInfo>
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_list)

        database = Firebase.database
        roomReference = database.reference.child("rooms")
        prefs = this.getSharedPreferences(
            getString(R.string.packageName), Context.MODE_PRIVATE
        )

        roomList = ArrayList<roomInfo>()
        roomReference.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onCancelled(error: DatabaseError) {
                Log.i("", "")
            }

            override fun onDataChange(snapshot: DataSnapshot) {

                for (ds in snapshot.children) {
                    val room = ds.getValue<roomInfo>()
                    roomList.add(room!!)
                }
                val adapter =
                    JoinRoomAdapter(
                        this@RoomListActivity,
                        R.layout.list_item_row,
                        roomList
                    )
                listView.adapter = adapter
            }
        })

        listView.setOnItemClickListener { parent, view, position, id ->
            if (roomList[position].password == getString(R.string.NO)) {
                joinRoom(position)
            } else {
                checkPassword(position)
            }
        }
    }

    private fun checkPassword(position: Int) {
        val layoutInflater =
            this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = layoutInflater.inflate(R.layout.dialog_password, null)
        val alertDialog = AlertDialog.Builder(this)
            .setPositiveButton("ENTER", null)
            .create()
        alertDialog.setTitle("Insert the password")
        alertDialog.setIcon(R.drawable.ic_baseline_lock_24)

        val edtGroupName = view.findViewById<View>(R.id.edt_groupName) as EditText
        alertDialog.setOnShowListener {
            val button =
                (alertDialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                val passEntered = edtGroupName.text.toString()
                if (passEntered.isEmpty()) {
                    Toast.makeText(this, "No password found", Toast.LENGTH_SHORT).show()
                } else {
                    if (passEntered == roomList[position].password) {
                        joinRoom(position)
                    }else{
                        Toast.makeText(this, "Password incorrect", Toast.LENGTH_SHORT).show()
                    }
                    alertDialog.dismiss()
                }
            }
        }
        alertDialog.setView(view)
        alertDialog.show()
    }

    private fun joinRoom(position: Int){
        val userId : String? = prefs.getString(getString(R.string.userId), "EMPTY")
        val userName : String? = prefs.getString(getString(R.string.userName), "EMPTY")
        if(userId != "EMPTY") {
            roomReference.child(roomList[position].reference).child("Players").child(userId.toString()).setValue(
                playerInfo(userName,userId)
            )
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("reference", roomList[position].reference)
            startActivity(intent)
            Toast.makeText(this, "Room joined", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this, getString(R.string.userNotFoundError), Toast.LENGTH_SHORT).show()
        }
    }
    override fun onPause() {
        super.onPause()
        val userId: String? = prefs.getString(getString(R.string.userId), "EMPTY")
        if (userId != "EMPTY") {
            database.reference.child("players").child(userId!!).removeValue()
        }
    }

    override fun onResume() {
        super.onResume()
        val userId: String? = prefs.getString(getString(R.string.userId), "EMPTY")
        val useName: String? = prefs.getString(getString(R.string.userName), "EMPTY")
        if (userId != "EMPTY") {
            database.reference.child("players").child(userId.toString()).setValue(playerInfo(useName,userId))
        }
    }
}
