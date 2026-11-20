package com.swordfish.lemuroid.lib.library.db.dao

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
    val VERSION_8_9: Migration =
        object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `datafiles`(
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `gameId` INTEGER NOT NULL,
                        `fileName` TEXT NOT NULL,
                        `fileUri` TEXT NOT NULL,
                        `lastIndexedAt` INTEGER NOT NULL,
                        `path` TEXT, FOREIGN KEY(`gameId`
                    ) REFERENCES `games`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )
                    """.trimIndent(),
                )

                database.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS `index_datafiles_id` ON `datafiles` (`id`)
                    """.trimIndent(),
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS `index_datafiles_fileUri` ON `datafiles` (`fileUri`)
                    """.trimIndent(),
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS `index_datafiles_gameId` ON `datafiles` (`gameId`)
                    """.trimIndent(),
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS `index_datafiles_lastIndexedAt` ON `datafiles` (`lastIndexedAt`)
                    """.trimIndent(),
                )
            }
        }

    val VERSION_9_10: Migration =
        object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                migrateGamesTableToVersion10(database)
                GameSearchDao.migrateFtsForVersion10(database)
            }
        }

    val VERSION_10_11: Migration =
        object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `cheats` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `gameId` INTEGER NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT,
                        `systemId` TEXT NOT NULL,
                        `cheatType` TEXT NOT NULL,
                        `code` TEXT NOT NULL,
                        `enabled` INTEGER NOT NULL,
                        `isMasterCode` INTEGER NOT NULL,
                        `masterCodeId` INTEGER,
                        `orderIndex` INTEGER NOT NULL,
                        FOREIGN KEY(`gameId`) REFERENCES `games`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`masterCodeId`) REFERENCES `cheats`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                    """.trimIndent(),
                )
                database.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_cheats_id` ON `cheats` (`id`)",
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_cheats_gameId` ON `cheats` (`gameId`)",
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_cheats_gameId_enabled` ON `cheats` (`gameId`, `enabled`)",
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_cheats_masterCodeId` ON `cheats` (`masterCodeId`)",
                )
            }
        }

    private fun migrateGamesTableToVersion10(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE games ADD COLUMN customName TEXT DEFAULT NULL",
        )
        database.execSQL(
            "ALTER TABLE games ADD COLUMN customCoverUri TEXT DEFAULT NULL",
        )
    }
}
