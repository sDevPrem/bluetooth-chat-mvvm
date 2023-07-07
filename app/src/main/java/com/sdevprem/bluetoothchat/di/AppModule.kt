package com.sdevprem.bluetoothchat.di

import com.sdevprem.bluetoothchat.data.chat.AndroidBTController
import com.sdevprem.bluetoothchat.domain.chat.BTController
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun providesBTController(btController: AndroidBTController): BTController
}