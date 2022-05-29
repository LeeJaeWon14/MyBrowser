package com.example.mybrowser.client

import android.graphics.Bitmap
import com.example.mybrowser.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.example.mybrowser.R
import com.example.mybrowser.databinding.ActivityWebViewBinding
import com.example.mybrowser.model.MyRoomDatabase
import com.example.mybrowser.model.TabEntity
import com.example.mybrowser.util.Pref
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyWebClient(private val binding: ActivityWebViewBinding) : WebViewClient() {
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        view?.let {
            binding.apply {
                ivNext.isEnabled = it.canGoForward()
                ivPrev.isEnabled = it.canGoBack()
                CoroutineScope(Dispatchers.IO).launch {
                    val list = MyRoomDatabase.getInstance(view.context).getTabDao()
                        .distinctCheckTab(url.toString())
                    withContext(Dispatchers.Main) {
                        if(list.isEmpty()) {
                            ivBookmark.setImageResource(R.drawable.ic_baseline_star_border_24)
                        }
                        else {
                            ivBookmark.setImageResource(R.drawable.ic_baseline_star_24)
                        }
                    }
                }
            }
            url?.let {
                binding.url.text = it

                Log.e(String.format(view.context?.getString(R.string.str_page_started)!!, it))
            }
        }
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        Log.e(view?.context?.getString(R.string.str_page_finished)!!)
//        url?.let {
//            if(it == "about:blank") return@let
//            CoroutineScope(Dispatchers.IO).launch {
//                MyRoomDatabase.getInstance(view.context).getTabDao().run {
//                    id?.let {
//                        // already have url
//                        // will adding update at room
//                        /* no-op */
//                    } ?: run {
//                        // new url
//                        Log.e("new Url! will adding on room")
//                        insertTabList(
//                            TabEntity(0, url = it)
//                        )
//                        selectTabList().also {
//                            Log.e("tab count is ${it.size}")
//                            Pref.getInstance(view.context)?.setValue(Pref.TAB_COUNT, it.size.toString())
//                            withContext(Dispatchers.Main) { binding.tvTabCount.text = it.size.toString() }
//                        }
//                    }
//                }
//            }
//        }
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        view?.let {
            when(error?.description) {
                it.context.getString(R.string.str_err_cleartext) -> {
                    it.loadUrl(request?.url.toString().replace("http", "https"))
                }
                else -> {
                    Log.e("Got Error, code is ${error?.description} and ${error?.errorCode}! request is ${request?.requestHeaders}")
//                    it.loadUrl("file://android_asset//error.html")
                    Toast.makeText(it.context, it.context.getString(R.string.str_unknown_error), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}