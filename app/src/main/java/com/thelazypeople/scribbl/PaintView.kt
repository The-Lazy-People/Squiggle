package com.thelazypeople.scribbl

import android.content.Context
import android.graphics.*
import android.util.SparseArray
import android.view.MotionEvent
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.thelazypeople.scribbl.model.Information
import com.thelazypeople.scribbl.model.Stroke

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

    private var allStroke = mutableListOf<Stroke>()
    private var activeStroke = SparseArray<Stroke>()

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
        val pointerCount = event.pointerCount

        when(event.action)
        {
            MotionEvent.ACTION_DOWN -> {
                for(pc in 0 until pointerCount)
                    pointDown(event.x.toInt(), event.y.toInt(), event.getPointerId(0))
                //path.moveTo(pointX, pointY)
                //uploadToDatabase(pointX/canvasWidth , pointY/canvasHeight , 0)
                return true
            }
            MotionEvent.ACTION_UP -> {

                //path.moveTo(pointX, pointY)
                //uploadToDatabase(pointX/canvasWidth , pointY/canvasHeight , 1)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                for(pc in 0 until pointerCount)
                    pointMove(event.x.toInt(), event.y.toInt(), event.getPointerId(pc))
                //path.lineTo(pointX, pointY)
                //uploadToDatabase(pointX/canvasWidth , pointY/canvasHeight , 2)
            }
            MotionEvent.ACTION_POINTER_DOWN->{
                for(pc in 0 until pointerCount)
                    pointDown(event.x.toInt(), event.y.toInt(), event.getPointerId(pc))
            }
            MotionEvent.ACTION_POINTER_UP -> {
                return true
            }
            else -> return false
        }
        postInvalidate()
        return false
    }

    override fun onDraw(canvas: Canvas)
    {
        /*canvas.drawPath(path,brush)
        canvasHeight=canvas.height
        canvasWidth=canvas.width*/
        if(allStroke.isNotEmpty()){
            for(stroke in allStroke){
                    val path = stroke.Spath
                    val paint = stroke.Spaint
                    if(path!= null){
                        canvas.drawPath(path, paint)
                    }
            }
        }
    }

    private fun uploadToDatabase(pointX:Float, pointY:Float , type: Int) {
        val info =
            Information(pointX, pointY, type)
        postReference.push().setValue(info)
    }


    private fun pointDown(x: Int, y: Int, id: Int) {
        //create a paint with random color
        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = brushWidth
        paint.color = color

        //create the Stroke
        val pt = Point(x, y)
        val stroke = Stroke(paint)
        stroke.addPoint(pt)
        activeStroke.put(id, stroke)
        allStroke.add(stroke)
    }

    private fun pointMove(x: Int, y: Int, id: Int) {
        //retrieve the stroke and add new point to its path
        val stroke: Stroke = activeStroke.get(id)
        if (stroke != null) {
            val pt = Point(x, y)
            stroke.addPoint(pt)
        }
    }
}
