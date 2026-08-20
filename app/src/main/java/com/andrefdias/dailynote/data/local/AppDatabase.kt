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
        RoomEquipeViatura::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun calendarDao(): CalendarDao
    abstract fun quartelDao(): com.andrefdias.dailynote.data.local.dao.QuartelDao
    abstract fun viaturaDao(): com.andrefdias.dailynote.data.local.dao.ViaturaDao
    abstract fun militarDao(): com.andrefdias.dailynote.data.local.dao.MilitarDao
    abstract fun equipeServicoDao(): com.andrefdias.dailynote.data.local.dao.EquipeServicoDao

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
                .addMigrations(MIGRATION_5_6, MIGRATION_6_7)
                .fallbackToDestructiveMigration()
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
