package com.pengxh.autodingding.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.CountDownTimer
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

class DateTimeAdapter(context: Context, private val dataBeans: MutableList<DateTimeBean>) :
    RecyclerView.Adapter<DateTimeAdapter.ItemViewHolder>() {

    private val kTag = "DateTimeAdapter"
    private val countDownTimerHashMap by lazy { HashMap<String, CountDownTimer?>() }
    private var layoutInflater = LayoutInflater.from(context)

    @SuppressLint("NotifyDataSetChanged")
    fun setRefreshData(dataRows: MutableList<DateTimeBean>) {
        this.dataBeans.clear()
        this.dataBeans.addAll(dataRows)
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

        val time = "${timeBean.date} ${timeBean.time}"
        if (time.isEarlierThenCurrent()) {
            holder.countDownTextView.text = "任务已过期"
            holder.countDownTextView.setTextColor(Color.RED)
        } else {
            val diffCurrentMillis = time.diffCurrentMillis()

            holder.countDownTextView.setTextColor(Color.BLUE)

            holder.countDownProgress.max = diffCurrentMillis.toInt()
            //刷新列表先停止之前的定时器，否则会出现重复计时问题
            val downTimer = countDownTimerHashMap[timeBean.uuid]
            downTimer?.cancel()
            //重新计时
            val countDownTimer = object : CountDownTimer(diffCurrentMillis, 1) {
                override fun onTick(millisUntilFinished: Long) {
                    holder.countDownProgress.progress =
                        (diffCurrentMillis - millisUntilFinished).toInt()

                    holder.countDownTextView.text = "${millisUntilFinished / 1000}秒后自动打卡"
                }

                override fun onFinish() {
                    itemClickListener?.onCountDownFinish()
                    holder.countDownTextView.text = "任务已过期"
                    holder.countDownTextView.setTextColor(Color.RED)
                }
            }.start()
            countDownTimerHashMap[timeBean.uuid] = countDownTimer

//            var countDown = diffCurrentMillis
//            val timer = Timer().schedule(object : TimerTask() {
//                override fun run() {
//
//                    holder.countDownProgress.progress = countDown.toInt()
//                    countDown--
//                    if (countDown == 0L) {
//                        itemClickListener?.onCountDownFinish()
//                        holder.countDownTextView.text = "任务已过期"
//                        holder.countDownTextView.setTextColor(Color.RED)
//                    }
//                }
//            }, 0, 1)
        }
    }

    fun stopCountDownTimer(bean: DateTimeBean) {
        countDownTimerHashMap[bean.uuid]?.cancel()
    }

    private var itemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        this.itemClickListener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)

        fun onItemLongClick(position: Int)

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