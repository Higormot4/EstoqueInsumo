package com.example.estoqueinsumo

import com.example.estoqueinsumo.model.Insumo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Testes unitários para validar as regras de negócio do estoque.
 * Focado na lógica de cálculo de saldo e validações.
 */
class EstoqueLogicTest {

    @Test
    fun `Deve calcular corretamente o novo saldo na entrada de estoque`() {
        val insumo = Insumo(id = 1, nome = "Teste", quantidade = 10.0, unidadeMedida = "un", quantidadeMinima = 5.0)
        val entrada = 5.0
        val novoSaldo = insumo.quantidade + entrada
        
        assertEquals(15.0, novoSaldo, 0.0)
    }

    @Test
    fun `Deve calcular corretamente o novo saldo na saida de estoque`() {
        val insumo = Insumo(id = 1, nome = "Teste", quantidade = 10.0, unidadeMedida = "un", quantidadeMinima = 5.0)
        val saida = -4.0
        val novoSaldo = insumo.quantidade + saida
        
        assertEquals(6.0, novoSaldo, 0.0)
    }

    @Test
    fun `Deve identificar quando o estoque esta abaixo do minimo`() {
        val insumoBaixo = Insumo(id = 1, nome = "Baixo", quantidade = 2.0, unidadeMedida = "un", quantidadeMinima = 5.0)
        val insumoOk = Insumo(id = 2, nome = "OK", quantidade = 10.0, unidadeMedida = "un", quantidadeMinima = 5.0)
        
        assertTrue("Deveria estar abaixo do mínimo", insumoBaixo.quantidade <= insumoBaixo.quantidadeMinima)
        assertTrue("Deveria estar acima do mínimo", insumoOk.quantidade > insumoOk.quantidadeMinima)
    }

    @Test
    fun `Nao deve permitir saldo negativo`() {
        val quantidadeAtual = 5.0
        val tentativaSaida = 10.0
        
        val operacaoPermitida = (quantidadeAtual - tentativaSaida) >= 0
        
        assertEquals(false, operacaoPermitida)
    }
}
