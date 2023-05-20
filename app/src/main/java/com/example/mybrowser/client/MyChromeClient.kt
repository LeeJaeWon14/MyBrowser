package com.example.mybrowser.client

import android.webkit.ConsoleMessage
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import com.example.mybrowser.util.Log

class MyChromeClient : WebChromeClient() {
    override fun onJsAlert(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        AlertDialog.Builder(view?.context!!)
            .setMessage(message)
            .setPositiveButton("확인") { _, _ ->
                result?.confirm()
            }
            .show()

        return super.onJsAlert(view, url, message, result)
    }

    override fun onJsConfirm(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        AlertDialog.Builder(view?.context!!)
            .setMessage(message)
            .setPositiveButton("확인") { _, _ ->
                result?.confirm()
            }
            .setNegativeButton("취소") { _, _ ->
                result?.cancel()
            }
            .show()
        return super.onJsConfirm(view, url, message, result)
    }

    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        Log.e("[Web Log] $consoleMessage")
        return super.onConsoleMessage(consoleMessage)
    }
}