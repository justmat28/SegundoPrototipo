package com.jmaa.segundoprototipo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.jmaa.segundoprototipo.databinding.FragmentHistoryBinding
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: MermaAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = MermaAdapter(emptyList())
        binding.rvHistorialCompleto.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistorialCompleto.adapter = adapter
        cargarHistorial()
    }

    private fun cargarHistorial() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val mermas = SupabaseManager.client.from("mermas").select().decodeList<Merma>()
                val productos = SupabaseManager.client.from("productos").select().decodeList<Producto>()
                val lineas = SupabaseManager.client.from("lineas_produccion").select().decodeList<LineaProduccion>()
                val motivos = SupabaseManager.client.from("motivos_merma").select().decodeList<MotivoMerma>()

                val uiItems = mermas.map { merma ->
                    val prod = productos.find { it.id == merma.productoId }?.nombre ?: "Producto"
                    val lin = lineas.find { it.id == merma.lineaId }?.nombre ?: "Línea"
                    val mot = motivos.find { it.id == merma.motivoId }?.descripcion ?: "Motivo"
                    MermaUIModel(merma.id, prod, lin, mot, merma.cantidadKg, merma.tipoMerma, merma.createdAt ?: "")
                }

                withContext(Dispatchers.Main) {
                    adapter.updateData(uiItems)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}