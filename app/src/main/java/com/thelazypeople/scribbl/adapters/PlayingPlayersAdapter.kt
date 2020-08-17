package com.thelazypeople.scribbl.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.thelazypeople.scribbl.R
import com.thelazypeople.scribbl.model.playerInfo
import kotlinx.android.synthetic.main.each_playing_player.view.*

class PlayingPlayersAdapter (
    val players: MutableList<playerInfo>, val colorProvider:MutableList<Boolean>
): RecyclerView.Adapter<PlayingPlayersAdapter.PlayingPlayersViewHolder>(){

    class PlayingPlayersViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayingPlayersViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.each_playing_player, parent, false)
        return PlayingPlayersViewHolder(itemView)
    }

    override fun getItemCount(): Int = players.size

    override fun onBindViewHolder(holder: PlayingPlayersViewHolder, position: Int) {
        holder.itemView.name_each_player.text = players[position].Name
        if(colorProvider[position]) {
            holder.itemView.name_each_player.setBackgroundColor(Color.GREEN)
        }
        holder.itemView.score_each_player.text = players[position].score.toString()
    }

}