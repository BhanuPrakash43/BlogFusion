package com.example.blogapp

import android.os.Bundle
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.blogapp.Model.BlogItemModel

class ReadMoreActivity : AppCompatActivity() {

    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private var scaleFactor = 1f
    private var isZooming = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_read_more)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val backButton = findViewById<ImageButton>(R.id.backButton)
        val blogDescription = findViewById<TextView>(R.id.blogDescriptionTextView)
        val zoomContainer = findViewById<ScrollView>(R.id.zoomContainer)

        backButton.setOnClickListener {
            finish()
        }

        val blogs = intent.getParcelableExtra<BlogItemModel>("blogItem")
        if (blogs != null) {
            findViewById<TextView>(R.id.titleText).text = blogs.heading
            findViewById<TextView>(R.id.userName).text = blogs.username
            findViewById<TextView>(R.id.date).text = blogs.date
            blogDescription.text = blogs.post

            val userImageUrl = blogs.profileImage
            Glide.with(this)
                .load(userImageUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(findViewById(R.id.profileImage))
        } else {
            Toast.makeText(this, "Failed to load blogs", Toast.LENGTH_SHORT).show()
        }

        // Initialize ScaleGestureDetector
        scaleGestureDetector = ScaleGestureDetector(
            this,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    scaleFactor *= detector.scaleFactor
                    scaleFactor = scaleFactor.coerceIn(1f, 2f)
                    blogDescription.scaleX = scaleFactor
                    blogDescription.scaleY = scaleFactor
                    isZooming = true
                    return true
                }
            })

        // Attach touch listener to ScrollView
        zoomContainer.setOnTouchListener { _, event ->
            if (event.pointerCount > 1) {
                // Pass pinch-to-zoom events to ScaleGestureDetector
                scaleGestureDetector.onTouchEvent(event)
                true
            } else {
                // Allow default ScrollView behavior for single-touch events
                if (!isZooming) {
                    zoomContainer.onTouchEvent(event)
                }

                // Reset `isZooming` if the user stops touching
                if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                    isZooming = false
                }
                true
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        // Pass touch events to ScaleGestureDetector
        if (event != null) {
            scaleGestureDetector.onTouchEvent(event)
        }
        return super.onTouchEvent(event)
    }
}

