package com.thelazypeople.scribbl

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.thelazypeople.scribbl.model.Information

var brush = Paint()
var path = Path()
lateinit var params:LinearLayout.LayoutParams

class PaintView(context: Context?): android.view.View(context) {

    private lateinit var database: FirebaseDatabase
    private lateinit var postReference: DatabaseReference
    var canvasHeight=1
    var canvasWidth=1
    var reference:String?=""
    var color = Color.BLACK
    var brushWidth = 14f
    var isclear=0

    init{
        brush.isAntiAlias =true
        brush.color = Color.BLACK
        brush.style=Paint.Style.STROKE
        brush.strokeJoin=Paint.Join.ROUND
        brush.strokeWidth=18f
        params= LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    }

    fun clear() {
        path = Path()
        postInvalidate()
    }

    fun start(x:Float,y:Float)
    {
        path.moveTo(x*canvasWidth, y*canvasHeight)
        postInvalidate()
    }

    fun co(x:Float,y:Float)
    {
        path.lineTo(x*canvasWidth, y*canvasHeight)
        postInvalidate()
    }

    fun end(x:Float,y:Float)
    {
        path.lineTo(x*canvasWidth, y*canvasHeight)
        postInvalidate()
    }

    fun getref(ref:String?){
        reference=ref
        database = Firebase.database
        postReference = database.reference.child("drawingData").child(reference!!)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val pointX = event.x
        val pointY = event.y

        when(event.action)
        {
            MotionEvent.ACTION_DOWN -> {
                if(isclear==1){
                    postReference.removeValue()
                    isclear=0
                }
                path.moveTo(pointX, pointY)
                uploadToDatabase(pointX/canvasWidth , pointY/canvasHeight , 0)
                return true
            }
            MotionEvent.ACTION_UP -> {

                path.moveTo(pointX, pointY)
                uploadToDatabase(pointX/canvasWidth , pointY/canvasHeight , 1)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                path.lineTo(pointX, pointY)
                uploadToDatabase(pointX/canvasWidth , pointY/canvasHeight , 2)
            }
            else -> return false
        }
        postInvalidate()
        return false
    }

    override fun onDraw(canvas: Canvas)
    {
        canvas.drawPath(path,brush)
        canvasHeight=canvas.height
        canvasWidth=canvas.width
    }

    private fun uploadToDatabase(pointX:Float, pointY:Float , type: Int) {
        val info =
            Information(pointX, pointY, type)
        Log.i("DOWNLOADORNOT",info!!.type.toString()+" "+info.pointX.toString()+" "+info.pointY.toString())
        postReference.push().setValue(info)
    }
}
