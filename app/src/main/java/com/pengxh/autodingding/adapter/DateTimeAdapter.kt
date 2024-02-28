package com.pengxh.autodingding.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.pengxh.autodingding.R
import com.pengxh.autodingding.bean.DateTimeBean
import com.pengxh.autodingding.extensions.diffCurrentMillis
import com.pengxh.autodingding.extensions.isEarlierThenCurrent

@SuppressLint("SetTextI18n")
class DateTimeAdapter(context: Context, private val dataBeans: MutableList<DateTimeBean>) :
    RecyclerView.Adapter<DateTimeAdapter.ItemViewHolder>() {

    private val kTag = "DateTimeAdapter"
    private var layoutInflater = LayoutInflater.from(context)
    private var countDownTimer: CountDownTimer? = null

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

        val time = "${timeBean.date} ${timeBean.time}"
        if (time.isEarlierThenCurrent()) {
            holder.countDownTextView.text = "任务已过期"
            holder.countDownTextView.setTextColor(Color.RED)
        } else {
            val diffCurrentMillis = time.diffCurrentMillis()

            holder.countDownTextView.setTextColor(Color.BLUE)

            holder.countDownProgress.max = diffCurrentMillis.toInt()
            countDownTimer = object : CountDownTimer(diffCurrentMillis, 1) {
                override fun onTick(millisUntilFinished: Long) {
                    holder.countDownProgress.progress =
                        (diffCurrentMillis - millisUntilFinished).toInt()

                    holder.countDownTextView.text = "${millisUntilFinished / 1000}秒后自动打卡"
                }

                override fun onFinish() {
                    itemClickListener?.onCountDownFinish()
                }
            }.start()
        }
    }

    fun stopCountDownTimer() {
        countDownTimer?.cancel()
        Log.d(kTag, "countDownTimer => 已取消")
    }

    private var itemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        this.itemClickListener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(layoutPosition: Int)

        fun onItemLongClick(view: View?, layoutPosition: Int)

        fun onCountDownFinish()
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var timeView: TextView = itemView.findViewById(R.id.timeView)
        var dateView: TextView = itemView.findViewById(R.id.dateView)
        var weekDayView: TextView = itemView.findViewById(R.id.weekDayView)
        var countDownTextView: TextView = itemView.findViewById(R.id.countDownTextView)
        var countDownProgress: LinearProgressIndicator = itemView.findViewById(
            R.id.countDownProgress
        )
    }
}