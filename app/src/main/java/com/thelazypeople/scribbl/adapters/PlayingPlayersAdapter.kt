package com.thelazypeople.scribbl.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.thelazypeople.scribbl.R
import com.thelazypeople.scribbl.model.playerInfo
import kotlinx.android.synthetic.main.each_playing_player.view.*
import com.thelazypeople.scribbl.GameActivity

/** Adapter used to populate current Playing Player List in [GameActivity]. */
class PlayingPlayersAdapter(
    private val players: MutableList<playerInfo>, private val colorProvider: MutableList<Boolean>
) : RecyclerView.Adapter<PlayingPlayersAdapter.PlayingPlayersViewHolder>() {

    class PlayingPlayersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayingPlayersViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.each_playing_player, parent, false)
        return PlayingPlayersViewHolder(itemView)
    }

    override fun getItemCount(): Int = players.size

    override fun onBindViewHolder(holder: PlayingPlayersViewHolder, position: Int) {
        holder.itemView.name_each_player.text = players[position].Name
        // If user guessed the correct word, Background color of that player is set to Green.
        if (colorProvider[position]) {
            holder.itemView.name_each_player.setBackgroundColor(Color.GREEN)
        }
        holder.itemView.score_each_player.text = players[position].score.toString()
    }
}
