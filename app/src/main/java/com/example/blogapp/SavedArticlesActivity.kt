package com.example.blogapp

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.adapter.BlogAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SavedArticlesActivity : AppCompatActivity() {

    private val savedBlogsArticles = mutableListOf<BlogItemModel>()
    private lateinit var blogAdapter: BlogAdapter
    private val auth = FirebaseAuth.getInstance()

    private lateinit var noSavedBlogsTextView: TextView // Reference to the TextView that displays the message

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_saved_articles)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize TextView for no saved blogs message
        noSavedBlogsTextView = findViewById(R.id.noSavedBlogsTextView)

        // Initialize blogAdapter
        blogAdapter = BlogAdapter(savedBlogsArticles.filter { it.isSaved }.toMutableList())

        val recyclerView = findViewById<RecyclerView>(R.id.savedArticleRecyclerview)
        val backButton = findViewById<ImageButton>(R.id.backButton)

        backButton.setOnClickListener {
            finish()
        }

        recyclerView.adapter = blogAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userReference =
                FirebaseDatabase.getInstance("https://blog-app-219a7-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("users").child(userId).child("saveBlogPosts")

            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (postSnapshot in snapshot.children) {
                        val postId = postSnapshot.key
                        val isSaved = postSnapshot.value as Boolean
                        if (postId != null && isSaved) {
                            // Fetch the corresponding blog item on postId using a coroutine
                            CoroutineScope(Dispatchers.IO).launch {
                                val blogItem = fetchBlogItem(postId)

                                if (blogItem != null) {
                                    savedBlogsArticles.add(blogItem)

                                    launch(Dispatchers.Main) {
                                        blogAdapter.updateData(savedBlogsArticles)
                                        updateUI()
                                    }
                                }
                            }
                        }
                    }

                    // After the loop, check if savedBlogsArticles is empty
                    updateUI()
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }

    private suspend fun fetchBlogItem(postId: String): BlogItemModel? {
        val blogReference =
            FirebaseDatabase.getInstance("https://blog-app-219a7-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("blogs")

        return try {
            val dataSnapshot = blogReference.child(postId).get().await()
            val blogData = dataSnapshot.getValue(BlogItemModel::class.java)
            blogData
        } catch (e: Exception) {
            null
        }
    }

    private fun updateUI() {
        if (savedBlogsArticles.isEmpty()) {
            // If no saved blogs, show the "no saved blogs" message
            noSavedBlogsTextView.visibility = TextView.VISIBLE
        } else {
            // Hide the "no saved blogs" message and show the RecyclerView
            noSavedBlogsTextView.visibility = TextView.GONE
        }
    }
}
