package com.pengxh.autodingding.utils

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.pengxh.autodingding.bean.HistoryRecordBean
import com.pengxh.autodingding.bean.MailInfo
import com.pengxh.kt.lite.extensions.show
import com.pengxh.kt.lite.utils.SaveKeyValues
import jxl.Workbook
import jxl.WorkbookSettings
import jxl.format.Alignment
import jxl.format.Border
import jxl.format.BorderLineStyle
import jxl.format.Colour
import jxl.write.*
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @date: 2020/4/15 17:14
 */
object ExcelUtils {

    private const val TAG = "ExcelUtils"
    private lateinit var arial14format: WritableCellFormat
    private lateinit var arial10format: WritableCellFormat
    private lateinit var arial12format: WritableCellFormat
    private const val UTF8_ENCODING = "UTF-8"

    /**
     * 初始化Excel
     *
     * @param fileName
     * @param colName
     */
    fun initExcel(fileName: String?, colName: Array<String>) {
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

    /**
     * 单元格的格式设置 字体大小 颜色 对齐方式、背景颜色等...
     */
    private fun format() {
        try {
            val arial14font = WritableFont(WritableFont.ARIAL, 14, WritableFont.BOLD)
            arial14font.colour = Colour.LIGHT_BLUE
            arial14format = WritableCellFormat(arial14font)
            arial14format.alignment = Alignment.CENTRE
            arial14format.setBorder(Border.ALL, BorderLineStyle.THIN)
            arial14format.setBackground(Colour.VERY_LIGHT_YELLOW)
            arial10format =
                WritableCellFormat(WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD))
            arial10format.alignment = Alignment.CENTRE
            arial10format.setBorder(Border.ALL, BorderLineStyle.THIN)
            arial10format.setBackground(Colour.GRAY_25)
            arial12format = WritableCellFormat(WritableFont(WritableFont.ARIAL, 10))
            arial10format.alignment = Alignment.CENTRE //对齐格式
            arial12format.setBorder(Border.ALL, BorderLineStyle.THIN) //设置边框
        } catch (e: WriteException) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun writeObjListToExcel(
        context: Context, objList: List<HistoryRecordBean>?, fileName: String?
    ) {
        if (objList != null && objList.isNotEmpty()) {
            var writeBook: WritableWorkbook? = null
            var `in`: InputStream? = null
            try {
                val setEncode = WorkbookSettings()
                setEncode.encoding = UTF8_ENCODING
                `in` = FileInputStream(fileName)
                val workbook: Workbook = Workbook.getWorkbook(`in`)
                val file = File(fileName)
                writeBook = Workbook.createWorkbook(file, workbook)
                val sheet: WritableSheet = writeBook.getSheet(0)
                for (j in objList.indices) {
                    val historyBean: HistoryRecordBean = objList[j]
                    val uuid: String = historyBean.uuid
                    val date: String = historyBean.date
                    val message: String = historyBean.message
                    //第一行留作表头
                    sheet.addCell(Label(0, j + 1, uuid, arial12format))
                    sheet.addCell(Label(1, j + 1, date, arial12format))
                    sheet.addCell(Label(2, j + 1, message, arial12format))
                    sheet.setRowView(j + 1, 350) //设置行高
                }
                writeBook.write()
                Log.d(TAG, "writeObjListToExcel: 导出表格到本地成功")
                //然后发送邮件到指定邮箱
                val emailAddress = SaveKeyValues.getValue(Constant.EMAIL_ADDRESS, "") as String
                if (TextUtils.isEmpty(emailAddress)) {
                    "邮箱未填写，无法导出".show(context)
                    return
                }
                Thread {
                    val mailInfo: MailInfo = MailInfoUtil.createAttachMail(emailAddress, file)
                    MailSender.sendAccessoryMail(mailInfo)
                }.start()
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
                if (`in` != null) {
                    try {
                        `in`.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}