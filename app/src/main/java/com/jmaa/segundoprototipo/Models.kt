package com.jmaa.segundoprototipo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Producto(
    val id: Long = 0,
    val nombre: String,
    val activo: Boolean = true
)

@Serializable
data class LineaProduccion(
    val id: Long = 0,
    val nombre: String,
    val activo: Boolean = true
)

@Serializable
data class MotivoMerma(
    val id: Long = 0,
    val descripcion: String,
    val activo: Boolean = true
)

@Serializable
data class Merma(
    val id: String? = null,
    @SerialName("producto_id") val productoId: Long,
    @SerialName("linea_id") val lineaId: Long,
    @SerialName("tipo_merma") val tipoMerma: String,
    @SerialName("cantidad_kg") val cantidadKg: Double,
    @SerialName("motivo_id") val motivoId: Long,
    val observaciones: String? = null,
    @SerialName("usuario_id") val usuarioId: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

data class MermaUIModel(
    val id: String?,
    val productoNombre: String,
    val lineaNombre: String,
    val motivoDescripcion: String,
    val cantidadKg: Double,
    val tipoMerma: String,
    val fechaHora: String
)