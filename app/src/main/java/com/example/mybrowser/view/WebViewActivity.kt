package com.example.mybrowser.view

import android.content.Intent
import android.os.Bundle
import com.example.mybrowser.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
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
import com.example.mybrowser.model.TabEntity
import com.example.mybrowser.util.Pref
import com.example.mybrowser.viewmodel.BrowseViewModel
import kotlinx.coroutines.*

class WebViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWebViewBinding
    private lateinit var keyManager: InputMethodManager
    private val viewModel: BrowseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent.getStringExtra("changeTab")?.let {
            Log.e("tab changed, url is $it")
            initUi(it)
        } ?: Pref.getInstance(this)?.getString(Pref.RESUME)?.let {
            Log.e("resume url is $it")
            initUi(it)
        }
//        intent.getBooleanExtra("newTab", false).also { isNewTab ->
//            when(isNewTab) {
//                true -> {
//                    Pref.getInstance(this)?.getString(Pref.HOME)?.let {
//                        Log.e("home url is $it")
//                        initUi(it)
//                    }
//                }
//                false -> {
//
//                }
//            }
//        }

        keyManager = getSystemService(InputMethodManager::class.java)
        observeData()

        // Handled method for share action
        if(intent?.action == Intent.ACTION_SEND) {
            when(intent.type) {
                "text/plain" -> {
                    initUi(intent.getStringExtra(Intent.EXTRA_TEXT).toString())
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Pref.getInstance(this@WebViewActivity)?.getString(Pref.TAB_COUNT)?.let {
            if(it == "")
                viewModel.tabCount.value = 0
            else
                viewModel.tabCount.value = it.toInt()
        }
    }

    override fun onPause() {
        super.onPause()
        Pref.getInstance(this)?.run {
            setValue(Pref.RESUME, binding.wvWebView.url.toString())
            setValue(Pref.TAB_COUNT, binding.tvTabCount.text.toString())
        }
    }

    private fun initUi(targetUrl: String) {
        binding.apply {
            wvWebView.apply {
                webViewClient = MyWebClient(binding)
                settings.apply {
                    javaScriptEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                    textZoom = 95 // Set text size of WebView, Default is 100.
                }
                if(isEmptyHome()?.not() == true)
                {
                    loadUrl(wvWebView, targetUrl)
                }

                setOnScrollChangeListener { view, i, i2, i3, i4 ->

                }
            }

            slWebLayout.apply {
                setOnRefreshListener {
                    isRefreshing = false
                    wvWebView.reload()
                }
            }

            ivNext.apply {
                isEnabled = false
                setOnClickListener { wvWebView.goForward() }
            }
            ivPrev.apply {
                isEnabled = false
                setOnClickListener { wvWebView.goBack() }
            }
            ivBookmark.apply {
                CoroutineScope(Dispatchers.IO).launch {
                    val list = MyRoomDatabase.getInstance(this@WebViewActivity).getTabDao()
                        .selectTabList()
                    list.forEach { entity ->
                        if(entity.url == targetUrl)
                            setImageResource(R.drawable.ic_baseline_star_24)
                    }
                }
                setOnClickListener {
                    if(targetUrl == "") return@setOnClickListener
                    CoroutineScope(Dispatchers.IO).launch {
                        val list = MyRoomDatabase.getInstance(this@WebViewActivity).getTabDao()
                            .distinctCheckTab(targetUrl)
                        withContext(Dispatchers.Main) {
                            (it as ImageView).apply {
                                if(list.isEmpty()) {
                                    setImageResource(R.drawable.ic_baseline_star_24)
                                    CoroutineScope(Dispatchers.IO).launch {
                                        MyRoomDatabase.getInstance(this@WebViewActivity).getTabDao()
                                            .insertTabList(TabEntity(
                                                0, "", targetUrl
                                            ))
                                        viewModel.tabCount.postValue(
                                            MyRoomDatabase.getInstance(this@WebViewActivity).getTabDao()
                                                .selectTabList().size
                                        )
                                    }
                                }
                                else {
                                    setImageResource(R.drawable.ic_baseline_star_border_24)
                                    CoroutineScope(Dispatchers.IO).launch {
                                        MyRoomDatabase.getInstance(this@WebViewActivity).getTabDao()
                                            .deleteTab(targetUrl)
                                        viewModel.tabCount.postValue(
                                            MyRoomDatabase.getInstance(this@WebViewActivity).getTabDao()
                                                .selectTabList().size
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
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
                                            viewModel.homeUrlLive.postValue(makeUrl(v.text.toString()))
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
                        if(it.isEmpty()) {
                            Toast.makeText(this@WebViewActivity, getString(R.string.str_empty_home_url), Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
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
                                        Log.e("search url is $url")
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

    private fun observeData() {
        viewModel.apply {
            tabCount.observe(this@WebViewActivity, Observer {
                binding.tvTabCount.text = it.toString()
            })
            homeUrlLive.observe(this@WebViewActivity, Observer {
                Log.e("homeUrl Changed")
            })
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
        view.loadUrl(newUrl)
    }

//    private fun getUrlId(url: String) : Int? {
//        var id: Int? = null
//        CoroutineScope(Dispatchers.IO).launch {
//            id = MyRoomDatabase.getInstance(this@WebViewActivity).getTabDao()
//                .selectUrlId(url)
//        }
//        Log.e("id is $id")
//        return id
//    }

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