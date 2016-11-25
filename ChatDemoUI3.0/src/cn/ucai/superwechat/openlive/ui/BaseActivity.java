package cn.ucai.superwechat.openlive.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;


import java.util.Arrays;

import cn.ucai.superwechat.BuildConfig;
import cn.ucai.superwechat.Constant;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.openlive.model.ConstantApp;
import cn.ucai.superwechat.openlive.model.EngineConfig;
import cn.ucai.superwechat.openlive.model.MyEngineEventHandler;
import cn.ucai.superwechat.openlive.model.WorkerThread;
import cn.ucai.superwechat.utils.L;
import io.agora.rtc.RtcEngine;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final View layout = findViewById(Window.ID_ANDROID_CONTENT);
        ViewTreeObserver vto = layout.getViewTreeObserver();
        //ViewTreeObserver.OnGlobalLayoutListener接口：
        // 当在一个视图树中全局布局发生改变或者视图树中的某个视图的可视状态发生改变时，所要调用的回调函数的接口类
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                initUIandEvent();
            }
        });
    }

    protected abstract void initUIandEvent();

    protected abstract void deInitUIandEvent();

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

                boolean checkPermissionResult = checkSelfPermissions();

                if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.M)) {
                    // so far we do not use OnRequestPermissionsResultCallback
                }
            }
        }, 500);
    }

    private boolean checkSelfPermissions() {
        return checkSelfPermission(Manifest.permission.RECORD_AUDIO, ConstantApp.PERMISSION_REQ_ID_RECORD_AUDIO) &&
                checkSelfPermission(Manifest.permission.CAMERA, ConstantApp.PERMISSION_REQ_ID_CAMERA) &&
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, ConstantApp.PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE);
    }

    @Override
    protected void onDestroy() {
        deInitUIandEvent();
        super.onDestroy();
    }

    public final void closeIME(View v) {
        InputMethodManager mgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(v.getWindowToken(), 0); // 0 force close IME
        v.clearFocus();
    }

    public final void closeIMEWithoutFocus(View v) {
        InputMethodManager mgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(v.getWindowToken(), 0); // 0 force close IME
    }

    public void openIME(final EditText v) {
        final boolean focus = v.requestFocus();
        if (v.hasFocus()) {
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager mgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    boolean result = mgr.showSoftInput(v, InputMethodManager.SHOW_FORCED);
                    L.e("openIME " + focus + " " + result);
                }
            });
        }
    }

    public boolean checkSelfPermission(String permission, int requestCode) {
        L.e("checkSelfPermission " + permission + " " + requestCode);
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    requestCode);
            return false;
        }

        if (Manifest.permission.CAMERA.equals(permission)) {
            L.e("init workerThread....");
            SuperWeChatApplication.getInstance().initWorkerThread();
        }
        return true;
    }

    protected RtcEngine rtcEngine() {
        return ((SuperWeChatApplication) getApplication()).getWorkerThread().getRtcEngine();
    }

    protected final WorkerThread worker() {
        return ((SuperWeChatApplication) getApplication()).getWorkerThread();
    }

    protected final EngineConfig config() {
        return ((SuperWeChatApplication) getApplication()).getWorkerThread().getEngineConfig();
    }

    protected final MyEngineEventHandler event() {
        return ((SuperWeChatApplication) getApplication()).getWorkerThread().eventHandler();
    }

    public final void showLongToast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        L.e("onRequestPermissionsResult " + requestCode + " " + Arrays.toString(permissions) + " " + Arrays.toString(grantResults));
        switch (requestCode) {
            case ConstantApp.PERMISSION_REQ_ID_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkSelfPermission(Manifest.permission.CAMERA, ConstantApp.PERMISSION_REQ_ID_CAMERA);
                } else {
                    finish();
                }
                break;
            }
            case ConstantApp.PERMISSION_REQ_ID_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, ConstantApp.PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE);
                    ((SuperWeChatApplication) getApplication()).initWorkerThread();
                } else {
                    finish();
                }
                break;
            }
            case ConstantApp.PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    finish();
                }
                break;
            }
        }
    }

    protected void initVersionInfo() {
        String version = "V " + BuildConfig.VERSION_NAME + "(Build: " + BuildConfig.VERSION_CODE
                + ", " + ConstantApp.APP_BUILD_DATE + ", SDK: " + Constant.MEDIA_SDK_VERSION + ")";
//        TextView textVersion = (TextView) findViewById(R.id.app_version);
//        textVersion.setText(version);
    }
}
