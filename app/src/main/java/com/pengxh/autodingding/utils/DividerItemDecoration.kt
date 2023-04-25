package com.pengxh.autodingding.utils

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class DividerItemDecoration(private val leftMargin: Float, private val rightMargin: Float) :
    RecyclerView.ItemDecoration() {

    private val bottomLinePaint by lazy { Paint() }

    init {
        bottomLinePaint.isAntiAlias = true
        bottomLinePaint.color = Color.LTGRAY
    }

    //画分割线
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val childCount: Int = parent.childCount
        for (i in 0 until childCount) {
            val view: View = parent.getChildAt(i)
            c.drawRect(
                leftMargin,
                view.bottom.toFloat(),
                parent.width.toFloat() - rightMargin,
                (view.bottom + 1).toFloat(),
                bottomLinePaint
            )
        }
    }
}