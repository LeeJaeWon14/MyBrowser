package com.example.mybrowser.view

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mybrowser.R
import com.example.mybrowser.adapter.TabListAdapter
import com.example.mybrowser.databinding.ActivityTabBinding
import com.example.mybrowser.model.MyRoomDatabase
import com.example.mybrowser.util.Pref
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TabActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTabBinding
    private var tabCount: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTabBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tabCount = Pref.getInstance(this@TabActivity)?.getString(Pref.TAB_COUNT)!!

        actionBar?.hide()
        setSupportActionBar(binding.tbTabToolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = String.format(
                    getString(R.string.str_tab_count),
                    this@TabActivity.tabCount
            )
        }

        binding.apply {
            rvTabList.apply {
                CoroutineScope(Dispatchers.IO).launch {
                    val list = MyRoomDatabase.getInstance(this@TabActivity).getTabDao()
                        .selectTabList()
                    withContext(Dispatchers.Main) {
                        layoutManager = LinearLayoutManager(this@TabActivity)
                        adapter = TabListAdapter(list)
                    }
//                    Pref.getInstance(this@TabActivity)?.setValue(Pref.TAB_COUNT, list.size.toString())
                }
            }
            btnAddTab.setOnClickListener {
                startActivity(Intent(this@TabActivity, WebViewActivity::class.java).apply {
                    putExtra("newTab", true)
                })
//                Pref.getInstance(this@TabActivity)?.setValue(Pref.TAB_COUNT, this@TabActivity.tabCount + 1)
                finish()
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