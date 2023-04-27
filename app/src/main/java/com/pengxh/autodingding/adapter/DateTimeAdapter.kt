package com.pengxh.autodingding.adapter

import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.pengxh.autodingding.R
import com.pengxh.autodingding.bean.DateTimeBean
import com.pengxh.autodingding.extensions.diffCurrentMillis

class DateTimeAdapter(context: Context, private val dataBeans: MutableList<DateTimeBean>) :
    RecyclerView.Adapter<DateTimeAdapter.ItemViewHolder>() {

    private val kTag = "DateTimeAdapter"
    private var layoutInflater: LayoutInflater = LayoutInflater.from(context)
    private lateinit var countDownTimer: CountDownTimer

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
            itemClickListener?.onItemLongClick(it, position)
            true
        }

        holder.timerSwitch.setOnCheckedChangeListener { _, isChecked ->
            val time = "${timeBean.date} ${timeBean.time}"
            val diffCurrentMillis = time.diffCurrentMillis()
            Log.d(kTag, "onBindViewHolder: $diffCurrentMillis")
            if (isChecked) {
//                holder.countDownTextView.text = "0小时${diffCurrentTime}分钟后自动打卡"
                holder.countDownProgress.max = diffCurrentMillis.toInt()
                countDownTimer = object : CountDownTimer(diffCurrentMillis, 1) {
                    override fun onTick(millisUntilFinished: Long) {
//                        holder.countDownTextView.text =
//                            "${(diffCurrentMillis - millisUntilFinished)}"
                        holder.countDownProgress.progress = (
                                diffCurrentMillis - millisUntilFinished).toInt()
                    }

                    override fun onFinish() {

                    }
                }.start()
            } else {
//                holder.countDownTextView.text = "0小时0分钟后自动打卡"
                countDownTimer.cancel()
            }
        }
    }

    private var itemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        this.itemClickListener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(layoutPosition: Int)

        fun onItemLongClick(view: View?, layoutPosition: Int)
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var timeView: TextView = itemView.findViewById(R.id.timeView)
        var dateView: TextView = itemView.findViewById(R.id.dateView)
        var weekDayView: TextView = itemView.findViewById(R.id.weekDayView)
        var timerSwitch: Switch = itemView.findViewById(R.id.timerSwitch)
        var countDownTextView: TextView = itemView.findViewById(R.id.countDownTextView)
        var countDownProgress: LinearProgressIndicator = itemView.findViewById(
            R.id.countDownProgress
        )
    }
}