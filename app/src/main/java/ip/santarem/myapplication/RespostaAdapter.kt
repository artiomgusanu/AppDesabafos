package ip.santarem.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListAdapter
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class ResponseAdapter(private val responses: List<Respostas>) : RecyclerView.Adapter<ResponseAdapter.ResponseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResponseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_resposta, parent, false)
        return ResponseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResponseViewHolder, position: Int) {
        val response = responses[position]

        // Atualizando o conteúdo da resposta
        holder.tvUserName.text = response.userName
        holder.tvTimestamp.text = response.timestamp
        holder.tvResponse.text = response.Resposta
    }

    override fun getItemCount(): Int {
        return responses.size
    }

    // ViewHolder para associar os itens da lista
    inner class ResponseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUserName: TextView = itemView.findViewById(R.id.tv_user_name)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tv_timestamp)
        val tvResponse: TextView = itemView.findViewById(R.id.tv_response)
    }
}

