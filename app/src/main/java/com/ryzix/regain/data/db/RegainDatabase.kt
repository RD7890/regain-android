package com.ryzix.regain.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [HistoryEntity::class], version = 1, exportSchema = false)
abstract class RegainDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile private var INSTANCE: RegainDatabase? = null

        fun getInstance(context: Context): RegainDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    RegainDatabase::class.java,
                    "regain.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
