package ip.santarem.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Desabafo(val categoria: String, val texto: String, val data: String)

class DesabafoAdapter(private val desabafos: List<Desabafo>) :
    RecyclerView.Adapter<DesabafoAdapter.DesabafoViewHolder>() {

    class DesabafoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategoria: TextView = view.findViewById(R.id.tvCategoria)
        val tvDesabafo: TextView = view.findViewById(R.id.tvDesabafo)
        val tvData: TextView = view.findViewById(R.id.tvData)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DesabafoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_desabafo, parent, false)
        return DesabafoViewHolder(view)
    }

    override fun onBindViewHolder(holder: DesabafoViewHolder, position: Int) {
        val desabafo = desabafos[position]
        holder.tvCategoria.text = "Categoria: ${desabafo.categoria}"
        holder.tvDesabafo.text = desabafo.texto
        holder.tvData.text = desabafo.data
    }

    override fun getItemCount(): Int = desabafos.size
}
