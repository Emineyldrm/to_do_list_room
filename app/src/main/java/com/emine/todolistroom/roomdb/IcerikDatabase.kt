package com.emine.todolistroom.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.emine.todolistroom.model.Icerik

@Database(entities = [Icerik::class], version = 1)
abstract class IcerikDatabase : RoomDatabase() {
    abstract fun icerikDao(): IcerikDAO
}

