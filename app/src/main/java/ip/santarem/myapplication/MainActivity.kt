package ip.santarem.myapplication

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
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

        // Log para verificar se onCreate está sendo chamado corretamente
        Log.d("MainActivity", "onCreate chamado")

        // Verificar o estado de login
        val sharedPrefs = getSharedPreferences("login_prefs", MODE_PRIVATE)
        val isRemembered = sharedPrefs.getBoolean("remember_me", false)
        val currentUser = FirebaseAuth.getInstance().currentUser

        Log.d("MainActivity", "isRemembered: $isRemembered, currentUser: ${currentUser?.uid ?: "null"}")

        if (!isRemembered || currentUser == null) {
            // Usuário não autenticado ou preferências não lembradas
            Log.d("MainActivity", "Usuário não autenticado, redirecionando para login.")
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        // Se o usuário estiver autenticado e a preferência for "lembrar de mim", continua carregando a MainActivity
        Log.d("MainActivity", "Usuário autenticado, carregando a MainActivity.")
        setContentView(R.layout.activity_main)

        // Inicializando Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Inicializando RecyclerView
        recyclerView = findViewById(R.id.rvPosts)
        adapter = PostAdapter(posts)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Configurar listener para atualizações em tempo real
        setupRealtimeListener()

        val btnPost = findViewById<ImageButton>(R.id.btnPost)
        val btnAddImage = findViewById<ImageButton>(R.id.btnAddImage)
        val etPostContent = findViewById<EditText>(R.id.etPostContent)
        val imageViewPreview = findViewById<ImageView>(R.id.imageViewPreview)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Adicionar imagem
        btnAddImage.setOnClickListener {
            Log.d("MainActivity", "Botão de adicionar imagem clicado.")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
        }

        // Publicar post
        btnPost.setOnClickListener {
            Log.d("MainActivity", "Botão de publicar clicado.")
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

        // Configurar navegação no BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    Log.d("MainActivity", "Você já está na página inicial")
                    Toast.makeText(this, "Você já está na página inicial", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.profile -> {
                    Log.d("MainActivity", "Navegando para o perfil.")
                    val intent = Intent(this, ProfileFragment::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        // Carregar os posts existentes no Firestore
        Log.d("MainActivity", "Carregando posts do Firestore.")
        loadPostsFromFirestore()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("MainActivity", "onActivityResult chamado, requestCode: $requestCode, resultCode: $resultCode")
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                selectedImageUri = uri

                // Mostrar a pré-visualização da imagem
                Log.d("MainActivity", "Imagem selecionada: $uri")
                val imageViewPreview = findViewById<ImageView>(R.id.imageViewPreview)
                imageViewPreview.visibility = View.VISIBLE
                Glide.with(this).load(uri).into(imageViewPreview)
            }
        }
    }

    private fun postToFirestore(content: String, imageUri: Uri?) {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid ?: "Anônimo"

        Log.d("MainActivity", "Postando no Firestore, userId: $userId, content: $content, imageUri: $imageUri")

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
                    .addOnSuccessListener {
                        Log.d("MainActivity", "Post publicado com sucesso!")
                        Toast.makeText(this, "Post publicado!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Log.e("MainActivity", "Erro ao publicar o post!")
                        Toast.makeText(this, "Erro ao publicar o post!", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Log.e("MainActivity", "Erro ao recuperar usuário para post!")
                Toast.makeText(this, "Erro ao recuperar utilizador!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadPostsFromFirestore() {
        firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                Log.d("MainActivity", "Posts carregados com sucesso!")
                posts.clear()
                for (document in result) {
                    val content = document.getString("content") ?: ""
                    val imageUri = document.getString("imageUri")
                    val userName = document.getString("userName") ?: "Utilizador Desconhecido"
                    val timestamp = document.getString("timestamp") ?: "Data desconhecida"

                    posts.add(Post(content, imageUri, userName, timestamp))
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Log.e("MainActivity", "Erro ao carregar posts do Firestore!")
                Toast.makeText(this, "Erro ao carregar posts!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupRealtimeListener() {
        firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("MainActivity", "Erro ao escutar atualizações: ${error.message}")
                    Toast.makeText(this, "Erro ao escutar atualizações: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    Log.d("MainActivity", "Atualizações recebidas dos posts em tempo real!")
                    posts.clear()
                    for (document in snapshots) {
                        val content = document.getString("content") ?: ""
                        val imageUri = document.getString("imageUri")
                        val userName = document.getString("userName") ?: "Utilizador Desconhecido"
                        val timestamp = document.getString("timestamp") ?: "Data desconhecida"

                        posts.add(Post(content, imageUri, userName, timestamp))
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }

    companion object {
        const val REQUEST_CODE_PICK_IMAGE = 1
    }
}
