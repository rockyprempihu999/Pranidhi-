package com.pranidhi.di

import android.content.Context
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton
import com.pranidhi.nlu.NluInterpreter
import com.pranidhi.plugin.PluginRunner

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton fun provideNlu(@ApplicationContext ctx: Context) = NluInterpreter(ctx)
    @Provides @Singleton fun providePluginRunner() = PluginRunner
}
