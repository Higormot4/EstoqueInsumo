package com.example.estoqueinsumo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.estoqueinsumo.database.InsumoRepository
import com.example.estoqueinsumo.model.Insumo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel responsável pela lógica de negócio do estoque.
 * Gerencia as operações de CRUD, validações e regras de movimentação.
 */
class EstoqueViewModel(private val repository: InsumoRepository) : ViewModel() {

    private val _insumos = MutableStateFlow<List<Insumo>>(emptyList())
    val insumos: StateFlow<List<Insumo>> = _insumos

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro

    init {
        listarTodos()
    }

    /**
     * 1. Adicionar novo insumo com validações.
     */
    fun adicionarInsumo(nome: String, quantidade: Double, unidade: String, qtdMinima: Double, validade: String?) {
        if (nome.isBlank()) {
            _erro.value = "O nome do insumo não pode estar vazio."
            return
        }
        if (quantidade < 0) {
            _erro.value = "A quantidade inicial não pode ser negativa."
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            // Regra: verificar se já existe um insumo com o mesmo nome
            val existentes = repository.queryByName(nome)
            val existeExato = existentes.any { it.nome.equals(nome, ignoreCase = true) }

            if (existeExato) {
                withContext(Dispatchers.Main) {
                    _erro.value = "Insumo já cadastrado. Sugerimos atualizar a quantidade do item existente."
                }
            } else {
                val novoInsumo = Insumo(
                    nome = nome,
                    quantidade = quantidade,
                    unidadeMedida = unidade,
                    quantidadeMinima = qtdMinima,
                    dataValidade = validade
                )
                repository.insert(novoInsumo)
                listarTodos()
            }
        }
    }

    /**
     * 2. Listar todos os insumos (ordenados por nome via query).
     */
    fun listarTodos() {
        viewModelScope.launch(Dispatchers.IO) {
            val lista = repository.queryAll()
            _insumos.value = lista
        }
    }

    /**
     * 3. Atualizar quantidade (entrada/saída) com registro de movimentação.
     */
    fun atualizarQuantidade(insumo: Insumo, mudanca: Double, tipo: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val novaQuantidade = insumo.quantidade + mudanca

            // Regra: não permitir estoque negativo na saída
            if (novaQuantidade < 0) {
                withContext(Dispatchers.Main) {
                    _erro.value = "Saldo insuficiente! Operação cancelada."
                }
                return@launch
            }

            val insumoAtualizado = insumo.copy(quantidade = novaQuantidade)
            repository.update(insumoAtualizado)
            
            // 7. Registrar movimentação
            repository.registrarMovimentacao(insumo.id, tipo, mudanca)
            
            listarTodos()
        }
    }

    /**
     * 4. Remover insumo (apenas se quantidade == 0).
     */
    fun removerInsumo(insumo: Insumo) {
        if (insumo.quantidade > 0) {
            _erro.value = "Não é possível remover um insumo com saldo em estoque."
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(insumo.id)
            listarTodos()
        }
    }

    /**
     * 5. Buscar insumo por nome (busca parcial).
     */
    fun buscarPorNome(nome: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val lista = repository.queryByName(nome)
            _insumos.value = lista
        }
    }

    /**
     * 6. Gerar alerta de baixo estoque.
     */
    fun filtrarEstoqueBaixo() {
        viewModelScope.launch(Dispatchers.IO) {
            val lista = repository.queryLowStock()
            _insumos.value = lista
        }
    }

    fun limparErro() {
        _erro.value = null
    }
}
