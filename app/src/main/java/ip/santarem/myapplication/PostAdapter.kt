package ip.santarem.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PostAdapter(private val posts: List<Post>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val contentTextView: TextView = view.findViewById(R.id.tvContent)
        val imageView: ImageView = view.findViewById(R.id.ivPostImage)
        val userNameTextView: TextView = view.findViewById(R.id.tvUserName)
        val timestampTextView: TextView = view.findViewById(R.id.tvTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.contentTextView.text = post.content
        holder.userNameTextView.text = post.userName
        holder.timestampTextView.text = post.timestamp

        if (!post.imageUri.isNullOrEmpty()) {
            holder.imageView.visibility = View.VISIBLE
            Glide.with(holder.itemView.context).load(post.imageUri).into(holder.imageView)
        } else {
            holder.imageView.visibility = View.GONE
        }
    }

    override fun getItemCount() = posts.size
}
