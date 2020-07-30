package com.thelazypeople.scribbl

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_game.*
import java.lang.ref.Reference

class GameActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var postReference: DatabaseReference
    private lateinit var childEventListener: ChildEventListener
    private var reference: String? = ""

    private var downloadText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val intent: Intent = intent
        reference = intent.getStringExtra("reference")

        database = Firebase.database.reference
        if (reference != null) {
            postReference = database.child("game").child(reference.toString())

            childEventListener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val textValue = snapshot.getValue<String>()
                    downloadText += textValue + "\n"
                    textHolder.text = downloadText
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
                    // not needed
                }

            }
            postReference.addChildEventListener(childEventListener)
        }

        button.setOnClickListener {
            if (editText.text.toString() == "") {
                Toast.makeText(this, "Empty text", Toast.LENGTH_SHORT).show()
            } else {
                if(reference != null){
                    postReference.child("text").setValue(editText.text.toString().trim())
                    editText.text.clear()
                }else{
                    Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
}