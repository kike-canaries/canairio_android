package hpsaturn.pollutionreporter.di

import android.content.Context
import com.karumi.dexter.Dexter
import com.karumi.dexter.DexterBuilder.Permission
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object ApplicationModule {
    @Singleton
    @Provides
    fun provideDexter(@ApplicationContext context: Context): Permission =
        Dexter.withContext(context)

    @Singleton
    @Provides
    fun provideCalendar(): Calendar = Calendar.getInstance()
}