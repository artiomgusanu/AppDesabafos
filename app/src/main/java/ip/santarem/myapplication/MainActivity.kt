package ip.santarem.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PostAdapter
    private val posts = mutableListOf<Post>()

    // Declare a variável aqui, como nullable
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializando RecyclerView
        recyclerView = findViewById(R.id.rvPosts)
        adapter = PostAdapter(posts)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val btnPost = findViewById<ImageButton>(R.id.btnPost)
        val btnAddImage = findViewById<ImageButton>(R.id.btnAddImage)
        val etPostContent = findViewById<EditText>(R.id.etPostContent)

        // Para armazenar imagem
        var selectedImageUri: Uri? = null

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
                posts.add(Post(content, selectedImageUri?.toString()))
                adapter.notifyItemInserted(posts.size - 1)
                etPostContent.text.clear()
                selectedImageUri = null
                Toast.makeText(this, "Post publicado!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Por favor, insira um texto ou imagem!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK) {
            selectedImageUri = data?.data // Agora está acessível porque foi declarada na classe
        }
    }

    companion object {
        const val REQUEST_CODE_PICK_IMAGE = 1
    }
}
