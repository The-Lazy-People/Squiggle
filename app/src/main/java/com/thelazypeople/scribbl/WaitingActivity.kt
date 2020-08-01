package com.thelazypeople.scribbl

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import com.thelazypeople.scribbl.adapters.PlayersListAdapter
import com.thelazypeople.scribbl.model.playerInfo
import kotlinx.android.synthetic.main.activity_game.view.*
import kotlinx.android.synthetic.main.activity_waiting.*

class WaitingActivity : AppCompatActivity() {

    private var playersInGame = mutableListOf<playerInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting)

        val playerAdapter = PlayersListAdapter(playersInGame)
        val layoutManager = LinearLayoutManager(this)
        players_recycler.layoutManager = layoutManager
        players_recycler.adapter = playerAdapter

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



    }
}