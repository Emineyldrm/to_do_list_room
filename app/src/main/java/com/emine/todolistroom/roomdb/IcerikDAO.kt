package com.emine.todolistroom.roomdb

import androidx.annotation.IntegerRes
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.emine.todolistroom.model.Icerik
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable

@Dao
interface IcerikDAO {
    @Query("SELECT * FROM Icerik")
    fun getAll(): Flowable<List<Icerik>>
    @Query("SELECT * FROM Icerik WHERE id=:id")
    fun findByID(id:Int): Flowable<Icerik>
    @Insert
    fun insert(icerik: Icerik) : Completable
    @Delete
    fun delete(icerik: Icerik) : Completable
}


