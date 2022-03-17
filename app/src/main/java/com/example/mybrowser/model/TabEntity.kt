package com.example.mybrowser.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TabEntity(
        @PrimaryKey(autoGenerate = true)
        val id: Int,

        @ColumnInfo
        val title: String = "test",

        @ColumnInfo
        val url: String
)