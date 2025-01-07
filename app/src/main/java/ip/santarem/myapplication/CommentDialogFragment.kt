package ip.santarem.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CommentDialogFragment(private val postContent: String) : DialogFragment() {

    private val comments = mutableListOf<String>()
    private lateinit var commentAdapter: CommentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_comment_dialog, container, false)

        // Configurar RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewComments)
        recyclerView.layoutManager = LinearLayoutManager(context)
        commentAdapter = CommentAdapter(comments)
        recyclerView.adapter = commentAdapter

        // Mostrar o post
        val postTextView: TextView = view.findViewById(R.id.postTextView)
        postTextView.text = postContent

        // Adicionar comentário
        val commentInput: EditText = view.findViewById(R.id.commentInput)
        val sendButton: Button = view.findViewById(R.id.sendButton)
        sendButton.setOnClickListener {
            val newComment = commentInput.text.toString()
            if (newComment.isNotBlank()) {
                comments.add(newComment)
                commentAdapter.notifyItemInserted(comments.size - 1)
                commentInput.text.clear()
            }
        }

        return view
    }
}
