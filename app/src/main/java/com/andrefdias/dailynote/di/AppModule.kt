package com.andrefdias.dailynote.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.andrefdias.dailynote.data.local.AppDatabase
import com.andrefdias.dailynote.domain.repository.CalendarRepository
import com.andrefdias.dailynote.data.local.dao.CalendarDao
import com.andrefdias.dailynote.data.repository.RoomCalendarRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindCalendarRepository(
        roomCalendarRepository: RoomCalendarRepository
    ): CalendarRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        dataStoreSettingsRepository: com.andrefdias.dailynote.data.repository.DataStoreSettingsRepository
    ): com.andrefdias.dailynote.domain.repository.SettingsRepository

    @Binds
    @Singleton
    abstract fun bindQuartelRepository(
        quartelRepositoryImpl: com.andrefdias.dailynote.data.repository.QuartelRepositoryImpl
    ): com.andrefdias.dailynote.domain.repository.QuartelRepository

    @Binds
    @Singleton
    abstract fun bindViaturaRepository(
        viaturaRepositoryImpl: com.andrefdias.dailynote.data.repository.ViaturaRepositoryImpl
    ): com.andrefdias.dailynote.domain.repository.ViaturaRepository

    @Binds
    @Singleton
    abstract fun bindMilitarRepository(
        militarRepositoryImpl: com.andrefdias.dailynote.data.repository.MilitarRepositoryImpl
    ): com.andrefdias.dailynote.domain.repository.MilitarRepository

    @Binds
    @Singleton
    abstract fun bindEquipeServicoRepository(
        equipeServicoRepositoryImpl: com.andrefdias.dailynote.data.repository.EquipeServicoRepositoryImpl
    ): com.andrefdias.dailynote.domain.repository.EquipeServicoRepository

    @Binds
    @Singleton
    abstract fun bindOcorrenciaRepository(
        ocorrenciaRepositoryImpl: com.andrefdias.dailynote.data.repository.OcorrenciaRepositoryImpl
    ): com.andrefdias.dailynote.domain.repository.OcorrenciaRepository

    companion object {
        private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "dailynote_settings")

        @Provides
        @Singleton
        fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
            return context.settingsDataStore
        }

        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
            return AppDatabase.getDatabase(context)
        }

        @Provides
        @Singleton
        fun provideCalendarDao(database: AppDatabase): CalendarDao {
            return database.calendarDao()
        }

        @Provides
        @Singleton
        fun provideQuartelDao(database: AppDatabase): com.andrefdias.dailynote.data.local.dao.QuartelDao {
            return database.quartelDao()
        }

        @Provides
        @Singleton
        fun provideViaturaDao(database: AppDatabase): com.andrefdias.dailynote.data.local.dao.ViaturaDao {
            return database.viaturaDao()
        }

        @Provides
        @Singleton
        fun provideMilitarDao(database: AppDatabase): com.andrefdias.dailynote.data.local.dao.MilitarDao {
            return database.militarDao()
        }

        @Provides
        @Singleton
        fun provideEquipeServicoDao(database: AppDatabase): com.andrefdias.dailynote.data.local.dao.EquipeServicoDao {
            return database.equipeServicoDao()
        }

        @Provides
        @Singleton
        fun provideRetrofit(): retrofit2.Retrofit {
            val client = okhttp3.OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .build()

            return retrofit2.Retrofit.Builder()
                .baseUrl("https://script.google.com/macros/s/AKfycbwPmnIFCM284-fZiiL782feoSSFqauEpmNtM308fyoLvwCppuS7p-GpYicWUwH61sw1gg/")
                .client(client)
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                .build()
        }

        @Provides
        @Singleton
        fun provideOcorrenciasApi(retrofit: retrofit2.Retrofit): com.andrefdias.dailynote.data.remote.OcorrenciasApi {
            return retrofit.create(com.andrefdias.dailynote.data.remote.OcorrenciasApi::class.java)
        }
    }
}
