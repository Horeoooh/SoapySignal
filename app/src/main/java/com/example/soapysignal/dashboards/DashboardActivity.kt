package com.example.soapysignal.dashboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.example.soapysignal.R
import com.example.soapysignal.history.HistoryActivity
import com.example.soapysignal.home.HomeActivity

class DashboardActivity : Activity() {
    private lateinit var statusText: TextView
    private lateinit var statusDescription: TextView
    private lateinit var spinningContainer: RelativeLayout
    private var spinAnimation: RotateAnimation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Initialize views
        statusText = findViewById(R.id.statusText)
        statusDescription = findViewById(R.id.statusDescription)
        spinningContainer = findViewById(R.id.spinningContainer)

        // Set up spinning animation
        startSpinAnimation()

        // Set up bottom navigation
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        try {
            // Try to find individual navigation items if they exist
            val homeNav = findViewById<LinearLayout>(R.id.homeNav)
            val historyNav = findViewById<LinearLayout>(R.id.historyNav)
            val settingsNav = findViewById<LinearLayout>(R.id.settingsNav)

            if (homeNav != null && historyNav != null && settingsNav != null) {
                homeNav.setOnClickListener {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                historyNav.setOnClickListener {
                    val intent = Intent(this, HistoryActivity::class.java)
                    startActivity(intent)
                }

                settingsNav.setOnClickListener {
                    Toast.makeText(this, "Settings coming soon", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Fallback: use parent container and child indexes
                val bottomNavParent = findViewById<LinearLayout>(R.id.bottomNav)
                if (bottomNavParent != null && bottomNavParent.childCount >= 3) {
                    val homeNavChild = bottomNavParent.getChildAt(0) as LinearLayout
                    val historyNavChild = bottomNavParent.getChildAt(1) as LinearLayout
                    val settingsNavChild = bottomNavParent.getChildAt(2) as LinearLayout

                    homeNavChild.setOnClickListener {
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    }

                    historyNavChild.setOnClickListener {
                        val intent = Intent(this, HistoryActivity::class.java)
                        startActivity(intent)
                    }

                    settingsNavChild.setOnClickListener {
                        Toast.makeText(this, "Settings coming soon", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Navigation error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startSpinAnimation() {
        spinAnimation = RotateAnimation(
            0f, 360f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 2000
            repeatCount = Animation.INFINITE
            interpolator = LinearInterpolator()
        }
        spinningContainer.startAnimation(spinAnimation)
    }

    private fun stopSpinAnimation() {
        spinningContainer.clearAnimation()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSpinAnimation()
    }
}