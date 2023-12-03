package com.example.travelassistant.ui.reputations


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.travelassistant.R
import com.example.travelassistant.models.user.UserRepository
import com.example.travelassistant.ui.profile.ProfileFragment
import com.example.travelassistant.viewModels.LeaderboardViewModel
import com.example.travelassistant.viewModels.LeaderboardViewModelFactory
import com.example.travelassistant.viewModels.ProfileViewModel
import com.example.travelassistant.viewModels.ProfileViewModelFactory
import com.example.travelassistant.viewModels.UserViewModel
import com.example.travelassistant.viewModels.UserViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class ReputationsFragment : Fragment() {
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var viewModel: LeaderboardViewModel
    private lateinit var userRepository: UserRepository
    private lateinit var auth: FirebaseAuth
    private lateinit var pointsTextView: TextView
    private lateinit var pointDetailsTextView: TextView
    private lateinit var profileImageView: ImageView
    private lateinit var adapter: UserListAdapter
    private lateinit var recyclerView: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = FirebaseAuth.getInstance()
        return inflater.inflate(R.layout.fragment_reputations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userRepository = UserRepository()
        viewModel = ViewModelProvider(
            this@ReputationsFragment, LeaderboardViewModelFactory(userRepository)
        )[LeaderboardViewModel::class.java]

        profileViewModel = ViewModelProvider(
            requireActivity(), ProfileViewModelFactory(userRepository)
        )[ProfileViewModel::class.java]

        userViewModel = ViewModelProvider(
            requireActivity(), UserViewModelFactory(userRepository)
        )[UserViewModel::class.java]

        pointsTextView = view.findViewById(R.id.textViewReputationTitle)
        pointDetailsTextView = view.findViewById(R.id.textViewReputationDetail)
        profileImageView = view.findViewById(R.id.imageViewProfile)

        recyclerView = view.findViewById(R.id.leaderboardList)
        recyclerView.layoutManager = LinearLayoutManager(context)

        if (userViewModel.user.value == null) {
            userViewModel.getUser(auth.currentUser!!.uid)
        }

        viewModel.getTopUsers()

        observeViewModels()
    }

    private fun observeViewModels() {
        userViewModel.user.observe(viewLifecycleOwner) {
            pointsTextView.text = "Your reputation point is ${it.points}!"
            pointDetailsTextView.text =
                "Which means you have visited ${it.points} unique locations so far!"
            Glide.with(this)
                .load(auth.currentUser!!.photoUrl)
                .into(profileImageView)
        }

        viewModel.users.observe(viewLifecycleOwner) {
            adapter =
                UserListAdapter(it) { user ->
                    val args = Bundle()
                    args.putString(ProfileFragment.PROFILE_ID, user.id)
                    profileViewModel.getUser(user.id)
                    findNavController().navigate(R.id.action_nav_reputations_to_nav_profile, args)
                }
            recyclerView.adapter = adapter
        }
    }
}