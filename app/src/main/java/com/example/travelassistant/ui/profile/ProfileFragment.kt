package com.example.travelassistant.ui.profile


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.travelassistant.R
import com.example.travelassistant.models.user.UserRepository
import com.example.travelassistant.utils.CommonUtil
import com.example.travelassistant.utils.CoordinatesUtil
import com.example.travelassistant.viewModels.ProfileViewModel
import com.example.travelassistant.viewModels.ProfileViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {
    private lateinit var userRepository: UserRepository
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var locationTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = FirebaseAuth.getInstance()
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userRepository = UserRepository()
        profileViewModel = ViewModelProvider(
            requireActivity(), ProfileViewModelFactory(userRepository)
        )[ProfileViewModel::class.java]

        locationTextView = view.findViewById(R.id.profile_last_visited)
        val profileNameTextView = view.findViewById<TextView>(R.id.profile_name)
        val reputationTextView = view.findViewById<TextView>(R.id.profile_reputation)
        val joinedTextView = view.findViewById<TextView>(R.id.profile_join_date)
        val profileImage = view.findViewById<ImageView>(R.id.profile_image)

        val profileId = arguments?.getString(PROFILE_ID)

        if(profileId == null){
            profileViewModel.getUser(auth.currentUser!!.uid)
        }

        profileViewModel.user.observe(viewLifecycleOwner) {
            if(it != null){
                profileNameTextView.text = it.displayName
                reputationTextView.text = it.points.toString()
                joinedTextView.text = CommonUtil.formatDate(it.createdAt)
                if (it.imageUrl.toString().isNotEmpty()) {
                    Glide.with(this)
                        .load(it.imageUrl)
                        .into(profileImage)
                }

                if(it.currentLocation != null && !it.keepLocationPrivate){
                    setCurrentLocation(it.currentLocation)
                }
            }
        }
    }


    private fun setCurrentLocation(currentLocation: GeoPoint){
        lifecycleScope.launch {
            val addressList =
                CoordinatesUtil.getAddressFromLocation(requireContext(), currentLocation)
            withContext(Dispatchers.Main) {
                if (addressList.isNotEmpty()) {
                    locationTextView.text = addressList[0].getAddressLine(0)
                }
            }
        }
    }

    companion object{
        val PROFILE_ID = "profile_id"
    }
}