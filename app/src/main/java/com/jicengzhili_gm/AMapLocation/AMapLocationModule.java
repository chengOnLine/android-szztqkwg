package com.jicengzhili_gm.AMapLocation;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.JSApplicationIllegalArgumentException;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.jicengzhili_gm.AMapLocation.base.ErrorCode;
import com.jicengzhili_gm.AMapLocation.base.ResponseMsg;
import com.jicengzhili_gm.AMapLocation.bean.LocationBeanLoc;
import com.jicengzhili_gm.AMapLocation.db.SharedPreferencesUtils;
import com.jicengzhili_gm.AMapLocation.location.ILocation;
import com.jicengzhili_gm.AMapLocation.location.LocationImpl;
import com.jicengzhili_gm.AMapLocation.util.Constant;
import com.jicengzhili_gm.AMapLocation.util.GsonUtil;
import com.jicengzhili_gm.AMapLocation.util.JsonUtils;
import com.jicengzhili_gm.AMapLocation.util.Utils;
import com.yintao.util.TokenGenerator;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

import java.util.ArrayList;


public class AMapLocationModule extends ReactContextBaseJavaModule  {

    private ILocation location;
    private Callback locationCallback = null;


    public AMapLocationModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    /**
     * 定位监听
     */
    AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation location) {
            ResponseMsg msg = ResponseMsg.create();
            if (null != location) {
                if (location.getErrorCode() == 0) {
                    //可在其中解析amapLocation获取相应内容。
                    LocationBeanLoc bean = LocationBeanLoc.gdLocationConver(location);
                    msg.setCode(ErrorCode.SUCCESS);
                    msg.setMsg("定位数据返回成功");
                    msg.setData(bean);
//                    Log.e("AmapError","location SUCCESS:"+ bean);
                }else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    msg.setCode(location.getErrorCode());
                    msg.setMsg("定位失败");
                    msg.setData(location.getErrorInfo());
                }

            } else {
                msg.setMsg("定位失败");
                msg.setCode(1);
                msg.setData(new Object());
            }
            try {
                if (locationCallback != null) {
                    String result = GsonUtil.GsonString(msg);
                    Log.e("alen", "返回定位信息：" + result);
                    locationCallback.invoke(result);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            locationCallback = null;
        }
    };

    @ReactMethod
    public void initLoaction() {
        // 初始化，
        if (location == null) {
            try {
                location = new LocationImpl(getReactApplicationContext(), locationListener);
                //location.setLocationOption(getDefaultOption());
            } catch (Exception e) {
            }
        }
    }


    @ReactMethod
    public void startActivityFormJS(String token,boolean isSign,String signId,String signTime) {
        String name="com.jicengzhili_gm.AMapLocation.activity.AMapActivity";

        try {
            if(!isSign){
                Constant.qiandaoTime =Utils.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss");
            }else{
                Constant.qiandaoTime = signTime;
            }

            //初始化变量值
            Constant.isQiandao = isSign;
            Constant.signId = signId;
            Constant.token = token;

            Log.e("alen", "isQiandao: "+ Constant.isQiandao);
            Log.e("alen", "signId: "+ Constant.signId);
            Log.e("alen", "qiandaoTime: "+ Constant.qiandaoTime);

            SharedPreferencesUtils.setParam(getReactApplicationContext(), "qiandaoTime", Constant.qiandaoTime);
            SharedPreferencesUtils.setParam(getReactApplicationContext(), "signId", Constant.signId);
            SharedPreferencesUtils.setParam(getReactApplicationContext(), "qianDao", Constant.isQiandao);

            Activity currentActivity = getCurrentActivity();
            if (null != currentActivity) {
                Class toActivity = Class.forName(name);
                Intent intent = new Intent(currentActivity, toActivity);
                currentActivity.startActivity(intent);
            }

        } catch (Exception e) {
            throw new JSApplicationIllegalArgumentException("不能打开Activity : " + e.getMessage());
        }
    }

    @ReactMethod
    public void endActivityFormJS(){
        Constant.isQiandao = false;
        Constant.signId = "";
        SharedPreferencesUtils.setParam(getReactApplicationContext(), "qianDao", false);
        SharedPreferencesUtils.setParam(getReactApplicationContext(), "signId", "");
    }

    @ReactMethod
    public void startHistoryMapFromJS(String latlngs){
        String name="com.jicengzhili_gm.AMapLocation.activity.HistoryMapActivity";
        try{
            SharedPreferencesUtils.setParam(getReactApplicationContext(), "latlngs", latlngs);

            Activity currentActivity = getCurrentActivity();
            if(null!=currentActivity){
                Class toActivity = Class.forName(name);
                Intent intent = new Intent(currentActivity,toActivity);
                currentActivity.startActivity(intent);
            }

        }catch(Exception e){
            throw new JSApplicationIllegalArgumentException(
                    "不能打开Activity : "+e.getMessage());
        }
    }


    @ReactMethod
    public void openNavMap(String pkgName,String destLat, String destLng) {

        String PACKAGE_NAME_MINI_MAP = "com.autonavi.minimap";
        try {

            String tmpName = pkgName.trim();
            if (tmpName.equals(PACKAGE_NAME_MINI_MAP)) {
                // http://lbs.amap.com/api/amap-mobile/guide/android/navigation
                StringBuffer scheme = new StringBuffer("androidamap://navi?sourceApplication=").append(this.getName());

                scheme.append("&lat=").append(destLat).append("&lon=").append(destLng).append("&dev=").append(0)
                        .append("&style=").append(0);

                Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(scheme.toString()));
                intent.setPackage("com.autonavi.minimap");
                this.getCurrentActivity().startActivity(intent);
            }
        } catch (Exception e) {
        }
    }


    @ReactMethod
    public void startLocation(Callback callback) {
        try {
            locationCallback = callback;
            location.startLocation();
        } catch (Exception e) {
        }
    }

    
    //生成token
    @ReactMethod
    public void generateToken(Promise promise) {
        try{
            String tokenId="token";
            String tokenKey="SSS_form@2018";

            String token= TokenGenerator.generateToken(tokenId,tokenKey);
            WritableMap map= Arguments.createMap();
            map.putString("token", token);

            promise.resolve(map);
        } catch (Exception e) {
            promise.reject("2",e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return "AMapLocationModule";
    }
}
