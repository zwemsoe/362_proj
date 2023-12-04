package com.example.travelassistant

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.travelassistant.databinding.ActivityMainBinding
import com.example.travelassistant.models.user.UserRepository
import com.example.travelassistant.viewModels.NavigationViewModel
import com.example.travelassistant.viewModels.OnboardingViewModel
import com.example.travelassistant.viewModels.UserViewModel
import com.example.travelassistant.viewModels.UserViewModelFactory
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var userRepository: UserRepository
    private lateinit var userViewModel: UserViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController
    private lateinit var navigationViewModel: NavigationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        auth = FirebaseAuth.getInstance()

        val drawerLayout: DrawerLayout = binding.drawerLayout
        navController = findNavController(R.id.nav_host_fragment_content_main)

        val navView: NavigationView = binding.navView
        val headerView = navView.getHeaderView(0)
        val nameTextView = headerView.findViewById<TextView>(R.id.header_profile_name)
        val emailTextView = headerView.findViewById<TextView>(R.id.header_profile_email)
        val imageView: ImageView = headerView.findViewById(R.id.header_profile_image)

        userRepository = UserRepository()
        userViewModel = ViewModelProvider(
            this, UserViewModelFactory(userRepository)
        )[UserViewModel::class.java]

        navigationViewModel = ViewModelProvider(this)[NavigationViewModel::class.java]

        userViewModel.getUser(auth.currentUser!!.uid)

        userViewModel.user.observe(this) {
            if (it != null) {
                nameTextView.text = it.displayName
                emailTextView.text = it.email
                Glide.with(this).load(auth.currentUser!!.photoUrl).into(imageView)
            }

        }

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_todo,
                R.id.nav_myevents,
                R.id.nav_myprofile,
                R.id.nav_user_reputations,
                R.id.nav_settings,
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        myProfileNavMenuItemClick()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.signout -> {
                Firebase.auth.signOut()
                val intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun myProfileNavMenuItemClick() {
        val navView = findViewById<NavigationView>(R.id.nav_view)
        navView.menu.findItem(R.id.nav_myprofile).setOnMenuItemClickListener {
            navigationViewModel.showCurrentUser.value = true
            false
        }
    }

}