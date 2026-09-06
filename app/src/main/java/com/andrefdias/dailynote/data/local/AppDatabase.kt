package com.andrefdias.dailynote.data.local

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.andrefdias.dailynote.data.local.dao.CalendarDao
import com.andrefdias.dailynote.data.local.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        RoomEscalaConfig::class,
        RoomEquipe::class,
        RoomTurno::class,
        RoomCalendarEvento::class,
        RoomCalendarTarefa::class,
        RoomNotificacao::class,
        RoomCalendarSettings::class,
        RoomQuartel::class,
        RoomViatura::class,
        RoomMilitar::class,
        RoomEquipeServico::class,
        RoomEquipeViatura::class,
        RoomConfiguracao::class,
        RoomBackupLog::class,
        RoomNovaOcorrencia::class,
        RoomNovaVitima::class,
        RoomNovoVeiculo::class,
        RoomOcorrencia::class
    ],
    version = 16,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun calendarDao(): CalendarDao
    abstract fun quartelDao(): com.andrefdias.dailynote.data.local.dao.QuartelDao
    abstract fun viaturaDao(): com.andrefdias.dailynote.data.local.dao.ViaturaDao
    abstract fun militarDao(): com.andrefdias.dailynote.data.local.dao.MilitarDao
    abstract fun equipeServicoDao(): com.andrefdias.dailynote.data.local.dao.EquipeServicoDao
    abstract fun configuracaoDao(): com.andrefdias.dailynote.data.local.dao.ConfiguracaoDao
    abstract fun ocorrenciaDao(): com.andrefdias.dailynote.data.local.dao.OcorrenciaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        @Volatile
        private var isCreatedJustNow = false

        val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE militares ADD COLUMN mergulhador INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE militares ADD COLUMN ovb TEXT NOT NULL DEFAULT 'Não Habilitado'")
                db.execSQL("ALTER TABLE viaturas ADD COLUMN status TEXT NOT NULL DEFAULT 'Operacional'")
                db.execSQL("ALTER TABLE quartel ADD COLUMN municipio TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `equipe_viatura_new` (`id` TEXT NOT NULL, `equipeServicoId` TEXT NOT NULL, `viaturaId` TEXT NOT NULL, `militaresEscaladosJson` TEXT NOT NULL DEFAULT '[]', PRIMARY KEY(`id`), FOREIGN KEY(`equipeServicoId`) REFERENCES `equipe_servico`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                // Copy data
                db.execSQL("INSERT INTO `equipe_viatura_new` (`id`, `equipeServicoId`, `viaturaId`) SELECT `id`, `equipeServicoId`, `viaturaId` FROM `equipe_viatura`")
                db.execSQL("DROP TABLE `equipe_viatura`")
                db.execSQL("ALTER TABLE `equipe_viatura_new` RENAME TO `equipe_viatura`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_equipe_viatura_equipeServicoId` ON `equipe_viatura` (`equipeServicoId`)")
            }
        }

        val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `configuracoes` (`id` TEXT NOT NULL, `tema` TEXT NOT NULL, `backupAutomatico` TEXT NOT NULL, `backupSomenteWifi` INTEGER NOT NULL, `backupUriSaf` TEXT, `ultimoBackupData` TEXT, `ultimoBackupTamanho` INTEGER NOT NULL, `ultimoBackupStatus` TEXT, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `backup_log` (`id` TEXT NOT NULL, `dataHora` TEXT NOT NULL, `tipo` TEXT NOT NULL, `status` TEXT NOT NULL, `tamanho` INTEGER NOT NULL, `mensagem` TEXT, PRIMARY KEY(`id`))")
            }
        }

        val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `nova_ocorrencias` (`id` TEXT NOT NULL, `talao` TEXT NOT NULL, `data` TEXT NOT NULL, `hora` TEXT NOT NULL, `equipe` TEXT NOT NULL, `natureza` TEXT NOT NULL, `viatura` TEXT NOT NULL, `guarnicaoJson` TEXT NOT NULL, `latitude` REAL, `longitude` REAL, `rua` TEXT, `numero` TEXT, `bairro` TEXT, `cidade` TEXT, `fotosUrisJson` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            }
        }
        
        val MIGRATION_9_10 = object : androidx.room.migration.Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `nova_vitimas` (`id` TEXT NOT NULL, `ocorrenciaId` TEXT NOT NULL, `nome` TEXT NOT NULL, `idade` INTEGER, `pessoaId` TEXT, `lesoes` TEXT NOT NULL, `lesoesEstruturadasJson` TEXT NOT NULL, `destinoSocorro` TEXT NOT NULL, `quemSocorreu` TEXT NOT NULL, `resultadoOcorrencia` TEXT NOT NULL, `viaturaSocorroId` TEXT, `hospitalDestino` TEXT NOT NULL, `nomeMedico` TEXT NOT NULL, `crmMedico` TEXT NOT NULL, `sinaisVitaisJson` TEXT NOT NULL, `cpf` TEXT, `lesoesAparentes` TEXT, `transportadoPor` TEXT, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `novo_veiculos_envolvidos` (`id` TEXT NOT NULL, `ocorrenciaId` TEXT NOT NULL, `placa` TEXT NOT NULL, `modelo` TEXT NOT NULL, `cor` TEXT NOT NULL, `chassi` TEXT NOT NULL, `anoFabricacao` INTEGER, `anoModelo` INTEGER, `ano` TEXT NOT NULL, `proprietarioId` TEXT, `marca` TEXT NOT NULL, `versao` TEXT NOT NULL, `exercicio` TEXT NOT NULL, `urlCrlv` TEXT, `ocrTextoCrlv` TEXT, `ocrDadosEstruturadosJson` TEXT NOT NULL, `veiculoMasterId` TEXT, `condutorId` TEXT, `dadosMotoristaJson` TEXT, `renavam` TEXT, `monobloco` TEXT, `especie` TEXT, `tipoVeiculo` TEXT, `carroceria` TEXT, `categoriaVeiculo` TEXT, PRIMARY KEY(`id`))")
            }
        }

        val MIGRATION_10_11 = object : androidx.room.migration.Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `nova_ocorrencias` ADD COLUMN `pessoasJson` TEXT NOT NULL DEFAULT '[]'")
            }
        }
        val MIGRATION_11_12 = object : androidx.room.migration.Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `nova_ocorrencias` ADD COLUMN `historico` TEXT")
            }
        }
        val MIGRATION_12_13 = object : androidx.room.migration.Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `nova_ocorrencias` ADD COLUMN `apoios` TEXT NOT NULL DEFAULT '[]'")
            }
        }
        val MIGRATION_13_14 = object : androidx.room.migration.Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `novo_veiculos_envolvidos` ADD COLUMN `fotosVeiculoUrisJson` TEXT NOT NULL DEFAULT '[]'")
            }
        }
        
        val MIGRATION_14_15 = object : androidx.room.migration.Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `ocorrencias` (
                        `id` TEXT NOT NULL, 
                        `protocolo` TEXT NOT NULL, 
                        `natureza` TEXT NOT NULL, 
                        `latitude` REAL, 
                        `longitude` REAL, 
                        `dataHora` TEXT NOT NULL, 
                        `historico` TEXT, 
                        `fotos` TEXT NOT NULL, 
                        `status` TEXT NOT NULL, 
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_ocorrencias_protocolo` ON `ocorrencias` (`protocolo`)")
            }
        }

        val MIGRATION_15_16 = object : androidx.room.migration.Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `nova_ocorrencias` ADD COLUMN `isConcluida` INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dailynote.db"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        isCreatedJustNow = true
                    }
                })
                .addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16)
                .fallbackToDestructiveMigration(true)
                .build()
                
                INSTANCE = instance

                if (isCreatedJustNow) {
                    isCreatedJustNow = false
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            seedCalendarDefaultSettings(instance.calendarDao())
                        } catch (e: Exception) {
                            android.util.Log.e("FireDatabase", "Erro ao semear configurações: ${e.message}", e)
                        }
                    }
                }
                
                instance
            }
        }

        fun closeDatabase() {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
            }
        }

        private suspend fun seedCalendarDefaultSettings(dao: CalendarDao) {
            dao.insertSettings(
                RoomCalendarSettings(
                    id = "global_calendar_settings",
                    mostrarPopupInicial = true,
                    badgeHabilitado = true,
                    somHabilitado = true,
                    vibracaoHabilitada = true,
                    lembretesAntecipadosMinutos = 15,
                    popupExibidoHoje = null,
                    calendarioConfigurado = false
                )
            )
        }
    }
}
