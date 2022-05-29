package com.example.mybrowser.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BrowseViewModel : ViewModel() {
    val homeUrlLive: MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val tabCount: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }
    val url: MutableLiveData<String> by lazy { MutableLiveData<String>() }
}