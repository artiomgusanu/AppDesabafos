package ip.santarem.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class DesperateActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PostAdapter
    private val posts = mutableListOf<Post>()
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_desperate)

        // Inicializar Firebase Firestore
        firestore = FirebaseFirestore.getInstance()

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.RVDesesperatePosts)

        // Passando o onCommentClick ao adaptador para tratar o clique nos comentários
        adapter = PostAdapter(posts) { post ->
            openCommentDialog(post) // Função chamada quando o botão de comentário é clicado
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Carregar os posts da categoria "Desesperado"
        loadDesesperatePosts()
    }

    private fun loadDesesperatePosts() {
        firestore.collection("posts")
            .whereEqualTo("categoria", "Desesperado")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                posts.clear()
                for (document in result) {
                    val content = document.getString("content") ?: ""
                    val imageUri = document.getString("imageUri")
                    val userName = document.getString("userName") ?: "Utilizador Desconhecido"
                    val timestamp = document.getString("timestamp") ?: "Data desconhecida"
                    val categoria = document.getString("categoria") ?: "Sem categoria"

                    // Adiciona o post à lista
                    posts.add(Post(content, imageUri, userName, timestamp, categoria))
                }

                // Atualiza a lista do adapter com os posts carregados
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar posts!", Toast.LENGTH_SHORT).show()
            }
    }

    // Método para abrir o diálogo de comentários quando um post é clicado
    private fun openCommentDialog(post: Post) {
        // Aqui você pode adicionar a lógica para abrir o CommentDialogFragment
        val dialog = CommentDialogFragment(post.id)
        dialog.show(supportFragmentManager, "CommentDialogFragment")
    }
}
