package com.pengxh.autodingding.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pengxh.autodingding.R
import com.pengxh.autodingding.bean.TaskTimeBean
import com.pengxh.kt.lite.adapter.ViewHolder

class TaskTimeAdapter(context: Context, private val tasks: ArrayList<TaskTimeBean>) :
    RecyclerView.Adapter<ViewHolder>() {

    private val TYPE_LAST_ITEM = 0
    private val TYPE_TASK_ITEM = 1
    private var inflater = LayoutInflater.from(context)

    //最后按钮行，需要+1
    override fun getItemCount(): Int = tasks.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position == tasks.size) {
            TYPE_LAST_ITEM
        } else {
            TYPE_TASK_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == TYPE_TASK_ITEM) {
            ViewHolder(inflater.inflate(R.layout.item_task_rv_l, parent, false))
        } else {
            ViewHolder(inflater.inflate(R.layout.item_add_task_rv_l, parent, false))
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setRefreshData(dataRows: MutableList<TaskTimeBean>) {
        tasks.clear()
        tasks.addAll(dataRows)
        notifyDataSetChanged()
    }

    fun setLoadMoreData(dataRows: MutableList<TaskTimeBean>) {
        tasks.addAll(dataRows)
        notifyItemRangeInserted(tasks.size, dataRows.size)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder.itemViewType == TYPE_TASK_ITEM) {
            val item = tasks[position]
            holder.setText(R.id.startTimeView, "开始时间：${item.startTime}")
                .setText(R.id.endTimeView, "结束时间：${item.endTime}")

            holder.itemView.setOnClickListener { itemClickListener?.onItemClick(item) }
            holder.itemView.setOnLongClickListener {
                itemClickListener?.onItemLongClick(item)
                true
            }
        } else {
            //按钮行
            holder.setOnClickListener(R.id.addTaskButton) { itemClickListener?.onAddTaskClick() }
        }
    }

    private var itemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(itemClickListener: OnItemClickListener?) {
        this.itemClickListener = itemClickListener
    }

    interface OnItemClickListener {
        fun onAddTaskClick()

        fun onItemClick(bean: TaskTimeBean)

        fun onItemLongClick(bean: TaskTimeBean)
    }
}