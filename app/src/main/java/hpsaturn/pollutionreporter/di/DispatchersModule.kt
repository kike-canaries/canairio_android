package hpsaturn.pollutionreporter.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object DispatchersModule {

    @Retention(AnnotationRetention.BINARY)
    @Qualifier
    annotation class DefaultDispatcher

    @Retention(AnnotationRetention.BINARY)
    @Qualifier
    annotation class IoDispatcher

    @Retention(AnnotationRetention.BINARY)
    @Qualifier
    annotation class MainDispatcher

    @Singleton
    @IoDispatcher
    @Provides
    fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Singleton
    @DefaultDispatcher
    @Provides
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default


    @Singleton
    @MainDispatcher
    @Provides
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

}