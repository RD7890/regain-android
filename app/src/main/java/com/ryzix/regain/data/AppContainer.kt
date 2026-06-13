package com.ryzix.regain.data

import android.content.Context
import com.ryzix.regain.data.datastore.LockDataStore
import com.ryzix.regain.data.db.RegainDatabase
import com.ryzix.regain.repository.HistoryRepository
import com.ryzix.regain.repository.LockRepository

class AppContainer(context: Context) {
    val lockDataStore = LockDataStore(context)
    val database = RegainDatabase.getInstance(context)
    val historyRepository = HistoryRepository(database.historyDao())
    val lockRepository = LockRepository(lockDataStore, historyRepository)
}
