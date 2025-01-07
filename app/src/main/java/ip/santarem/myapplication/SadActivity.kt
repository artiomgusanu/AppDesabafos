package ip.santarem.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class SadActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PostAdapter
    private val posts = mutableListOf<Post>()
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sad)

        // Inicializar Firebase Firestore
        firestore = FirebaseFirestore.getInstance()

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.RVSadPosts)

        // Passando a função onCommentClick para o adapter
        adapter = PostAdapter(posts) { post ->
            openCommentDialog(post) // Chama a função para abrir o dialogo de comentário
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Carregar os posts da categoria "Triste"
        loadSadPosts()
    }

    private fun loadSadPosts() {
        firestore.collection("posts")
            .whereEqualTo("categoria", "Triste")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                posts.clear()  // Limpa a lista de posts antes de adicionar novos
                for (document in result) {
                    val content = document.getString("content") ?: ""
                    val imageUri = document.getString("imageUri")
                    val userName = document.getString("userName") ?: "Utilizador Desconhecido"
                    val timestamp = document.getString("timestamp") ?: "Data desconhecida"
                    val categoria = document.getString("categoria") ?: "Sem categoria"

                    // Adiciona cada post à lista
                    posts.add(Post(content, imageUri, userName, timestamp, categoria))
                }

                // Atualiza a lista de posts no adapter
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar posts!", Toast.LENGTH_SHORT).show()
            }
    }

    // Função para abrir o dialogo de comentário quando o botão de comentário for pressionado
    private fun openCommentDialog(post: Post) {
        val dialog = CommentDialogFragment(post.id)  // Passando o id do post para o fragmento de comentário
        dialog.show(supportFragmentManager, "CommentDialogFragment")
    }
}
