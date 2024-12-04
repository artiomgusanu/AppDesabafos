package ip.santarem.myapplication

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var etLoginEmail: EditText
    private lateinit var etLoginPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoToRegister: Button
    private lateinit var checkboxRememberMe: CheckBox

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences("login_prefs", MODE_PRIVATE)
        editor = sharedPreferences.edit()

        // Verifica se o utilizador está logado
        if (checkLoginStatus()) {
            navigateToMainActivity()
            return // Evita carregar o layout do LoginActivity
        }

        setContentView(R.layout.activity_login)

        // Vincular views
        etLoginEmail = findViewById(R.id.etLoginEmail)
        etLoginPassword = findViewById(R.id.etLoginPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoToRegister = findViewById(R.id.btnGoToRegister)
        checkboxRememberMe = findViewById(R.id.checkboxRememberMe)

        // Botão de Login
        btnLogin.setOnClickListener {
            val email = etLoginEmail.text.toString()
            val password = etLoginPassword.text.toString()
            val rememberMe = checkboxRememberMe.isChecked

            if (email.isNotBlank() && password.isNotBlank()) {
                // Tentativa de login com Firebase
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Salva o estado de login se o usuário escolher "lembrar-me"
                            if (rememberMe) {
                                editor.putBoolean("isLoggedIn", true)
                                editor.apply()
                            }

                            navigateToMainActivity()
                        } else {
                            Toast.makeText(this, "Erro ao fazer login!", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            }
        }

        // Botão para ir ao Registro
        btnGoToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    // Verifica o status de login
    private fun checkLoginStatus(): Boolean {
        // Verifica se o usuário já está autenticado com o Firebase
        val currentUser = auth.currentUser
        if (currentUser != null) {
            return true
        }

        // Se o usuário não estiver logado, verifica o estado de "lembrar-me"
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        return isLoggedIn
    }

    // Redireciona para a MainActivity
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Evita que o usuário volte para a tela de login
    }

    // Garantir que a verificação do estado de login é feita novamente ao retomar a atividade
    override fun onResume() {
        super.onResume()
        if (checkLoginStatus()) {
            navigateToMainActivity()
        }
    }
}
