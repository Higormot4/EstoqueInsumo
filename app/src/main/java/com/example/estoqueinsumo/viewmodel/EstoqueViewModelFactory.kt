package com.example.estoqueinsumo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.estoqueinsumo.database.InsumoRepository

/**
 * Fábrica para criar o EstoqueViewModel injetando o repositório necessário.
 */
class EstoqueViewModelFactory(private val repository: InsumoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EstoqueViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EstoqueViewModel(repository) as T
        }
        throw IllegalArgumentException("Classe ViewModel desconhecida")
    }
}
