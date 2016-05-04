package com.meituan.douya.v9utils;

import android.os.Bundle;

import com.meituan.douya.v9lib.activity.BaseWebViewActivity;

public class MainActivity extends BaseWebViewActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadUrl("http://www.baidu.com");
    }
}
