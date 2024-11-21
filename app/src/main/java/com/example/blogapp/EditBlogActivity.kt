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
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.FirebaseDatabase

class EditBlogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_blog)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val backButton = findViewById<ImageButton>(R.id.addImageButton)
        val blogTitle = findViewById<TextInputLayout>(R.id.blogTitle)
        val blogDescription = findViewById<TextInputLayout>(R.id.blogDescription)
        val editBlogButton = findViewById<Button>(R.id.editBlogButton)

        backButton.setOnClickListener {
            finish()
        }

        val blogItemModel = intent.getParcelableExtra<BlogItemModel>("blogItem")

        blogTitle.editText?.setText(blogItemModel?.heading)
        blogDescription.editText?.setText(blogItemModel?.post)

        editBlogButton.setOnClickListener {
            val updatedTitle = blogTitle.editText?.text.toString().trim()
            val updatedDescription = blogDescription.editText?.text.toString().trim()

            if (updatedTitle.isEmpty() || updatedDescription.isEmpty()) {
                Toast.makeText(this, "Please fill all the details", Toast.LENGTH_SHORT).show()
            } else {
                blogItemModel?.heading = updatedTitle
                blogItemModel?.post = updatedDescription

                if (blogItemModel != null) {
                    updateDataInFirebase(blogItemModel)
                }
            }
        }

    }

    private fun updateDataInFirebase(blogItemModel: BlogItemModel) {
        val databaseReference =
            FirebaseDatabase.getInstance("https://blog-app-219a7-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("blogs")
        val postId = blogItemModel.postId

        databaseReference.child(postId).setValue(blogItemModel)
            .addOnSuccessListener {
                Toast.makeText(this, "Blog updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Blog updation failed", Toast.LENGTH_SHORT).show()
            }
    }
}