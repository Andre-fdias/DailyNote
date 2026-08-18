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
    version = 5,
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
