package com.example.grinnet

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.grinnet.adapter.OnUserClickListener
import com.example.grinnet.adapter.UserAdapter
import com.example.grinnet.data.UserRequest
import com.example.grinnet.data.UserResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * A simple [Fragment] subclass.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment : Fragment(), OnUserClickListener {
    private lateinit var searchStatusText: TextView
    private lateinit var adapter: UserAdapter
    private val userList = mutableListOf<UserRequest>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        val searchUsernameInput = view.findViewById<EditText>(R.id.searchUsernameInput)
        val searchUsersButton = view.findViewById<ImageView>(R.id.searchUsersButton)
        searchStatusText = view.findViewById(R.id.searchStatusText)
        val searchResultsRecycler = view.findViewById<RecyclerView>(R.id.searchResultsRecycler)

        adapter = UserAdapter(userList, this)
        searchResultsRecycler.layoutManager = LinearLayoutManager(requireContext())
        searchResultsRecycler.adapter = adapter

        showInitialState()

        searchUsersButton.setOnClickListener {
            val username = searchUsernameInput.text.toString()
            searchUsername(username)
        }

        return view
    }

    private fun showInitialState() {
        searchStatusText.text = getString(R.string.searchTextInformationText)
        searchStatusText.visibility = View.VISIBLE
        userList.clear()
        adapter.notifyDataSetChanged()
    }

    private fun showNoResults() {
        searchStatusText.text = getString(R.string.searchTextNoCoincidences)
        searchStatusText.visibility = View.VISIBLE
        userList.clear()
        adapter.notifyDataSetChanged()
    }

    private fun showResults(users: List<UserRequest>) {
        searchStatusText.visibility = View.GONE
        userList.clear()
        userList.addAll(users)
        adapter.notifyDataSetChanged()
    }

    fun searchUsername(username: String) {
        val call = ApiClient.userService.getUserByUsername(username.lowercase())
        
        call.enqueue(object : Callback<MutableList<UserRequest>> {
            override fun onResponse(call: Call<MutableList<UserRequest>>, response: Response<MutableList<UserRequest>>) {
                if (response.isSuccessful && !response.body().isNullOrEmpty()){
                    showResults(response.body()!!)
                } else {
                    showNoResults()
                }
            }

            override fun onFailure(call: Call<MutableList<UserRequest>>, t: Throwable) {
            }
        })
    }

    override fun onUserClick(user: UserRequest) {
        (activity as MainActivity).changeFragmentToUserProfile(user)
    }

}