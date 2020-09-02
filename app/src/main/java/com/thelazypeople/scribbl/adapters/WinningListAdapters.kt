package com.thelazypeople.scribbl.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.thelazypeople.scribbl.R
import com.thelazypeople.scribbl.model.playerInfo
import kotlinx.android.synthetic.main.winner_each_list.view.*

class WinningListAdapters(
    private val players: MutableList<playerInfo>
) :RecyclerView.Adapter<WinningListAdapters.WinningViewHolder>(){

    class WinningViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WinningViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.winner_each_list, parent, false)
        return WinningViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: WinningViewHolder, position: Int) {
        holder.itemView.name_each_player2.text = players[position].Name
        holder.itemView.score_each_player2.text = players[position].score.toString()
        var rank = (position+1).toString()
        rank = "$rank."
        holder.itemView.rank_each_player.text = rank
    }

    override fun getItemCount(): Int = players.size
}