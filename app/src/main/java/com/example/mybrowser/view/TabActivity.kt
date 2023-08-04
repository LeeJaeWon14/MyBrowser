package com.example.mybrowser.view

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mybrowser.R
import com.example.mybrowser.adapter.TabListAdapter
import com.example.mybrowser.databinding.ActivityTabBinding
import com.example.mybrowser.databinding.DialogUrlSearchBinding
import com.example.mybrowser.model.MyRoomDatabase
import com.example.mybrowser.model.TabEntity
import com.example.mybrowser.util.Pref
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TabActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTabBinding
    private val tabCount: MutableLiveData<String> by lazy { MutableLiveData<String>() }
    private val keyManager: InputMethodManager by lazy { getSystemService(InputMethodManager::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTabBinding.inflate(layoutInflater)
        setContentView(binding.root)
        actionBar?.hide()
        setSupportActionBar(binding.tbTabToolbar)

        tabCount.value = Pref.getInstance(this@TabActivity)?.getString(Pref.TAB_COUNT)?.let {
            if(it == "") return@let 0.toString()
            else return@let it
        }.toString()

        tabCount.observe(this) { tab ->
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                title = String.format(
                    getString(R.string.str_tab_count),
                    tab
                )
            }
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
                }
            }
            btnAddTab.setOnClickListener {
                //todo: not implemented yet.
//                Toast.makeText(this@TabActivity, getString(R.string.str_not_impl_yet), Toast.LENGTH_SHORT).show()
                val dlgBinding = DialogUrlSearchBinding.inflate(layoutInflater)
                val dlg = AlertDialog.Builder(this@TabActivity).create()
                dlg.setView(dlgBinding.root)
                dlg.window?.setBackgroundDrawableResource(R.drawable.layout_border)

                dlgBinding.apply {
                    edtSearch.apply {
                        requestFocus()
                        keyManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
                        setOnEditorActionListener { v, actionId, event ->
                            when(actionId) {
                                EditorInfo.IME_ACTION_SEARCH -> {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        MyRoomDatabase.getInstance(this@TabActivity).getTabDao()
                                            .insertTabList(
                                                TabEntity(
                                                    0, "", "https://".plus("${v.text.toString()}/")
                                                )
                                            )
                                        tabCount.run {
                                            Pref.getInstance(this@TabActivity)?.setValue(Pref.TAB_COUNT, (value!!.toInt() + 1).toString())
                                            postValue((value!!.toInt() + 1).toString())
                                        }
                                        val tabList = MyRoomDatabase.getInstance(this@TabActivity).getTabDao()
                                            .selectTabList()
                                        withContext(Dispatchers.Main) {
                                            rvTabList.adapter = TabListAdapter(tabList)
                                        }
                                    }
                                    Toast.makeText(
                                        this@TabActivity,
                                        "저장되었습니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    keyManager.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                                    dlg.dismiss()
                                }
                            }
                            return@setOnEditorActionListener true
                        }
                    }

                    ivClearUrl.setOnClickListener {
                        edtSearch.setText("")
                    }
                    ivExitUrl.setOnClickListener {
                        dlg.dismiss()
                    }
                }
                dlg.setCancelable(false)
                dlg.show()
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