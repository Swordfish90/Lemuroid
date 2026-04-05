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
                database.execSQL(
                    "ALTER TABLE games ADD COLUMN customName TEXT DEFAULT NULL",
                )
                database.execSQL(
                    "ALTER TABLE games ADD COLUMN customCoverUri TEXT DEFAULT NULL",
                )

                database.execSQL("DROP TRIGGER IF EXISTS games_bu")
                database.execSQL("DROP TRIGGER IF EXISTS games_bd")
                database.execSQL("DROP TRIGGER IF EXISTS games_au")
                database.execSQL("DROP TRIGGER IF EXISTS games_ai")

                database.execSQL(
                    """
                    CREATE TRIGGER games_bu BEFORE UPDATE ON games BEGIN
                      DELETE FROM fts_games WHERE docid=old.id;
                    END;
                    """.trimIndent(),
                )
                database.execSQL(
                    """
                    CREATE TRIGGER games_bd BEFORE DELETE ON games BEGIN
                      DELETE FROM fts_games WHERE docid=old.id;
                    END;
                    """.trimIndent(),
                )
                database.execSQL(
                    """
                    CREATE TRIGGER games_au AFTER UPDATE ON games BEGIN
                      INSERT INTO fts_games(docid, title) VALUES(new.id, COALESCE(new.customName, new.title));
                    END;
                    """.trimIndent(),
                )
                database.execSQL(
                    """
                    CREATE TRIGGER games_ai AFTER INSERT ON games BEGIN
                      INSERT INTO fts_games(docid, title) VALUES(new.id, COALESCE(new.customName, new.title));
                    END;
                    """.trimIndent(),
                )

                database.execSQL("DELETE FROM fts_games")
                database.execSQL(
                    "INSERT INTO fts_games(docid, title) SELECT id, COALESCE(customName, title) FROM games",
                )
            }
        }
}
