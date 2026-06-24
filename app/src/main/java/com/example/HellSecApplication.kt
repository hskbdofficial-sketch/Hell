package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.MarketplaceRepository
import com.example.data.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HellSecApplication : Application() {

    lateinit var database: AppDatabase
    lateinit var repository: MarketplaceRepository
    lateinit var sessionManager: SessionManager

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        repository = MarketplaceRepository(database.appDao())
        sessionManager = SessionManager(this)

        // Seed Initial Data on startup asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            repository.seedInitialData()
        }
    }
}
