package com.pengxh.autodingding.utils;

import org.greenrobot.greendao.converter.PropertyConverter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeConvert implements PropertyConverter<String, Date> {

    private final DateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);

    /**
     * 转成对象的属性
     */
    @Override
    public String convertToEntityProperty(Date databaseValue) {
        return format.format(databaseValue);
    }

    /**
     * 转换成数据库的属性
     */
    @Override
    public Date convertToDatabaseValue(String entityProperty) {
        //9:00
        Date date;
        try {
            date = format.parse(entityProperty);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return date;
    }
}
