package ip.santarem.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PostAdapter
    private val posts = mutableListOf<Post>()

    private var selectedImageUri: Uri? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializando Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Inicializando RecyclerView
        recyclerView = findViewById(R.id.rvPosts)
        adapter = PostAdapter(posts)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Configurar listeners e carregamentos
        setupRealtimeListener()

        val btnPost = findViewById<ImageButton>(R.id.btnPost)
        val btnAddImage = findViewById<ImageButton>(R.id.btnAddImage)
        val etPostContent = findViewById<EditText>(R.id.etPostContent)
        val imageViewPreview = findViewById<ImageView>(R.id.imageViewPreview)

        // Adicionar imagem
        btnAddImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
        }

        // Publicar post
        btnPost.setOnClickListener {
            val content = etPostContent.text.toString()
            if (content.isNotBlank() || selectedImageUri != null) {
                postToFirestore(content, selectedImageUri)
                etPostContent.text.clear()
                selectedImageUri = null
                imageViewPreview.visibility = View.GONE
                imageViewPreview.setImageDrawable(null)
            } else {
                Toast.makeText(this, "Por favor, insira um texto ou imagem!", Toast.LENGTH_SHORT).show()
            }
        }

        // Carregar posts existentes do Firestore
        loadPostsFromFirestore()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                selectedImageUri = uri

                // Mostrar a pré-visualização da imagem
                val imageViewPreview = findViewById<ImageView>(R.id.imageViewPreview)
                imageViewPreview.visibility = View.VISIBLE
                Glide.with(this).load(uri).into(imageViewPreview)
            }
        }
    }

    private fun postToFirestore(content: String, imageUri: Uri?) {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid ?: "Anônimo"

        // Recuperar o nome de usuário do Firestore
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val userName = document.getString("username") ?: "Utilizador Desconhecido"
                val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

                val postMap = hashMapOf(
                    "content" to content,
                    "imageUri" to (imageUri?.toString() ?: ""),
                    "userId" to userId,
                    "userName" to userName,
                    "timestamp" to formattedDate
                )

                // Salvar o post no Firestore
                firestore.collection("posts")
                    .add(postMap)
                    .addOnSuccessListener { documentReference ->
                        // Adicionar o post diretamente na lista local
                        val newPost = Post(content, imageUri?.toString(), userName, formattedDate)
                        posts.add(0, newPost) // Adiciona no topo da lista
                        adapter.notifyItemInserted(0)
                        recyclerView.scrollToPosition(0) // Scroll para o novo post

                        Toast.makeText(this, "Post publicado!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Erro ao publicar o post!", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao recuperar utilizador!", Toast.LENGTH_SHORT).show()
            }
    }



    private fun loadPostsFromFirestore() {
        firestore.collection("posts")
            .orderBy("timestamp") // Ordena os posts pela data
            .get()
            .addOnSuccessListener { result ->
                posts.clear() // Limpa a lista local antes de carregar os novos posts
                for (document in result) {
                    val content = document.getString("content") ?: ""
                    val imageUri = document.getString("imageUri")
                    val userName = document.getString("userName") ?: "Utilizador Desconhecido"
                    val timestamp = document.getString("timestamp") ?: "Data desconhecida"

                    posts.add(Post(content, imageUri, userName, timestamp))
                }
                adapter.notifyDataSetChanged() // Atualiza o RecyclerView com os novos dados
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar posts!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupRealtimeListener() {
        firestore.collection("posts")
            .orderBy("timestamp") // Ordena os posts pela data
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(this, "Erro ao escutar atualizações: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    posts.clear()
                    for (document in snapshots) {
                        val content = document.getString("content") ?: ""
                        val imageUri = document.getString("imageUri")
                        val userName = document.getString("userName") ?: "Utilizador Desconhecido"
                        val timestamp = document.getString("timestamp") ?: "Data desconhecida"

                        posts.add(Post(content, imageUri, userName, timestamp))
                    }
                    adapter.notifyDataSetChanged() // Atualiza o RecyclerView com os novos dados
                }
            }
    }

    companion object {
        const val REQUEST_CODE_PICK_IMAGE = 1
    }
}
