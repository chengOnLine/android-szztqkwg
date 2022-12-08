package com.jicengzhili_gm.AMapLocation.util;

import android.content.Context;
import android.location.LocationManager;
import android.text.TextUtils;

import com.amap.api.location.AMapLocation;

import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 辅助工具类
 * @文件名称: Utils.java
 * @类型名称: Utils
 */
public class Utils {
    /**
     *  开始定位
     */
    public final static int MSG_LOCATION_START = 0;
    /**
     * 定位完成
     */
    public final static int MSG_LOCATION_FINISH = 1;
    /**
     * 停止定位
     */
    public final static int MSG_LOCATION_STOP= 2;


    /***
     * 上传定位信息
     */
    public final static int MSG_LOCATION_UPLOAD= 3;


    /***
     * 重发上传失败的定位信息
     */
    public final static int MSG_LOCATION_REUPLOAD= 4;


    /***
     * 后台更新在线状态
     */
    public final static int MSG_UPDATE_ONLINE_STATE= 5;


    public final static int MSG_UPDATE_QIANTUI_SUCCESS= 6;
    public final static int MSG_UPDATE_QIANTUI_FAIL= 7;


    public final static String KEY_URL = "URL";

    private static final int DEFAULT_TIME = 2000;
    private static long lastTime;

    /**
     * 根据定位结果返回定位信息的字符串
     * @param location
     * @return
     */
    public synchronized static String getLocationStr(AMapLocation location){
        if(null == location){
            return null;
        }
        StringBuffer sb = new StringBuffer();
        //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
        if(location.getErrorCode() == 0){
            sb.append("定位成功" + "\n");
            sb.append("定位类型: " + location.getLocationType() + "\n");
            sb.append("经    度    : " + location.getLongitude() + "\n");
            sb.append("纬    度    : " + location.getLatitude() + "\n");
            sb.append("精    度    : " + location.getAccuracy() + "米" + "\n");
            sb.append("提供者    : " + location.getProvider() + "\n");
            sb.append("速    度    : " + location.getSpeed() + "米/秒" + "\n");
            sb.append("角    度    : " + location.getBearing() + "\n");
            // 获取当前提供定位服务的卫星个数
            sb.append("星    数    : " + location.getSatellites() + "\n");
//			sb.append("国    家    : " + location.getCountry() + "\n");
//			sb.append("省            : " + location.getProvince() + "\n");
//			sb.append("市            : " + location.getCity() + "\n");
//			sb.append("城市编码 : " + location.getCityCode() + "\n");
//			sb.append("区            : " + location.getDistrict() + "\n");
//			sb.append("区域 码   : " + location.getAdCode() + "\n");
            sb.append("地    址    : " + location.getAddress() + "\n");
//			sb.append("兴趣点    : " + location.getPoiName() + "\n");
            //定位完成的时间
            sb.append("定位时间: " + formatUTC(location.getTime(), "yyyy-MM-dd HH:mm:ss") + "\n");
        } else {
            //定位失败
            sb.append("定位失败" + "\n");
            sb.append("错误码:" + location.getErrorCode() + "\n");
            sb.append("错误信息:" + location.getErrorInfo() + "\n");
            sb.append("错误描述:" + location.getLocationDetail() + "\n");
        }
        //定位之后的回调时间
        sb.append("回调时间: " + formatUTC(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss") + "\n");
        return sb.toString();
    }

    private static SimpleDateFormat sdf = null;
    public  static String formatUTC(long l, String strPattern) {
        if (TextUtils.isEmpty(strPattern)) {
            strPattern = "yyyy-MM-dd HH:mm:ss";
        }
        if (sdf == null) {
            try {
                sdf = new SimpleDateFormat(strPattern, Locale.CHINA);
            } catch (Throwable e) {
            }
        } else {
            sdf.applyPattern(strPattern);
        }
        return sdf == null ? "NULL" : sdf.format(l);
    }


    static SimpleDateFormat sDateFormat = new SimpleDateFormat(
            "yyyy-MM-dd hh:mm:ss");

    /***
     * 保存LOG到本地
     *
     * @param strlog
     */
    public static void write_log(String strlog) {
        if (Constant.IS_SAVE_LOG) {
            String date = sDateFormat.format(new java.util.Date());
            try {
                strlog = "\r\n" + date + "--->" + strlog + "\n\r";
                String fileName = "/sdcard/JwdLocation.txt";

                File file = new File(fileName);
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileWriter writer = new FileWriter(fileName, true);
                writer.write(strlog);
                writer.close();

            } catch (Exception e) {
            }
        }
    }

    /**
     *
     * @Author Alen
     * @Description 获取手机品牌
     * @Time 2018-1-5下午1:15:02
     */
    public static String getMobileBrand() {
        String company = android.os.Build.MANUFACTURER;// 厂商
        return company;
    }


    /***
     * 米转换成公里数
     * @param lenMeter
     * @return
     */
    public static String getFriendlyLength(int lenMeter) {
//		if (lenMeter > 10000) // 10 km
//		{
//			int dis = lenMeter / 1000;
//			return dis + "公里";
//		}
//
//		if (lenMeter > 1000) {
//			float dis = (float) lenMeter / 1000;
//			DecimalFormat fnum = new DecimalFormat("##0.0");
//			String dstr = fnum.format(dis);
//			return dstr + "公里";
//		}
//
//		if (lenMeter > 100) {
//			int dis = lenMeter / 50 * 50;
//			return dis + "米";
//		}
//
//		int dis = lenMeter / 10 * 10;
//		if (dis == 0) {
//			dis = 10;
//		}

//		return dis + "米";
        float dis = (float) lenMeter / 1000;
        DecimalFormat fnum = new DecimalFormat("##0.00");
        String dstr = fnum.format(dis);
        return dstr + "公里";

    }


    /**
     *
     * @Author Alen
     * @Description 判断是否开启GPS定位
     */
    public static boolean openGPSSettings(Context mContext) {

        LocationManager alm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        if (alm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return true;
        }
        return false;
    }

    /***
     * 判断字符串是否为数字
     *
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    public static boolean isSingle(){
        boolean isSingle ;
        long currentTime = System.currentTimeMillis();
        if(currentTime - lastTime <= DEFAULT_TIME){
            isSingle = true;
        }else{
            isSingle = false;
        }
        lastTime = currentTime;

        return isSingle;
    }



    /**
     * 两个时间相差距离多少天多少小时多少分多少秒
     * @param str1 时间参数 1 格式：1990-01-01 12:00:00
     * @param str2 时间参数 2 格式：2009-01-01 12:00:00
     * @return String 返回值为：xx天xx小时xx分xx秒
     */
    public static String getDistanceTime(String str1, String str2) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date one;
        Date two;
        long day = 0;
        long hour = 0;
        long min = 0;
        long sec = 0;
        try {
            one = df.parse(str1);
            two = df.parse(str2);
            long time1 = one.getTime();
            long time2 = two.getTime();
            long diff ;
            if(time1<time2) {
                diff = time2 - time1;
            } else {
                diff = time1 - time2;
            }
            day = diff / (24 * 60 * 60 * 1000);
            hour = (diff / (60 * 60 * 1000) - day * 24);
            min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
            sec = (diff/1000-day*24*60*60-hour*60*60-min*60);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(day>0)
            hour=day*24+hour;
        return hour + "小时" + min + "分" + sec + "秒";
    }

}
