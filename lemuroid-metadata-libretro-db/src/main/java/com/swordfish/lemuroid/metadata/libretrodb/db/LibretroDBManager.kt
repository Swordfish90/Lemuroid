package com.swordfish.lemuroid.metadata.libretrodb.db

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import java.util.concurrent.ExecutorService

class LibretroDBManager(private val context: Context, executorService: ExecutorService) {

    companion object {
        private const val DB_NAME = "libretro-db"
    }

    private var dbRelay: LibretroDatabase? = null

    suspend fun isDBReady(): LibretroDatabase {
        if (dbRelay == null) {
            dbRelay = Room.databaseBuilder(context, LibretroDatabase::class.java, DB_NAME)
                .createFromAsset("libretro-db.sqlite")
                .fallbackToDestructiveMigration()
                .build()
        }

        return dbRelay!!
    }
}
