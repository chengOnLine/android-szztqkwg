package com.jicengzhili_gm.AMapLocation.location;

/**
 * 定位对外公开的方法
 */
public interface ILocation {
    /**
     * 开始定位
     */
    public void startLocation();

    /**
     * 停止定位
     */
    public void stopLocation();

    /**
     * 销毁定位
     */
    public void destroyLocation();

    /**
     * 获取最近3s内精度最高的一次定位结果
     *
     * @param locationCacheEnable
     */
    public void setLocationCacheEnable(boolean locationCacheEnable);

    /**
     * 设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
     *
     * @param wifiScan
     */
    public void setWifiScan(boolean wifiScan);

    /**
     * 设置是否使用传感器
     *
     * @param sensorEnable
     */
    public void setSensorEnable(boolean sensorEnable);

    /**
     * 设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
     *
     * @param onceLocationLatest
     */
    public void setOnceLocationLatest(boolean onceLocationLatest);

    /**
     * 设置是否单次定位
     *
     * @param onceLoction
     */
    public void setOnceLocation(boolean onceLoction);

    /**
     * 设置是否返回逆地理地址信息
     *
     * @param needAddress
     */
    public void setNeedAddress(boolean needAddress);

    /**
     * 设置定位间隔。默认为2秒
     *
     * @param interval
     */
    public void setInterval(long interval);

    /**
     * 设置网络请求超时时间。默认为30秒。在仅设备模式下无效
     *
     * @param timeOut
     */
    public void setHttpTimeOut(int timeOut);

    /**
     * 设置是否gps优先，只在高精度模式下有效。默认关闭
     *
     * @param isFiest
     */
    public void setGpsFirst(boolean isFiest);

    public String getGPSStatusString(int statusCode);

}