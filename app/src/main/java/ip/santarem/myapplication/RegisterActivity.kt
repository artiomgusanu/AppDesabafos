package ip.santarem.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var etName: EditText
    private lateinit var etRegisterEmail: EditText
    private lateinit var etRegisterPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var btnBackToLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inicializar Firebase Auth e Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Vincular views
        etName = findViewById(R.id.etName)
        etRegisterEmail = findViewById(R.id.etRegisterEmail)
        etRegisterPassword = findViewById(R.id.etRegisterPassword)
        btnRegister = findViewById(R.id.btnRegister)
        btnBackToLogin = findViewById(R.id.btnBackToLogin)

        // Botão de Registro
        btnRegister.setOnClickListener {
            val name = etName.text.toString().ifBlank { generateRandomUsername() }
            val email = etRegisterEmail.text.toString()
            val password = etRegisterPassword.text.toString()

            if (email.isNotBlank() && password.isNotBlank()) {
                registerUser(name, email, password)
            } else {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            }
        }

        // Botão para voltar ao Login
        btnBackToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    /**
     * Realiza o registro do usuário no Firebase Authentication e salva o nome no Firestore.
     */
    private fun registerUser(name: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        saveUserToFirestore(userId, name, email)
                    } else {
                        Toast.makeText(this, "Erro ao obter ID do usuário.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorMessage = task.exception?.message ?: "Erro desconhecido"
                    if (errorMessage.contains("email")) {
                        Toast.makeText(this, "Este email já está em uso.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Erro ao registrar usuário: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    /**
     * Salva o nome de usuário e email no Firestore.
     */
    private fun saveUserToFirestore(userId: String, name: String, email: String) {
        val userMap = hashMapOf(
            "username" to name,
            "email" to email
        )

        firestore.collection("users").document(userId)
            .set(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Usuário registrado com sucesso!", Toast.LENGTH_SHORT).show()
                // Voltar ao login após registro
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao salvar dados no Firestore.", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Gera um nome de usuário aleatório caso o campo esteja vazio.
     */
    private fun generateRandomUsername(): String {
        return "User${(1000..9999).random()}"
    }
}
