package com.pengxh.autodingding.utils;

import org.jsoup.nodes.Document;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @description: TODO
 * @date: 2020/7/21 16:34
 */
public interface HttpListener {
    void onSuccess(Document result);

    void onFailure(Throwable e);
}
