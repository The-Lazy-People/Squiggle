package com.thelazypeople.scribbl

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
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
    private var reference:String=""
    private lateinit var childEventListenerForPlayers: ChildEventListener
    private lateinit var prefs: SharedPreferences
    private lateinit var playerReference: DatabaseReference
    private lateinit var database:DatabaseReference
    private var playersInGame = mutableListOf<playerInfo>()
    private var host=0
    private lateinit var valueEventListenerForGameStarted:ValueEventListener
    private var goToMainActivityBoolean : Boolean = true
    private var backButtonPressedBoolean : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting)
        database = Firebase.database.reference
        prefs = this.getSharedPreferences(
            getString(R.string.packageName), Context.MODE_PRIVATE
        )
        val intent: Intent = intent
        reference = intent.getStringExtra("reference")
        host=intent.getIntExtra("host",0)
        Log.i("TESTER",host.toString())

        val layoutManager = LinearLayoutManager(this)
        players_recycler.layoutManager = layoutManager


        if(host==0){
            btnStart.isClickable=false
            btnStart.isActivated=false
            btnStart.isEnabled=false
        }


        val rounds_spin: Spinner = findViewById(R.id.rounds_spin)
        ArrayAdapter.createFromResource(
            this,
            R.array.rounds_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            rounds_spin.adapter = adapter
        }

        val drawTime_spin: Spinner = findViewById(R.id.draw_time_spin)
        ArrayAdapter.createFromResource(
            this,
            R.array.draw_time_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            drawTime_spin.adapter = adapter
        }

        playerReference = database.child("rooms").child(reference).child("Players")
        childEventListenerForPlayers = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val player = snapshot.getValue<playerInfo>()
                if (player!=null) {
                    playersInGame.add(player)
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
                if (player!=null) {
                    playersInGame.remove(player)
                    val playerAdapter = PlayersListAdapter(playersInGame)
                    players_recycler.adapter = playerAdapter
                }

            }

        }
        playerReference.addChildEventListener(childEventListenerForPlayers)



        gameStartedRef = database.child("rooms").child(reference).child("gamestarted")
        valueEventListenerForGameStarted = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                Log.i("TESTER",snapshot.value.toString()+"yahi h")
                if (snapshot.value.toString()=="1"){
                    Log.i("TESTER","hogaya")
                    routeToGameActivity()
                }
            }


        }
        gameStartedRef.addValueEventListener(valueEventListenerForGameStarted)

        btnStart.setOnClickListener {

            database.child("rooms").child(reference).child("gamestarted").setValue(1)
            routeToGameActivity()
            Toast.makeText(this, "Game Started", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        backButtonPressedBoolean = false
        val userId: String? = prefs.getString(getString(R.string.userId), "EMPTY")
        val useName: String? = prefs.getString(getString(R.string.userName), "EMPTY")
        if (userId != "EMPTY") {
            Log.i("###WAITINGACTIVITY","done")
            database.child("rooms").child(reference).child("Players").child(userId.toString()).setValue(playerInfo(useName,userId))
        }
    }

    override fun onPause() {
        super.onPause()
        val userId: String? = prefs.getString(getString(R.string.userId), "EMPTY")
        if (userId != "EMPTY") {
            database.child("rooms").child(reference).child("Players").child(userId!!)
                .removeValue()
        }
        if(!backButtonPressedBoolean) {
            routeToMainActivity()
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

    private fun routeToGameActivity(){
        goToMainActivityBoolean = false
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("reference", reference)
        startActivity(intent)
    }
}