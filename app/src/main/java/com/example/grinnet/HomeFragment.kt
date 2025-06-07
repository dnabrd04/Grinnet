package com.example.grinnet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.grinnet.adapter.OnUserClickListener
import com.example.grinnet.adapter.PostAdapter
import com.example.grinnet.data.PostDTORequest
import com.example.grinnet.data.PostResponse
import com.example.grinnet.data.UserIdRequest
import com.example.grinnet.data.UserRequest
import com.example.grinnet.utils.SessionManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment(), OnUserClickListener {

    private lateinit var list: MutableList<PostResponse>
    private lateinit var postList: RecyclerView
    private lateinit var adapter: PostAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var exploreTab: TextView
    private lateinit var followedTab: TextView
    private var isFollowingMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        list = mutableListOf<PostResponse>()

        postList = view.findViewById(R.id.postList)
        adapter = PostAdapter(list, this.requireContext(), this)
        postList.layoutManager = LinearLayoutManager(requireActivity())
        postList.adapter = adapter

        exploreTab = view.findViewById(R.id.exploreTab)
        followedTab = view.findViewById(R.id.followedTab)
        updateTabStyles()

        exploreTab.setOnClickListener {
            isFollowingMode = false
            updateTabStyles()
            updatePostList()
        }

        followedTab.setOnClickListener {
            isFollowingMode = true
            updateTabStyles()
            updatePostList()
        }

        val addButton = view.findViewById<ImageButton>(R.id.addButton)
        addButton.setOnClickListener {
            showCreatePostView()
        }

        swipeRefreshLayout = view.findViewById(R.id.refreshList)
        swipeRefreshLayout.setOnRefreshListener {
            updatePostList()
        }
        updatePostList()

        return view
    }

    private fun updateTabStyles() {
        exploreTab.setTextColor(
            ContextCompat.getColor(requireContext(), if (isFollowingMode) R.color.gray else R.color.black)
        )
        followedTab.setTextColor(
            ContextCompat.getColor(requireContext(), if (isFollowingMode) R.color.black else R.color.gray)
        )
    }

    private fun updatePostList() {
        val firebaseId = Firebase.auth.currentUser?.uid
        var userIdRequest = UserIdRequest(-1L,"")
        swipeRefreshLayout.isRefreshing = true

        if (firebaseId != null) {
            userIdRequest = UserIdRequest(SessionManager.init(requireActivity()) ?: -1L, firebaseId)
        }

        if (isFollowingMode) {
            ApiClient.postService.getPostsFollowed(userIdRequest)
                .enqueue(object : Callback<MutableList<PostResponse>> {
                    override fun onResponse(
                        call: Call<MutableList<PostResponse>>,
                        response: Response<MutableList<PostResponse>>
                    ) {
                        Log.d("Respuesta del like", response.body().toString())
                        if (response.isSuccessful) {
                            val list = response.body()!!.asReversed()
                            adapter.updateData(list)
                        }

                        swipeRefreshLayout.isRefreshing = false
                    }

                    override fun onFailure(call: Call<MutableList<PostResponse>>, t: Throwable) {
                        Log.d("Error api", t.message.toString())
                    }

                })
        } else {
            ApiClient.postService.getPosts(userIdRequest)
                .enqueue(object : Callback<MutableList<PostResponse>> {
                    override fun onResponse(
                        call: Call<MutableList<PostResponse>>,
                        response: Response<MutableList<PostResponse>>
                    ) {
                        Log.d("Respuesta del like", response.body().toString())
                        if (response.isSuccessful) {
                            val list = response.body()!!.asReversed()
                            adapter.updateData(list)
                        }

                        swipeRefreshLayout.isRefreshing = false
                    }

                    override fun onFailure(call: Call<MutableList<PostResponse>>, t: Throwable) {
                        Log.d("Error api", t.message.toString())
                    }

                })
        }
    }

    private fun showCreatePostView(){
        val intent = Intent(requireActivity(), CreatePostActivity::class.java)
        startActivity(intent)
    }

    override fun onUserClick(user: UserRequest) {
        (activity as MainActivity).changeFragmentToUserProfile(user)
    }
}