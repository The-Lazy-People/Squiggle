package com.thelazypeople.scribbl

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.thelazypeople.scribbl.adapters.ChatAdapter
import com.thelazypeople.scribbl.model.ChatText
import com.thelazypeople.scribbl.model.Information
import com.thelazypeople.scribbl.model.playerInfo
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.chats_preview.*

class GameActivity : AppCompatActivity() {
    private var serverHost: Int=0
    private var userName=""
    private var userId=""
    private lateinit var prefs: SharedPreferences
    private lateinit var database: DatabaseReference
    private lateinit var postReference: DatabaseReference
    private lateinit var playerReference: DatabaseReference
    private lateinit var childEventListenerForChat: ChildEventListener
    private lateinit var childEventListenerForPlayers: ChildEventListener
    private lateinit var childEventListenerForGame: ChildEventListener
    var reference: String? = ""
    private var otherUserName:String?=""
    private var downloadText: String = ""
    private var playersList= mutableListOf<playerInfo>()
    private var chatsDisplay = mutableListOf<ChatText>()
    var playerCount:Long=0
    private lateinit var postRef:DatabaseReference
    private val baseCount:Long=1
    private var goToMainActivityBoolean : Boolean = true
    private var backButtonPressedBoolean : Boolean = false
    private var host=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val paintView=PaintView(this)
        main.addView(paintView)

        eraser.setOnClickListener {
            if (host==1) {
                paintView.clear()
                postRef.push().setValue(Information(10001f, 10001f, 3))
                paintView.isclear = 1
            }
        }



        prefs = this.getSharedPreferences(
            getString(R.string.packageName), Context.MODE_PRIVATE
        )
        userId= prefs.getString(getString(R.string.userId), "EMPTY")!!
        userName= prefs.getString(getString(R.string.userName), "EMPTY")!!
        val intent: Intent = intent
        reference = intent.getStringExtra("reference")
        serverHost=intent.getIntExtra("host",0)
        paintView.end(0f,0f)
        paintView.getref(reference)


        val chatAdapter = ChatAdapter(chats = chatsDisplay)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        chats_recycler.layoutManager = layoutManager
        chats_recycler.adapter = chatAdapter

        database = Firebase.database.reference
        if (reference != null) {
            if (serverHost==1){
                database.child("rooms").child(reference.toString()).child("info").child("chanceChange").setValue(1)
                paintView.host=1
                host=1
            }
            postReference = database.child("games").child(reference.toString()).child("Chats")

            // Chat reference for a room
            childEventListenerForChat = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val textObj = snapshot.getValue<ChatText>()

                    otherUserName=textObj?.userName
                    downloadText +=otherUserName+" : "+textObj?.text + "\n"
                    //textHolder.text =downloadText

                    otherUserName?.let { textObj?.text?.let { it1 -> ChatText(it, it1) } }?.let { chatsDisplay.add(it) }
                    chatAdapter.notifyDataSetChanged()
                    chats_recycler.scrollToPosition(chatsDisplay.size-1)

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

            // Drawing data reference of a room
            postRef=database.child("drawingData").child(reference.toString())
            childEventListenerForGame = object : ChildEventListener{

                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val info = snapshot.getValue<Information>()
                    Log.i("DOWNLOADORNOT",info!!.type.toString()+" "+info.pointX.toString()+" "+info.pointY.toString())
                    if(info.type == 0){
                        paintView.start(info.pointX , info.pointY)
                        Toast.makeText(this@GameActivity,"Start",Toast.LENGTH_SHORT).show()
                    }else if(info.type == 2){
                        paintView.co(info!!.pointX , info.pointY)
                    }
                    else if(info.type==1){
                        paintView.end(info!!.pointX , info.pointY)
                        Toast.makeText(this@GameActivity,"End",Toast.LENGTH_SHORT).show()
                    }
                    else if(info.type==3){
                        paintView.clear()
                        Toast.makeText(this@GameActivity,"Clear",Toast.LENGTH_SHORT).show()
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
            postRef.addChildEventListener(childEventListenerForGame)

            // Player reference in a room
            playerReference = database.child("rooms").child(reference.toString()).child("Players")
            childEventListenerForPlayers = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val playerInfoObj = snapshot.getValue<playerInfo>()
                    if(playerInfoObj!=null) {
                        playersList.add(playerInfoObj)
                        playerCount++
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
                    val playerInfoObj = snapshot.getValue<playerInfo>()
                    if(playerInfoObj!=null) {
                        playersList.remove(playerInfoObj)
                        playerCount--
                    }
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
                    chatAdapter.notifyDataSetChanged()
                    chats_recycler.scrollToPosition(chatsDisplay.size-1)
                }else{
                    Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
    private fun countdown(sec:Long){
        var countdown_timer = object : CountDownTimer(sec, 1000) {
            override fun onFinish() {
                Log.i("TIMER","Finish")
            }

            override fun onTick(p0: Long) {
                Log.i("TIMER",(p0/1000).toString())
            }
        }
        countdown_timer.start()
    }

    private fun uploadToDatabase(cur_text: String) {
        val userName : String? = prefs.getString(getString(R.string.userName), "EMPTY")
        val textObj = ChatText(userName!!,cur_text)
        postReference.push().setValue(textObj)
    }

    override fun onPause() {
        super.onPause()
        val userId: String? = prefs.getString(getString(R.string.userId), "EMPTY")
        Log.i("counter1",playerCount.toString())
        if (userId != "EMPTY") {
            database.child("rooms").child(reference.toString()).child("Players").child(userId!!)
                .removeValue()
        }
        Log.i("counter2",playerCount.toString())
        if(playerCount<=baseCount){
            database.child("rooms").child(reference.toString()).removeValue()
            database.child("games").child(reference.toString()).removeValue()
            database.child("drawingData").child(reference.toString()).removeValue()
        }
        Log.i("counter3",playerCount.toString())
        if(!backButtonPressedBoolean) {
            routeToMainActivity()
        }

    }

    override fun onResume() {
        super.onResume()
        backButtonPressedBoolean = false
        val userId: String? = prefs.getString(getString(R.string.userId), "EMPTY")
        val useName: String? = prefs.getString(getString(R.string.userName), "EMPTY")
        Log.i("#####USER ID", userId)
        Log.i("#####REFERENCE", reference.toString())
        if (userId != "EMPTY") {
            database.child("rooms").child(reference.toString()).child("Players").child(userId.toString()).setValue(playerInfo(useName,userId))
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        backButtonPressedBoolean = true
        routeToMainActivity()
    }

    private fun routeToMainActivity(){
        if(goToMainActivityBoolean){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}