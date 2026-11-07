package com.jmaa.segundoprototipo

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jmaa.segundoprototipo.databinding.ActivityRegisterMermaBinding
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterMermaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterMermaBinding
    private var listaProductos: List<Producto> = emptyList()
    private var listaLineas: List<LineaProduccion> = emptyList()
    private var listaMotivos: List<MotivoMerma> = emptyList()
    private val listaTiposMerma = listOf("Insumo", "Producto Terminado")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterMermaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cargarCatalogos()

        binding.btnVolver.setOnClickListener {
            finish()
        }

        binding.btnRegistrar.setOnClickListener {
            guardarMerma()
        }
    }

    private fun cargarCatalogos() {
        binding.progressLoading.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                listaProductos = SupabaseManager.client.from("productos").select().decodeList<Producto>()
                listaLineas = SupabaseManager.client.from("lineas_produccion").select().decodeList<LineaProduccion>()
                listaMotivos = SupabaseManager.client.from("motivos_merma").select().decodeList<MotivoMerma>()

                withContext(Dispatchers.Main) {
                    binding.progressLoading.visibility = View.GONE

                    val adapterProductos = ArrayAdapter(
                        this@RegisterMermaActivity,
                        android.R.layout.simple_dropdown_item_1line,
                        listaProductos.map { it.nombre }
                    )
                    binding.spProducto.setAdapter(adapterProductos)

                    val adapterLineas = ArrayAdapter(
                        this@RegisterMermaActivity,
                        android.R.layout.simple_dropdown_item_1line,
                        listaLineas.map { it.nombre }
                    )
                    binding.spLinea.setAdapter(adapterLineas)

                    val adapterMotivos = ArrayAdapter(
                        this@RegisterMermaActivity,
                        android.R.layout.simple_dropdown_item_1line,
                        listaMotivos.map { it.descripcion }
                    )
                    binding.spMotivo.setAdapter(adapterMotivos)

                    val adapterTipos = ArrayAdapter(
                        this@RegisterMermaActivity,
                        android.R.layout.simple_dropdown_item_1line,
                        listaTiposMerma
                    )
                    binding.spTipoMerma.setAdapter(adapterTipos)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressLoading.visibility = View.GONE
                    Toast.makeText(this@RegisterMermaActivity, "Modo sin conexión activo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun guardarMerma() {
        val prodNombre = binding.spProducto.text.toString().trim()
        val linNombre = binding.spLinea.text.toString().trim()
        val motDesc = binding.spMotivo.text.toString().trim()
        val tipo = binding.spTipoMerma.text.toString().trim()
        val cantStr = binding.etCantidad.text.toString().trim()
        val obs = binding.etObservaciones.text.toString().trim()

        if (prodNombre.isEmpty() || linNombre.isEmpty() || motDesc.isEmpty() || tipo.isEmpty() || cantStr.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val cantidadKg = cantStr.toDoubleOrNull()
        if (cantidadKg == null || cantidadKg <= 0.0) {
            Toast.makeText(this, "Cantidad en Kg no válida", Toast.LENGTH_SHORT).show()
            return
        }

        val productoId = listaProductos.find { it.nombre.equals(prodNombre, ignoreCase = true) }?.id ?: 1L
        val lineaId = listaLineas.find { it.nombre.equals(linNombre, ignoreCase = true) }?.id ?: 1L
        val motivoId = listaMotivos.find { it.descripcion.equals(motDesc, ignoreCase = true) }?.id ?: 1L
        val usuarioId = SupabaseManager.client.auth.currentUserOrNull()?.id

        val nuevaMerma = Merma(
            productoId = productoId,
            lineaId = lineaId,
            tipoMerma = tipo,
            cantidadKg = cantidadKg,
            motivoId = motivoId,
            observaciones = obs.ifEmpty { null },
            usuarioId = usuarioId
        )

        binding.btnRegistrar.isEnabled = false
        binding.progressLoading.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                SupabaseManager.client.from("mermas").insert(nuevaMerma)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegisterMermaActivity, "¡Merma guardada en Supabase!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                OfflineMermaManager.saveOffline(this@RegisterMermaActivity, nuevaMerma)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegisterMermaActivity, "Sin conexión. Guardado en cola offline.", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }
}