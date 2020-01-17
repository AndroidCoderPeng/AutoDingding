package com.pengxh.autodingding.utils;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @description: TODO
 * @date: 2020/1/17 14:47
 */
public interface HttpCallbackListener {
    void onError(Exception e);

    void onSuccess(String response);
}
