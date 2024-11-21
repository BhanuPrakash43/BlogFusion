package com.example.blogapp.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.R
import com.example.blogapp.ReadMoreActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BlogAdapter(private val items: MutableList<BlogItemModel>) :
    RecyclerView.Adapter<BlogAdapter.BlogViewHolder>() {

    private val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance("https://blog-app-219a7-default-rtdb.asia-southeast1.firebasedatabase.app").reference
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.blog_item, parent, false)
        return BlogViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {
        val blogItem = items[position]
        holder.bind(blogItem)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class BlogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val heading: TextView = itemView.findViewById(R.id.heading)
        private val profile: ImageView = itemView.findViewById(R.id.profile)
        private val username: TextView = itemView.findViewById(R.id.username)
        private val date: TextView = itemView.findViewById(R.id.date)
        private val post: TextView = itemView.findViewById(R.id.post)
        private val readMoreButton: TextView = itemView.findViewById(R.id.readMoreButton)
        private val likeCount: TextView = itemView.findViewById(R.id.like_count)
        private val likeButton: ImageButton = itemView.findViewById(R.id.like_button)
        private val postSaveButton: ImageButton = itemView.findViewById(R.id.post_save_button)

        fun bind(blogItemModel: BlogItemModel) {
            val postId = blogItemModel.postId
            val context = itemView.context

            heading.text = blogItemModel.heading
            Glide.with(profile.context).load(blogItemModel.profileImage).into(profile)
            username.text = blogItemModel.username
            date.text = blogItemModel.date
            post.text = blogItemModel.post
            likeCount.text = blogItemModel.likeCount.toString()

            readMoreButton.setOnClickListener {
                val intent = Intent(context, ReadMoreActivity::class.java)
                intent.putExtra("blogItem", blogItemModel)
                context.startActivity(intent)
            }

            val postLikeReference = databaseReference.child("blogs").child(postId).child("likes")
            postLikeReference.child(currentUser?.uid ?: "")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            likeButton.setImageResource(R.drawable.redheart)
                        } else {
                            likeButton.setImageResource(R.drawable.blackheart)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })

            likeButton.setOnClickListener {
                if (currentUser != null) {
                    handleLikedButtonClicked(postId, blogItemModel)
                } else {
                    Toast.makeText(
                        context,
                        "You have to login first to like this post",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            val userReference = databaseReference.child("users").child(currentUser?.uid ?: "")
            val postSaveReference = userReference.child("saveBlogPosts").child(postId)

            postSaveReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        postSaveButton.setImageResource(R.drawable.redsavesign)
                    } else {
                        postSaveButton.setImageResource(R.drawable.redsave)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

            postSaveButton.setOnClickListener {
                if (currentUser != null) {
                    handleSaveButtonClicked(postId, blogItemModel, postSaveButton)
                } else {
                    Toast.makeText(
                        context,
                        "You have to login first to save this post",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun handleLikedButtonClicked(postId: String, blogItemModel: BlogItemModel) {
        val userReference = databaseReference.child("users").child(currentUser!!.uid)
        val postLikeReference = databaseReference.child("blogs").child(postId).child("likes")

        postLikeReference.child(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        userReference.child("likes").child(postId).removeValue()
                            .addOnSuccessListener {
                                postLikeReference.child(currentUser.uid).removeValue()
                                blogItemModel.likedBy?.remove(currentUser.uid)
                                notifyDataSetChanged()

                                val newLikeCount = blogItemModel.likeCount - 1
                                blogItemModel.likeCount = newLikeCount
                                databaseReference.child("blogs").child(postId).child("likeCount")
                                    .setValue(newLikeCount)
                            }
                    } else {
                        userReference.child("likes").child(postId).setValue(true)
                            .addOnSuccessListener {
                                postLikeReference.child(currentUser.uid).setValue(true)
                                blogItemModel.likedBy?.add(currentUser.uid)
                                notifyDataSetChanged()

                                val newLikeCount = blogItemModel.likeCount + 1
                                blogItemModel.likeCount = newLikeCount
                                databaseReference.child("blogs").child(postId).child("likeCount")
                                    .setValue(newLikeCount)
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun handleSaveButtonClicked(
        postId: String,
        blogItemModel: BlogItemModel,
        postSaveButton: ImageButton
    ) {
        val userReference = databaseReference.child("users").child(currentUser!!.uid)
        userReference.child("saveBlogPosts").child(postId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        userReference.child("saveBlogPosts").child(postId).removeValue()
                            .addOnSuccessListener {
                                items.find { it.postId == postId }?.isSaved = false
                                notifyDataSetChanged()
                            }
                        postSaveButton.setImageResource(R.drawable.redsave)
                    } else {
                        userReference.child("saveBlogPosts").child(postId).setValue(true)
                            .addOnSuccessListener {
                                items.find { it.postId == postId }?.isSaved = true
                                notifyDataSetChanged()
                            }
                        postSaveButton.setImageResource(R.drawable.redsavesign)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun updateData(savedBlogsArticles: List<BlogItemModel>) {
        items.clear()
        items.addAll(savedBlogsArticles)
        notifyDataSetChanged()
    }
}
