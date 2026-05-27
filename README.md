# 📦 EstoqueInsumo

Aplicativo Android para **gerenciamento de estoque de insumos**, desenvolvido com Kotlin e Jetpack Compose. Permite cadastrar, monitorar e movimentar insumos com alertas automáticos de estoque baixo.

---

## 🚀 Funcionalidades

- **Cadastro de insumos** com nome, quantidade, unidade de medida, quantidade mínima e data de validade
- **Listagem** de todos os insumos ordenados alfabeticamente
- **Busca por nome** com correspondência parcial
- **Movimentação de estoque** — entradas e saídas com registro de histórico
- **Alerta de estoque baixo** — filtro automático de insumos abaixo da quantidade mínima
- **Validações de negócio** — impede duplicatas, estoque negativo e remoção de itens com saldo

---

## 🛠️ Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Kotlin 2.0.21 |
| UI | Jetpack Compose + Material3 |
| Arquitetura | MVVM (Model-View-ViewModel) |
| Banco de dados | SQLite via `SQLiteOpenHelper` |
| Reatividade | `StateFlow` + Coroutines |
| Build | Gradle 9.2.1 com Version Catalog |

---

## 🏗️ Arquitetura

O projeto segue o padrão **MVVM** com separação clara de responsabilidades:

```
app/src/main/java/com/example/estoqueinsumo/
│
├── model/
│   └── Insumo.kt                  # Data class da entidade
│
├── database/
│   ├── InsumoDAO.kt               # Interface de operações CRUD
│   ├── DatabaseHelper.kt          # SQLiteOpenHelper — criação e queries
│   └── InsumoRepository.kt        # Repositório com tratamento de erros
│
├── viewmodel/
│   ├── EstoqueViewModel.kt        # Lógica de negócio + StateFlow
│   └── EstoqueViewModelFactory.kt # Factory para injeção de dependência
│
└── MainActivity.kt                # UI completa em Jetpack Compose
```

---

## 📋 Pré-requisitos

- Android Studio Hedgehog ou superior
- JDK 17+
- Android SDK 21+ (minSdk)
- Dispositivo ou emulador com Android 5.0 (Lollipop) ou superior

---

## ▶️ Como executar

1. **Clone o repositório:**
   ```bash
   git clone https://github.com/seu-usuario/EstoqueInsumo.git
   cd EstoqueInsumo
   ```

2. **Abra no Android Studio:**
   - Selecione `File > Open` e aponte para a pasta do projeto.

3. **Sincronize as dependências:**
   - O Android Studio detectará o `build.gradle` automaticamente. Aguarde a sincronização do Gradle.

4. **Execute o app:**
   - Conecte um dispositivo físico ou inicie um emulador.
   - Clique em **Run ▶** ou pressione `Shift + F10`.

---

## 🗄️ Banco de Dados

O app utiliza **SQLite** gerenciado pelo `DatabaseHelper` (versão 2), com duas tabelas:

**`insumos`**
| Coluna | Tipo | Descrição |
|---|---|---|
| `id` | INTEGER PK | Identificador autoincremento |
| `nome` | TEXT UNIQUE | Nome do insumo |
| `quantidade` | REAL | Quantidade atual em estoque |
| `unidade_medida` | TEXT | Unidade (kg, L, un, etc.) |
| `quantidade_minima` | REAL | Limiar para alerta de estoque baixo |
| `data_validade` | TEXT | Data de validade (opcional) |

**`movimentacoes`**
| Coluna | Tipo | Descrição |
|---|---|---|
| `mov_id` | INTEGER PK | Identificador da movimentação |
| `insumo_id` | INTEGER FK | Referência ao insumo |
| `tipo` | TEXT | `ENTRADA` ou `SAIDA` |
| `quantidade` | REAL | Quantidade movimentada |
| `data_hora` | INTEGER | Timestamp Unix da operação |

---

## 🧪 Testes

O projeto inclui testes unitários para validação da lógica de negócio:

```bash
# Testes unitários
./gradlew test

# Testes instrumentados (requer dispositivo/emulador)
./gradlew connectedAndroidTest
```

---

## 📌 Regras de Negócio

- Nomes de insumos são **únicos** (case-insensitive)
- A quantidade em estoque **não pode ser negativa**
- Um insumo só pode ser **removido se o saldo for zero**
- Toda movimentação (entrada/saída) é **registrada com timestamp**
- Insumos com `quantidade ≤ quantidade_minima` são sinalizados como **estoque baixo**

---

## 📄 Prints 

<img width="358" height="792" alt="Captura de tela 2026-05-27 143614" src="https://github.com/user-attachments/assets/8a96f023-3ed9-4ecf-aa16-629ae743d20b" />
<img width="364" height="782" alt="Captura de tela 2026-05-27 143719" src="https://github.com/user-attachments/assets/5bb30f1d-8fa0-45ed-a496-2e47ac40cea3" />
<img width="353" height="798" alt="Captura de tela 2026-05-27 143754" src="https://github.com/user-attachments/assets/7b674927-2b03-40f2-ab85-a88e9defd99b" />


