package com.swordfish.lemuroid.lib.library.db.entity

import androidx.recyclerview.widget.DiffUtil
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(
    tableName = "cheats",
    foreignKeys = [
        ForeignKey(
            entity = Game::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Cheat::class,
            parentColumns = ["id"],
            childColumns = ["masterCodeId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index("id", unique = true),
        Index("gameId"),
        Index("gameId", "enabled"),
        Index("masterCodeId"),
    ],
)
data class Cheat(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gameId: Int,
    val title: String,
    val description: String? = null,
    val systemId: String,
    val cheatType: String,
    val code: String,
    val enabled: Boolean = true,
    val isMasterCode: Boolean = false,
    val masterCodeId: Long? = null,
    val orderIndex: Int = 0,
) : Serializable {
    companion object {
        val DIFF_CALLBACK =
            object : DiffUtil.ItemCallback<Cheat>() {
                override fun areItemsTheSame(
                    oldItem: Cheat,
                    newItem: Cheat,
                ): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: Cheat,
                    newItem: Cheat,
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }
}
