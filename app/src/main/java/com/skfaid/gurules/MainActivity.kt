package com.skfaid.gurules
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.skfaid.gurules.ui.completeuserprofile.CompleteUserProfileActivity
import com.skfaid.gurules.ui.login.LoginActivity
import com.skfaid.gurules.model.UsersProfile
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity() {

    lateinit var mAuth: FirebaseAuth
    lateinit var databaseReference : DatabaseReference
    private var contentusercek: MutableList<UsersProfile> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onStart() {
        super.onStart()
        checkUserData()
    }

    private fun checkUserData() {
        if (mAuth.currentUser !=null){
            databaseReference.child("users").orderByChild("uid").equalTo(mAuth.currentUser?.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(databaseError: DatabaseError) {
                         Log.e("Error", "LoadDatabase: onCancelled", databaseError.toException())
                    }
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val content = dataSnapshot.children.flatMap {
                            mutableListOf(it.getValue(UsersProfile::class.java))
                        }
                        contentusercek.addAll(content as List<UsersProfile>)
                        if (contentusercek?.isNotEmpty() == false) returnToCompleteUserProfile()
                        Log.d("userlogin", mAuth.currentUser?.uid.toString())
                        Log.d("useruidprofile", contentusercek.toString())
                    }
                })
        }else{
            returnToSignInActivity()
        }

    }
    fun returnToCompleteUserProfile() {
        finish() // close this activity
        startActivity<CompleteUserProfileActivity>() // open complete user profile activity
    }
    fun returnToSignInActivity() {
        finish() // close this activity
        startActivity<LoginActivity>() // open sign in activity
    }

    }


