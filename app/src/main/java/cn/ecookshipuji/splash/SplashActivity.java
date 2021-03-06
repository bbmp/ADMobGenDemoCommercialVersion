package cn.ecookshipuji.splash;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.admob.admobgensdk.ad.listener.ADMobGenSplashAdListener;
import cn.admob.admobgensdk.ad.splash.ADMobGenSplashView;
import cn.admob.admobgensdk.common.ADMobGenSDK;
import cn.ecookshipuji.MainActivity;
import cn.ecookshipuji.MyApplication;
import cn.ecookshipuji.R;

public class SplashActivity extends Activity implements ADMobGenSplashAdListener {
    /**
     * 根据实际情况申请
     */
    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_PHONE_STATE
            , Manifest.permission.WRITE_EXTERNAL_STORAGE
            , Manifest.permission.ACCESS_NETWORK_STATE
            , Manifest.permission.ACCESS_WIFI_STATE
            , Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private boolean needJumpMain = false;
    private boolean readyJump = false;
    private List<String> permissionList = new ArrayList<>();
    private static final String TAG = "ADMobGen_Log";
    private ADMobGenSplashView adMobGenSplashView;
    private FrameLayout flContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 全屏
        ADMobGenSDK.instance().fullScreen(this);
        setContentView(R.layout.activity_splash);

        // 6.0及以上获取没有申请的权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : PERMISSIONS) {
                int checkSelfPermission = ContextCompat.checkSelfPermission(this, permission);
                if (PackageManager.PERMISSION_GRANTED == checkSelfPermission) {
                    continue;
                }
                permissionList.add(permission);
            }
        }

        flContainer = (FrameLayout) findViewById(R.id.flContainer);

        initAd();
    }

    private void initAd() {
        // 开屏广告高度默认为屏幕高度的75%,可自定义高度比例,但不能低于0.75;
        // 第三个参数是广告位序号（默认为0，用于支持单样式多广告位，无需要可以填0或者使用其他构造方法）
        adMobGenSplashView = new ADMobGenSplashView(this, 0.85f, MyApplication.adIndex);
        // 设置是否沉浸式（跳过按钮topMargin会加上状态栏高度），不设置默认false
        adMobGenSplashView.setImmersive(false);
        // 设置开屏广告监听
        adMobGenSplashView.setListener(this);
        flContainer.addView(adMobGenSplashView);
        // 存在未申请的权限则先申请
        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), 1);
        } else {
            // 加载开屏广告
            adMobGenSplashView.loadAd();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            // 加载开屏广告
            adMobGenSplashView.loadAd();
        }
    }

    @Override
    public void onADExposure() {
        Log.e(TAG, "广告展示曝光回调，但不一定是曝光成功了，比如一些网络问题导致上报失败 ::::: ");
    }

    @Override
    public void onADReceiv() {
        Log.e(TAG, "广告获取成功了 ::::: ");
    }

    @Override
    public void onADFailed(String error) {
        Log.e(TAG, "广告获取失败了 ::::: " + error);
        Toast.makeText(SplashActivity.this, error + "", Toast.LENGTH_SHORT).show();
        jumpMain();
    }

    @Override
    public void onADClick() {
        Log.e(TAG, "广告被点击了 ::::: ");
        readyJump = true;
    }

    @Override
    public void onAdClose() {
        readyJump = true;
        Log.e(TAG, "广告被关闭了 ::::: ");
        checkJump();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume:::::: ");
        needJumpMain = true;
        checkJump();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause:::::: ");
        needJumpMain = false;
    }

    @Override
    protected void onDestroy() {
        if (adMobGenSplashView != null) {
            adMobGenSplashView.destroy();
            adMobGenSplashView = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 防止用户返回键退出APP影响曝光
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 检查是否满足跳转的条件
     */
    private void checkJump() {
        if (needJumpMain && readyJump) {
            jumpMain();
        }
    }

    /**
     * 跳转到主界面
     */
    public void jumpMain() {
        needJumpMain = false;
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
