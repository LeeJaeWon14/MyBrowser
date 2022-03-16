package com.example.mybrowser.view

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mybrowser.databinding.ActivityTabBinding

class TabActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTabBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTabBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }

        binding.apply {
            rvTabList.apply {
                layoutManager = LinearLayoutManager(this@TabActivity)
                adapter = null
            }
            btnAddTab.setOnClickListener {

            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> { onBackPressed() }
        }
        return true
    }
}