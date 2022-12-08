package com.jicengzhili_gm.AMapLocation.activity;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.jicengzhili_gm.AMapLocation.bean.LatLonPoint;
import com.jicengzhili_gm.AMapLocation.bean.LocationBean;
import com.jicengzhili_gm.AMapLocation.db.SharedPreferencesUtils;
import com.jicengzhili_gm.AMapLocation.db.SqlIteOperate;
import com.jicengzhili_gm.AMapLocation.net.HttpControl;
import com.jicengzhili_gm.AMapLocation.net.OkHttpClientManager;
import com.jicengzhili_gm.AMapLocation.service.LocationService;
import com.jicengzhili_gm.AMapLocation.util.Constant;
import com.jicengzhili_gm.AMapLocation.util.Converter;
import com.jicengzhili_gm.AMapLocation.util.JsonUtils;
import com.jicengzhili_gm.AMapLocation.util.Utils;
import com.jicengzhili_gm.AMapLocation.widget.MessageDialog;
import com.jicengzhili_gm.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import okhttp3.Response;

//
public class AMapActivity  extends Activity implements View.OnClickListener,AMapLocationListener {

    private static final String TAG = "TrajectoryMapActivity";
    private AMapLocationClient locationClient = null;
    private Context mContext;
    private LocationBean qiantuiBean = null;

    private boolean isHaveGpsPermisson = true;
    private Button backBtn,visual_3d,visual_2d;
    private Button qiandaoBtn,qiantuiBtn;
    private TextView text_signAddress,text_qdtime,text_sc;

    String date_tv=Utils.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd");

    private ArrayList<LocationBean> mLocationArray;
    private List<List<LatLng>> mLocationPointArray;

    private float fullDistance = 0.0f;

    private MessageDialog mMessageDialog;

    private MapView mMapView;
    private AMap mAMap;
    private Polyline mPolyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_playback);

        try {
            initPermission();
            updatePrivacyShow(this);

            mContext = this;
            locationClient = new AMapLocationClient(this);

            int userId = (int) SharedPreferencesUtils.getParam(mContext, "userId", 0);
            Constant.signId = (String) SharedPreferencesUtils.getParam(mContext, "signId", "0");
            Constant.userId = Integer.toString(userId);
            Constant.qiandaoTime = (String) SharedPreferencesUtils.getParam(mContext, "qiandaoTime", "");
            //签到状态
            Constant.isQiandao = (boolean) SharedPreferencesUtils.getParam(mContext, "qianDao", false);

            //获取地图控件引用
            mMapView = (MapView) findViewById(R.id.map);
            //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
            mMapView.onCreate(savedInstanceState);

            init();
            initView();
            //刷新签到状态
            freshData();


            //动态注册一个广播
            IntentFilter filter = new IntentFilter();
            filter.addAction("FRESH_LOCATION_POINT");
            filter.addAction("FRESH_QIANTUI_STATE");

            registerReceiver(mReceiver, filter, "com.android.permission.RECV_2", null);

            Log.e("alen", "mReceiver 被注册");
            // Toast.makeText(mContext, "mReceiver被注册", Toast.LENGTH_SHORT).show();

            if (Constant.isQiandao) {
                rsfTime();

                Log.e("alen", "onCreated");
                Log.e("alen", "qiandaoTime:" + Constant.qiandaoTime);

                long lastSignTime = (long) SharedPreferencesUtils.getParam(mContext, "signTime", System.currentTimeMillis());
                long curTime = System.currentTimeMillis();

                if ((curTime - lastSignTime) > (12L * 60L * 60L * 1000L)) {
//                    Log.e("alen", "签到超过12小时，执行签退操作");
//                    doQiantui();
                } else {
                    if (!Constant.isServiceRunning) {
                        startService(new Intent(this, LocationService.class));
                    }
                }
            }

            //清理30天以前的数据
            clear30Data();

        } catch (Exception e) {
//            e.printStackTrace();
            Log.e("ex", e.getMessage());
        }
    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("alen", "mReceiver 被调用，action：" + intent.getAction());
            if (intent.getAction().equals("FRESH_LOCATION_POINT")) {

                if(date_tv.equals(Utils.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd"))) { //当天的位置进行刷新

                    LocationBean location = (LocationBean) intent.getSerializableExtra("location");
                    //Toast.makeText(mContext, "FRESH_LOCATION_POINT lng:"+location.getLongitude(), Toast.LENGTH_SHORT).show();

                    freshPolylineInPlayGround(new LatLng(location.getLatitude(), location.getLongitude()));
                }
            } else if (intent.getAction().equals("FRESH_QIANTUI_STATE")) {
                Log.e("alen", "FRESH_QIANTUI_STATE被调用");
                //清空地图轨迹
                mAMap.clear(true);

                //刷新签到状态
                freshData();

                //刷新轨迹信息
                freshLocationLineData();
            }
        }
    };

    /**
     * 初始化AMap对象
     */
    private void init() {
        if (mAMap == null) {
            mAMap = mMapView.getMap();

            MyLocationStyle myLocationStyle;
            myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
            myLocationStyle.interval(5000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
            mAMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
            mAMap.getUiSettings().setMyLocationButtonEnabled(true); //设置默认定位按钮是否显示，非必需设置。
            mAMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。

            AMapLocation lastLocation = locationClient.getLastKnownLocation();
            if(lastLocation!=null){
                Toast.makeText(mContext, "获取到当前位置", Toast.LENGTH_SHORT).show();
                mAMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude())),250,null);
                mAMap.moveCamera(CameraUpdateFactory.zoomTo(mAMap.getMaxZoomLevel()-2)); // 设置缩放级别

            }else{
            	Log.e(TAG, "未获取到最后的定位信息，重新定位");
                Toast.makeText(mContext, "未获取到最后的定位信息,重新定位", Toast.LENGTH_SHORT).show();

            	AMapLocationClientOption option = new AMapLocationClientOption();
                option.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.SignIn);
                locationClient.setLocationOption(option);
                //设置定位监听
                locationClient.setLocationListener((AMapLocationListener) this);
                locationClient.startLocation();
            }
        }
    }

    private void initView(){
        mMessageDialog = new MessageDialog(this);

        backBtn = (Button) findViewById(R.id.backBtn);
        visual_3d = (Button) findViewById(R.id.visual_3d);
        visual_2d = (Button) findViewById(R.id.visual_2d);
        backBtn.setOnClickListener(this);
        visual_3d.setOnClickListener(this);
        visual_2d.setOnClickListener(this);

        qiandaoBtn = (Button) findViewById(R.id.qiandaoBtn);
        qiantuiBtn = (Button) findViewById(R.id.qiantuiBtn);

        text_signAddress=(TextView)findViewById(R.id.text_signAddress);
        text_qdtime=(TextView)findViewById(R.id.text_qdtime);
        text_sc=(TextView)findViewById(R.id.text_sc);

        qiandaoBtn.setOnClickListener(this);
        qiantuiBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View arg0) {
        // TODO Auto-generated method stub
        int i = arg0.getId();

        Log.e("alen", "onClick：" + arg0.getId());

        try {
            if (i == R.id.backBtn) {
                AMapActivity.this.finish();

            } else if (i == R.id.qiantuiBtn) {
                if (Constant.isQiandao) { //已签到
                    mMessageDialog.showDialog("您确定要签退吗", mQiantuiCallBack);
                }
            } else if (i == R.id.qiandaoBtn) {

                if(Utils.isSingle()){
                    Log.e("alen", "2000ms内重复提交！");
                    return;
                }

                if (!Constant.isQiandao) { //未签到
                    if (!Utils.openGPSSettings(mContext)) {
                        mMessageDialog.showDialog("未开启GPS无法保存有效轨迹，请立即设置", mMessageCallBack);
                    }

                    if (!isHaveGpsPermisson) {
                        mMessageDialog.showDialog("定位权限被禁用,请授予应用定位权限", mGpsCallBack);
                    }
                }

                doQiandao();
            } else if (i == R.id.visual_3d) {
                SharedPreferencesUtils.setParam(mContext, Constant.SETTING_23D, 1);

                visual_3d.setBackgroundResource(R.drawable.check_bg);
                visual_2d.setBackgroundResource(R.drawable.normal_bg);
                if (mAMap != null) {
                    mAMap.moveCamera(CameraUpdateFactory.changeBearing(0f));// 上北下南
                    mAMap.moveCamera(CameraUpdateFactory.changeTilt(30.0f));// 2D
                }
            } else if (i == R.id.visual_2d) {
                SharedPreferencesUtils.setParam(mContext, Constant.SETTING_23D, 0);

                visual_2d.setBackgroundResource(R.drawable.check_bg);
                visual_3d.setBackgroundResource(R.drawable.normal_bg);
                if (mAMap != null) {
                    mAMap.moveCamera(CameraUpdateFactory.changeBearing(0f));// 上北下南
                    mAMap.moveCamera(CameraUpdateFactory.changeTilt(0.0f));// 2D
                }
            }
        } catch (Exception ex) {
            Log.e("alen", ex.getMessage());
        }
    }

    public  void clear30Data() {
        //清理30天以前的数据
        try {
            LocationBean tmpBean = SqlIteOperate.getFirstLocationRecord(mContext);
            if (tmpBean != null) {
                //Log.e(TAG, "第一条数据日期："+tmpBean.getDate()+";Id:"+tmpBean.getLocationId());

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date firstDate = sdf.parse(tmpBean.getDate());
                    long monthTime = 30L * 24L * 60L * 60L * 1000L;
                    if ((System.currentTimeMillis() - firstDate.getTime()) > monthTime) {
                        //超过30天，进行删除操作

                        //Log.e(TAG, "执行删除操作");
                        SqlIteOperate.delete(mContext, tmpBean.getDate());
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception ex) {

        }
    }

    private void doQiandao(){
        if(!Constant.isQiandao){ //未签到
            AMapLocation loc = locationClient.getLastKnownLocation();
            LocationBean bean = null;
            if(loc!=null){
                bean = new LocationBean();

                LatLonPoint wgsPoint = Converter.toWGS84Point(loc.getLatitude(), loc.getLongitude());
                bean.setLongitude(wgsPoint.getLongitude());
                bean.setLatitude(wgsPoint.getLatitude());
                bean.setAddress(loc.getAddress());
                bean.setTime(Utils.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss"));
                bean.setDate(Utils.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd"));
                bean.setStateType(1);
            }else{
                //签到定位
                AMapLocationClientOption option = new AMapLocationClientOption();
                option.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.SignIn);
                locationClient.setLocationOption(option);
                //设置定位监听
                locationClient.setLocationListener(this);
                locationClient.startLocation();

                mMessageDialog.showDialog("未能有效定位，请稍后进行签到.", mTipCallBack);
                return;
            }

            HttpControl.doSignInControl(bean, new OkHttpClientManager.HttpCallBack() {
                @Override
                public void onSuccess(Response response) {
                    try {
                        String res = response.body().string();
                        Log.e("alen", "签到返回结果："+res);

                        String Message = JsonUtils.getMessage(res);

                        boolean isSuc = JsonUtils.getFlagValue(res);
                        if(isSuc){
                            String id = JsonUtils.getIdValue(res);
                            Constant.signId = id;
                            SharedPreferencesUtils.setParam(mContext, "signId", id);
                            Message msg = new Message();
                            msg.what = 3;
                            msg.obj = "签到成功！";
                            mHandler.sendMessage(msg);
                        }else{
                            Message msg = new Message();
                            msg.what = 1;
                            msg.obj = Message;
                            mHandler.sendMessage(msg);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Message msg = new Message();
                        msg.what = 1;
                        msg.obj = "网络请求失败，请重试！";
                        mHandler.sendMessage(msg);
                        Log.e("alen", "签退返回结果："+e);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Message msg = new Message();
                    msg.what = 1;
                    msg.obj = "网络请求失败，请重试！";
                    mHandler.sendMessage(msg);
                    Log.e("alen", "签退返回结果："+e);
                }
            });
        }
    }

    private void doQiantui() {

        if (Constant.isQiandao) { //已签到
//			Log.e("alen","Constant.upLoadLocation.size:"+Constant.upLoadLocation.size()+"");
            if (Constant.upLoadLocation.size() > 0) {
                mHandler.sendEmptyMessageDelayed(Utils.MSG_LOCATION_REUPLOAD, 500);
            } else {
                mHandler.sendEmptyMessageDelayed(2, 500);
            }
        }

    }


    /***
     * 签到成功
     */
    private void qiandaoSuccess(){

        qiandaoBtn.setBackgroundResource(R.drawable.dark_btn_border);
        qiantuiBtn.setBackgroundResource(R.drawable.trackplay_btn_border);

        Constant.isQiandao = true;
        SharedPreferencesUtils.setParam(mContext,"curMileage",0.0f);
        SharedPreferencesUtils.setParam(mContext, "qianDao", true);
        SharedPreferencesUtils.setParam(mContext, "signTime", System.currentTimeMillis());
        SharedPreferencesUtils.setParam(mContext, "signDate", Utils.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd"));

        rsfTime();

        AMapLocation loc = locationClient.getLastKnownLocation();
        LocationBean bean = null;
        if(loc!=null){
            bean = new LocationBean();

            LatLonPoint wgsPoint = Converter.toWGS84Point(loc.getLatitude(), loc.getLongitude());
            bean.setLongitude(wgsPoint.getLongitude());
            bean.setLatitude(wgsPoint.getLatitude());
            bean.setAddress(loc.getAddress());
            bean.setTime(Utils.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss"));
            bean.setDate(Utils.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd"));
            bean.setStateType(1);
        }
        SqlIteOperate.saveLocation(mContext, bean,false);

        if(date_tv.equals(Utils.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd"))){ //当天的位置进行刷新
            mLocationArray = SqlIteOperate.getDateLocationRecords(mContext,date_tv);
            Log.e(TAG, date_tv+"的轨迹数量："+mLocationArray.size());

            if(mLocationArray.size()>0){
                mAMap.clear(true);
                addPolylineInPlayGround();
            }else{
                SharedPreferencesUtils.setParam(mContext, "curFullDistance", 0.0f);
                mAMap.clear(true);
            }
        }

        Constant.upLoadLocation.clear();
        SharedPreferencesUtils.setParam(mContext,"curMileage",0.0f);
        SharedPreferencesUtils.setParam(mContext, "signAddress","");
        fullDistance = 0.0f;

        startService(new Intent(this,LocationService.class));
    }


    /***
     * 签退成功
     */
    private void qiantuiSuccess(){
        qiandaoBtn.setBackgroundResource(R.drawable.trackplay_btn_border);
        qiantuiBtn.setBackgroundResource(R.drawable.dark_btn_border);

        Log.e("alen","stopService");

        stopService(new Intent(this,LocationService.class));

        Constant.isQiandao = false;
        Constant.signId = "";
        SharedPreferencesUtils.setParam(mContext, "qianDao", false);
        SharedPreferencesUtils.setParam(mContext, "signId", "");

        SqlIteOperate.saveLocation(mContext, qiantuiBean,false);

        if(date_tv.equals(Utils.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd"))){ //当天的位置进行刷新
            mLocationArray = SqlIteOperate.getDateLocationRecords(mContext,date_tv);
            //Log.e(TAG, date_tv.getText().toString()+"的轨迹数量："+mLocationArray.size());

            if(mLocationArray.size()>0){
                mAMap.clear(true);
                addPolylineInPlayGround();
            }else{
                mAMap.clear(true);
            }
        }

        String sqTime=Constant.qiandaoTime;
        String nowTime=Utils.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss");
        String distinceTime=Utils.getDistanceTime(sqTime,nowTime);
        text_sc.setText(distinceTime);

        Constant.upLoadLocation.clear();
        SharedPreferencesUtils.setParam(mContext,"curMileage",0.0f);
        fullDistance = 0.0f;
    }

    public static void updatePrivacyShow(Context context){
        AMapLocationClient.updatePrivacyShow(context,true,true);
        AMapLocationClient.updatePrivacyAgree(context,true);
    }

    /***
     * 刷新状态
     */
    private void freshData(){
        Boolean isQiandao = (boolean) SharedPreferencesUtils.getParam(mContext, "qianDao", false);

        if(!isQiandao){ //未签到
            qiandaoBtn.setBackgroundResource(R.drawable.trackplay_btn_border);
            qiantuiBtn.setBackgroundResource(R.drawable.dark_btn_border);
        }else{ //已签到
            qiandaoBtn.setBackgroundResource(R.drawable.dark_btn_border);
            qiantuiBtn.setBackgroundResource(R.drawable.trackplay_btn_border);
        }
    }

    /***
     * 刷新当天的轨迹信息
     */
    private void freshLocationLineData(){

        mLocationArray = SqlIteOperate.getDateLocationRecords(this,date_tv);
        Log.e(TAG, "今天的点个数："+mLocationArray.size());
        Utils.write_log("今天的点个数："+mLocationArray.size());

        if(mLocationArray.size()>0){
            addPolylineInPlayGround();
        }
    }


    /**
     * 添加轨迹线
     */
    private void addPolylineInPlayGround() {
        Utils.write_log("addPolylineInPlayGround");

        mLocationPointArray = readAllLatLngs();
        Utils.write_log("mLocationPointArray.size = "+mLocationPointArray.size());
//        List<Integer> colorList = new ArrayList<Integer>();
//        List<BitmapDescriptor> bitmapDescriptors = new ArrayList<BitmapDescriptor>();
//
        int[] colors = new int[]{ContextCompat.getColor(mContext, R.color.color1),
                ContextCompat.getColor(mContext, R.color.color4)};

        for(int i =0;i<mLocationPointArray.size();i++){
            List<LatLng> tmpList = mLocationPointArray.get(i);
            //Log.d("log数组", String.valueOf(tmpList));
            if(tmpList.size()>0){
                mPolyline = mAMap.addPolyline(new PolylineOptions()
//        		.setCustomTexture(BitmapDescriptorFactory.fromResource(R.drawable.custtexture)) //setCustomTextureList(bitmapDescriptors)
//				.setCustomTextureIndex(texIndexList)
                        .color(colors[1])
//    			.color(colors[0])
                        .addAll(tmpList)
                        .useGradient(true)
                        .width(12));

//        LatLngBounds bounds = new LatLngBounds(list.get(0), list.get(0));
                mAMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLocationPointArray.get(i).get(0), mAMap.getMaxZoomLevel()-2));
                addMarkersToMap(tmpList.get(0), 0);  //绘制起点
                Log.e("alen","addPolylineInPlayGround方法被调用！");
                if(i==mLocationPointArray.size()-1 && Constant.isQiandao){

                }else{
                    addMarkersToMap(tmpList.get(tmpList.size()-1), 1); //绘制终点
                }
            }
        }
    }

    /**
     * 读取坐标点
     * @return
     */
    private List<List<LatLng>> readAllLatLngs() {
        List<List<LatLng>> allPoints = new ArrayList<List<LatLng>>();

        List<LatLng> points = null;
        int size = mLocationArray.size();
        for (int i = 0; i < size; i ++) {
            LocationBean bean = mLocationArray.get(i);
            Utils.write_log("bean.getTime()："+bean.getTime());

            if(bean.getStateType() == 1){ //签到
                Utils.write_log("签到，创建数组");
                if(points == null){
                    points = new ArrayList<LatLng>();

                    if(i == size-1){ //最后一条数据仍为记录中
                        Utils.write_log("最后一条数据仍为签到");
                        allPoints.add(points);
                    }
                }else{
                    Utils.write_log("已有数据，进行添加");
                    allPoints.add(points);
                    Utils.write_log("此时allPoints数组的长度："+allPoints.size()+"；此时points数组的长度："+points.size());
                    points = new ArrayList<LatLng>();
                }
            }else if(bean.getStateType() == 2){ //记录中
                Utils.write_log("记录中");
                if(points == null){
                    points = new ArrayList<LatLng>();
                }

                if(points!=null){
                    LatLng latLng1 = new LatLng(bean.getLatitude(), bean.getLongitude());
                    if(i+1<size){
                        LatLng latLng2 = new LatLng(mLocationArray.get(i+1).getLatitude(), mLocationArray.get(i+1).getLongitude());
                        float distance = AMapUtils.calculateLineDistance(latLng1, latLng2);
                        fullDistance += distance;
                    }

                    if(i == size-1){ //最后一条数据仍为记录中
                        Utils.write_log("最后一条数据仍为记录中");
                        allPoints.add(points);
                        points = null;
                    }else{
                        points.add(latLng1);
                        Utils.write_log("添加进去");
                    }
                }
            }else if(bean.getStateType() == 3){ //签退
                if(points!=null){
                    Utils.write_log("签退，添加数组");
                    allPoints.add(points);
                    Utils.write_log("此时allPoints数组的长度："+allPoints.size()+"；此时points数组的长度："+points.size());
                    points = null;
                }
            }
        }
        return allPoints;
    }

    /**
     * 在地图上添加marker
     */
    private void addMarkersToMap(LatLng latlng,int type) {
        if(type == 0){ //起点
            MarkerOptions markerOption = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.amap_start))
                    .position(latlng)
                    .draggable(true);
            Marker marker = mAMap.addMarker(markerOption);
        }else{ //终点
            MarkerOptions markerOption = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.amap_end))
                    .position(latlng)
                    .draggable(true);
            Marker marker = mAMap.addMarker(markerOption);
        }
    }

    /**
     * 刷新轨迹线
     */
    private void freshPolylineInPlayGround(LatLng latLng) {
//        Utils.write_log("freshPolylineInPlayGround");
        mAMap.clear(true);

        int[] colors = new int[]{ContextCompat.getColor(mContext, R.color.color1),
                ContextCompat.getColor(mContext, R.color.color4)};

        if(mLocationPointArray!=null && mLocationPointArray.size()>0){
            List<LatLng> mLastLatLngArray = mLocationPointArray.get(mLocationPointArray.size()-1);
            if(mLastLatLngArray!=null){
                if(mLastLatLngArray.size()>0){
                    LatLng latLng2 = mLastLatLngArray.get(mLastLatLngArray.size()-1);
                    float distance = AMapUtils.calculateLineDistance(latLng, latLng2);
                    fullDistance += distance;
                }

                mLastLatLngArray.add(latLng);

//                licheng_tv.setText(Utils.getFriendlyLength((int)fullDistance));
                //Log.e(TAG, "今日里程："+fullDistance);

                Log.e(TAG, "坐标添加完成，进行刷新显示");
                for(int i =0;i<mLocationPointArray.size();i++){
                    List<LatLng> tmpList = mLocationPointArray.get(i);
                    if(tmpList.size()>0){
                        if(i != (mLocationPointArray.size()-1)){
                            mPolyline = mAMap.addPolyline(new PolylineOptions()
                                    .color(colors[1])
                                    .addAll(tmpList)
                                    .useGradient(true)
                                    .width(12));
                        }else{
                            mPolyline = mAMap.addPolyline(new PolylineOptions()
                                    .color(colors[0])
                                    .addAll(tmpList)
                                    .useGradient(true)
                                    .width(12));
                        }

                        addMarkersToMap(tmpList.get(0), 0);  //绘制起点

                        if(i != (mLocationPointArray.size()-1)){
                            Log.e("alen","freshPolylineInPlayGround方法被调用！");
                            addMarkersToMap(tmpList.get(tmpList.size()-1), 1); //绘制终点
                        }
                    }
                }

                mAMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        }
    }

    ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(2);
    //开启线程刷新
    private void  rsfTime() {
        scheduledThreadPool.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
//                Log.e("alen", "定时任务: " + System.currentTimeMillis());

                try {
                    mHandler.sendEmptyMessageDelayed(6, 100);
                } catch (Exception ex) {
                    Log.e("alen", "定时任务ex: " + ex.getMessage());
                }
            }
        }, 1, 1, TimeUnit.SECONDS); //此表示为延迟1秒后每3秒执行一次
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case 1:
                        Toast.makeText(mContext, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        break;

                    case 2:
                        Log.e("alen", "进行签退操作");
                        freshLocationLineData();
                        AMapLocation loc2 = locationClient.getLastKnownLocation();

                        if (loc2 != null) {
                            qiantuiBean = new LocationBean();
                            LatLonPoint wgsPoint = Converter.toWGS84Point(loc2.getLatitude(), loc2.getLongitude());
                            qiantuiBean.setLongitude(wgsPoint.getLongitude());
                            qiantuiBean.setLatitude(wgsPoint.getLatitude());
                            qiantuiBean.setAddress(loc2.getAddress());
                            qiantuiBean.setTime(Utils.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss"));
                            qiantuiBean.setDate(Utils.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd"));
                            qiantuiBean.setStateType(3);
                        } else {
                            mMessageDialog.showDialog("未能有效定位，请稍后进行签到.", mTipCallBack);
                            return;
                        }

//					float allMileage = (float) SharedPreferencesUtils.getParam(mContext,"curMileage",0.0f);
                        // Toast.makeText(mContext, "签到里程："+fullDistance, Toast.LENGTH_SHORT).show();

                        float mil = 0.0f;
                        HttpControl.doSignOutControl(qiantuiBean, mil, new OkHttpClientManager.HttpCallBack() {
                            @Override
                            public void onSuccess(Response response) {
                                try {
                                    String res = response.body().string();
                                    Log.e("alen", "签退返回结果：" + res);

                                    //当天的位置里程统计
                                    if (date_tv.equals(Utils.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd"))) {
                                        SharedPreferencesUtils.setParam(mContext, "curFullDistance", fullDistance);
                                    }

//								int result = JsonUtils.getCodeValue(res);
                                    String Message = JsonUtils.getMessage(res);
//								//Log.e("alen", "签退返回结果："+result);

                                    boolean isSuc = JsonUtils.getFlagValue(res);

                                    if (isSuc) {
                                        Message msg = new Message();
                                        msg.what = 5;
                                        msg.obj = "签退成功！";
                                        mHandler.sendMessage(msg);
                                    } else {
                                        Message msg = new Message();
                                        msg.what = 1;
                                        msg.obj = Message;
                                        mHandler.sendMessage(msg);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Message msg = new Message();
                                    msg.what = 1;
                                    msg.obj = "网络请求失败，请重试！";
                                    mHandler.sendMessage(msg);
                                    Log.e("alen", "签退返回结果：" + e);
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Message msg = new Message();
                                msg.what = 1;
                                msg.obj = "网络请求失败，请重试！";
                                mHandler.sendMessage(msg);
                                Log.e("alen", "签退返回结果：" + e);
                            }
                        });
                        break;
                    case 3:
                        Toast.makeText(mContext, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        qiandaoSuccess();
                        break;

                    case Utils.MSG_LOCATION_REUPLOAD:
                        if (Constant.upLoadLocation.size() == 0) {
                            mHandler.sendEmptyMessage(2);
                            break;
                        }
                        ArrayList<LocationBean> historyLocation = new ArrayList<LocationBean>();
                        Integer locaNum = 0;
                        if (Constant.upLoadLocation.size() > 30) {
                            locaNum = 30;
                        } else {
                            locaNum = Constant.upLoadLocation.size();
                        }
                        for (int i = 0; i < locaNum; i++) {
                            historyLocation.add(Constant.upLoadLocation.get(i));
                        }
//						historyLocation.add(tmpLocationBean);
                        String jsonData = JsonUtils.getJsonDataFromLocation(historyLocation);

                        final Integer finalLocaNum = locaNum;

                        float allMileage = (float) SharedPreferencesUtils.getParam(mContext, "curMileage", 0.0f);
                        HttpControl.doPostLocationDataControl(jsonData, allMileage, new OkHttpClientManager.HttpCallBack() {

                            @Override
                            public void onSuccess(Response response) {
                                // TODO Auto-generated method stub
                                try {
                                    String res = response.body().string();
                                    //Log.e("alen", "签退返回结果："+result);

                                    boolean isSuc = JsonUtils.getFlagValue(res);
                                    if (isSuc) {
//										Constant.upLoadLocation.clear();
                                        for (int i = 0; i < finalLocaNum; i++) {
                                            Constant.upLoadLocation.remove(0);
                                        }

                                        //上报完成后，在进行签退操作
                                        Message msg2 = new Message();
                                        msg2.what = 2;
                                        mHandler.sendMessageDelayed(msg2, 500);

                                    } else {
                                        Message msg = new Message();
                                        msg.what = 1;
                                        msg.obj = JsonUtils.getMessage(res);
                                        mHandler.sendMessage(msg);
                                    }
                                } catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                    Message msg = new Message();
                                    msg.what = 1;
                                    msg.obj = "网络请求失败，请重试！";
                                    mHandler.sendMessage(msg);
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                //Log.d("alen", "坐标上传失败：");

                                Message msg = new Message();
                                msg.what = 1;
                                msg.obj = "网络请求失败，请重试！";
                                mHandler.sendMessage(msg);
                            }
                        });
                        break;
                    case 5:
                        Toast.makeText(mContext, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        qiantuiSuccess();
                        break;

                    case 6:
                        if (Constant.isQiandao) {

                            String sqTime = Constant.qiandaoTime;
                            String nowTime = Utils.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss");
                            String distinceTime = Utils.getDistanceTime(sqTime, nowTime);

                            text_sc.setText(distinceTime);
                            text_qdtime.setText(sqTime);

                            //当前签到位置信息
                            String signAddress = (String) SharedPreferencesUtils.getParam(mContext, "signAddress", "");
                            text_signAddress.setText(signAddress);
                        }
                        break;

                    default:
                        break;
                }

            } catch (Exception ex) {
                Log.e("alen", ex.getMessage());
            }
        };
    };

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String permissions[] = {
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.CHANGE_CONFIGURATION,
                Manifest.permission.INTERNET,
                Manifest.permission.WAKE_LOCK,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat
                    .checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.

            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    toApplyList.toArray(tmpList), 123);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
            mMapView.onDestroy();

        } catch (Exception ex) {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();

        if(Constant.isQiandao){
            String signDate = (String) SharedPreferencesUtils.getParam(mContext, "signDate", "");
            String curDate = Utils.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd");
            if(!curDate.equals(signDate) && signDate.equals(date_tv)){
                // 跨日自动切换最新的一天
                //Log.e(TAG, "跨日自动切换最新的一天");

                //清空地图轨迹
                mAMap.clear(true);
                freshLocationLineData();
            }
        }

        if(!Utils.openGPSSettings(mContext)){
            mMessageDialog.showDialog("未开启GPS无法保存有效轨迹，请立即设置", mMessageCallBack);
        }

        //签到定位
        AMapLocationClientOption option = new AMapLocationClientOption();
        option.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.SignIn);
        locationClient.setLocationOption(option);
        //设置定位监听
        locationClient.setLocationListener(this);
        locationClient.startLocation();

    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }


    private MessageDialog.MessageDialogCallBack mGpsCallBack = new MessageDialog.MessageDialogCallBack() {

        @Override
        public void sure() {
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            startActivity(intent); // 打开系统设置界面
        }

        @Override
        public void cancel() {
        }
    };

    private MessageDialog.MessageDialogCallBack mQiantuiCallBack = new MessageDialog.MessageDialogCallBack() {

        @Override
        public void sure() {
            doQiantui();
        }

        @Override
        public void cancel() {
        }
    };

    private MessageDialog.MessageDialogCallBack mTipCallBack = new MessageDialog.MessageDialogCallBack() {

        @Override
        public void sure() {
        }

        @Override
        public void cancel() {
        }
    };

    private MessageDialog.MessageDialogCallBack mMessageCallBack = new MessageDialog.MessageDialogCallBack() {

        @Override
        public void sure() {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        @Override
        public void cancel() {
        }
    };
    @Override
    public void onLocationChanged(AMapLocation arg0) {
        if(arg0.getErrorCode() == 12){ //缺少定位权限
            isHaveGpsPermisson = false;
        }else{
            isHaveGpsPermisson = true;
        }

        if(mAMap!=null){
            mAMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(arg0.getLatitude(), arg0.getLongitude())),250,null);
            mAMap.moveCamera(CameraUpdateFactory.zoomTo(mAMap.getMaxZoomLevel()-2)); // 设置缩放级别
        }
    }
}
