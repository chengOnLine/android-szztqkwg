package com.jicengzhili_gm.AMapLocation.net;

import android.text.TextUtils;
import android.util.Log;

import com.jicengzhili_gm.AMapLocation.bean.LocationBean;
import com.jicengzhili_gm.AMapLocation.util.Constant;
import com.jicengzhili_gm.AMapLocation.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HttpControl {

    //签到接口
    public static final String signInControl="/qkwg-jcwg/flow/dailyAttendance/dailySignIn";

    //签退接口
    public static final String signOutControl="/qkwg-jcwg/flow/dailyAttendance/dailySignOut";

    //上传坐标
    public static final String uploadControl="/qkwg-jcwg/flow/userGis/uploadTraceList";



    /***
     * 签到
     * @param locationBean
     * @param callback
     */
    public static void doSignInControl(LocationBean locationBean, OkHttpClientManager.HttpCallBack callback){
        String url = "";
        //SharedPreferencesUtils.getParam(mContext, "gridName", gridName);
        String gridName= Constant.gridName;
        Log.e("alen","gridName:"+gridName);

        String streetId="",communityId="",gridId="";
        url = GetForMalIp()+signInControl;

//        url = GetForMalIp()+signInControl+"?signLat="+locationBean.getLatitude()+"&signLng="+locationBean.getLongitude()+"&signAddress="+locationBean.getAddress()+"&userId="+Constant.userId+"&taskType=1"+scGridParam;
//
//        url+="&token="+Constant.token+"&userUid="+Constant.userUid+"&clientDate="+ Utils.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss");
        Log.e("alen", "访问URL = " + url);

        String jsonresult = "";// 定义返回字符串
        try {
            String addr = locationBean.getAddress() == "" ? Constant.address : locationBean.getAddress();

            JSONObject jsonObj = new JSONObject();// pet对象，json形式
            jsonObj.put("streetId",streetId);
            jsonObj.put("communityId",communityId);
            jsonObj.put("gridId",gridId);
            jsonObj.put("signLat", locationBean.getLatitude());// 向pet对象里面添加值
            jsonObj.put("signLng", locationBean.getLongitude());
            jsonObj.put("signAddress", addr);
            jsonObj.put("userId", Constant.userId);
            jsonObj.put("taskType", 1);
            jsonObj.put("clientDate", Utils.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss"));
            jsonresult = jsonObj.toString();// 生成返回字符串
        } catch (JSONException e) {
            // TODO Auto-generated catch block
//            e.printStackTrace();
        }

//        OkHttpClientManager.getInstance().getUrl(url, callback);
        OkHttpClientManager.getInstance().postRequest(url, callback, jsonresult);
    }


    /***
     * 签退
     * @param locationBean
     * @param mCallBack
     */
    public static void doSignOutControl(LocationBean locationBean, Float mil, OkHttpClientManager.HttpCallBack mCallBack) {
        String url = "";
        url = GetForMalIp() + signOutControl;

        String jsonresult = "";// 定义返回字符串
        try {
            String addr = locationBean.getAddress() == "" ? Constant.address : locationBean.getAddress();

            JSONObject jsonObj = new JSONObject();// pet对象，json形式
            jsonObj.put("signOutLat", locationBean.getLatitude());// 向pet对象里面添加值
            jsonObj.put("signOutLng", locationBean.getLongitude());
            jsonObj.put("signOutAddress", addr);
            jsonObj.put("userId", Constant.userId);
            jsonObj.put("totalMileage", mil);
            jsonObj.put("id", Constant.signId);
            jsonObj.put("describe", Constant.txtQiantuiDesc);
            jsonresult = jsonObj.toString();// 生成返回字符串
        } catch (JSONException e) {
            // TODO Auto-generated catch block
//            e.printStackTrace();
        }

        Log.e("alen", "访问URL = " + url);
        Log.e("alen", "提交数据：" + jsonresult);

        OkHttpClientManager.getInstance().postRequest(url, mCallBack, jsonresult);
    }


    /***
     * 上传位置信息，每分钟上传一次
     * @param jsonData
     * @param callback
     */
    public static void doPostLocationDataControl(String jsonData, float allMileage, OkHttpClientManager.HttpCallBack callback){
        String url = GetForMalIp()+uploadControl;

//        url+="?token="+Constant.token+"&userUid="+Constant.userUid;
        Log.e("alen", "访问URL = " + url);
        Log.e("alen","上次的位置信息："+jsonData);

        OkHttpClientManager.getInstance().postRequest(url, callback, jsonData);
    }

    /*
     * 获取地址
     */
    public static String GetForMalIp(){
        String formalIp="";
        try{
//            formalIp = AES.decrypt(Constant.masterPassword, Constant.requestAddress);
            formalIp = Constant.REQUEST_URL;
            Log.e("alen","formalIp:"+formalIp);
        }catch (Exception e){
            Log.e("alen",e.getMessage());
            e.printStackTrace();
        }
        return formalIp;
    }

}

