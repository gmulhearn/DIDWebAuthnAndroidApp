package com.example.did.common.di

import android.app.Application
import android.content.Context
import com.example.did.common.DefaultDispatcherProvider
import com.example.did.common.DispatcherProvider
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(
    includes = [
        FragmentBindingModule::class
    ]
)
class AppModule {

    @Provides
    @Singleton
    fun provideApplication(context: Context): Application {
        return context.applicationContext as Application
    }

    @Provides
    @Singleton
    fun provideDispatchersProvider(impl: DefaultDispatcherProvider): DispatcherProvider = impl
}
