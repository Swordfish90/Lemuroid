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

    val VERSION_11_12: Migration =
        object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `game_details` (
                        `gameId` INTEGER NOT NULL,
                        `synopsis` TEXT,
                        `genres` TEXT,
                        `publisher` TEXT,
                        `releaseDate` TEXT,
                        `players` TEXT,
                        `rating` REAL,
                        `bannerUrl` TEXT,
                        `logoUrl` TEXT,
                        `boxArtFrontUrl` TEXT,
                        `boxArtBackUrl` TEXT,
                        `screenshotUrlsJson` TEXT,
                        `videoUrl` TEXT,
                        `scraperSource` TEXT,
                        `lastScrapedAt` INTEGER,
                        PRIMARY KEY(`gameId`),
                        FOREIGN KEY(`gameId`) REFERENCES `games`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                database.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_game_details_gameId` ON `game_details` (`gameId`)",
                )
            }
        }

    val VERSION_12_13: Migration =
        object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `game_details` ADD COLUMN `videoUrl` TEXT DEFAULT NULL")
            }
        }

    val VERSION_13_14: Migration =
        object : Migration(13, 14) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `collections` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent(),
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `collection_games` (
                        `collectionId` INTEGER NOT NULL,
                        `gameId` INTEGER NOT NULL,
                        `addedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`collectionId`, `gameId`),
                        FOREIGN KEY(`collectionId`) REFERENCES `collections`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`gameId`) REFERENCES `games`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_collection_games_collectionId` ON `collection_games` (`collectionId`)",
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_collection_games_gameId` ON `collection_games` (`gameId`)",
                )
            }
        }

    val VERSION_14_15: Migration =
        object : Migration(14, 15) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `games` ADD COLUMN `totalPlayTimeMs` INTEGER NOT NULL DEFAULT 0")
            }
        }

    val VERSION_15_16: Migration =
        object : Migration(15, 16) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `game_details` ADD COLUMN `crc32` TEXT DEFAULT NULL")
            }
        }

    val VERSION_16_17: Migration =
        object : Migration(16, 17) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `game_details` ADD COLUMN `ratingCount` INTEGER DEFAULT NULL")
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
