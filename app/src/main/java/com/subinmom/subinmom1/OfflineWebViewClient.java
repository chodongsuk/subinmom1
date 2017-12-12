package com.subinmom.subinmom1;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by ajay.jadhao on 16/12/2015.
 */
public class OfflineWebViewClient extends WebViewClient {

    private Activity mActivity;

    public OfflineWebViewClient(Activity activity) {
        mActivity = activity;
    }

    public static final String INTENT_PROTOCOL_START = "intent:";
    public static final String INTENT_PROTOCOL_INTENT = "#Intent;";
    public static final String INTENT_PROTOCOL_END = ";end;";
    public static final String GOOGLE_PLAY_STORE_PREFIX = "market://details?id=";


    @Override
    public boolean shouldOverrideUrlLoading(WebView mWebView, String url) {
        if (url.contains("http://api.flexplatform.net")) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            mActivity.startActivity(intent);
            return true;
        } else if (url.startsWith("tel:")) {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(url));
            mActivity.startActivity(intent);
            return true;
        }else if (url.startsWith("mailto:")) {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
            mActivity.startActivity(intent);
            return true;
        }else if (url.startsWith("sms:")) {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
            mActivity.startActivity(intent);
            return true;
        }else if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("javascript:") ) {
            if (android.os.Build.VERSION.SDK_INT >= 19) {
                if (url.startsWith(INTENT_PROTOCOL_START)) {
                    final int customUrlStartIndex = INTENT_PROTOCOL_START.length();
                    final int customUrlEndIndex = url.indexOf(INTENT_PROTOCOL_INTENT);
                    if (customUrlEndIndex < 0) {
                        return false;
                    } else {
                        //intent 빼고 보내기.
                        final String customUrl = url.substring(customUrlStartIndex, customUrlEndIndex);
                        Log.i("TEST_customUrl", customUrl + "");
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            intent.setData(Uri.parse(customUrl));
                            mActivity.getBaseContext().startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            final int packageStartIndex = customUrlEndIndex+ INTENT_PROTOCOL_INTENT.length();
                            final int packageEndIndex = url.indexOf(INTENT_PROTOCOL_END);

                            final String packageName = url.substring(packageStartIndex,	packageEndIndex < 0 ? url.length()	: packageEndIndex);
                            intent.setData(Uri.parse(GOOGLE_PLAY_STORE_PREFIX	+ packageName));
                            mActivity.getBaseContext().startActivity( intent );
                        }
                        return true;
                    }
                }else{
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    mActivity.startActivity(intent);
                }
            }else{
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                mActivity.startActivity(intent);
            }
            return true;
        }else{
            mWebView.loadUrl(url);
        }
        return true;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        ((WebActivity)mActivity).showProgress();
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        ((WebActivity)mActivity).showWebView();
        ((WebActivity)mActivity).hideProgress();
    }
}
