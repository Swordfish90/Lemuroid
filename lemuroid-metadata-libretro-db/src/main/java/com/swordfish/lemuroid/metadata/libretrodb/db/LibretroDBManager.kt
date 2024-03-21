package com.swordfish.lemuroid.metadata.libretrodb.db

import android.content.Context
import androidx.room.Room

class LibretroDBManager(private val context: Context) {
    companion object {
        private const val DB_NAME = "libretro-db"
    }

    val dbInstance: LibretroDatabase by lazy {
        Room.databaseBuilder(context, LibretroDatabase::class.java, DB_NAME)
            .createFromAsset("libretro-db.sqlite")
            .fallbackToDestructiveMigration()
            .build()
    }
}
