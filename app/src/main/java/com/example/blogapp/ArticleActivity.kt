package com.example.blogapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.adapter.ArticleAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ArticleActivity : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private val auth = FirebaseAuth.getInstance()
    private lateinit var blogAdapter: ArticleAdapter
    private val EDIT_BLOG_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_article)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val backButton = findViewById<ImageButton>(R.id.backButton)
        val recyclerView = findViewById<RecyclerView>(R.id.articleRecyclerView)
        val noPostsTextView = findViewById<TextView>(R.id.noPostsTextView)

        backButton.setOnClickListener {
            finish()
        }

        val currentUserId = auth.currentUser?.uid
        recyclerView.layoutManager = LinearLayoutManager(this)

        if (currentUserId != null) {
            blogAdapter =
                ArticleAdapter(this, emptyList(), object : ArticleAdapter.OnItemClickListener {
                    override fun onEditClick(blogItem: BlogItemModel) {
                        val intent = Intent(this@ArticleActivity, EditBlogActivity::class.java)
                        intent.putExtra("blogItem", blogItem)
                        startActivityForResult(intent, EDIT_BLOG_REQUEST_CODE)
                    }

                    override fun onReadMoreClick(blogItem: BlogItemModel) {
                        val intent = Intent(this@ArticleActivity, ReadMoreActivity::class.java)
                        intent.putExtra("blogItem", blogItem)
                        startActivity(intent)
                    }

                    override fun onDeleteClick(blogItem: BlogItemModel) {
                        deleteBlogPost(blogItem)
                    }
                })
        }
        recyclerView.adapter = blogAdapter

        // Get saved blog data from Database
        databaseReference =
            FirebaseDatabase.getInstance("https://blog-app-219a7-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("blogs")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val blogSavedList = ArrayList<BlogItemModel>()
                for (postSnapshot in snapshot.children) {
                    val blogSaved = postSnapshot.getValue(BlogItemModel::class.java)
                    if (blogSaved != null && currentUserId == blogSaved.userId) {
                        blogSavedList.add(blogSaved)
                    }
                }

                blogSavedList.reverse()

                if (blogSavedList.isEmpty()) {
                    // If no blogs are written, show the "No Posts" message and hide RecyclerView
                    noPostsTextView.visibility = TextView.VISIBLE
                    recyclerView.visibility = RecyclerView.GONE
                } else {
                    // Hide the "No Posts" message and show RecyclerView with blogs
                    noPostsTextView.visibility = TextView.GONE
                    recyclerView.visibility = RecyclerView.VISIBLE
                }

                blogAdapter.setData(blogSavedList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ArticleActivity, "Error loading Saved blog", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun deleteBlogPost(blogItem: BlogItemModel) {
        val postId = blogItem.postId
        val blogPostReference = databaseReference.child(postId)

        // Remove the blog post
        blogPostReference.removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Blog Post Deleted Successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Blog post failed to delete", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_BLOG_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

        }
    }
}
