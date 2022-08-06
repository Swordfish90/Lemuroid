package com.swordfish.lemuroid.metadata.libretrodb.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.swordfish.lemuroid.metadata.libretrodb.db.entity.LibretroRom

@Dao
interface GameDao {
    @Query("SELECT * FROM games WHERE romName LIKE :romName")
    suspend fun findByName(romName: String): List<LibretroRom>?

    @Query("SELECT * FROM games WHERE romName = :romName LIMIT 1")
    suspend fun findByFileName(romName: String): LibretroRom?

    @Query("SELECT * FROM games WHERE crc32 = :crc LIMIT 1")
    suspend fun findByCRC(crc: String): LibretroRom?

    @Query("SELECT * FROM games WHERE serial = :serial LIMIT 1")
    suspend fun findBySerial(serial: String): LibretroRom?
}
