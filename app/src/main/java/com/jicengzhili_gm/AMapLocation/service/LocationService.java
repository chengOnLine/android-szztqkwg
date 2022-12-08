package com.jicengzhili_gm.AMapLocation.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.jicengzhili_gm.AMapLocation.bean.LatLonPoint;
import com.jicengzhili_gm.AMapLocation.bean.LocationBean;
import com.jicengzhili_gm.AMapLocation.db.SharedPreferencesUtils;
import com.jicengzhili_gm.AMapLocation.db.SqlIteOperate;
import com.jicengzhili_gm.AMapLocation.net.HttpControl;
import com.jicengzhili_gm.AMapLocation.net.OkHttpClientManager;
import com.jicengzhili_gm.AMapLocation.util.Constant;
import com.jicengzhili_gm.AMapLocation.util.Converter;
import com.jicengzhili_gm.AMapLocation.util.JsonUtils;
import com.jicengzhili_gm.AMapLocation.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Response;

/**
 * Created by 西瓜 on 2018/9/10.
 */

public class LocationService extends Service {
    private static final String TAG = "LocationService";
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;

    private Intent alarmIntent = null;
    private PendingIntent alarmPi = null;
    private AlarmManager alarm = null;

    private Context mContext;

    //实时上传数量
    public static int uploadNum = 10;

    public static int upIndex = 1;//记录上传失败次数
    public static boolean isuploading = false;//判断是否正在上传，true 网络请求，正在上传中；false 没有网络请求

    //保存最后一个有效位置
    private LocationBean mLastLocationBean = null;

    //保存历史位置坐标点
//	private ArrayList<LocationBean> historyLocation = new ArrayList<LocationBean>();

    private float allMileage = 0.0f;

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

//        startForeground(1, new Notification());// 服务前台运行

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String CHANNEL_ID = "120";
            String CHANNEL_NAME = "治理通服务";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);

            Notification notification = new Notification.Builder(getApplicationContext(), CHANNEL_ID).build();
            startForeground(1, notification);
        } else {
            startForeground(1, new Notification());// 服务前台运行
        }

//		Log.e(TAG, "测试是否进入service...");
        mContext = this;
        Constant.isServiceRunning = true;
        Utils.write_log("service onCreate...");

        try {
            locationClient = new AMapLocationClient(this.getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        locationOption = new AMapLocationClientOption();
        // 设置定位模式为高精度模式
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        // 设置是否需要显示地址信息
//		locationOption.setNeedAddress(true);
        /**
         * 设置是否优先返回GPS定位结果，如果30秒内GPS没有返回定位结果则进行网络定位
         * 注意：只有在高精度模式下的单次定位有效，其他方式无效
         */
//		locationOption.setGpsFirst(true);
        // 设置发送定位请求的时间间隔,最小值为1000，如果小于1000，按照1000算
        locationOption.setInterval(5000);

        // 设置定位监听
        locationClient.setLocationListener(mAMapLocationListener);

        // 创建Intent对象，action为LOCATION
        alarmIntent = new Intent();//检测前
        alarmIntent.setAction("LOCATION");

        // 定义一个PendingIntent对象，PendingIntent.getBroadcast包含了sendBroadcast的动作。
        // 也就是发送了action 为"LOCATION"的intent
        alarmPi = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        // AlarmManager对象,注意这里并不是new一个对象，Alarmmanager为系统级服务
        alarm = (AlarmManager) getSystemService(ALARM_SERVICE);

        //动态注册一个广播
        IntentFilter filter = new IntentFilter();
        filter.addAction("LOCATION");
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(alarmReceiver, filter, "com.android.permission.RECV_2", null);

        allMileage = (float) SharedPreferencesUtils.getParam(mContext, "curMileage", 0.0f);

        // 设置定位参数
        locationClient.setLocationOption(locationOption);
        // 启动定位
        locationClient.startLocation();
        mHandler.sendEmptyMessage(Utils.MSG_LOCATION_START);

        if (null != alarm) {
            //设置一个闹钟，2秒之后每隔一段时间执行启动一次定位程序
            alarm.cancel(alarmPi);
            alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 5000, alarmPi);
        }

        acquireWakeLock();

//        mHandler.sendEmptyMessageDelayed(Utils.MSG_UPDATE_ONLINE_STATE, 1000 * 60 * 10);
    }

    PowerManager.WakeLock mWakeLock;// 电源锁

    /**
     * onCreate时,申请设备电源锁
     */
    private void acquireWakeLock() {

        if (null == mWakeLock) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                    | PowerManager.ON_AFTER_RELEASE, "myService2");
            if (null != mWakeLock) {
                mWakeLock.acquire();
                Log.e("alen","申请电源锁成功");
            }
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Utils.write_log("service onStartCommand...");
        return super.onStartCommand(intent, flags, startId);
    }

    Handler mHandler = new Handler() {
        public void dispatchMessage(final android.os.Message msg) {

            try{
            switch (msg.what) {
                //开始定位
                case Utils.MSG_LOCATION_START:
                    //Log.e("alen", "正在定位...");
                    break;
                // 定位完成
                case Utils.MSG_LOCATION_FINISH:

                    AMapLocation loc = (AMapLocation) msg.obj;
                    String result = Utils.getLocationStr(loc);

                    Log.e("alen", "定位完成...result：" + result);
//                    Log.e("alen", "getErrorCode: "+String.valueOf(loc.getErrorCode()));
//                    Log.e("alen", String.valueOf(loc.getProvider().equals("gps")) + ",accuracy: " + String.valueOf(loc.getAccuracy()));
//                    Log.e("alen", "speed: " + String.valueOf(loc.getSpeed()));
//                    Log.e("alen", "lat: " + loc.getLatitude() + ", lon: " + loc.getLongitude());

                    if (loc.getErrorCode() == 0) {
//                        if (loc.getProvider().equals("gps") && loc.getAccuracy() > 40.0f) {
//                            Utils.write_log("gps 精度大于10的点直接过滤");
//                            return;
//                        }
//
//                        if (loc.getSpeed() > 38) {
//                            Utils.write_log("速度大于38的直接过滤");
//                            return;
//                        }
//
//                        //不在光明区内的去掉
//                        if (loc.getLatitude() > 24 || loc.getLatitude() < 22 ||
//                                loc.getLongitude() > 114 || loc.getLongitude() < 113) {
//                            Utils.write_log("去除不在光明区范围内的点");
//                            return;
//                        }

                        String signAddress = (String) SharedPreferencesUtils.getParam(mContext, "signAddress", "");
                        if (signAddress == "") {
                            SharedPreferencesUtils.setParam(mContext, "signAddress", loc.getAddress());
                        }

                        LocationBean bean = new LocationBean();
                        bean.setLongitude(loc.getLongitude());
                        bean.setLatitude(loc.getLatitude());
                        bean.setAddress(loc.getAddress());
                        bean.setTime(Utils.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss"));
                        bean.setDate(Utils.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd"));
                        bean.setStateType(2);

                        if (mLastLocationBean == null) {
                            mLastLocationBean = bean;
                        } else {

                            LatLng latLng1 = new LatLng(mLastLocationBean.getLatitude(), mLastLocationBean.getLongitude());
                            LatLng latLng2 = new LatLng(bean.getLatitude(), bean.getLongitude());
                            float distance = AMapUtils.calculateLineDistance(latLng1, latLng2);

//                            float effectiveDis = loc.getSpeed() * 2;
//                            Log.e("alen", "两点间距：" + distance);

                            mLastLocationBean = bean;

                            if (distance > 0) { //判断是否是有效距离
                                Log.e("alen", "定位完成1: "+loc.getAddress());
                                Log.e("alen", "有效距离：" + distance + ";进行添加");

                                allMileage += distance;
                                SharedPreferencesUtils.setParam(mContext, "curMileage", allMileage);

                                LatLonPoint wgsPoint = Converter.toWGS84Point(loc.getLatitude(), loc.getLongitude());
                                LocationBean uploadBean = new LocationBean();
                                uploadBean.setTotalMileage(allMileage);
                                uploadBean.setLongitude(wgsPoint.getLongitude());
                                uploadBean.setLatitude(wgsPoint.getLatitude());
                                uploadBean.setTime(Utils.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss"));

                                Constant.upLoadLocation.add(uploadBean);
                                Message msg1 = new Message();
                                msg1.what = Utils.MSG_LOCATION_UPLOAD;
                                msg1.sendingUid = 0;
                                mHandler.sendMessageDelayed(msg1, 500 * upIndex);

                                Log.e("alen", "locIntent FRESH_LOCATION_POINT");
                                Intent locIntent = new Intent("FRESH_LOCATION_POINT");
                                locIntent.putExtra("location", bean);
                                sendBroadcast(locIntent, "com.android.permission.RECV_2");
                                Log.e("alen", "locIntent FRESH_LOCATION_POINT END");

                                SqlIteOperate.saveLocation(mContext, bean, true);
                            }
                        }
                    }
                    break;
                //停止定位
                case Utils.MSG_LOCATION_STOP:
                    //Log.e(TAG, "定位停止");
                    break;

                //上传定位信息
                case Utils.MSG_LOCATION_UPLOAD:

                    Log.e("alen", "MSG_LOCATION_UPLOAD：" + Constant.upLoadLocation.size());
                    if (Constant.upLoadLocation.size() < uploadNum || isuploading) {
                        break;
                    }
                    isuploading = true;//设为正在上传
                    final LocationBean tmpLocationBean = Constant.upLoadLocation.get(0);
//						Log.d("轨迹缓存", String.valueOf(Constant.upLoadLocation));
                    ArrayList<LocationBean> historyLocation = new ArrayList<LocationBean>();
                    Integer locaNum = 0;

                    if (msg.sendingUid == 0) {
                        locaNum = uploadNum;
                    } else {
                        locaNum = Constant.upLoadLocation.size();
                    }
                    for (int i = 0; i < locaNum; i++) {
                        historyLocation.add(Constant.upLoadLocation.get(i));
                    }
//						historyLocation.add(tmpLocationBean);
                    String jsonData = JsonUtils.getJsonDataFromLocation(historyLocation);

                    final Integer finalLocaNum = locaNum;
                    Log.e("alen", "坐标开始上传：" + jsonData);

                    HttpControl.doPostLocationDataControl(jsonData, allMileage, new OkHttpClientManager.HttpCallBack() {
                        @Override
                        public void onSuccess(Response response) {
                            isuploading = false;
                            // TODO Auto-generated method stub
                            try {
                                final String res = response.body().string();
                                Log.e("alen", "坐标上传成功：" + res);

                                JSONObject responseobj = new JSONObject(res);
                                boolean isSuc = responseobj.getBoolean("flag");
                                if (isSuc) {
                                    for (int i = 0; i < finalLocaNum; i++) {
                                        Constant.upLoadLocation.remove(0);
                                    }
                                    upIndex = 1;
                                } else {
                                    upIndex++;
                                }
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                                upIndex++;
                                return;
                            } catch (JSONException e) {
                                upIndex++;
                                e.printStackTrace();

                                return;
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            isuploading = false;
                            upIndex++;
                        }
                    });
                    break;

                case Utils.MSG_UPDATE_QIANTUI_SUCCESS:  //签退成功
//                    Intent mIntent = new Intent("FRESH_QIANTUI_STATE");
//                    sendBroadcast(mIntent);
//
//                    //停止服务
//                    stopSelf();
                    break;
//				case Utils.MSG_UPDATE_QIANTUI_FAIL: //签退失败
//					break;
                default:
                    break;
                }
            }catch (Exception ex){
                Log.e("alen", ex.getMessage());
            }
        };
    };

    /***
     * 高德位置更新回调
     */
    private AMapLocationListener mAMapLocationListener = new AMapLocationListener() {

        @Override
        public void onLocationChanged(AMapLocation loc) {
            if (null != loc) {
                Message msg = mHandler.obtainMessage();
                msg.obj = loc;
                msg.what = Utils.MSG_LOCATION_FINISH;
                mHandler.sendMessage(msg);
            }
        }
    };

    private BroadcastReceiver alarmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("LOCATION")) {
                if (null != locationClient) {
                    //Log.e("alen", "收到location广播123");
                    locationClient.startLocation();

                    long lastSignTime = (long) SharedPreferencesUtils.getParam(mContext, "signTime", System.currentTimeMillis());
                    long curTime = System.currentTimeMillis();
                    if ((curTime - lastSignTime) > (12L * 60L * 60L * 1000L)) {
                        //Log.e("alen", "签到超过12小时，执行签退操作");

//						doQiantui();
                    } else {
                        alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 5000, alarmPi);
                    }
                }
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                //Log.e("alen", "收到ACTION_SCREEN_OFF广播");
                //灭屏启动
                //MyApplication.startKeepLiveActivity(context);
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                //Log.e("alen", "收到ACTION_SCREEN_ON广播");
                //亮屏取消
                context.sendBroadcast(new Intent("FINISH_LIVE_ACTIVITY"));
            }
        }
    };


    @Override
    public void onDestroy() {
        Utils.write_log("service onDestroy...");
        //Log.e(TAG, "service onDestroy...");
        Constant.isServiceRunning = false;
        mHandler.removeMessages(Utils.MSG_UPDATE_ONLINE_STATE);

        if (null != locationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            locationClient.onDestroy();
            locationClient = null;
            locationOption = null;
        }

        alarm.cancel(alarmPi);

        if (null != alarmReceiver) {
            unregisterReceiver(alarmReceiver);
            alarmReceiver = null;
        }

        stopForeground(true);

        releaseWakeLock();
    }

    /**
     * onDestroy时，释放设备电源锁
     */
    private void releaseWakeLock() {
        if (null != mWakeLock) {
            mWakeLock.release();
            mWakeLock = null;
            Log.e("alen","关闭电源锁");
        }
    }
}
