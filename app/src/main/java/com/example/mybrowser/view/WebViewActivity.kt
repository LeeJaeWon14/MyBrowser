package com.example.mybrowser.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.mybrowser.R
import com.example.mybrowser.client.MyWebClient
import com.example.mybrowser.databinding.ActivityWebViewBinding
import com.example.mybrowser.databinding.DialogUrlSearchBinding
import com.example.mybrowser.model.MyRoomDatabase
import com.example.mybrowser.util.Pref
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WebViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWebViewBinding
    private lateinit var keyManager: InputMethodManager
    private val homeUrlLive: MutableLiveData<String> by lazy { MutableLiveData<String>() }
    private val tabCount: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        actionBar?.hide()
        setSupportActionBar(binding.llWebBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        intent.getStringExtra("changeTab")?.let {
            Log.e("Web", "tab changed, url is $it")
            initUi(it)
        } ?: intent.getBooleanExtra("newTab", false).also { isNewTab ->
            when(isNewTab) {
                true -> {
                    Pref.getInstance(this)?.getString(Pref.HOME)?.let {
                        Log.e("Web", "home url is $it")
                        initUi(it)
                    }
                }
                false -> {
                    Pref.getInstance(this)?.getString(Pref.RESUME)?.let {
                        Log.e("Web", "resume url is $it")
                        initUi(it)
                    }
                }
            }
        }


        keyManager = getSystemService(InputMethodManager::class.java)
        homeUrlLive.observe(this, Observer {
            Log.e("web", "homeUrl Changed")
        })
    }

    override fun onResume() {
        super.onResume()
        tabCount.value = Pref.getInstance(this@WebViewActivity)?.getString(Pref.TAB_COUNT)
        tabCount.observe(this, Observer {
            if(it.isEmpty())
                binding.tvTabCount.text = 0.toString()
            else
                binding.tvTabCount.text = it
        })
    }

    override fun onPause() {
        super.onPause()
        Pref.getInstance(this)?.setValue(Pref.RESUME, binding.wvWebView.url.toString())
    }

    override fun onDestroy() {
        super.onDestroy()
//        Pref.getInstance(this)?.setValue(Pref.RESUME, binding.wvWebView.url.toString())
    }

    private fun initUi(targetUrl: String) {
//        window.statusBarColor = getColor(R.color.light_gray)
        binding.apply {
            wvWebView.apply {
                webViewClient = MyWebClient(binding)
                settings.apply {
                    javaScriptEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                    textZoom = 95 // Set text size in WebView, Default is 100.
                }
                if(isEmptyHome()?.not() == true)
                {
                    loadUrl(wvWebView, targetUrl)
                }
            }

            slWebLayout.apply {
                setOnRefreshListener {
                    isRefreshing = false
                    wvWebView.reload()
                }
            }

            ivNext.setOnClickListener { deleteUrlInRoom(targetUrl); wvWebView.goForward() }
            ivPrev.setOnClickListener { deleteUrlInRoom(targetUrl); wvWebView.goBack() }

            ivHome.apply {
                setOnClickListener {
                    Pref.getInstance(this@WebViewActivity)?.getString(Pref.HOME)?.let {
                        if(it.isEmpty())
                            Toast.makeText(this@WebViewActivity, getString(R.string.str_empty_home_url), Toast.LENGTH_SHORT).show()
                        else
                            loadUrl(wvWebView, it)
                    }
                }
                setOnLongClickListener {
                    val dlgBinding = DialogUrlSearchBinding.inflate(layoutInflater)
                    val dlg = AlertDialog.Builder(this@WebViewActivity).create()
                    dlg.setView(dlgBinding.root)
                    dlg.window?.setBackgroundDrawableResource(R.drawable.layout_border)

                    dlgBinding.apply {
                        edtSearch.apply {
                            requestFocus()
                            keyManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
                            setOnEditorActionListener { v, actionId, event ->
                                when(actionId) {
                                    EditorInfo.IME_ACTION_SEARCH -> {
                                        if (Pref.getInstance(this@WebViewActivity)
                                                ?.setValue(Pref.HOME, v.text.toString())!!
                                        ) {
                                            Toast.makeText(
                                                this@WebViewActivity,
                                                "저장되었습니다.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            homeUrlLive.postValue(makeUrl(v.text.toString()))
                                            keyManager.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                                            dlg.dismiss()
                                        }
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

                    false
                }
            }

            // Init dialog UI
            url.apply {
                text = wvWebView.url
                setOnClickListener {
                    Pref.getInstance(this@WebViewActivity)?.getString(Pref.HOME)?.let {
                        if(it.isEmpty())
                            Toast.makeText(this@WebViewActivity, getString(R.string.str_empty_home_url), Toast.LENGTH_SHORT).show()
                    }
                    val dlgBinding = DialogUrlSearchBinding.inflate(layoutInflater)
                    val dlg = AlertDialog.Builder(this@WebViewActivity).create()
                    dlg.setView(dlgBinding.root)
                    dlg.window?.setBackgroundDrawableResource(R.drawable.layout_border)

                    dlgBinding.apply {
                        ivClearUrl.setOnClickListener {
                            edtSearch.setText("")
                            dlgBinding.edtSearch.requestFocus()
                            keyManager.showSoftInput(dlgBinding.edtSearch, InputMethodManager.SHOW_IMPLICIT)
                        }
                        ivExitUrl.setOnClickListener {
                            dlg.dismiss()
                        }
                        edtSearch.apply {
                            setOnEditorActionListener { v, actionId, event ->
                                when(actionId) {
                                    EditorInfo.IME_ACTION_SEARCH -> {
                                        val url = makeUrl(v.text.toString())
                                        loadUrl(wvWebView, url)
                                        Log.e("web", "search url is $url")
                                        keyManager.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                                        dlg.dismiss()
                                    }
                                    EditorInfo.IME_ACTION_NEXT -> { }
                                }
                                return@setOnEditorActionListener true
                            }
                            setText((it as TextView).text)
                        }
                    }
                    dlg.setCancelable(false)
                    dlg.show()
                }
            }
            rlTabCount.setOnClickListener {
                startActivity(Intent(this@WebViewActivity, TabActivity::class.java))
            }
        }
    }

    private fun isEmptyHome() : Boolean? {
        Pref.getInstance(this)?.getString(Pref.HOME)?.let {
            binding.apply {
                tvEmptyMsg.isVisible = it.isEmpty()
                slWebLayout.isVisible = it.isEmpty().not()
            }
            return it.isEmpty()
        }
        return null
    }

    private fun makeUrl(url: String) : String {
        if(url.contains("https").not()) {
            return if(url.contains("http"))
                url.replace("http", "https")
            else
                "https://".plus(url)
        }
        return url
    }

    private fun loadUrl(view: WebView, newUrl: String) {
        isEmptyHome()
//        if(binding.tvTabCount.text.toString().toInt() < 1)
//            tabCount.value += 1
        deleteUrlInRoom(newUrl)
        view.loadUrl(newUrl)
    }

    private fun deleteUrlInRoom(targetUrl: String) {
        // Delete now url in room before move new page.
        CoroutineScope(Dispatchers.IO).launch {
            MyRoomDatabase.getInstance(this@WebViewActivity).getTabDao()
                .deleteTab(targetUrl)
        }
    }

    private var time : Long = 0
    override fun onBackPressed() {
        binding.run {
            if(wvWebView.canGoBack()) wvWebView.goBack()
            else {
                if(System.currentTimeMillis() - time >= 2000) {
                    time = System.currentTimeMillis()
                    Toast.makeText(this@WebViewActivity, "한번 더 누르면 종료합니다", Toast.LENGTH_SHORT).show()
                }
                else if(System.currentTimeMillis() - time < 2000) {
                    finishAffinity()
                }
            }
        }
    }
}