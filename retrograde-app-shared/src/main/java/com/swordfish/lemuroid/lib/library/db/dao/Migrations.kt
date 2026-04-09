/*
 *
 *  *  RetrogradeApplicationComponent.kt
 *  *
 *  *  Copyright (C) 2017 Retrograde Project
 *  *
 *  *  This program is free software: you can redistribute it and/or modify
 *  *  it under the terms of the GNU General Public License as published by
 *  *  the Free Software Foundation, either version 3 of the License, or
 *  *  (at your option) any later version.
 *  *
 *  *  This program is distributed in the hope that it will be useful,
 *  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *  GNU General Public License for more details.
 *  *
 *  *  You should have received a copy of the GNU General Public License
 *  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  *
 *
 */

package com.swordfish.lemuroid.lib.library.db.dao

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
    val VERSION_9_10: Migration =
        object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Rebuild FTS index to include fileName column for improved search
                database.execSQL("DROP TABLE IF EXISTS fts_games")
                database.execSQL("DROP TRIGGER IF EXISTS games_bu")
                database.execSQL("DROP TRIGGER IF EXISTS games_bd")
                database.execSQL("DROP TRIGGER IF EXISTS games_au")
                database.execSQL("DROP TRIGGER IF EXISTS games_ai")
                database.execSQL(
                    """
                    CREATE VIRTUAL TABLE fts_games USING FTS4(
                      tokenize=unicode61 "remove_diacritics=1",
                      content="games",
                      title, fileName)
                    """,
                )
                database.execSQL(
                    """
                    CREATE TRIGGER games_bu BEFORE UPDATE ON games BEGIN
                      DELETE FROM fts_games WHERE docid=old.id;
                    END
                    """,
                )
                database.execSQL(
                    """
                    CREATE TRIGGER games_bd BEFORE DELETE ON games BEGIN
                      DELETE FROM fts_games WHERE docid=old.id;
                    END
                    """,
                )
                database.execSQL(
                    """
                    CREATE TRIGGER games_au AFTER UPDATE ON games BEGIN
                      INSERT INTO fts_games(docid, title, fileName) VALUES(new.id, new.title, new.fileName);
                    END
                    """,
                )
                database.execSQL(
                    """
                    CREATE TRIGGER games_ai AFTER INSERT ON games BEGIN
                      INSERT INTO fts_games(docid, title, fileName) VALUES(new.id, new.title, new.fileName);
                    END
                    """,
                )
                database.execSQL(
                    """
                    INSERT INTO fts_games(docid, title, fileName) SELECT id, title, fileName FROM games
                    """,
                )
            }
        }

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
}
