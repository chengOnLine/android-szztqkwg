package com.jicengzhili_gm.AMapLocation.util;

import android.util.Log;

import com.jicengzhili_gm.AMapLocation.base.ResponseMsg;
import com.jicengzhili_gm.AMapLocation.bean.LocationBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JsonUtils {
    /***
     * 根据经纬度数组转换成Json数据
     *
     * @param locationArray
     * @return
     */
    public static String getJsonDataFromLocation(ArrayList<LocationBean> locationArray) {
        String jsonresult = "";// 定义返回字符串

        try {
            JSONArray jsonarray = new JSONArray();// json数组，里面包含的内容为pet的所有对象
            for (int i = 0; i < locationArray.size(); i++) {
                JSONObject jsonObj = new JSONObject();// pet对象，json形式

                jsonObj.put("createTime", locationArray.get(i).getTime());// 向pet对象里面添加值
                jsonObj.put("lat", locationArray.get(i).getLatitude());
                jsonObj.put("lng", locationArray.get(i).getLongitude());
                jsonObj.put("userId", Integer.parseInt(Constant.userId));
                jsonObj.put("signId", Constant.signId);
                jsonObj.put("totalMileage", locationArray.get(i).getTotalMileage());

                // 把每个数据当作一对象添加到数组里
                jsonarray.put(jsonObj);// 向json数组里面添加pet对象
            }
            jsonresult = jsonarray.toString();// 生成返回字符串
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            Log.e("alen", "msg：" + e.getMessage());
            e.printStackTrace();
        }
        // Log.e("alen","生成的json串为:"+jsonresult);

        return jsonresult;
    }

    /***
     * 根据经纬度转换成Json数据
     *
     * @param locationBean
     * @return
     */
    public static String getJsonDataFromLocation2(LocationBean locationBean) {
        String jsonresult = "";// 定义返回字符串
        try {
            JSONObject jsonObj = new JSONObject();// pet对象，json形式
            jsonObj.put("createTime", locationBean.getTime());// 向pet对象里面添加值
            jsonObj.put("isError", 1);
            jsonObj.put("isWorkArea", 1);
            jsonObj.put("lat", locationBean.getLatitude());
            jsonObj.put("lng", locationBean.getLongitude());
            jsonObj.put("userId", Integer.parseInt(Constant.userId));
            jsonObj.put("workState", 1);
            jsonresult = jsonObj.toString();// 生成返回字符串
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // Log.e("alen","生成的json串为:"+jsonresult);

        return jsonresult;
    }


    public static String getJsonDataFromLocation3(ResponseMsg responseMsg) {
        String jsonresult = "";// 定义返回字符串
        try {
            JSONObject jsonObj = new JSONObject();// pet对象，json形式
            jsonObj.put("msg", responseMsg.getMsg());// 向pet对象里面添加值
            jsonObj.put("code", responseMsg.getCode());
            jsonObj.put("data", responseMsg.getData());
            jsonresult = jsonObj.toString();// 生成返回字符串
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
         Log.e("alen","生成的json串为:"+jsonresult);

        return jsonresult;
    }

    public static int getCodeValue(String result) {
        int code = 0;
        try {
            JSONObject responseobj = new JSONObject(result);
            if(Constant.isNewInterface){
                code = responseobj.getInt("result");
            }else {
                code = responseobj.getInt("code");
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return code;
    }

    public static boolean getFlagValue(String result){
        boolean flag=false;
        try {
            JSONObject responseobj = new JSONObject(result);
            flag = responseobj.getBoolean("flag");

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return flag;
    }

    public static String getIdValue(String result) {
        String id = "";
        try {
            JSONObject responseobj = new JSONObject(result);
            String data=responseobj.getString("data");
            JSONObject dataobj=new JSONObject(data);
            id = dataobj.getString("id");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return id;
    }

    public static String getMessage(String result) {
        String message = "";
        try {
            JSONObject responseobj = new JSONObject(result);
            message = responseobj.getString("msg");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return message;
    }
}
