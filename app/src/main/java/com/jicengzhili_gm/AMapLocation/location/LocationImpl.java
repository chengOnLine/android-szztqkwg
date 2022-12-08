package com.jicengzhili_gm.AMapLocation.location;


import android.content.Context;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.AMapLocationQualityReport;


public class LocationImpl implements ILocation {

    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;

    public LocationImpl(Context context, AMapLocationListener locationListener) {
        initLocation(context, locationListener);
    }

    @Override
    public void startLocation() {
        resetOption();
        // 设置定位参数
        locationClient.setLocationOption(locationOption);
        // 启动定位
        locationClient.startLocation();
    }

    @Override
    public void stopLocation() {
        // 停止定位
        locationClient.stopLocation();
    }

    @Override
    public void destroyLocation() {
        if (null != locationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            locationClient.onDestroy();
            locationClient = null;
            locationOption = null;
        }
    }

    private void initLocation(Context context, AMapLocationListener locationListener) {
        //初始化client
        try {
            locationClient = new AMapLocationClient(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        locationOption = getDefaultOption();
        //设置定位参数
        locationClient.setLocationOption(locationOption);
        // 设置定位监听
        locationClient.setLocationListener(locationListener);
    }

    /**
     * 默认的定位参数
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private AMapLocationClientOption getDefaultOption() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(2000);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
        return mOption;
    }

    @Override
    public void setLocationCacheEnable(boolean locationCacheEnable) {
        locationOption.setLocationCacheEnable(locationCacheEnable);
    }

    @Override
    public void setWifiScan(boolean wifiScan) {
        locationOption.setWifiScan(wifiScan);
    }

    @Override
    public void setSensorEnable(boolean sensorEnable) {
        locationOption.setSensorEnable(sensorEnable);
    }

    @Override
    public void setOnceLocationLatest(boolean onceLocationLatest) {
        locationOption.setOnceLocationLatest(onceLocationLatest);
    }

    @Override
    public void setOnceLocation(boolean onceLoction) {
        locationOption.setOnceLocation(onceLoction);
    }

    /**
     * 是否需要地址
     *
     * @param needAddress
     */
    @Override
    public void setNeedAddress(boolean needAddress) {
        locationOption.setNeedAddress(needAddress);
    }

    /**
     * 设置定位间隔
     *
     * @param interval
     */
    @Override
    public void setInterval(long interval) {
        locationOption.setInterval(interval);
    }

    @Override
    public void setHttpTimeOut(int timeOut) {
        locationOption.setHttpTimeOut(timeOut);
    }

    @Override
    public void setGpsFirst(boolean isFiest) {
        locationOption.setGpsFirst(isFiest);
    }

    /**
     * 获取GPS状态的字符串
     *
     * @param statusCode GPS状态码
     * @return
     */

    @Override
    public String getGPSStatusString(int statusCode) {
        String str = "";
        switch (statusCode) {
            case AMapLocationQualityReport.GPS_STATUS_OK:
                str = "GPS状态正常";
                break;
            case AMapLocationQualityReport.GPS_STATUS_NOGPSPROVIDER:
                str = "手机中没有GPS Provider，无法进行GPS定位";
                break;
            case AMapLocationQualityReport.GPS_STATUS_OFF:
                str = "GPS关闭，建议开启GPS，提高定位质量";
                break;
            case AMapLocationQualityReport.GPS_STATUS_MODE_SAVING:
                str = "选择的定位模式中不包含GPS定位，建议选择包含GPS定位的模式，提高定位质量";
                break;
            case AMapLocationQualityReport.GPS_STATUS_NOGPSPERMISSION:
                str = "没有GPS定位权限，建议开启gps定位权限";
                break;
        }
        return str;
    }

    private void resetOption() {

    }
}
