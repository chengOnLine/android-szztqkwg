package com.jicengzhili_gm.AMapLocation.activity;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.utils.overlay.SmoothMoveMarker;
import com.jicengzhili_gm.AMapLocation.bean.LocationBean;
import com.jicengzhili_gm.AMapLocation.db.SharedPreferencesUtils;
import com.jicengzhili_gm.AMapLocation.util.Constant;
import com.jicengzhili_gm.AMapLocation.util.Utils;
import com.jicengzhili_gm.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class HistoryMapActivity  extends Activity implements View.OnClickListener, AMap.OnMarkerClickListener, AMapLocationListener {
    private static final String TAG = "HistoryMapActivity";

    private AMapLocationClient locationClient = null;

    private MapView mMapView;
    private AMap mAMap;
    private Polyline mPolyline;
    private Button backBtn;

    private Context mContext;

    int permissionCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_historymap_playback);

        mContext = this;
        try {

            initPermission();
            updatePrivacyShow(this);

            WindowManager m = getWindowManager();
            Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
            Constant.height = d.getHeight();
            Constant.weight = d.getWidth();

            locationClient = new AMapLocationClient(this);
            mMapView = (MapView) findViewById(R.id.map);
            mMapView.onCreate(savedInstanceState);

            init();
            initView();

//        freshLocationLineData();
            mAMap.moveCamera(CameraUpdateFactory.changeBearing(0f));// 上北下南
            mAMap.moveCamera(CameraUpdateFactory.changeTilt(0.0f));// 2D

            //设置中心点和缩放比例
            mAMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(22.7731880547, 113.9171841004)));
            mAMap.moveCamera(CameraUpdateFactory.zoomTo(14)); // 设置缩放级别
//        画线
            List<LatLng> latLngs = new ArrayList<LatLng>();

            String latlngs = (String) SharedPreferencesUtils.getParam(mContext, "latlngs", "");
            //Log.i("jackietu","latlngs:"+latlngs);
            //有轨迹
            if (latlngs != "") {
                try {
                    JSONArray jsonArray = new JSONArray(latlngs);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        latLngs.add(new LatLng(jsonObject.getDouble("lat"), jsonObject.getDouble("lng")));
                    }
                    mPolyline = mAMap.addPolyline(new PolylineOptions().
                            addAll(latLngs).width(10).color(ContextCompat.getColor(mContext, R.color.color1)));
                    mAMap.moveCamera(CameraUpdateFactory.changeLatLng(latLngs.get(0)));
                    addMarkersToMap(latLngs.get(0), 0);  //绘制起点
                    addMarkersToMap(latLngs.get(latLngs.size() - 1), 1); //绘制终点
                } catch (JSONException e) {
                    Toast.makeText(mContext, "解析轨迹数据异常", Toast.LENGTH_SHORT).show();
//                    e.printStackTrace();
                }
            }

        } catch (Exception ex) {

        }
    }

    public static void updatePrivacyShow(Context context){
        AMapLocationClient.updatePrivacyShow(context,true,true);
        AMapLocationClient.updatePrivacyAgree(context,true);
    }

    /**
     * 初始化AMap对象
     */
    private void init() {
        if (mAMap == null) {
            mAMap = mMapView.getMap();

//            MyLocationStyle myLocationStyle;
//            myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
//            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
//            myLocationStyle.interval(5000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
//            mAMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
//            mAMap.getUiSettings().setMyLocationButtonEnabled(true); //设置默认定位按钮是否显示，非必需设置。
//            mAMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
//
//            AMapLocation lastLocation = locationClient.getLastKnownLocation();
//            if(lastLocation!=null){
//                mAMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude())),250,null);
//                mAMap.moveCamera(CameraUpdateFactory.zoomTo(mAMap.getMaxZoomLevel()-2)); // 设置缩放级别
//            }else{
//                Log.e(TAG, "未获取到最后的定位信息，重新定位");
//
//                AMapLocationClientOption option = new AMapLocationClientOption();
//                option.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.SignIn);
//                locationClient.setLocationOption(option);
//                //设置定位监听
//                locationClient.setLocationListener(this);
//                locationClient.startLocation();
//            }
            mAMap.setOnMarkerClickListener(this);// 设置点击marker事件监听器
        }
    }

    private void initView(){
        backBtn = (Button) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(this);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
        //Log.e(TAG, "onResume。。。。。。。");
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();

        //销毁时，需要销毁定位client
        if(null != locationClient){
            locationClient.onDestroy();
        }
    }
    /**
     *  个性化定制的信息窗口视图的类
     *  如果要定制化渲染这个信息窗口，需要重载getInfoWindow(Marker)方法。
     *  如果只是需要替换信息窗口的内容，则需要重载getInfoContents(Marker)方法。
     */
    AMap.InfoWindowAdapter infoWindowAdapter = new AMap.InfoWindowAdapter(){

        // 个性化Marker的InfoWindow 视图
        // 如果这个方法返回null，则将会使用默认的信息窗口风格，内容将会调用getInfoContents(Marker)方法获取
        @Override
        public View getInfoWindow(Marker marker) {

            return getInfoWindowView(marker);
        }

        // 这个方法只有在getInfoWindow(Marker)返回null 时才会被调用
        // 定制化的view 做这个信息窗口的内容，如果返回null 将以默认内容渲染
        @Override
        public View getInfoContents(Marker marker) {

            return getInfoWindowView(marker);
        }
    };

    LinearLayout infoWindowLayout;
    TextView title;
    TextView snippet;

    /**
     * 自定义View并且绑定数据方法
     * @param marker 点击的Marker对象
     * @return  返回自定义窗口的视图
     */
    private View getInfoWindowView(Marker marker) {
        if (infoWindowLayout == null) {
            infoWindowLayout = new LinearLayout(this);
            infoWindowLayout.setOrientation(LinearLayout.VERTICAL);
            title = new TextView(this);
            snippet = new TextView(this);
            title.setTextColor(Color.BLACK);
            snippet.setTextColor(Color.BLACK);
            infoWindowLayout.setBackgroundResource(R.drawable.infowindow_bg);

            infoWindowLayout.addView(title);
            infoWindowLayout.addView(snippet);
        }

        return infoWindowLayout;
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


    @Override
    public void onClick(View arg0) {
        // TODO Auto-generated method stub
        int i = arg0.getId();
        if (i == R.id.backBtn) {
            HistoryMapActivity.this.finish();

        } else if (i == R.id.visual_3d) {
            SharedPreferencesUtils.setParam(mContext,
                    Constant.SETTING_23D, 1);
            if (mAMap != null) {
                mAMap.moveCamera(CameraUpdateFactory.changeBearing(0f));// 上北下南
                mAMap.moveCamera(CameraUpdateFactory.changeTilt(30.0f));// 2D
            }
        } else if (i == R.id.visual_2d) {
            SharedPreferencesUtils.setParam(mContext,
                    Constant.SETTING_23D, 0);
            if (mAMap != null) {
                mAMap.moveCamera(CameraUpdateFactory.changeBearing(0f));// 上北下南
                mAMap.moveCamera(CameraUpdateFactory.changeTilt(0.0f));// 2D
            }
        } else {
        }
    }


    @Override
    public boolean onMarkerClick(Marker arg0) {
        // TODO Auto-generated method stub
        return false;
    }

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
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。
        for(int i = 0 ;i<permissions.length;i++){
            if(permissions[i].equals("android.permission.ACCESS_FINE_LOCATION")){
                if(grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    //打开允许定位的权限
                    //Log.e(TAG, "定位权限打开");

                    locationClient.startLocation();
                }else{
                    //未打开允许定位的权限
                    //Log.e(TAG, "定位权限拒绝");
                    permissionCount++;
                    if(permissionCount<4){
                        initPermission();
                    }
                }
            }
        }
    }

    @Override
    public void onLocationChanged(AMapLocation arg0) {
        if(mAMap!=null){
            mAMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(arg0.getLatitude(), arg0.getLongitude())),250,null);
            mAMap.moveCamera(CameraUpdateFactory.zoomTo(mAMap.getMaxZoomLevel()-2)); // 设置缩放级别
        }
    }
}
