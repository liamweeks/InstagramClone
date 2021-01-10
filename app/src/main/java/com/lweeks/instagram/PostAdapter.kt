package com.lweeks.instagram

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lweeks.instagram.models.Post
import kotlinx.android.synthetic.main.item_post.view.*

import com.lweeks.instagram.models.User
import java.math.BigInteger
import java.security.MessageDigest

class PostAdapter (val context: Context, val posts: List<Post>) :
    RecyclerView.Adapter<PostAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(post: Post) {
            var usernamehash = post.user?.username as String
            itemView.tvUsername.text = usernamehash
            itemView.tvDescription.text = post.description
            Glide.with(context).load(post.imageurl).into(itemView.ivPost)
            Glide.with(context).load(getProfileImageUrl(usernamehash)).into(itemView.ProfileImage)
            itemView.tvRelativeTime.text = DateUtils.getRelativeTimeSpanString(post.CreationTimeMS)

        }

        private fun getProfileImageUrl(username: String): String {
            val digest = MessageDigest.getInstance("MD5")
            val hash = digest.digest(username.toByteArray())
            val bigInt = BigInteger(hash)
            val hex = bigInt.abs().toString(16)
            return "https://www.gravatar.com/avatar/$hex?d=identicon"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false)
        return  ViewHolder(view)
    }

    override fun getItemCount() = posts.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(posts[position])
    }

}

