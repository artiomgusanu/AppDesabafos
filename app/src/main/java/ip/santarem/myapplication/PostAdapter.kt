package ip.santarem.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PostAdapter(
    private val posts: List<Post>,
    private val onCommentClick: (Post) -> Unit // Aqui está o parâmetro onCommentClick
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        // Atualizar o conteúdo do post
        holder.tvUserName.text = post.userName
        holder.tvTimestamp.text = post.timestamp
        holder.tvContent.text = post.content
        holder.tvCategoria.text = "Categoria: ${post.categoria}"

        // Lidar com a imagem, se houver
        if (!post.imageUri.isNullOrEmpty()) {
            holder.ivPostImage.visibility = View.VISIBLE
            Glide.with(holder.itemView.context).load(post.imageUri).into(holder.ivPostImage)
        } else {
            holder.ivPostImage.visibility = View.GONE
        }

        // Definir o comportamento do clique no comentário
        holder.btnComment.setOnClickListener {
            onCommentClick(post) // Chamar a função passada quando o botão for clicado
        }
    }

    override fun getItemCount(): Int = posts.size

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        val tvContent: TextView = itemView.findViewById(R.id.tvContent)
        val ivPostImage: ImageView = itemView.findViewById(R.id.ivPostImage)
        val tvCategoria: TextView = itemView.findViewById(R.id.tvCategoria)
        val btnComment: ImageButton = itemView.findViewById(R.id.btnComment)  // Botão para comentar
    }
}
