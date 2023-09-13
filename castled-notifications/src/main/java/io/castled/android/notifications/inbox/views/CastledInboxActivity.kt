package io.castled.android.notifications.inbox.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager

import io.castled.android.notifications.databinding.ActivityCastledInboxBinding

class CastledInboxActivity : AppCompatActivity() {
    // private variable to inflate the layout for the activity
    private lateinit var binding: ActivityCastledInboxBinding

    // variable to access the ViewModel class
  //  val viewModel : ContactViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // inflate the layout
        binding = ActivityCastledInboxBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.imgClose.setOnClickListener { finishAfterTransition() }
        // set onClickListener for the floating action button
        binding.floatingActionButton.setOnClickListener{
//            val intent = Intent(this , CreateContact::class.java)
//            startActivity(intent)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        val myMutableList: MutableList<String> = mutableListOf("apple", "banana", "cherry")


        binding.recyclerView.adapter = CastledInboxAdapter(this, myMutableList)
//        // Observe the LiveData returned by the getAllContacts method
//        viewModel.getAllContacts().observe(this , Observer { list->
//            // set the layout manager and the adapter for the recycler view
//            binding.recyclerView.adapter = CastledInboxAdapter(this,list)
//        })
    }
}
