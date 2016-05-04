package com.meituan.douya.v9lib.activity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.meituan.douya.v9lib.R;


/**
 * Created by duhangyu on 15/12/23.
 */
public class BaseWebViewActivity extends BaseActivity {

    protected WebView mWebView;
    private ImageView mMaskView;
    protected ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_webview);
        initWebView();
    }

    private void initWebView() {
        mMaskView = (ImageView) findViewById(R.id.web_view_mask);
        mWebView = (WebView) findViewById(R.id.web_view);
        mWebView.setHorizontalScrollBarEnabled(false);
        try {
            mWebView.getSettings().setJavaScriptEnabled(true);
        } catch (Throwable e) {
        }
        mWebView.getSettings().setAppCacheEnabled(true);
        mWebView.getSettings().setDatabaseEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setGeolocationEnabled(true);
        mWebView.setWebChromeClient(new InternalWebChromeClient());
        mWebView.setWebViewClient(new InternalWebViewClient());
        mWebView.setDownloadListener(new InternalDownloadListener());
        initDefaultProgressBar();
    }

    private void initDefaultProgressBar() {
        if (mProgressBar == null) {
            mProgressBar = new ProgressBar(this);
        }
    }

    protected void loadUrl(final String url) {
        if (TextUtils.isEmpty(url) || mWebView == null) {
            return;
        }
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing() && mWebView != null) {
                    mWebView.loadUrl(url);
                }
            }
        });
    }

    protected void updateTitle(String title) {
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.mProgressBar = progressBar;
    }

    protected boolean shouldOverrideUrlLoading(WebView view, String url) {
        return false;
    }

    protected void onPageStarted(WebView view, String url, Bitmap favicon) {
    }

    protected void onPageFinished(WebView view, String url) {
    }

    protected void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
    }

    protected void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        handler.cancel();
    }

    private boolean handleBackClick() {
        if (mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (handleBackClick()) {
            return;
        }
        super.onBackPressed();
    }

    protected void hideProgress() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    protected WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        return null;
    }

    private class InternalDownloadListener implements DownloadListener {

        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }


    private class InternalWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            boolean isLocalHandled = BaseWebViewActivity.this.shouldOverrideUrlLoading(view, url);
            if (isLocalHandled) {
                return true;
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (mProgressBar != null) {
                mProgressBar.setVisibility(View.VISIBLE);
            }
            BaseWebViewActivity.this.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            hideProgress();
            BaseWebViewActivity.this.onPageFinished(view, url);
        }


        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            BaseWebViewActivity.this.onReceivedError(view, errorCode, description, failingUrl);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            BaseWebViewActivity.this.onReceivedSslError(view, handler, error);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            return BaseWebViewActivity.this.shouldInterceptRequest(view, url);

        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            return BaseWebViewActivity.this.shouldInterceptRequest(view, request.getUrl().toString());
        }

        @Override
        public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
            super.doUpdateVisitedHistory(view, url, isReload);
            updateTitle(view.getTitle());
        }
    }


    private class InternalWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                hideProgress();
                return;
            }
            if (mProgressBar != null) {
                mProgressBar.setProgress(newProgress);
            }
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            updateTitle(title);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message,
                                 final JsResult result) {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setTitle(R.string.dialog_title_tips);
            builder.setMessage(message);
            builder.setPositiveButton(android.R.string.ok,
                    new AlertDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            result.confirm();
                        }
                    }
            );
            builder.setCancelable(false);
            builder.create();
            try {
                builder.show();
            } catch (Exception ex) {
            }
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message,
                                   final JsResult result) {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setTitle(R.string.dialog_title_tips);
            builder.setMessage(message);
            builder.setPositiveButton(android.R.string.ok,
                    new AlertDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            result.confirm();
                        }
                    }
            );
            builder.setNeutralButton(android.R.string.cancel,
                    new AlertDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            result.cancel();
                        }
                    }
            );
            builder.setCancelable(false);
            builder.create();
            try {
                builder.show();
            } catch (Exception ex) {
            }
            return true;

        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message,
                                  String defaultValue, final JsPromptResult result) {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setTitle(R.string.dialog_title_tips);
            builder.setMessage(message);
            final EditText editText = new EditText(view.getContext());
            editText.setText(defaultValue);
            builder.setView(editText);
            builder.setPositiveButton(android.R.string.ok,
                    new AlertDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            result.confirm(editText.getText().toString());
                        }
                    }
            );
            builder.setNeutralButton(android.R.string.cancel,
                    new AlertDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            result.cancel();
                        }
                    }
            );
            builder.setCancelable(false);
            builder.create();
            try {
                builder.show();
            } catch (Exception ex) {
            }
            return true;
        }
    }
}
