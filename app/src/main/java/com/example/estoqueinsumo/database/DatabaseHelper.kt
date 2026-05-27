package com.example.estoqueinsumo.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.estoqueinsumo.model.Insumo

/**
 * Gerenciador do Banco de Dados SQLite.
 * Responsável pela criação, atualização e operações diretas nas tabelas de insumos e movimentações.
 */
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "estoque_insumos.db"
        private const val DATABASE_VERSION = 2 // Incrementado para incluir a tabela de movimentações

        // Tabela de Insumos
        const val TABLE_INSUMOS = "insumos"
        const val COLUMN_ID = "id"
        const val COLUMN_NOME = "nome"
        const val COLUMN_QUANTIDADE = "quantidade"
        const val COLUMN_UNIDADE = "unidade_medida"
        const val COLUMN_QTD_MINIMA = "quantidade_minima"
        const val COLUMN_VALIDADE = "data_validade"

        // Tabela de Movimentações
        const val TABLE_MOVIMENTACOES = "movimentacoes"
        const val COLUMN_MOV_ID = "mov_id"
        const val COLUMN_MOV_INSUMO_ID = "insumo_id"
        const val COLUMN_MOV_TIPO = "tipo" // "ENTRADA" ou "SAIDA"
        const val COLUMN_MOV_QTD = "quantidade"
        const val COLUMN_MOV_DATA = "data_hora"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Criação da tabela de Insumos
        val createTableInsumos = """
            CREATE TABLE $TABLE_INSUMOS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NOME TEXT NOT NULL UNIQUE,
                $COLUMN_QUANTIDADE REAL NOT NULL,
                $COLUMN_UNIDADE TEXT NOT NULL,
                $COLUMN_QTD_MINIMA REAL NOT NULL,
                $COLUMN_VALIDADE TEXT
            )
        """.trimIndent()
        
        // Criação da tabela de Movimentações
        val createTableMovimentacoes = """
            CREATE TABLE $TABLE_MOVIMENTACOES (
                $COLUMN_MOV_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_MOV_INSUMO_ID INTEGER NOT NULL,
                $COLUMN_MOV_TIPO TEXT NOT NULL,
                $COLUMN_MOV_QTD REAL NOT NULL,
                $COLUMN_MOV_DATA INTEGER NOT NULL,
                FOREIGN KEY($COLUMN_MOV_INSUMO_ID) REFERENCES $TABLE_INSUMOS($COLUMN_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        db.execSQL(createTableInsumos)
        db.execSQL(createTableMovimentacoes)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Se estiver vindo da versão 1, cria a tabela de movimentações e adiciona UNIQUE ao nome
            // Simplificando: recriar tabelas (em prod usaríamos ALTER TABLE ou migração controlada)
            db.execSQL("DROP TABLE IF EXISTS $TABLE_MOVIMENTACOES")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_INSUMOS")
            onCreate(db)
        }
    }

    // --- Métodos de Insumo ---

    fun insert(insumo: Insumo): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NOME, insumo.nome)
            put(COLUMN_QUANTIDADE, insumo.quantidade)
            put(COLUMN_UNIDADE, insumo.unidadeMedida)
            put(COLUMN_QTD_MINIMA, insumo.quantidadeMinima)
            put(COLUMN_VALIDADE, insumo.dataValidade)
        }
        return db.insert(TABLE_INSUMOS, null, values)
    }

    fun update(insumo: Insumo): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NOME, insumo.nome)
            put(COLUMN_QUANTIDADE, insumo.quantidade)
            put(COLUMN_UNIDADE, insumo.unidadeMedida)
            put(COLUMN_QTD_MINIMA, insumo.quantidadeMinima)
            put(COLUMN_VALIDADE, insumo.dataValidade)
        }
        return db.update(TABLE_INSUMOS, values, "$COLUMN_ID = ?", arrayOf(insumo.id.toString()))
    }

    fun delete(id: Long): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_INSUMOS, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun queryAll(): List<Insumo> {
        val list = mutableListOf<Insumo>()
        val db = this.readableDatabase
        val cursor = db.query(TABLE_INSUMOS, null, null, null, null, null, "$COLUMN_NOME ASC")
        with(cursor) {
            while (moveToNext()) {
                list.add(cursorToInsumo(this))
            }
            close()
        }
        return list
    }

    fun queryById(id: Long): Insumo? {
        val db = this.readableDatabase
        val cursor = db.query(TABLE_INSUMOS, null, "$COLUMN_ID = ?", arrayOf(id.toString()), null, null, null)
        return cursor.use { if (it.moveToFirst()) cursorToInsumo(it) else null }
    }

    fun queryByName(nome: String): List<Insumo> {
        val list = mutableListOf<Insumo>()
        val db = this.readableDatabase
        val cursor = db.query(TABLE_INSUMOS, null, "$COLUMN_NOME LIKE ?", arrayOf("%$nome%"), null, null, "$COLUMN_NOME ASC")
        with(cursor) {
            while (moveToNext()) {
                list.add(cursorToInsumo(this))
            }
            close()
        }
        return list
    }

    fun queryLowStock(): List<Insumo> {
        val list = mutableListOf<Insumo>()
        val db = this.readableDatabase
        // Filtra onde quantidade é menor ou igual à mínima
        val cursor = db.query(TABLE_INSUMOS, null, "$COLUMN_QUANTIDADE <= $COLUMN_QTD_MINIMA", null, null, null, "$COLUMN_NOME ASC")
        with(cursor) {
            while (moveToNext()) {
                list.add(cursorToInsumo(this))
            }
            close()
        }
        return list
    }

    private fun cursorToInsumo(cursor: android.database.Cursor): Insumo {
        return Insumo(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
            nome = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOME)),
            quantidade = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_QUANTIDADE)),
            unidadeMedida = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNIDADE)),
            quantidadeMinima = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_QTD_MINIMA)),
            dataValidade = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VALIDADE))
        )
    }

    // --- Métodos de Movimentação ---

    fun insertMovimentacao(insumoId: Long, tipo: String, quantidade: Double, timestamp: Long): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_MOV_INSUMO_ID, insumoId)
            put(COLUMN_MOV_TIPO, tipo)
            put(COLUMN_MOV_QTD, quantidade)
            put(COLUMN_MOV_DATA, timestamp)
        }
        return db.insert(TABLE_MOVIMENTACOES, null, values)
    }
}
