package com.jmaa.segundoprototipo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jmaa.segundoprototipo.databinding.ItemMermaRecienteBinding

class MermaAdapter(private var items: List<MermaUIModel>) :
    RecyclerView.Adapter<MermaAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemMermaRecienteBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMermaRecienteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvProductoNombre.text = item.productoNombre
        holder.binding.tvDetalleSub.text = "${item.lineaNombre} • ${item.motivoDescripcion}"
        holder.binding.tvCantidadKg.text = "${item.cantidadKg} kg"
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<MermaUIModel>) {
        items = newItems
        notifyDataSetChanged()
    }
}