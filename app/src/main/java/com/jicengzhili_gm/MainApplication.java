package com.jicengzhili_gm;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import androidx.multidex.MultiDex;

import com.amap.api.location.AMapLocationClient;
import com.jicengzhili_gm.utils.CrashUtil;

import com.facebook.react.PackageList;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.soloader.SoLoader;
import com.jicengzhili_gm.AMapLocation.AMapLocationPackage;
import com.jicengzhili_gm.AMapLocation.mapView.ReactModulePackage;
import com.sangfor.sdk.SFMobileSecuritySDK;
import com.sangfor.sdk.base.SFConstants;
import com.sangfor.sdk.base.SFSDKFlags;
import com.sangfor.sdk.base.SFSDKMode;
import com.sangfor.sdk.base.SFSDKOptions;
import com.sangfor.sdk.utils.SFLogN;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class MainApplication extends Application implements ReactApplication {


  private final ReactNativeHost mReactNativeHost =
      new ReactNativeHost(this) {
        @Override
        public boolean getUseDeveloperSupport() {
          return BuildConfig.DEBUG;
        }

        @Override
        protected List<ReactPackage> getPackages() {
          @SuppressWarnings("UnnecessaryLocalVariable")
          List<ReactPackage> packages = new PackageList(this).getPackages();
          // Packages that cannot be autolinked yet can be added manually here, for example:
          // packages.add(new MyReactNativePackage());

            packages.add(new ReactModulePackage());
            packages.add(new AMapLocationPackage());
          return packages;
        }

        @Override
        protected String getJSMainModuleName() {
          return "index";
        }
      };

  @Override
  public ReactNativeHost getReactNativeHost() {
    return mReactNativeHost;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    
    CrashUtil.getInstance().init(this);
    updatePrivacyShow(this);

    SoLoader.init(this, /* native exopackage */ false);
    initializeFlipper(this, getReactNativeHost().getReactInstanceManager());

      /*
       *在客户端启动时检测Android版本，如果是Android4.2及以下版本则提示用户或者退出。
       * 防止WebView远程代码执行
       */
      if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1 ) {
          Toast.makeText(getApplicationContext(), "为防止WebView远程代码执行，请使用Android4.2以上版本运行此系统！", Toast.LENGTH_SHORT).show();
          android.os.Process.killProcess(android.os.Process.myPid());//杀死app
      }

  }


    public static void updatePrivacyShow(Context context){
        try{
            AMapLocationClient.updatePrivacyShow(context,true,true);
            AMapLocationClient.updatePrivacyAgree(context,true);
        }catch (Exception ex){
        }
    }


    /**
   * Loads Flipper in React Native templates. Call this in the onCreate method with something like
   * initializeFlipper(this, getReactNativeHost().getReactInstanceManager());
   *
   * @param context
   * @param reactInstanceManager
   */
  private static void initializeFlipper(
      Context context, ReactInstanceManager reactInstanceManager) {
    if (BuildConfig.DEBUG) {
      try {
        /*
         We use reflection here to pick up the class that initializes Flipper,
        since Flipper library is not available in release mode
        */
        Class<?> aClass = Class.forName("com.jicengzhili_gm.ReactNativeFlipper");
        aClass
            .getMethod("initializeFlipper", Context.class, ReactInstanceManager.class)
            .invoke(null, context, reactInstanceManager);
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    }
  }



   private static final String TAG = "MainApplication";
   private static SFSDKMode mSDKMode;

    @Override
   public void attachBaseContext(Context base) {
        MultiDex.install(base);
       super.attachBaseContext(base);


try {
       //只使用VPN功能场景
       SFSDKMode sdkMode = SFSDKMode.MODE_VPN;                 //表明启用VPN安全接入功能,详情参考集成指导文档
//        //只使用安全沙箱功能场景
//        SFSDKMode sdkMode = SFSDKMode.MODE_SANDBOX;             //表明启用安全沙箱功能,详情参考集成指导文档
//        //同时使用VPN功能+安全沙箱功能场景
//    SFSDKMode sdkMode = SFSDKMode.MODE_VPN_SANDBOX;         //表明同时启用VPN功能+安全沙箱功能,详情参考集成指导文档

       switch (sdkMode) {
           case MODE_VPN: {//只使用VPN功能场景
               int sdkFlags =  SFSDKFlags.FLAGS_HOST_APPLICATION;      //表明是单应用或者是主应用
               sdkFlags |= SFSDKFlags.FLAGS_VPN_MODE_TCP;              //表明使用VPN功能中的TCP模式

               SFMobileSecuritySDK.getInstance().initSDK(base, sdkMode, sdkFlags, null);//初始化SDK
               break;
           }
           case MODE_SANDBOX: {//只使用安全沙箱功能场景
               int sdkFlags =  SFSDKFlags.FLAGS_HOST_APPLICATION;      //表明是单应用或者是主应用
               sdkFlags |= SFSDKFlags.FLAGS_ENABLE_FILE_ISOLATION;     //表明启用安全沙箱功能中的文件隔离功能

               SFMobileSecuritySDK.getInstance().initSDK(base, sdkMode, sdkFlags, null);//初始化SDK
               break;
           }
           case MODE_VPN_SANDBOX: { //同时使用VPN功能+安全沙箱功能场景
               int sdkFlags =  SFSDKFlags.FLAGS_HOST_APPLICATION;      //表明是单应用或者是主应用
               sdkFlags |= SFSDKFlags.FLAGS_VPN_MODE_TCP;              //表明使用VPN功能中的TCP模式
               sdkFlags |= SFSDKFlags.FLAGS_ENABLE_FILE_ISOLATION;     //表明启用安全沙箱功能中的文件隔离功能

               SFMobileSecuritySDK.getInstance().initSDK(base, sdkMode, sdkFlags, null);//初始化SDK
               break;
           }
           default: {
               Toast.makeText(base, "SDK模式错误", Toast.LENGTH_LONG).show();
               return;
           }
       }

       //setAuthTimeout(30);//设置SDK网络超时时间
       mSDKMode = sdkMode; //保存使用的sdk模式
    } catch (Exception ex) {}

   }

   /**
    * @brief 是否使用VPN功能模式
    */
   public boolean isUseVpnMode() {
       if (mSDKMode == SFSDKMode.MODE_SANDBOX) {
           return false;
       }

       return true;
   }

   /**
    * @brief 是否使用安全沙箱功能模式
    */
   public boolean isUseSandboxMode() {
       if (mSDKMode == SFSDKMode.MODE_VPN) {
           return false;
       }

       return true;
   }

   /**
    * @brief 设置SDK网络超时时间，以秒为单位，不设置默认为30秒
    */
   void setAuthTimeout(int timeout) {
       JSONObject authTimeoutConfig = new JSONObject();
       try {
           //网络连接超时时间
           authTimeoutConfig.put(SFConstants.EXTRA_KEY_AUTH_CONNECT_TIMEOUT, timeout);
           //网络读取超时时间
           authTimeoutConfig.put(SFConstants.EXTRA_KEY_AUTH_READ_TIMEOUT, timeout);
       } catch (Exception ex) {
           ex.printStackTrace();
           SFLogN.error2(TAG, "make setAuthTimeout json failed!", ex.toString());
           return;
       }
       SFLogN.info(TAG, authTimeoutConfig.toString());
       //设置网络超时时间
       SFMobileSecuritySDK.getInstance().setSDKOptions(SFSDKOptions.OPTIONS_KEY_AUTH_TIMEOUT, authTimeoutConfig.toString());
   }


}
