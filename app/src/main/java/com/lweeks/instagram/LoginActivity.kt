package com.lweeks.instagram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import android.util.Log.i as i


private const val TAG = "LoginActivity"

open class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) { //if you have already entered your id/pass
            auth.currentUser!!.email?.let { goPostActivity(it) }
        }
        loginbtn.setOnClickListener {
            loginbtn.isEnabled = false // so you don't create a new post activity whenever you press login
            val email = email.text.toString()
//            if (email == "liam@test.com") {
//                var myIntent = Intent(this, LoginActivity::class.java);
//                myIntent.putExtra("loginemail", "liam")
//                startActivity(myIntent)
//                //useremail = "liam"
//            }
//            if (email == "hamilton@test.com") {
//                var myIntent = Intent(this, LoginActivity::class.java)
//                myIntent.putExtra("loginemail", "hamilton")
//                startActivity(myIntent)
//                //useremail = "hamilton"
//            }

            val password = password.text.toString()
//            var emailname = if (email.toString() == "liam@test.com") "liam" else "hamilton"
            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Credentials required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // ***Firebase Authentication***


            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                loginbtn.isEnabled = true
                if (task.isSuccessful) {
                    Toast.makeText(this, "Logged in", Toast.LENGTH_SHORT).show()
                    goPostActivity(email) //navigate to a new activity that displays all the posts in our app
                }
                else {
                    Log.i(TAG, "signInWithEmail failed", task.exception)
                    Toast.makeText(this, "Auth failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun goPostActivity(loggedInEmail: String) {
        Log.i(TAG, "goPostActivity")
        val intent = Intent(this, PostsActivity::class.java) // defines the new layout
        intent.putExtra("loginemail", loggedInEmail)
        startActivity(intent) // goes to the next layout
        finish() //finishes the current activity so you exit the app instead off go back to login
    }
}