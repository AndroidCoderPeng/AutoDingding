package com.pengxh.autodingding.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pengxh.autodingding.R
import com.pengxh.autodingding.bean.DateTimeBean

class DateTimeAdapter(context: Context) : RecyclerView.Adapter<DateTimeAdapter.ItemViewHolder>() {

    private var dataBeans: MutableList<DateTimeBean> = ArrayList()
    private var layoutInflater: LayoutInflater = LayoutInflater.from(context)

    @SuppressLint("NotifyDataSetChanged")
    fun setupDateTimeData(beans: MutableList<DateTimeBean>) {
        this.dataBeans = beans
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = dataBeans.size

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            layoutInflater.inflate(R.layout.item_timer_rv_l, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val timeBean = dataBeans[position]
        holder.timeView.text = timeBean.time
        holder.dateView.text = timeBean.date
        holder.weekDayView.text = timeBean.weekDay

        // 长按监听
        holder.itemView.setOnLongClickListener { v ->
            itemClickListener?.onItemLongClick(v, position)
            true
        }
    }

    private var itemClickListener: OnItemLongClickListener? = null

    fun setOnItemLongClickListener(listener: OnItemLongClickListener?) {
        this.itemClickListener = listener
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(view: View?, index: Int)
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var timeView: TextView = itemView.findViewById(R.id.timeView)
        var dateView: TextView = itemView.findViewById(R.id.dateView)
        var weekDayView: TextView = itemView.findViewById(R.id.weekDayView)
    }
}