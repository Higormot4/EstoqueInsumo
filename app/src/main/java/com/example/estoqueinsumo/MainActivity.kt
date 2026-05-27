package com.example.estoqueinsumo

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.estoqueinsumo.database.DatabaseHelper
import com.example.estoqueinsumo.database.InsumoRepository
import com.example.estoqueinsumo.model.Insumo
import com.example.estoqueinsumo.ui.theme.EstoqueInsumoTheme
import com.example.estoqueinsumo.viewmodel.EstoqueViewModel
import com.example.estoqueinsumo.viewmodel.EstoqueViewModelFactory

/**
 * Tela Principal do Aplicativo de Estoque.
 * Utiliza Jetpack Compose para uma interface reativa e moderna.
 * O LazyColumn aqui faz o papel do RecyclerView solicitado.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Inicialização manual do repositório para o ViewModel (Injeção de dependência simples)
        val dbHelper = DatabaseHelper(this)
        val repository = InsumoRepository(dbHelper)
        val factory = EstoqueViewModelFactory(repository)

        setContent {
            EstoqueInsumoTheme {
                val viewModel: EstoqueViewModel = viewModel(factory = factory)
                MainScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: EstoqueViewModel) {
    // Observando o estado do ViewModel (StateFlow)
    val insumos by viewModel.insumos.collectAsState()
    val erro by viewModel.erro.collectAsState()
    val context = LocalContext.current

    // Estados para controle de exibição de diálogos (Modais)
    var mostrarDialogCadastro by remember { mutableStateOf(false) }
    var mostrarDialogMovimentacao by remember { mutableStateOf(false) }
    var insumoSelecionado by remember { mutableStateOf<Insumo?>(null) }
    var queryBusca by remember { mutableStateOf("") }
    var modoAlerta by remember { mutableStateOf(false) }

    // Efeito para exibir mensagens de erro vindas da lógica de negócio
    LaunchedEffect(erro) {
        erro?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.limparErro()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estoque de Insumos") },
                actions = {
                    // Botão Alertas: Filtra itens com estoque baixo
                    IconButton(onClick = { 
                        modoAlerta = !modoAlerta
                        if (modoAlerta) viewModel.filtrarEstoqueBaixo() else viewModel.listarTodos()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Warning, 
                            contentDescription = "Alertas",
                            tint = if (modoAlerta) Color.Red else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // Botão para adicionar novo insumo (FAB)
            FloatingActionButton(onClick = { mostrarDialogCadastro = true }) {
                Icon(Icons.Default.Add, contentDescription = "Novo Insumo")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Campo de busca por nome (Busca Parcial)
            OutlinedTextField(
                value = queryBusca,
                onValueChange = { 
                    queryBusca = it
                    viewModel.buscarPorNome(it)
                },
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                placeholder = { Text("Buscar insumo...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            // Lista de Insumos (Substitui o RecyclerView)
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(insumos) { insumo ->
                    InsumoItem(
                        insumo = insumo,
                        onClick = {
                            insumoSelecionado = insumo
                            mostrarDialogMovimentacao = true
                        },
                        onDelete = { viewModel.removerInsumo(insumo) }
                    )
                }
            }
        }

        // Diálogo para Cadastrar Insumo
        if (mostrarDialogCadastro) {
            InsumoCadastroDialog(
                onDismiss = { mostrarDialogCadastro = false },
                onSave = { nome, qtd, un, min, valId ->
                    viewModel.adicionarInsumo(nome, qtd, un, min, valId)
                    mostrarDialogCadastro = false
                }
            )
        }

        // Diálogo para Movimentação (Entrada/Saída)
        if (mostrarDialogMovimentacao && insumoSelecionado != null) {
            MovimentacaoDialog(
                insumo = insumoSelecionado!!,
                onDismiss = { mostrarDialogMovimentacao = false },
                onConfirm = { qtd, tipo ->
                    viewModel.atualizarQuantidade(insumoSelecionado!!, qtd, tipo)
                    mostrarDialogMovimentacao = false
                }
            )
        }
    }
}

/**
 * Item da lista de insumos.
 * Possui indicador visual (fundo vermelho claro) se o estoque estiver baixo.
 */
@Composable
fun InsumoItem(insumo: Insumo, onClick: () -> Unit, onDelete: () -> Unit) {
    val estoqueBaixo = insumo.quantidade <= insumo.quantidadeMinima
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (estoqueBaixo) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = insumo.nome, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = "Saldo: ${insumo.quantidade} ${insumo.unidadeMedida}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (estoqueBaixo) {
                    Text(text = "ESTOQUE BAIXO (Mín: ${insumo.quantidadeMinima})", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Remover", tint = Color.Gray)
            }
        }
    }
}

/**
 * Componente de Diálogo para Cadastro.
 */
@Composable
fun InsumoCadastroDialog(onDismiss: () -> Unit, onSave: (String, Double, String, Double, String?) -> Unit) {
    var nome by remember { mutableStateOf("") }
    var qtd by remember { mutableStateOf("") }
    var un by remember { mutableStateOf("kg") }
    var min by remember { mutableStateOf("") }
    var validade by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cadastrar Insumo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text("Nome do Insumo") })
                OutlinedTextField(
                    value = qtd, 
                    onValueChange = { qtd = it }, 
                    label = { Text("Quantidade Inicial") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(value = un, onValueChange = { un = it }, label = { Text("Unidade (kg, L, un)") })
                OutlinedTextField(
                    value = min, 
                    onValueChange = { min = it }, 
                    label = { Text("Estoque Mínimo") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(value = validade, onValueChange = { validade = it }, label = { Text("Validade (opcional)") })
            }
        },
        confirmButton = {
            Button(onClick = { 
                onSave(nome, qtd.toDoubleOrNull() ?: 0.0, un, min.toDoubleOrNull() ?: 0.0, validade.ifBlank { null }) 
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

/**
 * Componente de Diálogo para Entrada/Saída.
 */
@Composable
fun MovimentacaoDialog(insumo: Insumo, onDismiss: () -> Unit, onConfirm: (Double, String) -> Unit) {
    var quantidadeStr by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("ENTRADA") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Movimentação: ${insumo.nome}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Saldo atual: ${insumo.quantidade} ${insumo.unidadeMedida}", fontWeight = FontWeight.Medium)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = tipo == "ENTRADA", onClick = { tipo = "ENTRADA" })
                    Text("Entrada")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = tipo == "SAIDA", onClick = { tipo = "SAIDA" })
                    Text("Saída")
                }
                
                OutlinedTextField(
                    value = quantidadeStr, 
                    onValueChange = { quantidadeStr = it }, 
                    label = { Text("Quantidade") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val valor = quantidadeStr.toDoubleOrNull() ?: 0.0
                if (valor > 0) {
                    val ajuste = if (tipo == "SAIDA") -valor else valor
                    onConfirm(ajuste, tipo)
                }
            }) { Text("Confirmar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
