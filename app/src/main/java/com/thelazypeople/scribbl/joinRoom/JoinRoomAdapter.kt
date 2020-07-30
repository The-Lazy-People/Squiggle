package com.thelazypeople.scribbl.joinRoom

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.thelazypeople.scribbl.R
import com.thelazypeople.scribbl.model.Value

class JoinRoomAdapter(var mCtx:Context , var resource:Int,var room_list:List<Value>)
    :ArrayAdapter<Value>( mCtx , resource , room_list ){

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(mCtx)
        val rowView = inflater.inflate(R.layout.list_item_row, null, true)
        val roomDesc : Value = room_list[position]

        val roomNameView : TextView = rowView.findViewById(R.id.roomName)
        val state : TextView = rowView.findViewById(R.id.state)

        roomNameView.text = roomDesc.roomname
        if(roomDesc.password.equals(mCtx.getString(R.string.NO))){
            state.text = mCtx.getString(R.string.OPEN)
        }else{
            state.text = mCtx.getString(R.string.CLOSED)
        }
        return rowView
    }

}
