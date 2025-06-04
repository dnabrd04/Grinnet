package com.example.grinnet

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grinnet.adapter.OnUserClickListener
import com.example.grinnet.adapter.PostAdapter
import com.example.grinnet.data.FollowRequest
import com.example.grinnet.data.PostListRequest
import com.example.grinnet.data.PostResponse
import com.example.grinnet.data.UserEmpty
import com.example.grinnet.data.UserRequest
import com.example.grinnet.notifications.FollowNotificationSender
import com.example.grinnet.utils.SessionManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date


/**
 * A simple [Fragment] subclass.
 * Use the [UserProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserProfileFragment : Fragment(), OnUserClickListener{

    private lateinit var list: MutableList<PostResponse>
    private lateinit var user: UserRequest
    private lateinit var followingCount: TextView
    private lateinit var followerCount: TextView
    private lateinit var followButton: Button
    private lateinit var userPostList: RecyclerView
    private lateinit var adapter: PostAdapter

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
        val followButton2 = view.findViewById<Button>(R.id.followButton2)
        val username = view.findViewById<TextView>(R.id.username)
        val description = view.findViewById<TextView>(R.id.description)
        followButton = view.findViewById(R.id.followButton)
        followingCount = view.findViewById(R.id.followingCount)
        followerCount = view.findViewById(R.id.followerCount)
        userPostList = view.findViewById(R.id.userPostList)
        list = mutableListOf<PostResponse>()
        adapter = PostAdapter(list, this.requireContext(), this)
        userPostList.layoutManager = LinearLayoutManager(requireActivity())
        userPostList.adapter = adapter

        showHideButton()
        getFollowerCount()
        getFollowingCount()
        initPostList(user.idUser!!)

        val usernameText = "@${user.username}"
        username.text = usernameText
        description.text = user.description

        if (user.image == "" || user.image.isEmpty()) {
            imageProfile.setImageResource(R.drawable.account_icon)
        } else {
            Glide.with(this).load(user.image).centerCrop().into(imageProfile)
        }

        followButton.setOnClickListener {
            followThisUser(user, SessionManager.init(requireActivity())!!)
        }

        followButton2.setOnClickListener {
            showUnfollowDialog(requireActivity(), user.username, SessionManager.init(requireActivity()) ?: -1L, user.idUser!!)
        }

        return view
    }

    fun initPostList(idUser: Long) {
        val postListRequest = PostListRequest(idUser, Firebase.auth.uid ?: "")
        val call = ApiClient.postService.getPostList(postListRequest)

        call.enqueue(object: Callback<MutableList<PostResponse>> {
            override fun onResponse(call: Call<MutableList<PostResponse>>, response: Response<MutableList<PostResponse>>) {
                if (response.isSuccessful) {
                    adapter.updateData(response.body()!!.asReversed())
                }
            }

            override fun onFailure(call: Call<MutableList<PostResponse>>, t: Throwable) {
            }
        })
    }

    fun showHideButton(){
        val callCheckFollow = ApiClient.followService.checkIfUserFollows(
            user.idUser!!,
            SessionManager.init(requireActivity())!!
        )

        callCheckFollow.enqueue(object : Callback<Boolean> {
            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                if (response.isSuccessful) {
                    val isFollowing = response.body() ?: false

                    if (isFollowing) {
                        followButton.visibility = View.GONE
                    }
                }
            }

            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                Log.d("Follow Check Error", t.message.toString())
            }
        })
    }

    fun getFollowerCount() {
        val callFollowers = ApiClient.followService.getFollowersByUser(user.idUser!!)

        callFollowers.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val text = "${getText(R.string.followerText)} ${response.body().toString()}"
                    followerCount.text = text
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("Error seguidores", t.message.toString())
            }
        })
    }

    fun getFollowingCount() {
        val callFollowings = ApiClient.followService.getFollowingsByUser(user.idUser!!)

        callFollowings.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val text = "${getText(R.string.followingText)} ${response.body().toString()}"
                    followingCount.text = text
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.d("Error seguidores", t.message.toString())
            }
        })
    }

    fun showUnfollowDialog(context: Context, username: String, followerId: Long, followedId: Long) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("¿Dejar de seguir a @$username?")
        builder.setMessage("Ya no verás los tweets de @$username en tu línea de tiempo.")

        builder.setPositiveButton("Dejar de seguir") { dialog, _ ->
            unfollowUser(followerId, followedId)
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.RED)
    }

    fun unfollowUser(followerId: Long, followedId: Long) {
        val call = ApiClient.followService.unfollowUser(followerId, followedId)
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("Unfollow", "Usuario dejado de seguir correctamente")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("Unfollow", "Error al dejar de seguir: ${t.message}")
            }
        })
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

    override fun onUserClick(user: UserRequest) {
    }
}