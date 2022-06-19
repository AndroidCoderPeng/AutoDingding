package com.pengxh.autodingding.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.pengxh.androidx.lite.utils.SaveKeyValues;
import com.pengxh.androidx.lite.widget.EasyToast;
import com.pengxh.autodingding.bean.HistoryRecordBean;
import com.pengxh.autodingding.bean.MailInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @date: 2020/4/15 17:14
 */
public class ExcelUtils {
    private static final String TAG = "ExcelUtils";
    private static WritableCellFormat arial14format = null;
    private static WritableCellFormat arial10format = null;
    private static WritableCellFormat arial12format = null;
    private final static String UTF8_ENCODING = "UTF-8";

    /**
     * 初始化Excel
     *
     * @param fileName
     * @param colName
     */
    public static void initExcel(String fileName, String[] colName) {
        format();
        WritableWorkbook writeBook = null;
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            writeBook = Workbook.createWorkbook(file);
            WritableSheet sheet = writeBook.createSheet("打卡记录表", 0);
            //创建标题栏
            sheet.addCell(new Label(0, 0, fileName, arial14format));
            for (int col = 0; col < colName.length; col++) {
                sheet.addCell(new Label(col, 0, colName[col], arial10format));
            }
            sheet.setRowView(0, 340); //设置行高
            writeBook.write();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writeBook != null) {
                try {
                    writeBook.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 单元格的格式设置 字体大小 颜色 对齐方式、背景颜色等...
     */
    private static void format() {
        try {
            WritableFont arial14font = new WritableFont(WritableFont.ARIAL, 14, WritableFont.BOLD);
            arial14font.setColour(jxl.format.Colour.LIGHT_BLUE);
            arial14format = new WritableCellFormat(arial14font);
            arial14format.setAlignment(jxl.format.Alignment.CENTRE);
            arial14format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
            arial14format.setBackground(jxl.format.Colour.VERY_LIGHT_YELLOW);

            arial10format = new WritableCellFormat(new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD));
            arial10format.setAlignment(jxl.format.Alignment.CENTRE);
            arial10format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
            arial10format.setBackground(Colour.GRAY_25);

            arial12format = new WritableCellFormat(new WritableFont(WritableFont.ARIAL, 10));
            arial10format.setAlignment(jxl.format.Alignment.CENTRE);//对齐格式
            arial12format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN); //设置边框
        } catch (WriteException e) {
            e.printStackTrace();
        }
    }

    public static void writeObjListToExcel(Context context, List<HistoryRecordBean> objList, String fileName) {
        if (objList != null && objList.size() > 0) {
            WritableWorkbook writeBook = null;
            InputStream in = null;
            try {
                WorkbookSettings setEncode = new WorkbookSettings();
                setEncode.setEncoding(UTF8_ENCODING);
                in = new FileInputStream(fileName);
                Workbook workbook = Workbook.getWorkbook(in);
                File file = new File(fileName);
                writeBook = Workbook.createWorkbook(file, workbook);
                WritableSheet sheet = writeBook.getSheet(0);
                for (int j = 0; j < objList.size(); j++) {
                    HistoryRecordBean historyBean = objList.get(j);
                    String uuid = historyBean.getUuid();
                    String date = historyBean.getDate();
                    String message = historyBean.getMessage();
                    //第一行留作表头
                    sheet.addCell(new Label(0, j + 1, uuid, arial12format));
                    sheet.addCell(new Label(1, j + 1, date, arial12format));
                    sheet.addCell(new Label(2, j + 1, message, arial12format));
                    sheet.setRowView(j + 1, 350); //设置行高
                }
                writeBook.write();
                Log.d(TAG, "writeObjListToExcel: 导出表格到本地成功");
                //然后发送邮件到指定邮箱
                String emailAddress = (String) SaveKeyValues.getValue(Constant.EMAIL_ADDRESS, "");
                if (TextUtils.isEmpty(emailAddress)) {
                    EasyToast.show(context, "邮箱未填写，无法导出");
                    return;
                }
                new Thread(() -> {
                    MailInfo mailInfo = MailInfoUtil.createAttachMail(emailAddress, file);
                    MailSender.getSender().sendAccessoryMail(mailInfo);
                }).start();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (writeBook != null) {
                    try {
                        writeBook.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
