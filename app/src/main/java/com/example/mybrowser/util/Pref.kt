package com.example.mybrowser.util

import android.content.Context
import android.content.SharedPreferences

class Pref(private val context : Context) {
    private val PREF_NAME = "PrefOfLee"
    private var preference : SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        const val HOME = "Web home"
        const val RESUME = "Page resume"
        const val TAB_COUNT = "Tab count"

        private var instance : Pref? =null
        @Synchronized
        fun getInstance(context: Context) : Pref? {
            if(instance == null)
                instance = Pref(context)

            return instance
        }
    }

    fun getString(id: String?) : String? {
        return preference.getString(id, "")
    }

    fun setValue(id: String?, value: String) : Boolean {
        return preference.edit()
            .putString(id, value)
            .commit()
    }

    fun removeValue(id: String?) : Boolean {
        return preference.edit()
            .remove(id)
            .commit()
    }
}