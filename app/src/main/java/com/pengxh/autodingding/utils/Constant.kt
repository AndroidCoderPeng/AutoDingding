package com.pengxh.autodingding.utils

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @date: 2019/12/29 12:42
 */
object Constant {
    //发送者邮箱账号
    const val USER_MAIL_ACCOUNT = "pengxh_net@163.com"

    //邮箱授权码，不是密码（有效期180天）
    const val PERMISSION_CODE = "FJYQrH3QVxKhZpQ3"

    //发送者邮箱地址
    const val MAIL_FROM_ADDRESS = "pengxh_net@163.com"

    //发送者邮箱服务器
    const val MAIL_SERVER = "smtp.163.com"
    const val MAIL_SERVER_PORT = "25"

    const val EMAIL_ADDRESS = "emailAddress"
    const val EMAIL_TITLE = "emailTitle"
    const val TIMEOUT = "timeout"
    const val BACK_TO_HOME = "backToHome"
    const val DING_DING_KEY = "dingDingKey"
    const val SKIP_WEEKEND_KEY = "skipWeekend"
    const val SKIP_HOLIDAY_KEY = "skipHoliday"

    const val TICK_TIME_CODE = 2024071701
    const val UPDATE_TICK_TIME_CODE = 2024071702

    const val NOTICE_LISTENER_CONNECTED_CODE = 2024090801
    const val NOTICE_LISTENER_DISCONNECTED_CODE = 2024090802

    const val HIDE_FLOATING_WINDOW_CODE = 2024112501
    const val SHOW_FLOATING_WINDOW_CODE = 2024112502

    const val START_TASK_CODE = 2024120801
    const val EXECUTE_NEXT_TASK_CODE = 2024120802
    const val COMPLETED_ALL_TASK_CODE = 2024120803

    const val START_COUNT_DOWN_TIMER_CODE = 2024121801
    const val CANCEL_COUNT_DOWN_TIMER_CODE = 2024121802

    const val DING_DING = "com.alibaba.android.rimet"
    const val WECHAT = "com.tencent.mm"
    const val QQ = "com.tencent.mobileqq"
    const val TIM = "com.tencent.tim"
    const val ZFB = "com.eg.android.AlipayGphone"

    const val FOREGROUND_RUNNING_SERVICE_TITLE = "应用前台保活服务，请勿关闭此通知"

    //https://timor.tech/api/holiday/year/2025/
}