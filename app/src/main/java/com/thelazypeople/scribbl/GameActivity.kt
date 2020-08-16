package com.thelazypeople.scribbl

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.thelazypeople.scribbl.adapters.ChatAdapter
import com.thelazypeople.scribbl.adapters.PlayingPlayersAdapter
import com.thelazypeople.scribbl.model.ChatText
import com.thelazypeople.scribbl.model.Information
import com.thelazypeople.scribbl.model.playerInfo
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.chats_preview.*
import kotlinx.android.synthetic.main.choose_word.*
import kotlinx.android.synthetic.main.game_content.*

class GameActivity : AppCompatActivity() {
    private var guessingWord: String=""
    private lateinit var valueEventListenerForGuessingWord: ValueEventListener
    private lateinit var guessingWordRef: DatabaseReference
    private lateinit var paintView: PaintView
    private var roundTillNow=0
    private lateinit var valueEventListenerForWhoesChance: ValueEventListener
    private lateinit var whoseChanceRef: DatabaseReference
    private var indexOfChance = -1
    private lateinit var valueEventListenerForChanceChange: ValueEventListener
    private lateinit var chanceChangeRef: DatabaseReference
    private var serverHost: Int = 0
    private var userName = ""
    private var userId = ""
    private lateinit var prefs: SharedPreferences
    private lateinit var database: DatabaseReference
    private lateinit var postReference: DatabaseReference
    private lateinit var playerReference: DatabaseReference
    private lateinit var childEventListenerForChat: ChildEventListener
    private lateinit var childEventListenerForPlayers: ChildEventListener
    private lateinit var childEventListenerForGame: ChildEventListener
    var reference: String? = ""
    private var otherUserName: String? = ""
    private var downloadText: String = ""
    private var playersList = mutableListOf<playerInfo>()
    private var chatsDisplay = mutableListOf<ChatText>()
    var playerCount: Long = 0
    private lateinit var postRef: DatabaseReference
    private val baseCount: Long = 1
    private var goToMainActivityBoolean: Boolean = false
    private var backButtonPressedBoolean: Boolean = false
    private var host = 0
    var timeLimit:Long=0
    var noOfRounds=0
    private lateinit var mDialog:Dialog
    var colorProvider= mutableListOf<Boolean>()
    var wordsCollection = mutableListOf<String>("hut","cloud","tent","bus","car","remote","chair","bucket","head","chutiya")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        paintView = PaintView(this)
        main.addView(paintView)

        eraser.setOnClickListener {
            if (host == 1) {
                paintView.clear()
                postRef.push().setValue(Information(10001f, 10001f, 3))
                paintView.isclear = 1
            }
        }

        //Drawer
        peoples.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        //Dialog
        button2.setOnClickListener {
            setDialog()
        }


        prefs = this.getSharedPreferences(
            getString(R.string.packageName), Context.MODE_PRIVATE
        )
        userId = prefs.getString(getString(R.string.userId), "EMPTY")!!
        userName = prefs.getString(getString(R.string.userName), "EMPTY")!!
        val intent: Intent = intent
        reference = intent.getStringExtra("reference")
        serverHost = intent.getIntExtra("host", 0)
        noOfRounds=intent.getIntExtra("rounds", 0)
        timeLimit=intent.getLongExtra("countdown", 0)
        timeLimit=timeLimit*1000
        paintView.end(0f, 0f)
        paintView.getref(reference)

        playing_players.layoutManager = LinearLayoutManager(this)



        val chatAdapter = ChatAdapter(chats = chatsDisplay)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        chats_recycler.layoutManager = layoutManager
        chats_recycler.adapter = chatAdapter

        database = Firebase.database.reference
        if (reference != null) {
            if (serverHost == 1) {
                database.child("rooms").child(reference.toString()).child("info")
                    .child("chanceChange").setValue(1)
                paintView.host = 1
                host = 1
            }
            postReference = database.child("games").child(reference.toString()).child("Chats")

            // Chat reference for a room
            childEventListenerForChat = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val textObj = snapshot.getValue<ChatText>()
                    if (textObj != null) {
                        if (textObj.text == "word guessed!!") {
                            for (i in 0..playersList.size-1){
                                if (playersList[i].UID == textObj.UID) {
                                    colorProvider[i]=true
                                }
                            }
                            val adapter = PlayingPlayersAdapter(playersList,colorProvider)
                            playing_players.adapter = adapter
                        }
                        otherUserName = textObj?.userName

                        downloadText += otherUserName + " : " + textObj?.text + "\n"
                            //textHolder.text =downloadText

                        otherUserName?.let { textObj?.text?.let { it1 -> ChatText(it, it1) } }
                            ?.let { chatsDisplay.add(it) }
                            chatAdapter.notifyDataSetChanged()
                            chats_recycler.scrollToPosition(chatsDisplay.size - 1)

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
            postReference.addChildEventListener(childEventListenerForChat)

            // Drawing data reference of a room
            postRef = database.child("drawingData").child(reference.toString())
            childEventListenerForGame = object : ChildEventListener {

                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val info = snapshot.getValue<Information>()
                    Log.i(
                        "DOWNLOADORNOT",
                        info!!.type.toString() + " " + info.pointX.toString() + " " + info.pointY.toString()
                    )
                    if (info.type == 0) {
                        paintView.start(info.pointX, info.pointY)
                        //Toast.makeText(this@GameActivity, "Start", Toast.LENGTH_SHORT).show()
                    } else if (info.type == 2) {
                        paintView.co(info!!.pointX, info.pointY)
                    } else if (info.type == 1) {
                        paintView.end(info!!.pointX, info.pointY)
                        //Toast.makeText(this@GameActivity, "End", Toast.LENGTH_SHORT).show()
                    } else if (info.type == 3) {
                        paintView.clear()
                        //Toast.makeText(this@GameActivity, "Clear", Toast.LENGTH_SHORT).show()
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
                    if (playerInfoObj != null) {
                        playersList.add(playerInfoObj)
                        colorProvider.add(false)
                        playerCount++
                        val adapter = PlayingPlayersAdapter(playersList,colorProvider)
                        playing_players.adapter = adapter
                        for (i in 0..playersList.size - 1) {
                            Log.i("playersadd", playersList[i].Name)
                        }
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
                    if (playerInfoObj != null) {
                        val temp = mutableListOf<playerInfo>()
                        val tempColor = mutableListOf<Boolean>()
                        for (i in 0..playersList.size - 1) {
                            if (playersList[i].UID != playerInfoObj.UID) {
                                temp.add(playersList[i])
                                tempColor.add(colorProvider[i])
                                Log.i("AAJAJA", playersList[i].Name)
                            }
                        }
                        playersList = temp
                        colorProvider=tempColor
                        playerCount--
                        val adapter = PlayingPlayersAdapter(playersList,colorProvider)
                        playing_players.adapter = adapter
                        for (i in 0..playersList.size - 1) {
                            Log.i("playersdel", playersList[i].Name)
                        }
                    }
                }

            }
            playerReference.addChildEventListener(childEventListenerForPlayers)


            chanceChangeRef = database.child("rooms").child(reference.toString()).child("info")
                .child("chanceChange")
            valueEventListenerForChanceChange = object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.value.toString() == "1") {
                        if (serverHost == 1) {
                           changeUserChance()
                        }
                    }
                }
            }
            chanceChangeRef.addValueEventListener(valueEventListenerForChanceChange)
            whoseChanceRef =
                database.child("rooms").child(reference.toString()).child("info").child("chanceUID")
            valueEventListenerForWhoesChance = object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {

                }
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value.toString() == userId) {
                        Toast.makeText(this@GameActivity, "MY CHANCE", Toast.LENGTH_SHORT).show()
                        uploadToDatabase(userName.trim()+" chance!!")
                        host=1
                        paintView.host=1
                        setDialog()
                    }
                    else{
                        host=0
                        paintView.host=0
                    }
                    for (i in 0..colorProvider.size-1){
                        colorProvider[i]=false
                        val adapter = PlayingPlayersAdapter(playersList,colorProvider)
                        playing_players.adapter = adapter
                    }
                }
            }
            whoseChanceRef.addValueEventListener(valueEventListenerForWhoesChance)
            var flag=0
            guessingWordRef =
                database.child("rooms").child(reference.toString()).child("info").child("wordToGuess")
            valueEventListenerForGuessingWord = object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {

                }
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (flag==0){
                        flag=1
                    }
                    else if (serverHost==1){
                        countdown(timeLimit)
                    }
                    guessingWord=snapshot.value.toString()
                }
            }
            guessingWordRef.addValueEventListener(valueEventListenerForGuessingWord)

        }

        button.setOnClickListener {
            if (editText.text.toString() == "") {
                Toast.makeText(this, "Empty text", Toast.LENGTH_SHORT).show()
            } else {
                if (reference != null) {
                    var wordToUpload="word guessed!!"
                    if (editText.text.toString()!=guessingWord){
                        wordToUpload=editText.text.toString().trim()
                    }
                    if (editText.text.toString()=="word guessed!!"){
                        wordToUpload="word guessed!"
                    }
                    uploadToDatabase(wordToUpload)
                    editText.text.clear()
                    chatAdapter.notifyDataSetChanged()
                    chats_recycler.scrollToPosition(chatsDisplay.size - 1)
                } else {
                    Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show()
                }
            }
        }


    }

    private fun setDialog() {
        mDialog = Dialog(this)
        mDialog.setContentView(R.layout.choose_word)
        wordsCollection.shuffle()
        val first = 0
        val second = 1
        val third = 2

        for ( i in 0..wordsCollection.size-1){
            if(i== first)
                mDialog.word1.setText(wordsCollection[0].toString())
            else if(i==second)
                mDialog.word2.setText(wordsCollection[1].toString())
            else if(i == third)
                mDialog.word3.setText(wordsCollection[2].toString())
            else break
        }


        val window = mDialog.window
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mDialog.setCanceledOnTouchOutside(false) // prevent dialog box from getting dismissed on outside touch
        mDialog.setCancelable(false)  //prevent dialog box from getting dismissed on back key pressed
        mDialog.show()

        mDialog.word1.setOnClickListener {
            Toast.makeText(this,mDialog.word1.text.toString(), Toast.LENGTH_SHORT).show()
            database.child("rooms").child(reference.toString()).child("info").child("wordToGuess")
                .setValue(mDialog.word1.text.toString())
            mDialog.dismiss()
        }
        mDialog.word2.setOnClickListener {
            Toast.makeText(this,mDialog.word2.text.toString(), Toast.LENGTH_SHORT).show()
            database.child("rooms").child(reference.toString()).child("info").child("wordToGuess")
                .setValue(mDialog.word2.text.toString())
            mDialog.dismiss()
        }
        mDialog.word3.setOnClickListener {
            Toast.makeText(this,mDialog.word3.text.toString(), Toast.LENGTH_SHORT).show()
            database.child("rooms").child(reference.toString()).child("info").child("wordToGuess")
                .setValue(mDialog.word3.text.toString())
            mDialog.dismiss()
        }
    }

    private fun changeUserChance() {
        database.child("rooms").child(reference.toString()).child("info").child("chanceChange")
            .setValue(0)
        indexOfChance++

        if (indexOfChance >= playersList.size) {
            roundTillNow++
            indexOfChance = 0
        }
        paintView.clear()
        postRef.push().setValue(Information(10001f, 10001f, 3))
        paintView.isclear = 1
        if (roundTillNow<noOfRounds) {
            Log.i("TIMER", "index" + indexOfChance.toString())
            Log.i("TIMER", playersList[indexOfChance].UID)
            database.child("rooms").child(reference.toString()).child("info").child("chanceUID")
                .setValue(playersList[indexOfChance].UID)
            Log.i("TIMER", roundTillNow.toString()+" - "+noOfRounds.toString())
            Log.i("TIMER", timeLimit.toString())

        }
        else{
            uploadToDatabase("GAME OVER!")

        }
    }
    private fun countdown(sec: Long) {
        var countdownTimer = object : CountDownTimer(sec, 1000) {
            override fun onFinish() {
                Log.i("TIMER", "Finish")
                database.child("rooms").child(reference.toString()).child("info")
                    .child("chanceChange").setValue(1)
                uploadToDatabase("Correct word is - "+guessingWord)
            }

            override fun onTick(p0: Long) {
                Log.i("TIMER", (p0 / 1000).toString())
            }
        }
        countdownTimer.start()
    }

    private fun uploadToDatabase(cur_text: String) {
        val textObj = ChatText(userId,userName, cur_text)
        postReference.push().setValue(textObj)
    }

    override fun onPause() {
        super.onPause()

            if (backButtonPressedBoolean) {
                deleteCurrentPlayer()
                deleteCurrentRoomIfNoOtherPlayerRemains()
            }

            //called when user cancel/exit the application
            if (!goToMainActivityBoolean) {
                deleteCurrentPlayer()
                deleteCurrentRoomIfNoOtherPlayerRemains()
            }


    }

    override fun onResume() {
        super.onResume()
        backButtonPressedBoolean = false
        goToMainActivityBoolean = false
        checkRoomExistOrNot()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        backButtonPressedBoolean = true
        routeToMainActivity()
    }

    private fun routeToMainActivity() {
        goToMainActivityBoolean = true
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun deleteCurrentPlayer() {
        val userId: String? = prefs.getString(getString(R.string.userId), "EMPTY")
        if (userId != "EMPTY") {
            database.child("rooms").child(reference.toString()).child("Players").child(userId!!)
                .removeValue()
        }
    }

    private fun deleteCurrentRoomIfNoOtherPlayerRemains() {
        if (playerCount <= baseCount) {
            database.child("rooms").child(reference.toString()).removeValue()
            database.child("games").child(reference.toString()).removeValue()
            database.child("drawingData").child(reference.toString()).removeValue()
        }
    }

    private fun checkRoomExistOrNot() {
        // room reference
        val rootRef =
            database.child("rooms").child(reference.toString())

        // check room exist or not
        rootRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // room exist. Add player to the room
                    val userId: String? = prefs.getString(getString(R.string.userId), "EMPTY")
                    val useName: String? = prefs.getString(getString(R.string.userName), "EMPTY")
                    if (userId != "EMPTY") {
                        Log.i("###WAITINGACTIVITY", "done")
                        database.child("rooms").child(reference.toString()).child("Players")
                            .child(userId.toString()).setValue(playerInfo(useName, userId))
                    }
                } else {
                    //room doesn't exist. Re-direct to MainActivity.kt
                    routeToMainActivity()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}