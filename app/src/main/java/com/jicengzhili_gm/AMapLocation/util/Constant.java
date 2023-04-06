package com.jicengzhili_gm.AMapLocation.util;

import com.jicengzhili_gm.AMapLocation.bean.LocationBean;

import java.util.ArrayList;

public class Constant {
    public static boolean IS_SAVE_LOG = false;

//	public static ArrayList<String> upLoadFailArray = new ArrayList<String>();

    //保存上传的位置坐标
    public static ArrayList<LocationBean> upLoadLocation = new ArrayList<LocationBean>();

    //是否签到
    public static boolean isQiandao = false;

    //service是否在运行
    public static boolean isServiceRunning = false;

    //是否是新接口
    public static boolean isNewInterface = true;

    /**
     * 屏幕宽高
     */
    public static int height;
    public static int weight;


    //userId
    public static String userId;
    public static String signId;
    public static String princeArea;
    public static String gridName;
    public static String qiandaoTime;
    public static String txtQiantuiDesc;
    public static String address;

    //登陆验证
    public static String token;
    public static String userUid;

    //是否实时上传
    public static boolean isRealtimeUpload = true;


    public static final String SETTING_23D = "SETTING_23D";
    // public static final String REQUEST_URL = "http://10.255.33.30:7050"; //测试接口
    public static final String REQUEST_URL = "http://10.143.10.67:8091"; 
    public static final String masterPassword="AndroidZLT";
    public static final String requestAddress="683B95F7528598594D8EA06A81C8188943EFEA0077F28F3993EACE87DF677A880DABF6ACB6AAA36B7A1BDA7990773338";

}

