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
import com.thelazypeople.scribbl.R
import com.thelazypeople.scribbl.WaitingActivity
import com.thelazypeople.scribbl.model.playerInfo
import com.thelazypeople.scribbl.model.roomInfo
import kotlinx.android.synthetic.main.activity_room_list.*

/** [RoomListActivity] is used to display all available current Rooms where player can join. */
class RoomListActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var roomReference: DatabaseReference
    private lateinit var prefs: SharedPreferences
    private lateinit var childEventListenerForRooms: ChildEventListener

    private var roomList = mutableListOf<roomInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_list)

        database = Firebase.database
        roomReference = database.reference.child(getString(R.string.rooms))
        prefs = this.getSharedPreferences(
            getString(R.string.packageName), Context.MODE_PRIVATE
        )

        fetchingRooms()

        /** When a particular room is clicked from the list */
        listView.setOnItemClickListener { _, _, position, _ ->
            if (roomList[position].password == getString(R.string.NO)) {
                joinRoom(position)
            } else {
                checkPassword(position)
            }
        }
    }

    /** Fetching Rooms from Firebase Database */
    private fun fetchingRooms() {

        /** Created a Room Array List which would pe populated to [JoinRoomAdapter]. */
        roomList = ArrayList<roomInfo>()
        val adapter =
            JoinRoomAdapter(
                this@RoomListActivity,
                R.layout.list_item_row,
                roomList
            )
        listView.adapter = adapter

        /** Added an event listener to track addition/deletion of rooms in Firebase.
         * Correspondingly, it is added/deleted locally in the application. */

        childEventListenerForRooms = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val room = snapshot.child(getString(R.string.info)).getValue<roomInfo>()
                if (room != null) {
                    Log.i("#######", room.toString())
                    roomList.add(room)
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // not needed
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // not needed
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // not needed
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val room = snapshot.child(getString(R.string.info)).getValue<roomInfo>()
                val temp = mutableListOf<roomInfo>()
                for (i in 0 until roomList.size) {
                    if (roomList[i].reference != room?.reference) {
                        temp.add(roomList[i])
                        Log.i("AAJAJA", roomList[i].roomname)
                    }
                }
                roomList = temp
                val joinRoomAdapter = JoinRoomAdapter(
                    this@RoomListActivity,
                    R.layout.list_item_row, roomList
                )
                listView.adapter = joinRoomAdapter
            }

        }
        roomReference.addChildEventListener(childEventListenerForRooms)
    }


    /** This function is executed when a user clicks on a room which is password protected. */
    private fun checkPassword(position: Int) {
        val layoutInflater =
            this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = layoutInflater.inflate(R.layout.dialog_password, null)
        val alertDialog = AlertDialog.Builder(this)
            .setPositiveButton(getString(R.string.ENTER), null)
            .create()
        alertDialog.setTitle(getString(R.string.insertPassword))
        alertDialog.setIcon(R.drawable.ic_baseline_lock_24)

        val edtGroupName = view.findViewById<View>(R.id.edt_groupName) as EditText
        alertDialog.setOnShowListener {
            val button =
                (alertDialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                val passEntered = edtGroupName.text.toString()
                if (passEntered.isEmpty()) {
                    Toast.makeText(this, getString(R.string.noPasswordFound), Toast.LENGTH_SHORT)
                        .show()
                } else {
                    if (passEntered == roomList[position].password) {
                        joinRoom(position)
                    } else {
                        Toast.makeText(
                            this, getString(R.string.passwordIncorrect), Toast.LENGTH_SHORT
                        ).show()
                    }
                    alertDialog.dismiss()
                }
            }
        }
        alertDialog.setView(view)
        alertDialog.show()
    }


    /** This function is executed when a user clicks on particular room to join it. */
    private fun joinRoom(position: Int) {
        val userId: String? = prefs.getString(getString(R.string.userId), getString(R.string.EMPTY))
        val userName: String? =
            prefs.getString(getString(R.string.userName), getString(R.string.EMPTY))
        if (userId != getString(R.string.EMPTY)) {
            database.reference.child(getString(R.string.rooms)).child(roomList[position].reference)
                .child(getString(R.string.Players))
                .child(userId.toString()).setValue(
                    playerInfo(userName, 0, userId)
                )
            val intent = Intent(this, WaitingActivity::class.java)
            intent.putExtra(getString(R.string.reference), roomList[position].reference)
            intent.putExtra(getString(R.string.host), 0)
            startActivity(intent)
            finish()
            Toast.makeText(this, getString(R.string.roomJoined), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, getString(R.string.userNotFoundError), Toast.LENGTH_SHORT).show()
        }
    }
}
