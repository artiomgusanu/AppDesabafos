package ip.santarem.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class RespostaActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var responseAdapter: ResponseAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var postId: String

    private lateinit var postContentTextView: TextView  // TextView para exibir o conteúdo do post
    private lateinit var postContentEditText: EditText  // EditText para editar o conteúdo da resposta
    private lateinit var viewResponsesButton: Button  // Botão para visualizar as respostas

    private val responsesList = mutableListOf<Respostas>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_respostas)

        // Inicializar Firestore
        firestore = FirebaseFirestore.getInstance()

        // Obter o ID do post passado pela Intent
        postId = intent.getStringExtra("POST_ID") ?: ""
        if (postId.isEmpty()) {
            Toast.makeText(this, "Erro: ID do post não encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Inicializar a interface
        postContentTextView = findViewById(R.id.tvContent)  // Exibir o conteúdo do post
        postContentEditText = findViewById(R.id.etPostContent)  // EditText para inserir a resposta
        viewResponsesButton = findViewById(R.id.btn_view_responses)  // Botão de ver respostas

        // Configurar a RecyclerView
        recyclerView = findViewById(R.id.recycler_view_responses)
        recyclerView.layoutManager = LinearLayoutManager(this)
        responseAdapter = ResponseAdapter(responsesList)
        recyclerView.adapter = responseAdapter

        // Carregar o conteúdo do post
        loadPostContent()

        // Configurar o clique no botão
        viewResponsesButton.setOnClickListener {
            val userInput = postContentEditText.text.toString().trim()

            if (userInput.isNotEmpty()) {
                compareContentAndShowResponses(userInput)
            } else {
                Toast.makeText(this, "Por favor, insira o conteúdo do post", Toast.LENGTH_SHORT).show()
            }
        }

    }

    // Carregar o conteúdo do post no Firestore
    private fun loadPostContent() {
        firestore.collection("posts")
            .document(postId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val postContent = document.getString("content")
                    postContentTextView.text = postContent // Exibe o conteúdo do post
                } else {
                    Toast.makeText(this, "Post não encontrado", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar conteúdo do post", Toast.LENGTH_SHORT).show()
            }
    }

    // Comparar o conteúdo inserido com o conteúdo do post
    private fun compareContentAndShowResponses(userInput: String) {
        firestore.collection("posts")
            .document(postId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val postContent = document.getString("content")

                    // Comparar o conteúdo do post com o conteúdo inserido pelo usuário
                    if (postContent == userInput) {
                        loadResponses() // Carregar as respostas se os conteúdos coincidirem
                    } else {
                        Toast.makeText(this, "O conteúdo não corresponde ao post", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Post não encontrado", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao comparar conteúdo do post", Toast.LENGTH_SHORT).show()
            }
    }

    // Carregar as respostas associadas ao post
    private fun loadResponses() {
        firestore.collection("respostas")
            .whereEqualTo("id_post", postId) // Filtra as respostas pelo ID do post
            .get()
            .addOnSuccessListener { querySnapshot ->
                responsesList.clear()
                for (document in querySnapshot.documents) {
                    val response = document.toObject(Respostas::class.java)
                    response?.let { responsesList.add(it) }
                }
                responseAdapter.notifyDataSetChanged() // Atualiza a RecyclerView com as respostas
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar respostas", Toast.LENGTH_SHORT).show()
            }
    }
}
