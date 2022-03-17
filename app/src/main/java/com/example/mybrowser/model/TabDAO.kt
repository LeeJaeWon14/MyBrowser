package com.example.mybrowser.model

import androidx.room.*

@Dao
interface TabDAO {
    @Query("SELECT * FROM TabEntity")
    fun selectTabList() : List<TabEntity>

    @Update
    fun updateTabList(entity: TabEntity)

    @Delete
    fun deleteTabList(entity: TabEntity)

    @Query("DELETE FROM TabEntity WHERE url = :url")
    fun deleteTab(url: String)

    @Insert
    fun insertTabList(entity: TabEntity)

    @Query("SELECT * FROM TabEntity WHERE url = :url")
    fun distinctCheckTab(url: String) : List<TabEntity>
}