package com.thelazypeople.scribbl

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.thelazypeople.scribbl.auth.AuthActivity
import com.thelazypeople.scribbl.joinRoom.RoomListActivity
import com.thelazypeople.scribbl.model.playerInfo
import com.thelazypeople.scribbl.model.roomInfo
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.create_room_dialog.*
import kotlinx.android.synthetic.main.create_room_dialog.close_btn
import kotlinx.android.synthetic.main.dialog_about_us.*


class MainActivity : AppCompatActivity() {
    val auth = Firebase.auth
    private lateinit var prefs: SharedPreferences
    private lateinit var database: FirebaseDatabase
    private lateinit var roomReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        logout.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            with(builder) {
                setMessage(R.string.logOut_alert)
                setPositiveButton("Yes") { dialogInterface, i ->
                    Firebase.auth.signOut()
                    startActivity(Intent(baseContext, AuthActivity::class.java))
                    finish()
                }
                setNeutralButton("Cancel") { dialogInterface, i ->
                }
                show()
            }
        }

        database = Firebase.database
        roomReference = database.reference.child(getString(R.string.rooms))
        prefs = this.getSharedPreferences(
            getString(R.string.packageName), Context.MODE_PRIVATE
        )

        join_here.setOnClickListener {
            val intent = Intent(this, RoomListActivity::class.java)
            startActivity(intent)
        }

        click_create_room.setOnClickListener {
            createRoom()
        }

        info.setOnClickListener {
            AboutUs()
        }
    }

    private fun AboutUs() {

        val mDialog = Dialog(this)
        mDialog.setContentView(R.layout.dialog_about_us)
        val window = mDialog.window
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        mDialog.show()

        mDialog.thelazypeople.movementMethod = LinkMovementMethod.getInstance()
        mDialog.adarsh.movementMethod = LinkMovementMethod.getInstance()
        mDialog.abhishek.movementMethod = LinkMovementMethod.getInstance()
        mDialog.ayushi.movementMethod = LinkMovementMethod.getInstance()
        mDialog.sayantan.movementMethod = LinkMovementMethod.getInstance()

        mDialog.close_btn2.setOnClickListener {
            mDialog.dismiss()
        }

    }

    private fun createRoom() {
        val mDialog = Dialog(this)
        mDialog.setContentView(R.layout.create_room_dialog)
        val window = mDialog.window
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        //mDialog.setCanceledOnTouchOutside(false) // prevent dialog box from getting dismissed on outside touch
        //mDialog.setCancelable(false)  //prevent dialog box from getting dismissed on back key pressed
        mDialog.show()

        mDialog.close_btn.setOnClickListener {
            mDialog.dismiss()
        }

        mDialog.password_switch_button.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                mDialog.closed_room_password.visibility = View.VISIBLE
                mDialog.passwordSwitchInfo.text = getString(R.string.closedRoomText)
            } else {
                mDialog.closed_room_password.visibility = View.GONE
                mDialog.passwordSwitchInfo.text = getString(R.string.openRoomText)
            }
        }

        mDialog.create_room_go.setOnClickListener {
            if (mDialog.room_name.text.isNullOrEmpty() || mDialog.password_switch_button.isChecked && mDialog.closed_room_password.text.isNullOrEmpty()) {
                Toast.makeText(this, getString(R.string.fieldEmpty), Toast.LENGTH_LONG).show()
            } else {
                val userId: String? =
                    prefs.getString(getString(R.string.userId), getString(R.string.EMPTY))
                val userName: String? =
                    prefs.getString(getString(R.string.userName), getString(R.string.EMPTY))
                if (userId != getString(R.string.EMPTY)) {
                    val referenceUuidPlusTimestamp =
                        userId.toString() + System.currentTimeMillis().toString()
                    database.reference.child(getString(R.string.drawingData))
                        .child(userId.toString()).removeValue()
                    if (mDialog.password_switch_button.isChecked) {
                        uploadRoomInfo(
                            referenceUuidPlusTimestamp,
                            mDialog.closed_room_password.text.toString(),
                            mDialog.room_name.text.toString().trim(),
                            userId.toString(),
                            userName.toString()
                        )
                    } else {
                        uploadRoomInfo(
                            referenceUuidPlusTimestamp,
                            getString(R.string.NO),
                            mDialog.room_name.text.toString().trim(),
                            userId.toString(),
                            userName.toString()
                        )
                    }
                    createAndJoinRoom(referenceUuidPlusTimestamp)
                } else {
                    Toast.makeText(this, getString(R.string.userNotFoundError), Toast.LENGTH_SHORT)
                        .show()
                }
            }

        }
    }
      private fun createAndJoinRoom(referenceUuidPlusTimestamp: String) {
       val intent = Intent(this, WaitingActivity::class.java)
       intent.putExtra(getString(R.string.reference), referenceUuidPlusTimestamp)
       intent.putExtra(getString(R.string.host), 1)
       startActivity(intent)
       finish()
       Toast.makeText(this, getString(R.string.roomCreated), Toast.LENGTH_SHORT).show()
   }

   private fun uploadRoomInfo(
       referenceUuidPlusTimestamp: String,
       password: String,
       roomname: String,
       userId: String,
       userName: String
   ) {
       val roomInfo = roomInfo(0, password, referenceUuidPlusTimestamp, roomname)
       roomReference.child(referenceUuidPlusTimestamp).child(getString(R.string.info))
           .setValue(roomInfo)
       roomReference.child(referenceUuidPlusTimestamp).child(getString(R.string.Players))
           .child(userId).setValue(playerInfo(userName, 0, userId))
       roomReference.child(referenceUuidPlusTimestamp).child(getString(R.string.server))
           .setValue(userId)
   }
}
