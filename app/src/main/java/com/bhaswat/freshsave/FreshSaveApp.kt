package com.bhaswat.freshsave

import android.app.Application
import com.bhaswat.freshsave.di.appModule
import io.appwrite.Client
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class FreshSaveApp : Application() {
    companion object {
        lateinit var client: Client
            private set
    }

    override fun onCreate() {
        super.onCreate()
        client = Client(this)
            .setEndpoint("https://cloud.appwrite.io/v1") // Your Appwrite endpoint
            .setProject("fwrp-bhaswat")       // Your Appwrite project ID
            .setSelfSigned(status = true)

        //Koin initialization
        startKoin {
            androidContext(this@FreshSaveApp)
            modules(appModule)
        }
    }
}