package com.thelazypeople.scribbl.model

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point

class Stroke(var paint: Paint) {
    val Spaint = paint
    var Spath: Path? = null

    public fun addPoint( pt: Point){
        if(Spath == null){
            Spath= Path()
            Spath?.moveTo(pt.x.toFloat(), pt.y.toFloat())
        }else{
            Spath?.lineTo(pt.x.toFloat(), pt.y.toFloat())
        }
    }
}