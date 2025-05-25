package com.bhaswat.freshsave.di

import com.bhaswat.freshsave.FreshSaveApp
import com.bhaswat.freshsave.data.remote.AppwriteInventoryRepository
import com.bhaswat.freshsave.repository.InventoryRepository
import com.bhaswat.freshsave.viewmodel.HomeViewModel
import io.appwrite.Client
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single<Client> { FreshSaveApp.client }
    single<InventoryRepository> { AppwriteInventoryRepository(get()) } bind InventoryRepository::class
    viewModel { HomeViewModel(get()) }
}