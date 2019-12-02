
package com.swordfish.lemuroid.metadata.libretrodb.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.swordfish.lemuroid.metadata.libretrodb.db.entity.LibretroRom
import io.reactivex.Maybe

@Dao
interface GameDao {

    @Query("SELECT * FROM games WHERE romName = :romName LIMIT 1")
    fun findByFileName(romName: String): Maybe<LibretroRom>

    @Query("SELECT * FROM games WHERE crc32 = :crc LIMIT 1")
    fun findByCRC(crc: String): Maybe<LibretroRom>
}
