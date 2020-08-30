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

/** [GameActivity] is where actual Scribble game commence. */
class GameActivity : AppCompatActivity() {
    private var wordGuessedOrNot: Boolean = false
    private lateinit var countdownTimer: CountDownTimer
    var booleanForCountdownStartedOrNot: Boolean = false
    private var guessingWord: String = ""
    private lateinit var valueEventListenerForGuessingWord: ValueEventListener
    private lateinit var guessingWordRef: DatabaseReference
    private lateinit var paintView: PaintView
    private var roundTillNow = 0
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
    private lateinit var chatAdapter: ChatAdapter
    var reference: String? = ""
    private var otherUserName: String? = ""
    private var playersList = mutableListOf<playerInfo>()
    private var chatsDisplay = mutableListOf<ChatText>()
    var playerCount: Long = 0
    private lateinit var postRef: DatabaseReference
    private val baseCount: Long = 1
    private var goToMainActivityBoolean: Boolean = false
    private var backButtonPressedBoolean: Boolean = false
    private var host = 0
    var timeLimit: Long = 0
    var noOfRounds = 0
    val fixedScore = 10
    private lateinit var mDialog: Dialog
    var colorProvider = mutableListOf<Boolean>() // colored List of Players for Navigational Drawer
    var wordsCollection = WordCollectionData().wordsCollection
    var colorSetForChats = mutableSetOf<String>() //contains UID of colored players in Chats.
    var hostUID: String = ""
    var numGuesPlayer = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        /** To setup [PaintView]. The canvas of drawing. */
        paintView = PaintView(this)
        main.addView(paintView)

        /** To clear the canvas. */
        eraser.setOnClickListener {
            if (host == 1) {
                paintView.clear()
                postRef.push().setValue(Information(10001f, 10001f, 3))
                paintView.isclear = 1
            }
        }

        peoples.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }


        prefs = this.getSharedPreferences(
            getString(R.string.packageName), Context.MODE_PRIVATE
        )
        prefs.edit().putInt(getString(R.string.scoreOfCurPlayer), 0).apply()
        userId = prefs.getString(getString(R.string.userId), getString(R.string.EMPTY))!!
        userName = prefs.getString(getString(R.string.userName), getString(R.string.EMPTY))!!
        val intent: Intent = intent
        reference = intent.getStringExtra(getString(R.string.reference))
        serverHost = intent.getIntExtra(getString(R.string.host), 0)
        noOfRounds = intent.getIntExtra(getString(R.string.rounds), 0)
        timeLimit = intent.getLongExtra(getString(R.string.countdown), 0)
        timeLimit *= 1000
        paintView.end(0f, 0f)
        paintView.getRef(reference)

        playing_players.layoutManager = LinearLayoutManager(this)

        chatAdapter = ChatAdapter(chats = chatsDisplay, colorSet = colorSetForChats)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        chats_recycler.layoutManager = layoutManager
        chats_recycler.adapter = chatAdapter

        database = Firebase.database.reference
        if (reference != null) {

            if (serverHost == 1) {
                database.child(getString(R.string.rooms)).child(reference.toString())
                    .child(getString(R.string.info))
                    .child(getString(R.string.chanceChange)).setValue(1)
                paintView.host = 1
                host = 1
            }

            /** Chat event listener for a room. */
            chatListener()

            /** Drawing data event listener of a room. */
            drawingDataEventListener()

            /** Player event listener in a room. */
            playerEventListener()

            /** Chance Change Event Listener of a room. */
            chanceChangeEventListener()

            /** Whose Chance Event Listener of a room. */
            whoseChanceEventListener()

            /** Word guessing Event Listener of a room. */
            guessingWordEventListener()

        }

        /** Chat send button. */
        button.setOnClickListener {
            if (editText.text.toString() == "") {
                Toast.makeText(this, getString(R.string.emptyText), Toast.LENGTH_SHORT).show()
            } else {
                if (reference != null) {
                    var wordToUpload = getString(R.string.wordGuessed)
                    if (editText.text.toString()
                            .toLowerCase() != guessingWord.toLowerCase() || host == 1 || wordGuessedOrNot
                    ) {
                        wordToUpload = editText.text.toString().trim()
                    } else {
                        var curScore = prefs.getInt(getString(R.string.scoreOfCurPlayer), 0)
                        curScore += fixedScore
                        database.child(getString(R.string.rooms)).child(reference.toString())
                            .child(getString(R.string.Players))
                            .child(userId).child(getString(R.string.score)).setValue(curScore)
                        prefs.edit().putInt(getString(R.string.scoreOfCurPlayer), curScore).apply()
                        wordGuessedOrNot = true
                    }
                    if (editText.text.toString() == getString(R.string.wordGuessed)) {
                        wordToUpload = getString(R.string.wordGuessedModified)
                    }
                    chatUploadToDatabase(wordToUpload)
                    editText.text.clear()
                    chatAdapter.notifyDataSetChanged()
                    chats_recycler.scrollToPosition(chatsDisplay.size - 1)
                } else {
                    Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show()
                }
            }
        }


    }

    /** Chat event listener for a room. */
    private fun chatListener() {
        postReference = database.child(getString(R.string.games)).child(reference.toString())
            .child(getString(R.string.Chats))

        childEventListenerForChat = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val textObj = snapshot.getValue<ChatText>()
                if (textObj != null) {
                    if (textObj.text == getString(R.string.wordGuessed)) {
                        colorSetForChats.add(textObj.UID)
                        for (i in 0 until playersList.size) {
                            if (playersList[i].UID == textObj.UID) {
                                colorProvider[i] = true
                            }
                        }
                        val adapter = PlayingPlayersAdapter(playersList, colorProvider)
                        playing_players.adapter = adapter
                    }
                    otherUserName = textObj.userName
                    chatsDisplay.add(ChatText(textObj.UID, textObj.userName, textObj.text))
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
    }

    /** Drawing data event listener of a room. */
    private fun drawingDataEventListener() {
        postRef = database.child(getString(R.string.drawingData)).child(reference.toString())
        childEventListenerForGame = object : ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val info = snapshot.getValue<Information>()
                Log.i(
                    "DOWNLOADORNOT",
                    info!!.type.toString() + " " + info.pointX.toString() + " " + info.pointY.toString()
                )
                if (info.type == 0) {
                    paintView.start(info.pointX, info.pointY)
                } else if (info.type == 2) {
                    paintView.co(info!!.pointX, info.pointY)
                } else if (info.type == 1) {
                    paintView.end(info!!.pointX, info.pointY)
                } else if (info.type == 3) {
                    paintView.clear()
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
    }

    private fun updateDrawingValue() {
        var curScore = prefs.getInt(getString(R.string.scoreOfCurPlayer), 0)
        curScore += 2
        prefs.edit().putInt(getString(R.string.scoreOfCurPlayer), curScore).apply()
        database.child(getString(R.string.rooms)).child(reference.toString())
            .child(getString(R.string.Players))
            .child(userId).child(getString(R.string.score)).setValue(curScore)
        for (i in 0 until playersList.size) {
            if (userId == playersList[i].UID) {
                playersList[i].score = curScore
            }
        }
    }

    private fun updateScoreToLocalList(playerInfoObj: playerInfo) {
        for (i in 0 until playersList.size) {
            if (playerInfoObj.UID.toString() == playersList[i].UID) {
                playersList[i].score = playerInfoObj.score
            }
        }
    }

    /** Player event listener in a room. */
    private fun playerEventListener() {
        playerReference = database.child(getString(R.string.rooms)).child(reference.toString())
            .child(getString(R.string.Players))
        childEventListenerForPlayers = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val playerInfoObj = snapshot.getValue<playerInfo>()
                if (playerInfoObj != null) {
                    playersList.add(playerInfoObj)
                    colorProvider.add(false)
                    playerCount++
                    val adapter = PlayingPlayersAdapter(playersList, colorProvider)
                    playing_players.adapter = adapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // not needed
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // not needed
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val playerInfoObj = snapshot.getValue<playerInfo>()
                if (host == 1) {
                    if (playerInfoObj?.UID.toString() != userId) {
                        updateDrawingValue()
                        updateScoreToLocalList(playerInfoObj!!)
                    }
                } else {
                    updateScoreToLocalList(playerInfoObj!!)
                }

                if (serverHost == 1) {
                    if (playerInfoObj?.UID != hostUID) {
                        numGuesPlayer++
                        if (numGuesPlayer == playersList.size - 1) {
                            cancelCountdownAndnextChance()
                        }
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val playerInfoObj = snapshot.getValue<playerInfo>()
                if (playerInfoObj != null) {
                    val temp = mutableListOf<playerInfo>()
                    val tempColor = mutableListOf<Boolean>()
                    for (i in 0 until playersList.size) {
                        if (playersList[i].UID != playerInfoObj.UID) {
                            temp.add(playersList[i])
                            tempColor.add(colorProvider[i])
                            Log.i("AAJAJA", playersList[i].Name)
                        }
                    }
                    playersList = temp
                    colorProvider = tempColor
                    playerCount--
                    val adapter = PlayingPlayersAdapter(playersList, colorProvider)
                    playing_players.adapter = adapter
                    for (i in 0 until playersList.size) {
                        Log.i("playersdel", playersList[i].Name)
                    }
                }
            }

        }
        playerReference.addChildEventListener(childEventListenerForPlayers)
    }

    private fun cancelCountdownAndnextChance() {
//        database.child(getString(R.string.rooms)).child(reference.toString())
//            .child(getString(R.string.info))
//            .child(getString(R.string.chanceChange)).setValue(1)
//        chatUploadToDatabase("Correct word is - $guessingWord")
        if (booleanForCountdownStartedOrNot)
        {
            countdownTimer.cancel()
            countdownTimer.onFinish()
        }

        booleanForCountdownStartedOrNot = false
    }

    /** Chance Change Event Listener of a room. */
    private fun chanceChangeEventListener() {
        chanceChangeRef = database.child(getString(R.string.rooms)).child(reference.toString())
            .child(getString(R.string.info))
            .child(getString(R.string.chanceChange))
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
    }

    /** Whose Chance Event Listener of a room. */
    private fun whoseChanceEventListener() {
        whoseChanceRef =
            database.child(getString(R.string.rooms)).child(reference.toString())
                .child(getString(R.string.info)).child(getString(R.string.chanceUID))
        valueEventListenerForWhoesChance = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value.toString() == userId) {
                    Toast.makeText(this@GameActivity, "MY CHANCE", Toast.LENGTH_SHORT).show()
                    chatUploadToDatabase(userName.trim() + " chance!!")
                    host = 1
                    paintView.host = 1
                    setDialog()
                } else {
                    host = 0
                    paintView.host = 0
                }
                for (i in 0 until colorProvider.size) {
                    colorProvider[i] = false
                    val adapter = PlayingPlayersAdapter(playersList, colorProvider)
                    playing_players.adapter = adapter
                }
                wordGuessedOrNot = false
                colorSetForChats.clear()
            }
        }
        whoseChanceRef.addValueEventListener(valueEventListenerForWhoesChance)
    }

    /** Word guessing Event Listener of a room. */
    private fun guessingWordEventListener() {
        var flag = 0
        guessingWordRef =
            database.child(getString(R.string.rooms)).child(reference.toString())
                .child(getString(R.string.info))
                .child(getString(R.string.wordToGuess))
        valueEventListenerForGuessingWord = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                if (flag == 0) {
                    flag = 1
                } else if (serverHost == 1) {
                    countdown(timeLimit)
                }
                guessingWord = snapshot.value.toString()
            }
        }
        guessingWordRef.addValueEventListener(valueEventListenerForGuessingWord)
    }

    /** Dialog to choose one of the Three words given as a choice to the player from the collection. */
    private fun setDialog() {
        mDialog = Dialog(this)
        mDialog.setContentView(R.layout.choose_word)
        wordsCollection.shuffle()
        val first = 0
        val second = 1
        val third = 2

        for (i in 0 until wordsCollection.size) {
            if (i == first)
                mDialog.word1.text = wordsCollection[0]
            else if (i == second)
                mDialog.word2.text = wordsCollection[1]
            else if (i == third)
                mDialog.word3.text = wordsCollection[2]
            else break
        }


        val window = mDialog.window
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        );
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mDialog.setCanceledOnTouchOutside(false) // prevent dialog box from getting dismissed on outside touch
        mDialog.setCancelable(false)  //prevent dialog box from getting dismissed on back key pressed
        mDialog.show()

        mDialog.word1.setOnClickListener {
            choseWordUploadToDatabase(mDialog.word1.text.toString())
            mDialog.dismiss()
        }
        mDialog.word2.setOnClickListener {
            choseWordUploadToDatabase(mDialog.word2.text.toString())
            mDialog.dismiss()
        }
        mDialog.word3.setOnClickListener {
            choseWordUploadToDatabase(mDialog.word3.text.toString())
            mDialog.dismiss()
        }
    }

    /** The next player to get the chance is decided through here. */
    private fun changeUserChance() {
        database.child(getString(R.string.rooms)).child(reference.toString())
            .child(getString(R.string.info)).child(getString(R.string.chanceChange))
            .setValue(0)
        indexOfChance++

        if (indexOfChance >= playersList.size) {
            roundTillNow++
            indexOfChance = 0
        }
        hostUID = playersList[indexOfChance].UID!!
        numGuesPlayer = 0
        paintView.clear()
        postRef.push().setValue(Information(10001f, 10001f, 3))
        paintView.isclear = 1
        if (roundTillNow < noOfRounds) {
            Log.i("TIMER", "index $indexOfChance")
            Log.i("TIMER", playersList[indexOfChance].UID)
            database.child(getString(R.string.rooms)).child(reference.toString())
                .child(getString(R.string.info)).child(getString(R.string.chanceUID))
                .setValue(playersList[indexOfChance].UID)
            Log.i("TIMER", "$roundTillNow - $noOfRounds")
            Log.i("TIMER", timeLimit.toString())

        } else {
            chatUploadToDatabase("GAME OVER!")
        }
    }

    /** Countdown timer for a round. */
    private fun countdown(sec: Long) {
        countdownTimer = object : CountDownTimer(sec, 1000) {
            override fun onFinish() {
                Log.i("TIMER", "Finish")
                database.child(getString(R.string.rooms)).child(reference.toString())
                    .child(getString(R.string.info))
                    .child(getString(R.string.chanceChange)).setValue(1)
                chatUploadToDatabase("Correct word is - $guessingWord")
            }

            override fun onTick(p0: Long) {
                Log.i("TIMER", (p0 / 1000).toString())
            }
        }
        countdownTimer.start()
        booleanForCountdownStartedOrNot = true
    }

    /** Chat messages upload to Firebase Database */
    private fun chatUploadToDatabase(cur_text: String) {
        val textObj = ChatText(userId, userName, cur_text)
        postReference.push().setValue(textObj)
    }

    /** Chosen word upload to Firebase Database */
    private fun choseWordUploadToDatabase(word: String) {
        Toast.makeText(this, word, Toast.LENGTH_SHORT).show()
        database.child(getString(R.string.rooms)).child(reference.toString())
            .child(getString(R.string.info)).child(getString(R.string.wordToGuess))
            .setValue(word)
    }

    override fun onPause() {
        super.onPause()

        if (backButtonPressedBoolean) {
            deleteCurrentPlayer()
            deleteCurrentRoomIfNoOtherPlayerRemains()
            if (booleanForCountdownStartedOrNot)
                countdownTimer.cancel()
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

    /** Route to [MainActivity]. */
    private fun routeToMainActivity() {
        goToMainActivityBoolean = true
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    /** Deletion of current Player from the Room. */
    private fun deleteCurrentPlayer() {
        val userId: String? = prefs.getString(getString(R.string.userId), getString(R.string.EMPTY))
        if (userId != getString(R.string.EMPTY)) {
            database.child(getString(R.string.rooms)).child(reference.toString())
                .child(getString(R.string.Players)).child(userId!!)
                .removeValue()
        }
    }

    /** Delete Room and corresponding data if no Player remains. */
    private fun deleteCurrentRoomIfNoOtherPlayerRemains() {
        if (playerCount <= baseCount) {
            database.child(getString(R.string.rooms)).child(reference.toString()).removeValue()
            database.child(getString(R.string.games)).child(reference.toString()).removeValue()
            database.child(getString(R.string.drawingData)).child(reference.toString())
                .removeValue()
        }
    }

    /** Check whether Rooms exist or not while Joining.
     * If exist, add the Player to the Room.
     * Else, redirect Player to [MainActivity]*/
    private fun checkRoomExistOrNot() {
        // room reference
        val rootRef =
            database.child(getString(R.string.rooms)).child(reference.toString())

        rootRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userId: String? =
                        prefs.getString(getString(R.string.userId), getString(R.string.EMPTY))
                    val userName: String? =
                        prefs.getString(getString(R.string.userName), getString(R.string.EMPTY))
                    if (userId != getString(R.string.EMPTY)) {
                        Log.i("###WAITINGACTIVITY", "done")
                        database.child(getString(R.string.rooms)).child(reference.toString())
                            .child(getString(R.string.Players))
                            .child(userId.toString()).setValue(playerInfo(userName, 0, userId))
                    }
                } else {
                    routeToMainActivity()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}