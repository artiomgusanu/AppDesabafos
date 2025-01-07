package ip.santarem.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
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

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.rvPosts)
        adapter = PostAdapter(posts) { post ->
            openCommentDialog(post)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Configurar listener para atualizações em tempo real
        setupRealtimeListener()

        // Recuperar categoria passada pela Intent
        val category = intent.getStringExtra("category") ?: "Zangado"
        Log.d("MainActivity", "Categoria recebida: $category")

        // Inicialização dos componentes da UI
        val btnPost = findViewById<ImageButton>(R.id.btnPost)
        val btnAddImage = findViewById<ImageButton>(R.id.btnAddImage)
        val etPostContent = findViewById<EditText>(R.id.etPostContent)
        val imageViewPreview = findViewById<ImageView>(R.id.imageViewPreview)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Adicionar imagem
        btnAddImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
        }

        // Publicar post
        btnPost.setOnClickListener {
            val content = etPostContent.text.toString()
            val categoria = findViewById<Spinner>(R.id.spinnerCategoria).selectedItem.toString()

            if (content.isNotBlank() || selectedImageUri != null) {
                postToFirestore(content, selectedImageUri, categoria)
                etPostContent.text.clear()
                selectedImageUri = null
                imageViewPreview.visibility = View.GONE
                imageViewPreview.setImageDrawable(null)
            } else {
                Toast.makeText(this, "Por favor, insira um texto ou imagem!", Toast.LENGTH_SHORT).show()
            }
        }

        // Configurar navegação no BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    Toast.makeText(this, "Você já está na página inicial", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.profile -> {
                    val intent = Intent(this, ProfileFragment::class.java)
                    startActivity(intent)
                    true
                }
                R.id.categories -> {
                    val intent = Intent(this, CategoriesFragment::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        // Carregar posts da categoria correta
        loadPostsFromFirestore()
    }

    override fun onStop() {
        super.onStop()
        auth.signOut()
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

    private fun postToFirestore(content: String, imageUri: Uri?, categoria: String) {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid ?: "Anônimo"

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val userName = document.getString("username") ?: "Utilizador Desconhecido"
                val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

                val postMap = hashMapOf(
                    "content" to content,
                    "imageUri" to (imageUri?.toString() ?: ""),
                    "userId" to userId,
                    "userName" to userName,
                    "categoria" to categoria,
                    "timestamp" to formattedDate
                )

                firestore.collection("posts")
                    .add(postMap)
                    .addOnSuccessListener {
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

    private fun postResponseToFirestore(postId: String, respostaContent: String) {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid ?: "Anônimo"

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val userName = document.getString("username") ?: "Utilizador Desconhecido"
                val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

                val responseMap = hashMapOf(
                    "id_post" to postId,
                    "Resposta" to respostaContent,
                    "userId" to userId,
                    "userName" to userName,
                    "timestamp" to formattedDate
                )

                firestore.collection("responses")
                    .add(responseMap)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Resposta publicada com sucesso!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Erro ao publicar a resposta!", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao recuperar utilizador!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openCommentDialog(post: Post) {
        val dialog = CommentDialogFragment(post.id)
        dialog.show(supportFragmentManager, "CommentDialogFragment")
    }

    private fun loadPostsFromFirestore() {
        Log.d("MainActivity", "Carregando todos os posts")

        firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                posts.clear()

                if (result.isEmpty) {
                    Log.d("MainActivity", "Nenhum post encontrado!")
                    Toast.makeText(this, "Nenhum post encontrado!", Toast.LENGTH_SHORT).show()
                }

                for (document in result) {
                    val content = document.getString("content") ?: ""
                    val imageUri = document.getString("imageUri")
                    val userName = document.getString("userName") ?: "Utilizador Desconhecido"
                    val timestamp = document.getString("timestamp") ?: "Data desconhecida"
                    val categoria = document.getString("categoria") ?: "Sem categoria"

                    posts.add(Post(content, imageUri, userName, timestamp, categoria))
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("MainActivity", "Erro ao carregar posts: ${exception.message}")
                Toast.makeText(this, "Erro ao carregar posts!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupRealtimeListener() {
        firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
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
                        val categoria = document.getString("categoria") ?: "Sem categoria"

                        posts.add(Post(content, imageUri, userName, timestamp, categoria))
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }

    companion object {
        const val REQUEST_CODE_PICK_IMAGE = 1
    }
}
