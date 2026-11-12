package come.asg.ominoy.metadata.libretrodb.db

import androidx.room.Database
import androidx.room.RoomDatabase
import come.asg.ominoy.metadata.libretrodb.db.dao.GameDao
import come.asg.ominoy.metadata.libretrodb.db.entity.LibretroRom

@Database(
    entities = [LibretroRom::class],
    version = 8,
    exportSchema = false,
)
abstract class LibretroDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
}
