package com.example.blogapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.blogapp.Model.UserData
import com.example.blogapp.databinding.ActivitySignInAndRegistrationBinding
import com.example.blogapp.register.WelcomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class SignInAndRegistrationActivity : AppCompatActivity() {
    private val binding: ActivitySignInAndRegistrationBinding by lazy {
        ActivitySignInAndRegistrationBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.signinBackImageBtn.setOnClickListener {
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }

        // Initialize firebase authentication
        auth = FirebaseAuth.getInstance()
        database =
            FirebaseDatabase.getInstance("https://blog-app-219a7-default-rtdb.asia-southeast1.firebasedatabase.app")  // This url added because i changed the region
        storage = FirebaseStorage.getInstance()

        // Adjust visibility for login
        val action = intent.getStringExtra("action")
        if (action == "login") {
            binding.loginEmailAddress.visibility = View.VISIBLE
            binding.loginPassword.visibility = View.VISIBLE
            binding.loginButton.visibility = View.VISIBLE

            binding.registerNewHere.isEnabled = false
            binding.registerNewHere.alpha = 0.5f
            binding.registerButton.isEnabled = false
            binding.registerButton.alpha = 0.5f

            binding.registerName.visibility = View.GONE
            binding.registerEmail.visibility = View.GONE
            binding.registerPassword.visibility = View.GONE
            binding.cardView.visibility = View.GONE

            binding.loginButton.setOnClickListener {
                val loginEmail: String = binding.loginEmailAddress.text.toString()
                val loginPassword: String = binding.loginPassword.text.toString()

                if (loginEmail.isEmpty() || loginPassword.isEmpty()) {
                    Toast.makeText(this, "Please fill all the details", Toast.LENGTH_SHORT).show()
                } else {
                    auth.signInWithEmailAndPassword(loginEmail, loginPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Login Successfull", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(
                                    this,
                                    "Login failed. Please enter correct details",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            }
        } else if (action == "register") {
            binding.loginButton.isEnabled = false
            binding.loginButton.alpha = 0.5f

            binding.registerButton.setOnClickListener {
                val registerName = binding.registerName.text.toString()
                val registerEmail = binding.registerEmail.text.toString()
                val registerPassword = binding.registerPassword.text.toString()

                if (registerName.isEmpty() || registerEmail.isEmpty() || registerPassword.isEmpty()) {
                    Toast.makeText(this, "Please fill all the details", Toast.LENGTH_SHORT).show()
                } else {
                    auth.createUserWithEmailAndPassword(registerEmail, registerPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                auth.signOut()
                                user?.let {
                                    // Save user data into Firebase real time database
                                    val userReference = database.getReference("users")
                                    val userId = user.uid
                                    val userData = UserData(
                                        registerName,
                                        registerEmail,
                                        registerPassword
                                    )
                                    userReference.child(userId).setValue(userData)

                                    // Upload image to Firebase storage
                                    val storageReference: StorageReference =
                                        storage.reference.child("profile_image/$userId.jpg")
                                    storageReference.putFile(imageUri!!)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                storageReference.downloadUrl.addOnCompleteListener { imageUri ->
                                                    val imageUrl = imageUri.result.toString()

                                                    // Save the image url to the real time database
                                                    userReference.child(userId)
                                                        .child("profileImage")
                                                        .setValue(imageUrl)
                                                }
                                            }
                                        }

                                    Toast.makeText(
                                        this,
                                        "User Register Successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    startActivity(Intent(this, WelcomeActivity::class.java))
                                    finish()
                                }
                            } else {
                                Toast.makeText(this, "User Registration Failed", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                }
            }
        }

        // Set default avatar image initially
        Glide.with(this)
            .load(R.drawable.img2)  // Replace with your default avatar drawable
            .apply(RequestOptions.circleCropTransform())
            .into(binding.registerUserImage)

        // set on clicklistner for the choose image
        binding.cardView.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Select Image"),
                PICK_IMAGE_REQUEST
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null)
            imageUri = data.data
        Glide.with(this)
            .load(imageUri)
            .apply(RequestOptions.circleCropTransform())
            .into(binding.registerUserImage)

    }
}
