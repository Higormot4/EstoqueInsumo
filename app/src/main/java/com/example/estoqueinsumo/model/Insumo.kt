package com.example.estoqueinsumo.model

/**
 * Representa um insumo no estoque.
 *
 * @property id Identificador único no banco de dados (autoincremento).
 * @property nome Nome do insumo (ex: Farinha, Ácido Cítrico).
 * @property quantidade Quantidade atual em estoque.
 * @property unidadeMedida Unidade de medida (ex: kg, L, un).
 * @property quantidadeMinima Quantidade mínima recomendada para alerta de estoque baixo.
 * @property dataValidade Data de validade do insumo (opcional).
 */
data class Insumo(
    val id: Long = 0,
    val nome: String,
    val quantidade: Double,
    val unidadeMedida: String,
    val quantidadeMinima: Double,
    val dataValidade: String? = null
)
