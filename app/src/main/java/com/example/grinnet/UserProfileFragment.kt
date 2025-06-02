package com.example.grinnet

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.grinnet.data.FollowRequest
import com.example.grinnet.data.UserEmpty
import com.example.grinnet.data.UserRequest
import com.example.grinnet.notifications.FollowNotificationSender
import com.example.grinnet.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date


/**
 * A simple [Fragment] subclass.
 * Use the [UserProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserProfileFragment : Fragment() {

    private lateinit var user: UserRequest



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            user = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getSerializable("user", UserRequest::class.java)!!
            } else {
                it.getSerializable("user")!! as UserRequest
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_profile, container, false)

        val imageProfile = view.findViewById<ImageView>(R.id.profileImage)
        val followButton = view.findViewById<Button>(R.id.followButton)
        val username = view.findViewById<TextView>(R.id.username)
        val description = view.findViewById<TextView>(R.id.description)
        val followingCount = view.findViewById<TextView>(R.id.followingCount)
        val followerCount = view.findViewById<TextView>(R.id.followerCount)

        username.text = user.username
        description.text = user.description

        Log.d("Imagen", user.image)
        if (user.image == "" || user.image.isEmpty()) {
            imageProfile.setImageResource(R.drawable.account_icon)
        } else {
            Glide.with(this).load(user.image).centerCrop().into(imageProfile)
        }

        followButton.setOnClickListener {
            followThisUser(user, SessionManager.init(requireActivity())!!)
        }

        return view
    }

    private fun followThisUser (userFollowed: UserRequest, idFollower: Long) {
        val follow = FollowRequest(null, UserEmpty(idFollower), userFollowed, Date())
        val call = ApiClient.followService.createFollow(follow)
        call.enqueue(object : Callback<FollowRequest> {
            override fun onResponse(call: Call<FollowRequest>, response: Response<FollowRequest>) {
                if (response.isSuccessful) {
                    sendFollowNotification(userFollowed.tokenPush, idFollower)
                }
            }

            override fun onFailure(call: Call<FollowRequest>, t: Throwable) {
            }

        })
    }

    fun sendFollowNotification(fcmToken: String, followerName: Long) {
        FollowNotificationSender.sendFollowNotification(
            requireActivity(),
            fcmToken,
            followerName
        )
    }
}