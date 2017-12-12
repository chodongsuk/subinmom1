package com.subinmom.subinmom1;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ajay.jadhao on 16/12/2015.
 */
public class MyWebChromeClient extends WebChromeClient {

    private final Activity mActivity;
    private ValueCallback<Uri> UploadMessage;
    public static final int INPUT_FILE_REQUEST_CODE = 1;
    public static final String EXTRA_FROM_NOTIFICATION = "EXTRA_FROM_NOTIFICATION";

    public ValueCallback<Uri[]> mFilePathCallback;
    public String mCameraPhotoPath;
    private static final String TAG = "filechooser";

    public MyWebChromeClient(Activity activity){
        mActivity = activity;
    }

    @Override
    public boolean onJsAlert(WebView view, String url,
                             String message, final JsResult result) {
        // TODO Auto-generated method stub
        new AlertDialog.Builder(mActivity)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok,
                        new AlertDialog.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog,
                                    int which) {
                                result.confirm();
                            }
                        }).setCancelable(false).create().show();
        return true;
    }

    @Override
    public boolean onCreateWindow(WebView view, boolean isDialog,
                                  boolean isUserGesture, Message resultMsg) {
        // TODO Auto-generated method stub
        view.removeAllViews();
        WebView newView = new WebView(mActivity);
        newView.setWebViewClient(new WebViewClient());
        // Create dynamically a new view
        newView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        view.addView(newView);
        WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
        transport.setWebView(newView);
        resultMsg.sendToTarget();
        return true;
    }

    @Override
    public void onReachedMaxAppCacheSize(long spaceNeeded, long totalUsedQuota,
                                         WebStorage.QuotaUpdater quotaUpdater)
    {
        quotaUpdater.updateQuota(spaceNeeded * 2);
    }


    // For Android < 3.0
    public void openFileChooser( ValueCallback<Uri> uploadMsg ){
        openFileChooser(uploadMsg, "", "");
    }
    // For Android 3.0+
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType ){
        openFileChooser(uploadMsg, "", "");
    }
    //For Android 4.1
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture){
        setUploadMessage(uploadMsg);
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        mActivity.startActivityForResult(Intent.createChooser(i, "File Chooser"), WebActivity.FILECHOOSER_RESULTCODE);
    }


    //For Android 5.0
    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                     FileChooserParams fileChooserParams) {
        if(mFilePathCallback != null) {
            mFilePathCallback.onReceiveValue(null);
        }
        mFilePathCallback = filePathCallback;

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 해당 ACTION 이 수행 될 수 있는 application 이 있는지 packageManager 로 부터 정보를 얻는다.
        if (takePictureIntent.resolveActivity(mActivity.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
                takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e(TAG, "Unable to create Image File", ex);
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            } else {
                takePictureIntent = null;
            }
        }

        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        contentSelectionIntent.setType("image/*");

        Intent[] intentArray;
        if(takePictureIntent != null) {
            intentArray = new Intent[]{takePictureIntent};
        } else {
            intentArray = new Intent[0];
        }

        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
        chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray); //여러개 묶어서 보내기
        mActivity.startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);

        return true;
    }


    public ValueCallback<Uri> getUploadMessage() {
        return UploadMessage;
    }

    public void setUploadMessage(ValueCallback<Uri> uploadMessage) {
        UploadMessage = uploadMessage;
    }

    //임시파일 생성.
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return imageFile;

    }
}
