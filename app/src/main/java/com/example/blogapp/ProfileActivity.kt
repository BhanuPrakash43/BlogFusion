package com.example.blogapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.blogapp.register.WelcomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var userProfileImage: ImageView
    private lateinit var userName: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val backImageButton = findViewById<ImageButton>(R.id.backImageButton)
        userProfileImage = findViewById(R.id.userProfile)
        userName = findViewById(R.id.userName)
        val addNewBlogButton = findViewById<Button>(R.id.addNewBlogButton)
        val articlesButton = findViewById<Button>(R.id.articlesButton)
        val logoutButton = findViewById<Button>(R.id.logoutButton)

        backImageButton.setOnClickListener {
            finish()
        }

        // To go to add article page
        addNewBlogButton.setOnClickListener {
            startActivity(Intent(this, AddArticleActivity::class.java))
        }

        // To go to Your Article Activity
        articlesButton.setOnClickListener {
            startActivity(Intent(this, ArticleActivity::class.java))
        }

        // To logout
        logoutButton.setOnClickListener {
            auth.signOut()

            // navigate
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        databaseReference =
            FirebaseDatabase.getInstance("https://blog-app-219a7-default-rtdb.asia-southeast1.firebasedatabase.app").reference.child(
                "users"
            )

        val userId = auth.currentUser?.uid

        if (userId != null) {
            loadUserProfileData(userId)
        }
    }

    private fun loadUserProfileData(userId: String) {
        val userReference = databaseReference.child(userId)

        // Load user profile image
        userReference.child("profileImage").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profileImageUrl = snapshot.getValue(String::class.java)
                if (profileImageUrl != null)
                    Glide.with(this@ProfileActivity)
                        .load(profileImageUrl)
                        .into(userProfileImage)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@ProfileActivity,
                    "Failed to load user image",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        // Load username
        userReference.child("name").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fetchedUserName = snapshot.getValue(String::class.java)

                if (userName != null) {
                    userName.text = fetchedUserName
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}