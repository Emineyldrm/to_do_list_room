package com.emine.todolistroom.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Icerik (
    @ColumnInfo(name = "gorev")
    var gorev: String,
    @ColumnInfo(name = "icerik")
    var icerik: String,
    @ColumnInfo(name = "gorsel")
    var gorsel: ByteArray
){
    @PrimaryKey(autoGenerate = true)
    var id=0
}



