package com.example.mybrowser.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mybrowser.R
import com.example.mybrowser.model.TabEntity

class TabListAdapter(private val _list: List<TabEntity>?) : RecyclerView.Adapter<TabListAdapter.TabListViewHolder>() {
    private val tabList = _list?.toMutableList()
    class TabListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSiteTitle: TextView = view.findViewById(R.id.tv_site_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tab_list_item, parent, false)
        return TabListViewHolder(view)
    }

    override fun onBindViewHolder(holder: TabListViewHolder, position: Int) {
        holder.apply {
            tvSiteTitle.text = tabList?.get(position)?.url
        }
    }

    override fun getItemCount(): Int {
        return tabList?.size ?: 0
    }
}