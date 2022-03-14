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
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        view?.let {
            binding.apply {
                ivNext.isEnabled = it.canGoForward()
                ivPrev.isEnabled = it.canGoBack()
            }
        }
        binding.url.text = url
        Log.e("Web", "page started")
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        Log.e("Web", "page finished")
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
//        Log.e("web", "error, ${error?.description}")

        Log.e("Web", "Got Error, code is ${error?.description} and ${error?.errorCode}! request is ${request?.requestHeaders}")
//        view?.let {
//            when(error?.description) {
//                it.context.getString(R.string.str_err_cleartext) -> {
//                    it.loadUrl(request?.url.toString().replace("http", "https"))
//                }
//                else -> {
//                    Log.e("Web", "Got Error, code is ${error?.description} and ${error?.errorCode}! request is ${request?.requestHeaders}")
//                }
//            }
//        }
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        view?.let {
            if(request?.url.toString().contains("https").not()) {
                it.loadUrl(request?.url.toString().replace("http", "https"))
            }
        }
        return false
    }
}