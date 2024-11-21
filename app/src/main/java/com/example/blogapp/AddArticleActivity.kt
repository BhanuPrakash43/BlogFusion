package com.example.blogapp

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.Model.UserData
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date

class AddArticleActivity : AppCompatActivity() {

    private val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance("https://blog-app-219a7-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("blogs")
    private val userReference: DatabaseReference =
        FirebaseDatabase.getInstance("https://blog-app-219a7-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("users")
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_article)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val backImageButton = findViewById<ImageButton>(R.id.addImageButton)
        val blogTitle = findViewById<TextInputLayout>(R.id.blogTitle)
        val blogDescription = findViewById<TextInputLayout>(R.id.blogDescription)

        val addNewBlogButton = findViewById<Button>(R.id.addNewBlogButton)

        backImageButton.setOnClickListener {
            finish()
        }

        addNewBlogButton.setOnClickListener {

            val title = blogTitle.editText?.text.toString().trim()
            val description = blogDescription.editText?.text.toString().trim()

            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            } else {
                // Get current user
                val user: FirebaseUser? = auth.currentUser

                if (user != null) {
                    val userId = user.uid

                    // Fetch username and user profile from database
                    userReference.child(userId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val userData = snapshot.getValue(UserData::class.java)
                                if (userData != null) {
                                    val userNameFromDB = userData.name
                                    val userImageUrlFromDB = userData.profileImage

                                    val currentDate = SimpleDateFormat("yyyy-MM-dd").format(Date())

                                    // Create a BlogItemModel
                                    val blogItem = BlogItemModel(
                                        heading = title,
                                        username = userNameFromDB,
                                        date = currentDate,
                                        userId = userId,
                                        post = description,
                                        likeCount = 0,
                                        profileImage = userImageUrlFromDB
                                    )

                                    // Generate a unique key for the blog post
                                    val key = databaseReference.push().key
                                    if (key != null) {
                                        blogItem.postId = key
                                        val blogReference = databaseReference.child(key)
                                        blogReference.setValue(blogItem).addOnCompleteListener {
                                            if (it.isSuccessful) {
                                                finish()
                                            } else {
                                                Toast.makeText(
                                                    this@AddArticleActivity,
                                                    "Failed to add blog",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(
                                    this@AddArticleActivity,
                                    "Failed to retrieve user data",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                }
            }
        }
    }
}
