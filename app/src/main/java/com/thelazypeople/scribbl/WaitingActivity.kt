package com.thelazypeople.scribbl

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.thelazypeople.scribbl.adapters.PlayersListAdapter
import com.thelazypeople.scribbl.model.playerInfo
import kotlinx.android.synthetic.main.activity_waiting.*


class WaitingActivity : AppCompatActivity() {

    private lateinit var gameStartedRef: DatabaseReference
    private var reference: String = ""
    private lateinit var childEventListenerForPlayers: ChildEventListener
    private lateinit var prefs: SharedPreferences
    private lateinit var playerReference: DatabaseReference
    private lateinit var database: DatabaseReference
    private var playersInGame = mutableListOf<playerInfo>()
    private var host = 0
    private var noOfRounds : String = ""
    private var timeLimit : String = ""
    private lateinit var valueEventListenerForGameStarted: ValueEventListener
    private var goToMainActivityBoolean: Boolean = false
    private var goToGameActivityBoolean: Boolean = false
    private var backButtonPressedBoolean: Boolean = false
    private lateinit var timerSpinner : Spinner
    private lateinit var roundsSpinner : Spinner

    var playerCount: Long = 0
    private val baseCount: Long = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting)
        database = Firebase.database.reference
        prefs = this.getSharedPreferences(
            getString(R.string.packageName), Context.MODE_PRIVATE
        )
        val intent: Intent = intent
        reference = intent.getStringExtra("reference")
        host = intent.getIntExtra("host", 0)
        Log.i("TESTER", host.toString())

        val layoutManager = LinearLayoutManager(this)
        players_recycler.layoutManager = layoutManager


        if (host == 0) {
            btnStart.isClickable = false
            btnStart.isActivated = false
            btnStart.isEnabled = false
            rounds_spin.isClickable = false
            rounds_spin.isActivated = false
            rounds_spin.isEnabled = false
            draw_time_spin.isClickable = false
            draw_time_spin.isActivated = false
            draw_time_spin.isEnabled = false
        }

        roundsSpinner = findViewById(R.id.rounds_spin)
        ArrayAdapter.createFromResource(
            this,
            R.array.rounds_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            roundsSpinner.adapter = adapter
        }

        roundsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                noOfRounds = resources.getStringArray(R.array.rounds_array)[0]
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                noOfRounds = resources.getStringArray(R.array.rounds_array)[position]
            }

        }

        timerSpinner = findViewById(R.id.draw_time_spin)
        ArrayAdapter.createFromResource(
            this,
            R.array.draw_time_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            timerSpinner.adapter = adapter
        }

        timerSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                timeLimit = resources.getStringArray(R.array.draw_time_array)[0]
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                timeLimit = resources.getStringArray(R.array.draw_time_array)[position]
            }

        }

        playerReference = database.child("rooms").child(reference).child("Players")
        childEventListenerForPlayers = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val player = snapshot.getValue<playerInfo>()
                if (player != null) {
                    playersInGame.add(player)
                    playerCount++
                    val playerAdapter = PlayersListAdapter(playersInGame)
                    players_recycler.adapter = playerAdapter
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
                val player = snapshot.getValue<playerInfo>()
                if (player != null) {
                    playerCount--
                    val temp = mutableListOf<playerInfo>()
                    for (i in 0..playersInGame.size - 1) {
                        if (playersInGame[i].UID != player.UID) {
                            temp.add(playersInGame[i])
                            Log.i("AAJAJA", playersInGame[i].Name)
                        }
                    }
                    playersInGame = temp
                    val playerAdapter = PlayersListAdapter(playersInGame)
                    players_recycler.adapter = playerAdapter
                }
            }
        }
        playerReference.addChildEventListener(childEventListenerForPlayers)



        gameStartedRef = database.child("rooms").child(reference).child("info").child("gamestarted")
        valueEventListenerForGameStarted = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                Log.i("TESTER", snapshot.value.toString() + "yahi h")
                if (snapshot.value.toString() == "1") {
                    Log.i("TESTER", "hogaya")
                    routeToGameActivity()
                }
            }


        }
        gameStartedRef.addValueEventListener(valueEventListenerForGameStarted)

        btnStart.setOnClickListener {

            database.child("rooms").child(reference).child("info").child("gamestarted").setValue(1)
            routeToGameActivity()
            Toast.makeText(this, "Game Started", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        backButtonPressedBoolean = false
        goToGameActivityBoolean = false
        goToMainActivityBoolean = false
        checkRoomExistOrNot()
    }

    override fun onPause() {
        super.onPause()

        if (backButtonPressedBoolean) {
            deleteCurrentPlayer()
            deleteCurrentRoomIfNoOtherPlayerRemains()
        }

        //called when user cancel/exit the application
        if (!goToGameActivityBoolean && !goToMainActivityBoolean) {
            deleteCurrentPlayer()
            deleteCurrentRoomIfNoOtherPlayerRemains()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        backButtonPressedBoolean = true
        routeToMainActivity()
    }

    private fun routeToMainActivity() {
        goToMainActivityBoolean = true
        goToGameActivityBoolean = false
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun routeToGameActivity() {
        goToMainActivityBoolean = false
        goToGameActivityBoolean = true
        val intent = Intent(this, GameActivity::class.java)
        var t1=noOfRounds.toInt()
        var t2=timeLimit.toLong()
        intent.putExtra("reference", reference)
        intent.putExtra("host", host)
        intent.putExtra("rounds", t1)
        intent.putExtra("countdown", t2)
        startActivity(intent)
    }

    private fun deleteCurrentPlayer() {
        val userId: String? = prefs.getString(getString(R.string.userId), "EMPTY")
        if (userId != "EMPTY") {
            database.child("rooms").child(reference).child("Players").child(userId!!)
                .removeValue()
        }
    }

    private fun deleteCurrentRoomIfNoOtherPlayerRemains() {
        if (playerCount <= baseCount) {
            Log.i("#########", "watingDestroyRoomCalled")
            database.child("rooms").child(reference).removeValue()
        }
    }

    private fun checkRoomExistOrNot() {
        // room reference
        val rootRef =
            database.child("rooms").child(reference)

        // check room exist or not
        rootRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // room exist. Add player to the room
                    val userId: String? = prefs.getString(getString(R.string.userId), "EMPTY")
                    val userName: String? = prefs.getString(getString(R.string.userName), "EMPTY")
                    if (userId != "EMPTY") {
                        Log.i("###WAITINGACTIVITY", "done")
                        database.child("rooms").child(reference).child("Players")
                            .child(userId.toString()).setValue(playerInfo( userName, 0, userId))
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