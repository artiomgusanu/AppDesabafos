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
        val tvContent: TextView = view.findViewById(R.id.tvContent)
        val tvUsername: TextView = view.findViewById(R.id.tvUsername)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val ivImage: ImageView = view.findViewById(R.id.ivImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.tvContent.text = post.text
        holder.tvUsername.text = post.username

        // Formatando data
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = Date(post.timestamp)
        holder.tvDate.text = sdf.format(date)

        // Mostrar imagem (se houver)
        if (post.imageUrl != null) {
            holder.ivImage.visibility = View.VISIBLE
            Glide.with(holder.itemView.context).load(post.imageUrl).into(holder.ivImage)
        } else {
            holder.ivImage.visibility = View.GONE
        }
    }

    override fun getItemCount() = posts.size
}

