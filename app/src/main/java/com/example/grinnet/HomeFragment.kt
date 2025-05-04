package com.example.grinnet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.grinnet.adapter.PostAdapter
import com.example.grinnet.data.PostResponse
import com.example.grinnet.data.UserRequest
import okhttp3.internal.notifyAll
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {

    private lateinit var list: MutableList<PostResponse>
    private lateinit var postList: RecyclerView
    private lateinit var adapter: PostAdapter

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
        adapter = PostAdapter(list, this.requireContext())
        postList.layoutManager = LinearLayoutManager(requireActivity())
        postList.adapter = adapter

        val addButton = view.findViewById<ImageButton>(R.id.addButton)
        addButton.setOnClickListener {
            showCreatePostView()
        }

        updatePostList()

        return view
    }

    private fun updatePostList() {
        ApiClient.postService.getPosts().enqueue(object: Callback<MutableList<PostResponse>>{
            override fun onResponse(
                call: Call<MutableList<PostResponse>>,
                response: Response<MutableList<PostResponse>>
            ) {
                if(response.isSuccessful) {
                    adapter.updateData(response.body()!!)
                    val list = response.body() as List<PostResponse>
                    for(element in list) {
                        Log.d("Respuesta del like", element.toString())
                    }
                }
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
}