package ip.santarem.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView // Alterado para email
    private lateinit var btnResetPassword: Button
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_profile)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Vincular views
        tvUsername = findViewById(R.id.tvUsername)
        tvEmail = findViewById(R.id.tvEmail) // Alterado para email
        btnResetPassword = findViewById(R.id.btnResetPassword)
        btnLogout = findViewById(R.id.btnLogout)

        // Carregar informações do usuário
        loadUserInfo()

        // Redefinir senha
        btnResetPassword.setOnClickListener {
            val email = auth.currentUser?.email
            if (email != null) {
                auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Email de redefinição enviado!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Erro ao enviar email de redefinição!", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Email do usuário não encontrado!", Toast.LENGTH_SHORT).show()
            }
        }

        // Logout
        btnLogout.setOnClickListener {
            // Apagar preferências de login
            val editor = getSharedPreferences("login_prefs", MODE_PRIVATE).edit()
            editor.clear()
            editor.apply()

            // Fazer logout no Firebase
            auth.signOut()

            // Redirecionar para LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun loadUserInfo() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            Log.d("ProfileFragment", "User ID: $userId")  // Adicionando log para depuração

            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Verificar se o documento contém os campos esperados
                        val username = document.getString("username")
                        val email = currentUser.email // Pegando o email diretamente do FirebaseAuth
                        Log.d("ProfileFragment", "Username: $username, Email: $email")  // Adicionando log para depuração

                        tvUsername.text = username ?: "Usuário desconhecido"
                        tvEmail.text = email ?: "Email não disponível"
                    } else {
                        Log.e("ProfileFragment", "Documento não encontrado")
                        tvUsername.text = "Usuário não encontrado"
                        tvEmail.text = "Email não encontrado"
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("ProfileFragment", "Erro ao carregar dados: ${exception.message}")  // Log de erro
                    tvUsername.text = "Erro ao carregar dados"
                    tvEmail.text = "Erro ao carregar dados"
                }
        } else {
            Log.e("ProfileFragment", "Usuário não autenticado")
            tvUsername.text = "Não autenticado"
            tvEmail.text = "Não autenticado"
        }
    }
}
