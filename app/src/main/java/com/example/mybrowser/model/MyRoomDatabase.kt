package com.example.mybrowser.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TabEntity::class], version = 1, exportSchema = false)
abstract class MyRoomDatabase : RoomDatabase(){
    abstract fun getTabDao() : TabDAO

    companion object {
        private var instance: MyRoomDatabase? = null

        @Synchronized
        fun getInstance(context: Context) : MyRoomDatabase {
            instance?.let {
                return it
            } ?: run {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyRoomDatabase::class.java,
                "tabRoom.db"
                ).build()
                return instance!!
            }
        }
    }
}