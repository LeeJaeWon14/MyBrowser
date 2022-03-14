package com.example.mybrowser.client

import android.graphics.Bitmap
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.mybrowser.R
import com.example.mybrowser.databinding.ActivityWebViewBinding

class MyWebClient(private val binding: ActivityWebViewBinding) : WebViewClient() {
    private var checkCleartext: Boolean = false
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        view?.let {
            binding.apply {
                ivNext.isEnabled = it.canGoForward()
                ivPrev.isEnabled = it.canGoBack()
            }
        }
        binding.url.text = url
        Log.e("Web", view?.context?.getString(R.string.str_page_started) + ", $url")
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        Log.e("Web", view?.context?.getString(R.string.str_page_finished)!!)
        if(checkCleartext) {
            checkCleartext = false
            view.clearHistory()
        }
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        Log.e("Web", "Got Error, code is ${error?.description} and ${error?.errorCode}! request is ${request?.requestHeaders}")
        view?.let {
            when(error?.description) {
                it.context.getString(R.string.str_err_cleartext) -> {
                    it.loadUrl(request?.url.toString().replace("http", "https"))
                    checkCleartext = true
                }
            }
        }

//        when(error?.errorCode) {
//
//        }
    }

//    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
//        Log.e("web", "receive url ~~~ ${request?.url.toString()}")
//        view?.let {
//            if(request?.url.toString().contains("https").not()) {
//                it.loadUrl(request?.url.toString().replace("http", "https"))
//            }
//        }
//        return false
//    }
}