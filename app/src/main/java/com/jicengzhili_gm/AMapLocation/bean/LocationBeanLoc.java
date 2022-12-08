package com.jicengzhili_gm.AMapLocation.bean;

import com.amap.api.location.AMapLocation;

public class LocationBeanLoc {
    private double latitude;// 纬度 获取纬度
    private double longitude; // 经度 获取经度
    private float accuracy; // 精度 获取定位精度 单位:米
    private double altitude; // 海拔 获取海拔高度信息
    private float speed; // 速度 单位：米/秒
    private float bearing; // 方向角 获取方向角信息
    private String buildingId; // 室内定位建筑物Id 获取室内定位建筑物Id V3.2.0版本起
    private String floor; // 室内定位楼层 获取室内定位楼层 V3.2.0版本起
    private String address; // 地址描述 获取地址描述 模式为仅设备模式(Device_Sensors)时无此信息
    private String country; // 国家 获取国家名称 模式为仅设备模式(Device_Sensors)时无此信息
    private String province; // 省 获取省名称 模式为仅设备模式(Device_Sensors)时无此信息
    private String city; // 城市 获取城市名称 模式为仅设备模式(Device_Sensors)时无此信息
    private String district; // 城区 获取城区名称 模式为仅设备模式(Device_Sensors)时无此信息
    private String street; // 街道 获取街道名称 V2.3.0版本起 模式为仅设备模式(Device_Sensors)时无此信息
    private String streetNum; // 街道门牌号 获取街道门牌号信息 V2.3.0版本起 模式为仅设备模式(Device_Sensors)时无此信息
    private String cityCode; // 城市编码 获取城市编码信息 模式为仅设备模式(Device_Sensors)时无此信息
    private String adCode; // 区域编码 获取区域编码信息 模式为仅设备模式(Device_Sensors)时无此信息
    private String poiName; // 当前位置POI名称 获取当前位置的POI名称 模式为仅设备模式(Device_Sensors)时无此信息
    private String aoiName; // 当前位置所处AOI名称 获取当前位置所处AOI名称 V2.4.0版本起 模式为仅设备模式(Device_Sensors)时无此信息
    private int gpsStatus; // 设备当前 GPS 状态 获取GPS当前状态，返回值可参考AMapLocation类提供的常量 V3.1.0版本起 模式为仅设备模式(Device_Sensors)时提供此信息
    private int locationType; // 定位来源 获取定位结果来源 可参考定位类型编码表
    private String locationDetail; // 定位信息描述 定位信息描述 用于问题排查
//    private String errorInfo; // 定位错误信息描述 定位出现异常的描述 可参考定位错误码表


    public static final LocationBeanLoc gdLocationConver(AMapLocation amapLocation) {
        LocationBeanLoc bean = new LocationBeanLoc();
        // 属性名称参见 http://lbs.amap.com/api/android-location-sdk/guide/android-location/getlocation
        bean.latitude = amapLocation.getLatitude();
        bean.longitude = amapLocation.getLongitude();
        bean.accuracy = amapLocation.getAccuracy();
        bean.altitude = amapLocation.getAltitude();
        bean.speed = amapLocation.getSpeed();
        bean.bearing = amapLocation.getBearing();
        bean.buildingId = amapLocation.getBuildingId();
        bean.floor = amapLocation.getFloor();
        bean.address = amapLocation.getAddress();
        bean.country = amapLocation.getCountry();
        bean.province = amapLocation.getProvince();
        bean.city = amapLocation.getCity();
        bean.district = amapLocation.getDistrict();
        bean.street = amapLocation.getStreet();
        bean.streetNum = amapLocation.getStreetNum();
        bean.cityCode = amapLocation.getCityCode();
        bean.adCode = amapLocation.getAdCode();
        bean.poiName = amapLocation.getPoiName();
        bean.aoiName = amapLocation.getAoiName();
        bean.gpsStatus = amapLocation.getGpsAccuracyStatus();
        bean.locationType = amapLocation.getLocationType();
        bean.locationDetail = amapLocation.getLocationDetail();
//        bean.errorInfo = amapLocation.getErrorInfo();

        return bean;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public String getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(String buildingId) {
        this.buildingId = buildingId;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getStreetNum() {
        return streetNum;
    }

    public void setStreetNum(String streetNum) {
        this.streetNum = streetNum;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getAdCode() {
        return adCode;
    }

    public void setAdCode(String adCode) {
        this.adCode = adCode;
    }

    public String getPoiName() {
        return poiName;
    }

    public void setPoiName(String poiName) {
        this.poiName = poiName;
    }

    public String getAoiName() {
        return aoiName;
    }

    public void setAoiName(String aoiName) {
        this.aoiName = aoiName;
    }

    public int getGpsStatus() {
        return gpsStatus;
    }

    public void setGpsStatus(int gpsStatus) {
        this.gpsStatus = gpsStatus;
    }

    public int getLocationType() {
        return locationType;
    }

    public void setLocationType(int locationType) {
        this.locationType = locationType;
    }

    public String getLocationDetail() {
        return locationDetail;
    }

    public void setLocationDetail(String locationDetail) {
        this.locationDetail = locationDetail;
    }
}

