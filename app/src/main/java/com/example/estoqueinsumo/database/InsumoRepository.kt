package com.example.estoqueinsumo.database

import android.util.Log
import com.example.estoqueinsumo.model.Insumo

/**
 * Repositório que gerencia a manipulação de dados de Insumos e Movimentações.
 * Implementa a interface InsumoDAO e utiliza o DatabaseHelper para acessar o SQLite.
 */
class InsumoRepository(private val dbHelper: DatabaseHelper) : InsumoDAO {

    companion object {
        private const val TAG = "InsumoRepository"
    }

    override fun insert(insumo: Insumo): Long {
        return try {
            dbHelper.insert(insumo)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao inserir insumo", e)
            -1L
        }
    }

    override fun update(insumo: Insumo): Int {
        return try {
            dbHelper.update(insumo)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao atualizar insumo", e)
            0
        }
    }

    override fun delete(id: Long): Int {
        return try {
            dbHelper.delete(id)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao deletar insumo", e)
            0
        }
    }

    override fun queryAll(): List<Insumo> {
        return try {
            dbHelper.queryAll()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao listar insumos", e)
            emptyList()
        }
    }

    override fun queryById(id: Long): Insumo? {
        return try {
            dbHelper.queryById(id)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar insumo por ID", e)
            null
        }
    }

    /**
     * Busca insumos por nome (parcial).
     */
    override fun queryByName(nome: String): List<Insumo> {
        return try {
            dbHelper.queryByName(nome)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar insumo por nome", e)
            emptyList()
        }
    }

    /**
     * Retorna insumos com estoque baixo.
     */
    override fun queryLowStock(): List<Insumo> {
        return try {
            dbHelper.queryLowStock()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar estoque baixo", e)
            emptyList()
        }
    }

    /**
     * Registra uma movimentação (entrada ou saída).
     */
    override fun registrarMovimentacao(insumoId: Long, tipo: String, quantidade: Double): Long {
        return try {
            dbHelper.insertMovimentacao(insumoId, tipo, quantidade, System.currentTimeMillis())
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao registrar movimentação", e)
            -1L
        }
    }
}
