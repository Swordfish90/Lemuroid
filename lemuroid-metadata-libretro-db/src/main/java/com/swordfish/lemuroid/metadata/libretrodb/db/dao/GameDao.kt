package com.swordfish.lemuroid.metadata.libretrodb.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import com.swordfish.lemuroid.metadata.libretrodb.db.entity.LibretroRom

@Dao
interface GameDao {
    @Query(
        "SELECT * FROM games WHERE system = :system AND name IS NOT NULL " +
            "AND artwork IS NOT NULL AND name LIKE :query ESCAPE '!' GROUP BY name ORDER BY name",
    )
    fun searchGamesWithArtwork(system: String, query: String): PagingSource<Int, LibretroRom>

    @Query("SELECT * FROM games WHERE romName = :romName")
    suspend fun findByFileName(romName: String): List<LibretroRom>

    @Query("SELECT * FROM games WHERE romName IS NULL AND name = :name")
    suspend fun findByGameName(name: String): List<LibretroRom>

    @Query(
        "SELECT * FROM games " +
            "WHERE system = :system AND normalizedName = :normalizedName AND artwork IS NOT NULL " +
            "ORDER BY CASE WHEN artwork = '' THEN name ELSE artwork END, id",
    )
    suspend fun findArtworkByNormalizedName(system: String, normalizedName: String): List<LibretroRom>

    @Query("SELECT * FROM games WHERE crc32 = :crc LIMIT 1")
    suspend fun findByCRC(crc: String): LibretroRom?

    @Query("SELECT * FROM games WHERE serial = :serial LIMIT 1")
    suspend fun findBySerial(serial: String): LibretroRom?
}
