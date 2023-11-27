package com.example.travelassistant

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.core.animation.doOnEnd
import com.example.travelassistant.utils.ANIMATION_DURATION
import com.example.travelassistant.utils.slideUpAnimation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

private const val SPLASH_DURATION = 3000L

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        setupSplashExitAnimation()

        auth = Firebase.auth

        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = auth.currentUser
            var intent: Intent = if(currentUser != null){
                Intent(this, MainActivity::class.java)
            }else {
                Intent(this, SignInActivity::class.java)
            }
            startActivity(intent)
            finish()
        }, SPLASH_DURATION)
    }



    private fun setupCustomSplashEnterAnimation() {
        val rootView = findViewById<View>(android.R.id.content)
        rootView.slideUpAnimation().start()
    }

    /**
     * @source: https://developer.android.com/develop/ui/views/launch/splash-screen
     */
    private fun setupSplashExitAnimation() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return
        }

        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val slideUp = ObjectAnimator.ofFloat(
                splashScreenView, View.TRANSLATION_Y, 0f, -splashScreenView.height.toFloat()
            )
            slideUp.interpolator = AnticipateInterpolator()
            slideUp.duration = ANIMATION_DURATION
            slideUp.doOnEnd { splashScreenView.remove() }

            slideUp.start()
            setupCustomSplashEnterAnimation()
        }
    }
}