package com.lweeks.instagram
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.auth.User
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.lweeks.instagram.models.Post
import com.lweeks.instagram.models.User
import kotlinx.android.synthetic.main.activity_create.*

private var signedInUser: User? = null //user type from google, not project
private const val TAG = "CreateActivity"
private const val PICK_PHOTO_CODE = 1234
private lateinit var firestoreDb: FirebaseFirestore

class CreateActivity : AppCompatActivity() {
    private var photoUri: Uri? = null
    private lateinit var storageReference: StorageReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)
        storageReference = FirebaseStorage.getInstance().reference
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

        btnPickImage.setOnClickListener {
            Log.i(TAG, "Opening image picker on device")
            val imagePickerIntent = Intent(Intent.ACTION_GET_CONTENT)
            imagePickerIntent.type = "image/*" //open any app that allows you to choose pics
            if (imagePickerIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(imagePickerIntent, PICK_PHOTO_CODE) //create a code to act like an ID
            }
        }

        btnPost.setOnClickListener {
            handleSubmitButtonClick()
        }
    }

    private fun handleSubmitButtonClick() {
        if (photoUri == null) {
            Toast.makeText(this, "Please select an image to post", Toast.LENGTH_LONG).show()
            return //return early because it is an error case
        }

        if (description.text.toString() == "") {
            Toast.makeText(this, "Please add a description", Toast.LENGTH_LONG).show()
            return
        }
        if (addressParts[0] == null) {
            Toast.makeText(this, "No signed in user", Toast.LENGTH_LONG).show()
            return
        }

        btnPost.isEnabled = false
        val photoUploadUri = photoUri as Uri
        val photoReference = storageReference.child("images/${System.currentTimeMillis()}--photo.jpg") //giving each photo a unique name

        //Upload photo to Firebase Storage
        photoReference.putFile(photoUploadUri)
            .continueWithTask { photoUploadTask ->
                Log.i(TAG, "Uploaded bytes: ${photoUploadTask.result?.bytesTransferred}")
                //Retrieve image URL of the uploaded image
                photoReference.downloadUrl
            }.continueWithTask { downloadUrlTask ->
                //Create a post object with image URL and add that to the post collection
                val post = Post(
                    description.text.toString(),
                    downloadUrlTask.result.toString(),
                    System.currentTimeMillis(),
                    intent.getStringExtra(EXTRA_USERNAME)?.let { User(it) }) //signedInUser
                firestoreDb.collection("posts").add(post)
            }.addOnCompleteListener { postCreationTask ->
                btnPost.isEnabled = true
                if (!postCreationTask.isSuccessful) {
                    Log.e(TAG, "Error during Firebase operations", postCreationTask.exception)
                    Toast.makeText(this, "Failed to post! (error with backend)", Toast.LENGTH_LONG).show()
                }
                description.text.clear()
                imageView.setImageResource(0)
                Toast.makeText(this, "Posted!", Toast.LENGTH_LONG).show()
                val ProfileIntent = Intent(this, ProfileActivity::class.java)
                ProfileIntent.putExtra(EXTRA_USERNAME, addressParts[0]) //signedInUser?.username
                startActivity(ProfileIntent)
                finish()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PHOTO_CODE) { //makes sure code is the same as our request code so we have the correct image
            if (resultCode == Activity.RESULT_OK) { //if the user has selected an image
                photoUri = data?.data //location of photo that was selected
                Log.i(TAG, "Image has been selected photo. URI: $photoUri")
                imageView.setImageURI(photoUri)
            } else {Toast.makeText(this, "Post action cancelled", Toast.LENGTH_LONG).show()}

        }
    }
}