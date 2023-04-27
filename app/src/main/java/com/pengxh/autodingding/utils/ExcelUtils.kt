package com.pengxh.autodingding.utils

import jxl.Workbook
import jxl.format.Alignment
import jxl.format.Border
import jxl.format.BorderLineStyle
import jxl.format.Colour
import jxl.write.*
import java.io.File

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @date: 2020/4/15 17:14
 */
object ExcelUtils {

    private lateinit var arial14format: WritableCellFormat
    private lateinit var arial10format: WritableCellFormat

    /**
     * 单元格的格式设置 字体大小 颜色 对齐方式、背景颜色等...
     */
    private fun format() {
        try {
            arial10format =
                WritableCellFormat(WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD))
            arial10format.setBorder(Border.ALL, BorderLineStyle.THIN)
            arial10format.setBackground(Colour.GRAY_25)
            arial10format.alignment = Alignment.CENTRE //对齐格式

            val arial14font = WritableFont(WritableFont.ARIAL, 14, WritableFont.BOLD)
            arial14font.colour = Colour.LIGHT_BLUE
            arial14format = WritableCellFormat(arial14font)
            arial14format.alignment = Alignment.CENTRE
            arial14format.setBorder(Border.ALL, BorderLineStyle.THIN)
            arial14format.setBackground(Colour.VERY_LIGHT_YELLOW)
        } catch (e: WriteException) {
            e.printStackTrace()
        }
    }

    /**
     * 初始化Excel
     *
     * @param fileName
     * @param colName
     */
    fun initExcel(fileName: String, colName: Array<String>) {
        format()
        var writeBook: WritableWorkbook? = null
        try {
            val file = File(fileName)
            if (!file.exists()) {
                file.createNewFile()
            }
            writeBook = Workbook.createWorkbook(file)
            val sheet: WritableSheet = writeBook.createSheet("打卡记录表", 0)
            //创建标题栏
            sheet.addCell(Label(0, 0, fileName, arial14format))
            for (col in colName.indices) {
                sheet.addCell(Label(col, 0, colName[col], arial10format))
            }
            sheet.setRowView(0, 340) //设置行高
            writeBook.write()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (writeBook != null) {
                try {
                    writeBook.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}