package com.example.blogapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.adapter.BlogAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private val blogItems = mutableListOf<BlogItemModel>()
    private lateinit var auth: FirebaseAuth
    private lateinit var profileImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val savedArticleButton = findViewById<ImageButton>(R.id.savedArticleButton)
        val cardViewImg = findViewById<CardView>(R.id.cardView2)
        profileImage = findViewById(R.id.profile_image)
        val recyclerView = findViewById<RecyclerView>(R.id.blogRecyclerView)
        val floatingAddArticleButton =
            findViewById<FloatingActionButton>(R.id.floatingAddArticleButton)

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance(
            "https://blog-app-219a7-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).reference.child("blogs")

        // Redirect to Saved Articles
        savedArticleButton.setOnClickListener {
            startActivity(Intent(this, SavedArticlesActivity::class.java))
        }

        // Redirect to Profile
        cardViewImg.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Load User Profile Image
        val userId = auth.currentUser?.uid
        if (userId != null) {
            loadUserProfileImage(userId)
        }

        // Set up RecyclerView

        val blogAdapter = BlogAdapter(blogItems)
        recyclerView.adapter = blogAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch data from Firebase
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                blogItems.clear()
                for (snapshot in snapshot.children) {
                    val blogItem = snapshot.getValue(BlogItemModel::class.java)
                    if (blogItem != null) {
                        blogItems.add(blogItem)
                    }
                }
                blogItems.reverse() // Reverse the list for correct order
                blogAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Blog loading failed", Toast.LENGTH_SHORT).show()
            }
        })

        // Floating Action Button for Adding Articles
        floatingAddArticleButton.setOnClickListener {
            startActivity(Intent(this, AddArticleActivity::class.java))
        }
    }

    private fun loadUserProfileImage(userId: String) {
        val userReference = FirebaseDatabase.getInstance(
            "https://blog-app-219a7-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).reference.child("users").child(userId)

        userReference.child("profileImage").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profileImageUrl = snapshot.getValue(String::class.java)
                if (profileImageUrl != null) {
                    Glide.with(this@MainActivity)
                        .load(profileImageUrl)
                        .apply(RequestOptions.circleCropTransform())
                        .into(profileImage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error loading profile image", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }
}
