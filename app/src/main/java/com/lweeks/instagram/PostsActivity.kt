package com.lweeks.instagram


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.lweeks.instagram.models.Post
import com.lweeks.instagram.LoginActivity.*
import com.lweeks.instagram.models.User
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_posts.*


private const val TAG = "PostsActivity"
const val EXTRA_USERNAME = "EXTRA_USERNAME"
var addressParts = listOf<String>()
open class PostsActivity : AppCompatActivity() { //inherit from AppCompatActivity()

    private var signedInUser: String = "liam"
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var posts: MutableList<Post>
    private lateinit var adapter: PostAdapter
    // var emailname = if (email.toString() == "liam@test.com") "liam" else "hamilton"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts)

        // create the layout file that represents one post -DONE!
        // create data source
        posts = mutableListOf<Post>()
        // create adapter
        adapter = PostAdapter(this, posts)
        // bind adapter and layout manager to recycler view
        rvposts.adapter = adapter
        rvposts.layoutManager = LinearLayoutManager(this)
        firestoreDb = FirebaseFirestore.getInstance()

        firestoreDb.collection("users")
                .document(FirebaseAuth.getInstance().currentUser?.uid as String)
                .get()
                .addOnSuccessListener { userSnapshot ->
                    //signedInUser = userSnapshot.toObject(User::class.java)
                    Log.i(TAG, "User signed in: ${signedInUser}")
                }
                .addOnFailureListener { exception ->
                    Log.i(TAG, "Failed fetching signed in user", exception)
                }

        var PostReference = firestoreDb
                .collection("posts")
                .limit(20)
                .orderBy("creation_time(ms)", Query.Direction.DESCENDING)

        val username = intent.getStringExtra(EXTRA_USERNAME)
        if (username != null) {
            supportActionBar?.title = username
            PostReference = PostReference.whereEqualTo("user.username", username) // str is the user path into our firebase in user map with the variable for username
        }

        PostReference.addSnapshotListener { snapshot, exception -> // app wil automatically get updates whenever a document changes
            if (exception != null || snapshot == null) {
                Log.e(TAG, "Exception when querying posts", exception)
                return@addSnapshotListener
            }
            val postlist = snapshot.toObjects(Post::class.java)
            posts.clear()
            posts.addAll(postlist)
            adapter.notifyDataSetChanged()
            for (i in postlist) {
                Log.i(TAG, "Post ${i}")
            }
        }

        fabCreate.setOnClickListener {
            var x = intent.getStringExtra("loginemail")
            var usernamefromemail = x?.split("@")
            val Createintent = Intent(this, CreateActivity::class.java)
            Createintent.putExtra(EXTRA_USERNAME, usernamefromemail?.get(0))
            startActivity(Createintent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_posts, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //            val username = intent.getStringExtra(EXTRA_USERNAME)
        if (item.itemId == R.id.menu_posts) {
            var x = intent.getStringExtra("loginemail")
            Log.i(TAG,"In onOptionsItemSelected, x==" + x)
            if (x != null) {
                addressParts = x.split('@')
            }
            val intent = Intent(this, ProfileActivity::class.java)
            if (x != null) {
                intent.putExtra(EXTRA_USERNAME, addressParts[0])

            }
                startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

}