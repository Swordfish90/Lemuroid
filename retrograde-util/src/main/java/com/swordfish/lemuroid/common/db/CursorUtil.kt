package com.swordfish.lemuroid.common.db

import android.database.Cursor

fun Cursor.asSequence(): Sequence<Cursor> = generateSequence { if (moveToNext()) this else null }
