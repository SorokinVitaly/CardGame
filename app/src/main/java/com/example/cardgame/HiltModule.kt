package com.example.cardgame

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
abstract class HiltModule {
    @Suppress("unused")
    @Binds
    abstract fun bindLocalDataRepository(impl: LocalDataRepositoryImpl): LocalDataRepository
    @Suppress("unused")
    @Binds
    abstract fun bindHistory(impl: HistoryImpl): History
}