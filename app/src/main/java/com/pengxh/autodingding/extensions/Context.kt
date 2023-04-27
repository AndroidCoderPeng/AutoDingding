package com.pengxh.autodingding.extensions

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.text.TextUtils
import androidx.core.app.NotificationManagerCompat
import com.pengxh.autodingding.bean.HistoryRecordBean
import com.pengxh.autodingding.utils.Constant
import com.pengxh.autodingding.utils.MailInfoCreator
import com.pengxh.kt.lite.extensions.show
import com.pengxh.kt.lite.utils.SaveKeyValues
import jxl.Workbook
import jxl.WorkbookSettings
import jxl.write.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

/**
 * 检测通知监听服务是否被授权
 * */
fun Context.notificationEnable(): Boolean {
    val packages = NotificationManagerCompat.getEnabledListenerPackages(this)
    return packages.contains(this.packageName)
}

/**
 * 检查手机上是否安装了指定的软件
 */
fun Context.isAppAvailable(packageName: String): Boolean {
    val packageManager = this.packageManager
    //获取所有已安装程序的包信息
    val packages = packageManager.getInstalledPackages(0)
    val packageNames: MutableList<String> = ArrayList()
    for (i in packages.indices) {
        val packName = packages[i].packageName
        packageNames.add(packName)
    }
    return packageNames.contains(packageName)
}

/**
 * 打开指定包名的apk
 */
fun Context.openApplication(packageName: String) {
    val packageManager = this.packageManager
    val resolveIntent = Intent(Intent.ACTION_MAIN, null)
    resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER)
    resolveIntent.setPackage(packageName)
    val apps = packageManager.queryIntentActivities(resolveIntent, 0)
    val iterator: Iterator<ResolveInfo> = apps.iterator()
    if (!iterator.hasNext()) {
        return
    }
    val resolveInfo = iterator.next()
    val className = resolveInfo.activityInfo.name
    val intent = Intent(Intent.ACTION_MAIN)
    intent.addCategory(Intent.CATEGORY_LAUNCHER)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val cn = ComponentName(packageName, className)
    intent.component = cn
    this.startActivity(intent)
}

fun Context.writeObjToExcel(objList: List<HistoryRecordBean>, fileName: String) {
    var writeBook: WritableWorkbook? = null
    var inputStream: InputStream? = null
    try {
        val setEncode = WorkbookSettings()
        setEncode.encoding = "UTF-8"
        inputStream = FileInputStream(fileName)
        val workbook: Workbook = Workbook.getWorkbook(inputStream)
        val file = File(fileName)
        writeBook = Workbook.createWorkbook(file, workbook)
        val sheet: WritableSheet = writeBook.getSheet(0)

        val arial12format = WritableCellFormat(WritableFont(WritableFont.ARIAL, 10))
        arial12format.setBorder(Border.ALL, BorderLineStyle.THIN) //设置边框

        for (j in objList.indices) {
            val historyBean = objList[j]
            val uuid = historyBean.uuid
            val date = historyBean.date
            val message = historyBean.message
            //第一行留作表头
            sheet.addCell(Label(0, j + 1, uuid, arial12format))
            sheet.addCell(Label(1, j + 1, date, arial12format))
            sheet.addCell(Label(2, j + 1, message, arial12format))
            sheet.setRowView(j + 1, 350) //设置行高
        }
        writeBook.write()
        //然后发送邮件到指定邮箱
        val emailAddress = SaveKeyValues.getValue(Constant.EMAIL_ADDRESS, "") as String
        if (TextUtils.isEmpty(emailAddress)) {
            "邮箱未填写，无法导出".show(this)
            return
        }
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                val mailInfo = MailInfoCreator.createAttachMail(emailAddress, file)
                mailInfo.sendAccessoryMail()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        writeBook?.close()
        inputStream?.close()
    }
}