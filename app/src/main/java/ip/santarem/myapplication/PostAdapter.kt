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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        // Atualizando o conteúdo do post
        holder.tvUserName.text = post.userName
        holder.tvTimestamp.text = post.timestamp
        holder.tvContent.text = post.content
        holder.tvCategoria.text = "Categoria: ${post.categoria}"  // Aqui a categoria é atualizada dinamicamente

        // Se tiver imagem
        if (!post.imageUri.isNullOrEmpty()) {
            holder.ivPostImage.visibility = View.VISIBLE
            Glide.with(holder.itemView.context).load(post.imageUri).into(holder.ivPostImage)
        } else {
            holder.ivPostImage.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    // ViewHolder para associar os itens da lista
    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        val tvContent: TextView = itemView.findViewById(R.id.tvContent)
        val ivPostImage: ImageView = itemView.findViewById(R.id.ivPostImage)
        val tvCategoria: TextView = itemView.findViewById(R.id.tvCategoria)  // Aqui o TextView da categoria
    }
}
