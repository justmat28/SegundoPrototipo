package com.jmaa.segundoprototipo

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.jmaa.segundoprototipo.databinding.FragmentReportsBinding
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        generarReportes()
    }

    private fun generarReportes() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val mermas = SupabaseManager.client.from("mermas").select().decodeList<Merma>()
                val motivos = SupabaseManager.client.from("motivos_merma").select().decodeList<MotivoMerma>()
                val totalGlobalKg = mermas.sumOf { it.cantidadKg }

                val agrupado = mermas.groupBy { it.motivoId }
                    .mapValues { entry -> entry.value.sumOf { it.cantidadKg } }

                withContext(Dispatchers.Main) {
                    agrupado.forEach { (motivoId, kgTotal) ->
                        val desc = motivos.find { it.id == motivoId }?.descripcion ?: "Motivo #$motivoId"
                        val porcentaje = if (totalGlobalKg > 0) ((kgTotal / totalGlobalKg) * 100).toInt() else 0

                        val itemView = LinearLayout(requireContext()).apply {
                            orientation = LinearLayout.VERTICAL
                            setPadding(0, 16, 0, 16)
                        }

                        val tvLabel = TextView(requireContext()).apply {
                            text = "$desc: ${String.format("%.1f", kgTotal)} kg ($porcentaje%)"
                            setTextColor(Color.parseColor("#16191E"))
                            textSize = 14f
                        }

                        val progressBar = ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal).apply {
                            max = 100
                            progress = porcentaje
                            progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#EF3340"))
                        }

                        itemView.addView(tvLabel)
                        itemView.addView(progressBar)
                        binding.containerMotivos.addView(itemView)
                    }
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