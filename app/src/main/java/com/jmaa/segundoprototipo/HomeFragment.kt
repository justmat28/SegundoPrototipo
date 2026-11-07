package com.jmaa.segundoprototipo

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.jmaa.segundoprototipo.databinding.FragmentHomeBinding
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: MermaAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycler()
        cargarUsuario()

        binding.btnAbrirRegistro.setOnClickListener {
            startActivity(Intent(requireContext(), RegisterMermaActivity::class.java))
        }

        binding.btnRefresh.setOnClickListener {
            cargarDashboard()
        }

        binding.btnLogout.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                SupabaseManager.client.auth.signOut()
                withContext(Dispatchers.Main) {
                    startActivity(Intent(requireContext(), LoginActivity::class.java))
                    requireActivity().finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        cargarDashboard()
    }

    private fun setupRecycler() {
        adapter = MermaAdapter(emptyList())
        binding.rvUltimosRegistros.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUltimosRegistros.adapter = adapter
    }

    private fun cargarUsuario() {
        val email = SupabaseManager.client.auth.currentUserOrNull()?.email ?: "Operador de Planta"
        binding.tvUserEmail.text = email
    }

    private fun cargarDashboard() {
        lifecycleScope.launch(Dispatchers.IO) {
            // Sincronización transparente si existían mermas guardadas offline
            val synced = OfflineMermaManager.syncPendingMermas(requireContext())

            try {
                val mermas = SupabaseManager.client.from("mermas").select().decodeList<Merma>()
                val productos = SupabaseManager.client.from("productos").select().decodeList<Producto>()
                val lineas = SupabaseManager.client.from("lineas_produccion").select().decodeList<LineaProduccion>()
                val motivos = SupabaseManager.client.from("motivos_merma").select().decodeList<MotivoMerma>()

                val totalKg = mermas.sumOf { it.cantidadKg }

                val uiItems = mermas.take(10).map { merma ->
                    val prod = productos.find { it.id == merma.productoId }?.nombre ?: "Producto"
                    val lin = lineas.find { it.id == merma.lineaId }?.nombre ?: "Línea"
                    val mot = motivos.find { it.id == merma.motivoId }?.descripcion ?: "Motivo"
                    MermaUIModel(merma.id, prod, lin, mot, merma.cantidadKg, merma.tipoMerma, merma.createdAt ?: "")
                }

                withContext(Dispatchers.Main) {
                    binding.tvMermaHoyKg.text = String.format("%.1f kg", totalKg)
                    binding.tvRegistrosCount.text = "${mermas.size} eventos registrados en planta"
                    adapter.updateData(uiItems)
                    if (synced > 0) {
                        Toast.makeText(requireContext(), "Sincronizados $synced registros pendientes", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    val pending = OfflineMermaManager.getPendingMermas(requireContext()).size
                    if (pending > 0) {
                        Toast.makeText(requireContext(), "Modo sin conexión. $pending mermas pendientes.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}