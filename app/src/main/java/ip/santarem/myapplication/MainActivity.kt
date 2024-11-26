package ip.santarem.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewDesabafos)

        val desabafos = listOf(
            Desabafo("Triste", "Hoje foi um dia difícil...", "25/11/2024"),
            Desabafo("Irritado", "O trânsito está impossível!", "25/11/2024"),
            Desabafo("Confuso", "Não sei o que fazer com meu futuro.", "25/11/2024")
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = DesabafoAdapter(desabafos)
    }
}
