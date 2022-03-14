package com.example.mybrowser

import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.WebSettings
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.mybrowser.client.MyWebClient
import com.example.mybrowser.databinding.ActivityWebViewBinding
import com.example.mybrowser.databinding.DialogUrlSearchBinding
import com.example.mybrowser.util.Pref

class WebViewActivity : AppCompatActivity() {
    companion object {
        const val BASE_URL = "https://velog.io/@jeep_chief_14"
        const val NAVER = "https://www.naver.com"
        const val TEST = "http://www.kyungmin.ac.kr"
    }

    private lateinit var binding: ActivityWebViewBinding
    private lateinit var keyManager: InputMethodManager
    private val homeUrlLive: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        keyManager = getSystemService(InputMethodManager::class.java)

        homeUrlLive.value = Pref.getInstance(this)?.getString(Pref.HOME)
        homeUrlLive.observe(this, Observer {
            initUi(it)
        })
    }

    private fun initUi(homeUrl: String) {
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
                if(isEmptyHome(homeUrl).not())
                    loadUrl(homeUrl)
            }

            slWebLayout.apply {
                setOnRefreshListener {
                    isRefreshing = false
                    wvWebView.reload()
                }
            }

            ivNext.setOnClickListener { wvWebView.goForward() }
            ivPrev.setOnClickListener { wvWebView.goBack() }

            ivHome.apply {
                setOnClickListener { wvWebView.loadUrl(homeUrl) }
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
                                            homeUrlLive.postValue(v.text.toString())
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
                                        wvWebView.loadUrl(url)
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
        }
    }

    private fun isEmptyHome(homeUrl: String) : Boolean {
        binding.apply {
            tvEmptyMsg.isVisible = homeUrl.isEmpty()
            slWebLayout.isVisible = homeUrl.isEmpty().not()
        }
        return homeUrl.isEmpty()
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