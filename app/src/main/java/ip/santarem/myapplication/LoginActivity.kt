package ip.santarem.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicialize o FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Inicialize os campos
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        registerButton = findViewById(R.id.registerButton)
        loginButton = findViewById(R.id.loginButton)

        // Função de registro
        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotBlank() && password.isNotBlank()) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Registro bem-sucedido!", Toast.LENGTH_SHORT).show()

                            // Salvar dados no Firebase após o registro
                            saveUserData(email)  // Chama a função para salvar os dados do usuário
                        } else {
                            Toast.makeText(this, "Erro ao registrar usuário: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            }
        }

        // Função de login
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotBlank() && password.isNotBlank()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()

                            // Salvar dados no Firebase após o login
                            saveUserData(email)  // Chama a função para salvar os dados do usuário
                        } else {
                            Toast.makeText(this, "Erro ao fazer login: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Função para salvar os dados do usuário no Firebase
    private fun saveUserData(email: String) {
        val user = auth.currentUser
        val userId = user?.uid
        val userEmail = email

        if (userId != null) {
            // Usando Realtime Database
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("users").child(userId)

            // Criando um objeto com as informações a serem salvas
            val userData = UserData(username = userEmail)

            // Salvando os dados do usuário
            myRef.setValue(userData).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Dados salvos com sucesso no Realtime Database!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Erro ao salvar dados no Realtime Database: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }

            // Ou, se preferir usar Firestore:
            val firestore = FirebaseFirestore.getInstance()
            val userRef = firestore.collection("users").document(userId)

            // Salvando os dados no Firestore
            userRef.set(userData).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Dados salvos com sucesso no Firestore!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Erro ao salvar dados no Firestore: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

// Modelo de dados que será salvo no Firebase
data class UserData(val username: String)
