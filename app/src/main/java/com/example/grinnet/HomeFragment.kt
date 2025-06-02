package com.example.grinnet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.grinnet.adapter.OnUserClickListener
import com.example.grinnet.adapter.PostAdapter
import com.example.grinnet.data.PostResponse
import com.example.grinnet.data.UserIdRequest
import com.example.grinnet.data.UserRequest
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

    private fun updatePostList() {
        val firebaseId = Firebase.auth.currentUser?.uid
        var userIdRequest = UserIdRequest("")

        if (firebaseId != null) {
            userIdRequest = UserIdRequest(firebaseId)
        }

        ApiClient.postService.getPosts(userIdRequest).enqueue(object: Callback<MutableList<PostResponse>>{
            override fun onResponse(
                call: Call<MutableList<PostResponse>>,
                response: Response<MutableList<PostResponse>>
            ) {
                Log.d("Respuesta del like", response.body().toString())
                if(response.isSuccessful) {
                    adapter.updateData(response.body()!!)
                    val list = response.body() as List<PostResponse>
                    for(element in list) {
                        Log.d("Respuesta del like", element.toString())
                    }
                } else {
                    Log.d("Respuesta del like", response.errorBody().toString())
                }

                swipeRefreshLayout.isRefreshing = false
            }

            override fun onFailure(call: Call<MutableList<PostResponse>>, t: Throwable) {
                Log.d("Error api", t.message.toString())
            }

        })
    }

    private fun showCreatePostView(){
        val intent = Intent(requireActivity(), CreatePostActivity::class.java)
        startActivity(intent)
    }

    override fun onUserClick(user: UserRequest) {
        (activity as MainActivity).changeFragmentToUserProfile(user)
    }
}