package ip.santarem.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class CategoriesFragment : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_categories)

        // Set up click listeners for each category button
        val buttonAngry = findViewById<Button>(R.id.button_angry)
        val buttonSad = findViewById<Button>(R.id.button_sad)
        val buttonDesperate = findViewById<Button>(R.id.button_desperate)

        buttonAngry.setOnClickListener {
            val intent = Intent(this, AngryActivity::class.java)
            startActivity(intent)
        }

        buttonSad.setOnClickListener {
            val intent = Intent(this, SadActivity::class.java)
            startActivity(intent)
        }

        buttonDesperate.setOnClickListener {
            val intent = Intent(this, DesperateActivity::class.java)
            startActivity(intent)
        }
    }
}
