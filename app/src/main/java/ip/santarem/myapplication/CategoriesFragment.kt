package ip.santarem.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class CategoriesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_categories, container, false)

        // Set up click listeners for each category button
        val buttonAngry = view.findViewById<Button>(R.id.button_angry)
        val buttonSad = view.findViewById<Button>(R.id.button_sad)
        val buttonDesperate = view.findViewById<Button>(R.id.button_desperate)

        buttonAngry.setOnClickListener {
            val intent = Intent(activity, AngryActivity::class.java)
            startActivity(intent)
        }

        buttonSad.setOnClickListener {
            val intent = Intent(activity, SadActivity::class.java)
            startActivity(intent)
        }

        buttonDesperate.setOnClickListener {
            val intent = Intent(activity, DesperateActivity::class.java)
            startActivity(intent)
        }

        return view
    }

}
