package com.pengxh.daily.app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.pengxh.daily.app.R
import com.pengxh.daily.app.bean.DailyTaskBean
import com.pengxh.kt.lite.extensions.convertColor

class DailyTaskAdapter(
    private val context: Context,
    private val dataBeans: MutableList<DailyTaskBean>
) : RecyclerView.Adapter<DailyTaskAdapter.ItemViewHolder>() {

    private val kTag = "DailyTaskAdapter"
    private var layoutInflater = LayoutInflater.from(context)
    private var mPosition = -1
    private var actualTime = "--:--:--"

    fun updateCurrentTaskState(position: Int) {
        this.mPosition = position
        notifyDataSetChanged()
    }

    fun updateCurrentTaskState(position: Int, actualTime: String) {
        this.mPosition = position
        this.actualTime = actualTime
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = dataBeans.size

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            layoutInflater.inflate(R.layout.item_daily_task_rv_l, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val taskBean = dataBeans[position]
        holder.taskTimeView.text = taskBean.time

        holder.itemView.setOnClickListener {
            itemClickListener?.onItemClick(taskBean, position)
        }

        // 长按监听
        holder.itemView.setOnLongClickListener {
            itemClickListener?.onItemLongClick(taskBean, position)
            true
        }

        if (position == mPosition) {
            holder.itemView.isSelected = true
            holder.taskStateView.visibility = View.VISIBLE
            holder.actualTimeCardView.visibility = View.VISIBLE
            holder.actualTimeView.text = actualTime
            holder.taskTimeView.setTextColor(R.color.lib_hint_color.convertColor(context))
        } else {
            holder.itemView.isSelected = false
            holder.taskStateView.visibility = View.GONE
            holder.actualTimeCardView.visibility = View.GONE
            holder.actualTimeView.text = "--:--:--"
            holder.taskTimeView.setTextColor(R.color.black.convertColor(context))
        }
    }

    private var itemClickListener: OnItemClickListener<DailyTaskBean>? = null

    fun setOnItemClickListener(listener: OnItemClickListener<DailyTaskBean>) {
        this.itemClickListener = listener
    }

    interface OnItemClickListener<T> {
        fun onItemClick(item: T, position: Int)

        fun onItemLongClick(item: T, position: Int)
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var actualTimeCardView: RelativeLayout = itemView.findViewById(R.id.actualTimeCardView)
        var actualTimeView: TextView = itemView.findViewById(R.id.actualTimeView)
        var taskStateView: CardView = itemView.findViewById(R.id.taskStateView)
        var taskTimeView: TextView = itemView.findViewById(R.id.taskTimeView)
    }
}