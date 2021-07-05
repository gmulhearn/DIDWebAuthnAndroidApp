package com.gmulhearn.didwebauthn.common.di

import android.content.Context
import com.gmulhearn.didwebauthn.MyApplication
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AppModule::class,
    AndroidInjectionModule::class,
    AndroidSupportInjectionModule::class
])
interface AppComponent {

    fun inject(app: MyApplication)

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }

}
