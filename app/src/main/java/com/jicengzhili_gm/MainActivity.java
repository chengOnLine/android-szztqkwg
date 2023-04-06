package com.jicengzhili_gm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;
import android.Manifest;

import androidx.core.app.ActivityCompat;

import com.amap.api.location.AMapLocationClient;
import com.facebook.react.ReactActivity;
import com.facebook.react.bridge.Promise;
import com.facebook.react.modules.core.PermissionAwareActivity;
import com.facebook.react.modules.core.PermissionListener;
import com.jicengzhili_gm.utils.AntiHijackingUtil;

import org.devio.rn.splashscreen.SplashScreen;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class MainActivity extends ReactActivity {

  boolean needAlarm;
  //如果没有这个方法的话可以把整一个方法都复制进去
      protected String[] needPermissions = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.INSTANT_APP_FOREGROUND_SERVICE
    };


  @Override
  protected void onCreate(Bundle savedInstanceState) {
      SplashScreen.show(this);  // <--添加这一句

      super.onCreate(savedInstanceState);

      updatePrivacyShow(this);

      permissionsCheck(this, null, needPermissions, new Callable<Void>() {
          @Override
          public Void call() throws Exception {
              try {
                  ;
              } catch (Exception ex) {
                  ex.printStackTrace();
              }
              return null;
          }
      });
  }

    public static void updatePrivacyShow(Context context){
        try{
            AMapLocationClient.updatePrivacyShow(context,true,true);
            AMapLocationClient.updatePrivacyAgree(context,true);
        }catch (Exception ex){
        }
    }

  
    private void permissionsCheck(final Activity activity, final Promise promise, final String[] requiredPermissions, final Callable<Void> callback) {

        List<String> missingPermissions = new ArrayList<>();
        for (String permission : requiredPermissions) {
            int status = ActivityCompat.checkSelfPermission(activity, permission);
            if (status != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }

        if (!missingPermissions.isEmpty()) {

            ((PermissionAwareActivity) activity).requestPermissions(missingPermissions.toArray(new String[missingPermissions.size()]), 1, new PermissionListener() {

                @Override
                public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
                    if (requestCode == 1) {

                        for (int grantResult : grantResults) {
                            if (grantResult == PackageManager.PERMISSION_DENIED) {
                                promise.reject("E_PERMISSION_MISSING", "Required permission missing");
                                return true;
                            }
                        }

                        try {
                            callback.call();
                        } catch (Exception e) {
                            promise.reject("E_CALLBACK_ERROR", "Unknown error", e);
                        }
                    }

                    return true;
                }
            });

            return;
        }

        // all permissions granted
        try {
            callback.call();
        } catch (Exception e) {
            promise.reject("E_CALLBACK_ERROR", "Unknown error", e);
        }
    }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    //判断程序进入后台是否是用户自身造成的（触摸返回键或HOME键），是则无需弹出警示。
    if((keyCode== KeyEvent.KEYCODE_BACK || keyCode==KeyEvent.KEYCODE_HOME) && event.getRepeatCount()==0){
      needAlarm = false;
    }
    return super.onKeyDown(keyCode, event);
  }

  @Override
  protected void onPause() {

    /*
     *防止apk反编译的技术4---对抗JD-GUI查看源码
     *对抗JD-GUI查看源码的方法：用JD-GUI查看源码时，有些函数根本看不到源码，直接提示error错误，我们就利用这点来保护我们的apk
     */
    switch (0) {
      case 1:
        JSONObject jsoObj;
        String date = null;
        String second = null;
        try {
          jsoObj = new JSONObject();
          date = jsoObj.getString("date");
          second = jsoObj.getString("second");
        } catch (JSONException e) {
          e.printStackTrace();
        }
        JDGUICombat(date, second);
        break;
    }

    /**********************************************************************************结束******************************************************/
    //若程序进入后台不是用户自身造成的，则需要弹出警示
//    boolean safe = AntiHijackingUtil.checkActivity(this);
//    Toast.makeText(getApplicationContext(), "您的app界面被覆盖，请确认当前使用环境是否安全", Toast.LENGTH_SHORT).show();

    //启动我们的AlarmService,用于给出覆盖了正常Activity的类名
//    Intent intent = new Intent(this, AlarmService.class);
//    startService(intent);
    super.onPause();
  }

  public void JDGUICombat(String a,String b){}

  /**
   * Returns the name of the main component registered from JavaScript. This is used to schedule
   * rendering of the component.
   */
  @Override
  protected String getMainComponentName() {
    return "JiCengZhiLi_GM";
  }
}
