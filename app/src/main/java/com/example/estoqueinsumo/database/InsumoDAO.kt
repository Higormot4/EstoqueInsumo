package com.example.estoqueinsumo.database

import com.example.estoqueinsumo.model.Insumo

/**
 * Interface que define as operações de CRUD para a entidade Insumo.
 */
interface InsumoDAO {
    fun insert(insumo: Insumo): Long
    fun update(insumo: Insumo): Int
    fun delete(id: Long): Int
    fun queryAll(): List<Insumo>
    fun queryById(id: Long): Insumo?
    
    // Novos métodos para a Etapa 2
    fun queryByName(nome: String): List<Insumo>
    fun queryLowStock(): List<Insumo>
    fun registrarMovimentacao(insumoId: Long, tipo: String, quantidade: Double): Long
}
