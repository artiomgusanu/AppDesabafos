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
        val userId = auth.currentUser?.uid ?: "Anônimo"
        val userName = "Usuário" // Aqui você pode buscar o nome real do usuário do Firestore
        val timestamp = System.currentTimeMillis()
        val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))

        val postMap = hashMapOf(
            "content" to content,
            "imageUri" to (imageUri?.toString() ?: ""),
            "userId" to userId,
            "userName" to userName,
            "timestamp" to formattedDate
        )

        firestore.collection("posts")
            .add(postMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Post publicado!", Toast.LENGTH_SHORT).show()
                loadPostsFromFirestore()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao publicar o post!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadPostsFromFirestore() {
        firestore.collection("posts")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { result ->
                posts.clear()
                for (document in result) {
                    val content = document.getString("content") ?: ""
                    val imageUri = document.getString("imageUri")
                    val userName = document.getString("userName") ?: "Anônimo"
                    val timestamp = document.getString("timestamp") ?: ""

                    posts.add(Post(content, imageUri, userName, timestamp))
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar posts!", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        const val REQUEST_CODE_PICK_IMAGE = 1
    }
}
