package com.example.grinnet

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.grinnet.data.UserRequest


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
        val username = view.findViewById<ImageView>(R.id.username)
        val description = view.findViewById<ImageView>(R.id.description)
        val followingCount = view.findViewById<ImageView>(R.id.followingCount)
        val followerCount = view.findViewById<ImageView>(R.id.followerCount)

        if (user.image != "") {
            imageProfile.setImageResource(R.drawable.account_icon)
        } else {
            Glide.with(this).load(user.image).centerCrop().into(imageProfile)
        }

        return view
    }
}