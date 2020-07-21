package com.pengxh.autodingding.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.SocketTimeoutException;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @author: Pengxh
 * @email: 290677893@qq.com
 * @description: TODO
 * @date: 2020/7/21 16:34
 */
public class HttpHelper {

    private static Connection createConnection(String url) {
        return Jsoup.connect(url)
                .userAgent(obtainAgent())
                .timeout(30 * 1000)
                .ignoreHttpErrors(true);
    }

    private static String obtainAgent() {
        return Constant.UA[new Random().nextInt(15)];
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // 获取所有NetworkInfo对象
        NetworkInfo[] networkInfoArray = connectivityManager.getAllNetworkInfo();
        for (NetworkInfo info : networkInfoArray) {
            if (info.getState() == NetworkInfo.State.CONNECTED) {
                return true;// 存在可用的网络连接
            }
        }
        return false;
    }

    public static void getDocumentData(String link, HttpListener listener) {
        Observable.create((ObservableOnSubscribe<Document>) emitter -> {
            Connection connection = createConnection(link);
            int statusCode = connection.execute().statusCode();
            if (statusCode == 200) {
                emitter.onNext(connection.get());
            } else {
                emitter.onError(new SocketTimeoutException());
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Document>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Document result) {
                listener.onSuccess(result);
            }

            @Override
            public void onError(Throwable e) {
                listener.onFailure(e);
            }

            @Override
            public void onComplete() {

            }
        });
    }
}
