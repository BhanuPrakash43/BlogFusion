package com.example.blogapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setMargins
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.blogapp.Model.UserData
import com.example.blogapp.register.WelcomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class SignInAndRegistrationActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var registerUserImage: ImageView
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_in_and_registration)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val signInBackImageBtn = findViewById<ImageButton>(R.id.signinBackImageBtn)
        val loginEmailAddress = findViewById<EditText>(R.id.login_email_address)
        val loginPassword = findViewById<EditText>(R.id.login_password)
        val loginButton = findViewById<Button>(R.id.login_button)
        val registerNewHere = findViewById<TextView>(R.id.register_new_here)
        val registerCardView = findViewById<CardView>(R.id.cardView)
        registerUserImage = findViewById(R.id.registerUserImage)
        val registerName = findViewById<EditText>(R.id.register_name)
        val registerEmail = findViewById<EditText>(R.id.register_email)
        val registerPassword = findViewById<EditText>(R.id.register_password)
        val registerButton = findViewById<Button>(R.id.register_button)
        val textBlogFusion = findViewById<TextView>(R.id.textBlogFusion)

        signInBackImageBtn.setOnClickListener {
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
            loginEmailAddress.visibility = View.VISIBLE
            loginPassword.visibility = View.VISIBLE
            loginButton.visibility = View.VISIBLE

            registerName.visibility = View.GONE
            registerEmail.visibility = View.GONE
            registerPassword.visibility = View.GONE
            registerCardView.visibility = View.GONE
            registerNewHere.visibility = View.GONE
            registerButton.visibility = View.GONE

            loginButton.setOnClickListener {
                val loginEmail: String = loginEmailAddress.text.toString()
                val loginPassword: String = loginPassword.text.toString()

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

            loginButton.visibility = View.GONE
            registerNewHere.visibility = View.GONE

            val layoutParams = textBlogFusion.layoutParams as ViewGroup.MarginLayoutParams
            val marginTop = dpToPx(16, this)
            layoutParams.setMargins(marginTop)
            textBlogFusion.layoutParams = layoutParams

            registerButton.setOnClickListener {
                val registerName = registerName.text.toString()
                val registerEmail = registerEmail.text.toString()
                val registerPassword = registerPassword.text.toString()

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
                                        "User Registered Successfully",
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
            .into(registerUserImage)

        // set on clicklistner for the choose image
        registerCardView.setOnClickListener {
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
            .into(registerUserImage)

    }

    private fun dpToPx(dp: Int, context: Context): Int {
        val displayMetrics = context.resources.displayMetrics
        return (dp * displayMetrics.density).toInt()
    }
}
