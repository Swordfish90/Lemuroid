
package com.codebutler.retrograde.metadata.libretrodb.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.codebutler.retrograde.metadata.libretrodb.db.dao.GameDao
import com.codebutler.retrograde.metadata.libretrodb.db.entity.LibretroRom

@Database(
        entities = [LibretroRom::class],
        version = 1,
        exportSchema = false)
abstract class LibretroDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
}
