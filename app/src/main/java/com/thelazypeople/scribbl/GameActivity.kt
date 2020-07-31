package com.thelazypeople.scribbl

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.thelazypeople.scribbl.model.ChatText
import com.thelazypeople.scribbl.model.playerInfo
import kotlinx.android.synthetic.main.activity_game.*
import java.lang.ref.Reference

class GameActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences
    private lateinit var database: DatabaseReference
    private lateinit var postReference: DatabaseReference
    private lateinit var playerReference: DatabaseReference
    private lateinit var childEventListenerForChat: ChildEventListener
    private lateinit var childEventListenerForPlayers: ChildEventListener
    private var reference: String? = ""
    private var otherUserName:String?=""
    private var downloadText: String = ""
    private var playersList= mutableListOf<playerInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        prefs = this.getSharedPreferences(
            getString(R.string.packageName), Context.MODE_PRIVATE
        )
        val intent: Intent = intent
        reference = intent.getStringExtra("reference")

        database = Firebase.database.reference
        if (reference != null) {
            postReference = database.child("rooms").child(reference.toString()).child("Chats")

            childEventListenerForChat = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val textObj = snapshot.getValue<ChatText>()

                    otherUserName=textObj?.userName
                    downloadText +=otherUserName+" : "+textObj?.text + "\n"
                    textHolder.text =downloadText
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
                    // not needed
                }

            }
            postReference.addChildEventListener(childEventListenerForChat)


            playerReference = database.child("rooms").child(reference.toString()).child("Players")

            childEventListenerForPlayers = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val playerInfoObj = snapshot.getValue<playerInfo>()
                    if(playerInfoObj!=null) {
                        playersList.add(playerInfoObj)
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
                    // not needed
                }

            }
            playerReference.addChildEventListener(childEventListenerForPlayers)
        }

        button.setOnClickListener {
            if (editText.text.toString() == "") {
                Toast.makeText(this, "Empty text", Toast.LENGTH_SHORT).show()
            } else {
                if(reference != null){
                    uploadToDatabase(editText.text.toString().trim())
                    editText.text.clear()
                }else{
                    Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun uploadToDatabase(cur_text: String) {
        val userName : String? = prefs.getString(getString(R.string.userName), "EMPTY")
        val textObj = ChatText(cur_text,userName!!)
        postReference.push().setValue(textObj)
    }
}