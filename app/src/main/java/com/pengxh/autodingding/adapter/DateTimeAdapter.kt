package com.pengxh.autodingding.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pengxh.autodingding.R
import com.pengxh.autodingding.bean.DateTimeBean

class DateTimeAdapter(context: Context, private val dataBeans: MutableList<DateTimeBean>) :
    RecyclerView.Adapter<DateTimeAdapter.ItemViewHolder>() {

    private val kTag = "DateTimeAdapter"
    private var layoutInflater = LayoutInflater.from(context)

    private var mPosition = -1

    fun updateCurrentTaskState(position: Int) {
        this.mPosition = position
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
        holder.dateView.text = timeBean.date
        holder.timeView.text = timeBean.time
        holder.weekDayView.text = timeBean.weekDay

        holder.itemView.setOnClickListener {
            itemClickListener?.onItemClick(position)
        }

        // 长按监听
        holder.itemView.setOnLongClickListener {
            itemClickListener?.onItemLongClick(position)
            true
        }

        holder.itemView.isSelected = position == mPosition
    }

    private var itemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        this.itemClickListener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)

        fun onItemLongClick(position: Int)
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var timeView: TextView = itemView.findViewById(R.id.timeView)
        var dateView: TextView = itemView.findViewById(R.id.dateView)
        var weekDayView: TextView = itemView.findViewById(R.id.weekDayView)
    }
}