package ip.santarem.myapplication

import android.content.Intent
import android.os.Bundle
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
    private lateinit var tvName: TextView
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
        tvName = findViewById(R.id.tvName)
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
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    tvUsername.text = document.getString("username") ?: "Usuário desconhecido"
                    tvName.text = document.getString("name") ?: "Nome não disponível"
                }
                .addOnFailureListener {
                    tvUsername.text = "Erro ao carregar dados"
                    tvName.text = "Erro ao carregar dados"
                }
        } else {
            tvUsername.text = "Não autenticado"
            tvName.text = "Não autenticado"
        }
    }
}
