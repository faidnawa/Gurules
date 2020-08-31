package com.skfaid.gurules.ui.detailguru

import android.os.Bundle
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import coil.api.load
import com.google.firebase.database.*
import com.skfaid.gurules.R
import com.skfaid.gurules.model.Detailguru
import com.skfaid.gurules.util.Constants
import kotlinx.android.synthetic.main.activity_detail_guru.*
import org.jetbrains.anko.toast


class DetailGuruActivity : AppCompatActivity() {
    var mConstraintSet1 = ConstraintSet() // create a Constraint Set
    var mConstraintSet2 = ConstraintSet()
    var mConstraintLayout : ConstraintLayout? = null
    private var isOpen = false
    private var uid: String? = ""
    private lateinit var databaseReference: DatabaseReference
    private var gurudetail: MutableList<Detailguru> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_detail_guru)
        mConstraintLayout=findViewById(R.id.layout_detail_guru)
        mConstraintSet2.clone(this,R.layout.profile_guru_expanded)
        mConstraintSet1.clone(mConstraintLayout)

        photokuu.setOnClickListener(View.OnClickListener {
            if (!isOpen) {
                TransitionManager.beginDelayedTransition(mConstraintLayout)
                mConstraintSet2.applyTo(mConstraintLayout)
                isOpen = !isOpen
            } else {
                TransitionManager.beginDelayedTransition(mConstraintLayout)
                mConstraintSet1.applyTo(mConstraintLayout)
                isOpen = !isOpen
            }
        })
        cover.setOnClickListener(View.OnClickListener {
            if (!isOpen) {
                TransitionManager.beginDelayedTransition(mConstraintLayout)
                mConstraintSet2.applyTo(mConstraintLayout)
                isOpen = !isOpen
            } else {
                TransitionManager.beginDelayedTransition(mConstraintLayout)
                mConstraintSet1.applyTo(mConstraintLayout)
                isOpen = !isOpen
            }
        })

//        val context: Context = this

        databaseReference = FirebaseDatabase.getInstance().reference
        val intent = intent
        uid = intent.getStringExtra(Constants.UID)
        getShop(uid.toString())
    }

    fun getShop(uid:String) {
        databaseReference.child("guru")
            .orderByChild("uid")
            .equalTo(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Error", "LoadDatabase: onCancelled", databaseError.toException())
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val content = dataSnapshot.children.flatMap {
                        mutableListOf(it.getValue(Detailguru::class.java))

                    }
                    gurudetail.addAll(content as List<Detailguru>)
                    gurudetail.forEach{respondata->
                        supportActionBar?.apply {
                            title = respondata.nama
                            setDisplayHomeAsUpEnabled(true)
                            setDisplayShowHomeEnabled(true)
                        }
//                        Log.d("detailguru", respondata.nama.toString())
                        photokuu.load(respondata.foto) {
                            placeholder(R.drawable.loading_animation)
                            error(R.drawable.ic_broken_image)
                        }
                        cover.load(respondata.foto) {
                            placeholder(R.drawable.loading_animation)
                            error(R.drawable.ic_broken_image)
                        }
                        toast(respondata.nama.toString())
                        tv_nama_guru.text=respondata.nama


                    }
                }
            })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}
