package com.thelazypeople.scribbl.joinRoom

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
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
import com.thelazypeople.scribbl.model.Value
import kotlinx.android.synthetic.main.activity_room_list.*

class RoomListActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var roomReference: DatabaseReference
    private lateinit var roomList: ArrayList<Value>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_list)

        database = Firebase.database
        roomReference = database.reference.child("rooms")

        roomList = ArrayList<Value>()

        roomReference.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onCancelled(error: DatabaseError) {
                Log.i("", "")
            }

            override fun onDataChange(snapshot: DataSnapshot) {

                for (ds in snapshot.children) {
                    val room = ds.getValue<Value>()
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
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("reference", roomList[position].reference)
        startActivity(intent)
        Toast.makeText(this, "Room joined", Toast.LENGTH_SHORT).show()
    }
}
