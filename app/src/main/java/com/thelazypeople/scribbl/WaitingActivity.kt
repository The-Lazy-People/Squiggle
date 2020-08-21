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
    private var noOfRounds: String = ""
    private var timeLimit: String = ""
    private lateinit var valueEventListenerForGameStarted: ValueEventListener
    private var goToMainActivityBoolean: Boolean = false
    private var goToGameActivityBoolean: Boolean = false
    private var backButtonPressedBoolean: Boolean = false
    private lateinit var timerSpinner: Spinner
    private lateinit var roundsSpinner: Spinner

    var playerCount: Long = 0
    private val baseCount: Long = 1

    private val GAME_STARTED_CONST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting)
        database = Firebase.database.reference
        prefs = this.getSharedPreferences(
            getString(R.string.packageName), Context.MODE_PRIVATE
        )
        val intent: Intent = intent
        reference = intent.getStringExtra(getString(R.string.reference))
        host = intent.getIntExtra(getString(R.string.host), 0)
        Log.i("TESTER", host.toString())

        val layoutManager = LinearLayoutManager(this)
        players_recycler.layoutManager = layoutManager


        if (host == 0) {
            disableButtonsForNonHost()
        }

        /** Timer Spinner to set the guessing time of each turn in seconds. */
        setForTimerSpinner()

        /** Rounds Spinner to set the number of Total rounds in a Game. */
        setForRoundsSpinner()

        /** Fetching Players from Firebase Database */
        setForPlayerEventListener()

        /** Game Started Tracking For Non Host Players. */
        trackGameStartedForNonHost()

        /** Host can start the game by pressing this button. */
        btnStart.setOnClickListener {
            database.child(getString(R.string.rooms)).child(reference)
                .child(getString(R.string.info)).child(getString(R.string.gamestarted)).setValue(GAME_STARTED_CONST)
            routeToGameActivity()
            Toast.makeText(this, getString(R.string.startedGameText), Toast.LENGTH_SHORT).show()
        }
    }

    private fun disableButtonsForNonHost() {
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

    /** Game Started Tracking For Non Host Players. */
    private fun trackGameStartedForNonHost() {
        gameStartedRef = database.child(getString(R.string.rooms)).child(reference)
            .child(getString(R.string.info)).child(getString(R.string.gamestarted))

        valueEventListenerForGameStarted = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                Log.i("TESTER", snapshot.value.toString() + "yahi h")
                if (snapshot.value.toString() == GAME_STARTED_CONST.toString()) {
                    Log.i("TESTER", "hogaya")
                    routeToGameActivity()
                }
            }
        }
        gameStartedRef.addValueEventListener(valueEventListenerForGameStarted)
    }

    /** Fetching Players from Firebase Database */
    private fun setForPlayerEventListener() {
        playerReference = database.child(getString(R.string.rooms)).child(reference)
            .child(getString(R.string.Players))

        /** Added an event listener to track addition/deletion of Players in Firebase.
         * Correspondingly, it is added/deleted locally in the application. */

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
                    for (i in 0 until playersInGame.size) {
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
    }

    /** Timer Spinner to set the guessing time of each turn in seconds. */
    private fun setForTimerSpinner() {
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

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                timeLimit = resources.getStringArray(R.array.draw_time_array)[position]
            }
        }
    }

    /** Rounds Spinner to set the number of Total rounds in a Game. */
    private fun setForRoundsSpinner() {
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

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                noOfRounds = resources.getStringArray(R.array.rounds_array)[position]
            }

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

    /** Route to [MainActivity]. */
    private fun routeToMainActivity() {
        goToMainActivityBoolean = true
        goToGameActivityBoolean = false
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    /** Route to [GameActivity]. */
    private fun routeToGameActivity() {
        goToMainActivityBoolean = false
        goToGameActivityBoolean = true
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra(getString(R.string.reference), reference)
        intent.putExtra(getString(R.string.host), host)
        intent.putExtra(getString(R.string.rounds), noOfRounds.toInt())
        intent.putExtra(getString(R.string.countdown), timeLimit.toLong())
        startActivity(intent)
    }

    /** Deletion of current Player from the Room. */
    private fun deleteCurrentPlayer() {
        val userId: String? = prefs.getString(getString(R.string.userId), getString(R.string.EMPTY))
        if (userId != getString(R.string.EMPTY)) {
            database.child(getString(R.string.rooms)).child(reference)
                .child(getString(R.string.Players)).child(userId!!)
                .removeValue()
        }
    }

    /** Delete Room if no Player remains. */
    private fun deleteCurrentRoomIfNoOtherPlayerRemains() {
        if (playerCount <= baseCount) {
            database.child(getString(R.string.rooms)).child(reference).removeValue()
        }
    }

    /** Check whether Rooms exist or not while Joining.
     * If exist, add the Player to the Room.
     * Else, redirect Player to [MainActivity]*/
    private fun checkRoomExistOrNot() {
        // room reference
        val rootRef =
            database.child(getString(R.string.rooms)).child(reference)

        rootRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userId: String? =
                        prefs.getString(getString(R.string.userId), getString(R.string.EMPTY))
                    val userName: String? =
                        prefs.getString(getString(R.string.userName), getString(R.string.EMPTY))
                    if (userId != getString(R.string.EMPTY)) {
                        database.child(getString(R.string.rooms)).child(reference)
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
