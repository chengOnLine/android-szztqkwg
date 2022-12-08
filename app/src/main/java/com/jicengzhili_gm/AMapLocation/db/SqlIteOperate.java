package com.jicengzhili_gm.AMapLocation.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jicengzhili_gm.AMapLocation.bean.LocationBean;

import java.util.ArrayList;

/**
 * Created by 西瓜 on 2018/9/10.
 * SQLite数据库操作
 */

public class SqlIteOperate {
    private static Object obj=new Object();

    //SQLITE数据库操作类

    private DBHelper dbHelp;//得到数据库连接
    private SQLiteDatabase db;//数据库操作对象

    //location_record 表
    public static final String LOCATIONRECORD_TABLE_NAME="LOCATIONRECORDS";
    public static final String LOCATION_ID="_id";
    public static final String LONGITUDE="longitude";
    public static final String LATITUDE="latitude";
    public static final String ADDRESS="address";
    public static final String TIME="time";
    public static final String DATE="locationdate";
    public static final String STATE_TYPE="state";//1:：签到  2：记录中  3：签退
    public static final String CREATE_LOCATIONRECORD_TABLE="CREATE TABLE IF NOT EXISTS "
            + LOCATIONRECORD_TABLE_NAME+" ( "
            + LOCATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
            + LONGITUDE + " text, "
            + LATITUDE + " text, "
            + ADDRESS + " TEXT, "
            + TIME + " text, "
            + DATE + " text, "
            + STATE_TYPE + " INTEGER)";

    public SqlIteOperate(){super();}

    public SqlIteOperate(Context mContext){
        super();
        dbHelp=new DBHelper(mContext);
    }

    /***************************Location record ADQU(增删查改)**************************************/
    /*
        获取所有的Location Record记录
     */
    public static ArrayList<LocationBean> getAllLocationRecords(Context mContext){
        DBHelper dbHelper=new DBHelper(mContext);
        SQLiteDatabase db=dbHelper.getReadableDatabase();

        ArrayList<LocationBean> list=new ArrayList<LocationBean>();
        LocationBean tmpLocationBean=null;

        synchronized (obj){
            Cursor cursor=db.query(SqlIteOperate.LOCATIONRECORD_TABLE_NAME,new String[]{LOCATION_ID,LONGITUDE,LATITUDE,ADDRESS,TIME,DATE,STATE_TYPE},null,null,null,null,"time asc");

            while(cursor.moveToNext()){
                tmpLocationBean=new LocationBean();
                tmpLocationBean.setLocationId(cursor.getInt(0));
                tmpLocationBean.setLongitude(Double.parseDouble(cursor.getString(1)));
                tmpLocationBean.setLatitude(Double.parseDouble(cursor.getString(2)));
                tmpLocationBean.setAddress(cursor.getString(3));
                tmpLocationBean.setTime(cursor.getString(4));
                tmpLocationBean.setDate(cursor.getString(5));
                tmpLocationBean.setStateType(cursor.getInt(6));
                list.add(tmpLocationBean);
            }

            if(cursor!=null){
                cursor.close();
            }
            if(db!=null){
                db.close();
            }

            if(dbHelper!=null){
                dbHelper.close();
            }

            return list;
        }
    }

    /*
        获取最前一条记录
     */
    public static LocationBean getFirstLocationRecord(Context mContext){
        DBHelper dbHelper=new DBHelper(mContext);
        SQLiteDatabase db=dbHelper.getReadableDatabase();

        LocationBean tmpLocationBean=null;
        synchronized (obj) {
            Cursor cursor = db.query(SqlIteOperate.LOCATIONRECORD_TABLE_NAME, new String[]{LOCATION_ID, LONGITUDE, LATITUDE, ADDRESS, TIME, DATE, STATE_TYPE}, null, null, null, null, "locationdate asc");
            if (cursor != null) {
                boolean isMoveTo = cursor.moveToFirst();
                if (isMoveTo) {
                    tmpLocationBean = new LocationBean();
                    tmpLocationBean.setLocationId(cursor.getInt(0));
                    tmpLocationBean.setLongitude(Double.parseDouble(cursor.getString(1)));
                    tmpLocationBean.setLatitude(Double.parseDouble(cursor.getString(2)));
                    tmpLocationBean.setAddress(cursor.getString(3));
                    tmpLocationBean.setTime(cursor.getString(4));
                    tmpLocationBean.setDate(cursor.getString(5));
                    tmpLocationBean.setStateType(cursor.getInt(6));
                }
            }

            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }

            if (dbHelper != null) {
                dbHelper.close();
            }
        }

        return tmpLocationBean;
    }

    /*
        获取某天的Location Record记录
     */
    public static ArrayList<LocationBean> getDateLocationRecords(Context mContext,String date){
        DBHelper dbHelper = new DBHelper(mContext);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        ArrayList<LocationBean> list = new ArrayList<LocationBean>();
        LocationBean tmpLocationBean = null;

        synchronized (obj) {
            Cursor cursor = db.query(SqlIteOperate.LOCATIONRECORD_TABLE_NAME, new String[]{LOCATION_ID,LONGITUDE,LATITUDE,ADDRESS,TIME,DATE,STATE_TYPE} ,"locationdate = ?", new String[]{date}, null, null, "time asc");

            if(cursor != null){
                while(cursor.moveToNext()){
                    tmpLocationBean = new LocationBean();
                    tmpLocationBean.setLocationId(cursor.getInt(0));
                    tmpLocationBean.setLongitude(Double.parseDouble(cursor.getString(1)));
                    tmpLocationBean.setLatitude(Double.parseDouble(cursor.getString(2)));
                    tmpLocationBean.setAddress(cursor.getString(3));
                    tmpLocationBean.setTime(cursor.getString(4));
                    tmpLocationBean.setDate(cursor.getString(5));
                    tmpLocationBean.setStateType(cursor.getInt(6));
                    list.add(tmpLocationBean);
                }
            }

            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }

            if(dbHelper!=null){
                dbHelper.close();
            }
        }

        return list;
    }

    /*
        保存 Location 信息
     */
    public static void saveLocation(Context mContext,LocationBean bean,boolean isCover) {
//		if(isCover){
//			boolean isExists = queryLocationExists(mContext, bean);
//			if(!isExists){
//				Log.e("alen", "经纬度存在...");
//				return;
//			}
//		}

        DBHelper dbHelper = new DBHelper(mContext);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        if(bean.getLatitude()>0 && bean.getLongitude()>0){
            try{
                synchronized (obj) {
                    String sql = "insert into "+SqlIteOperate.LOCATIONRECORD_TABLE_NAME+"(longitude,latitude,address,time,locationdate,state) values (?,?,?,?,?,?)";
                    Object[] bindArgs = {bean.getLongitude()+"",bean.getLatitude()+"",bean.getAddress(),bean.getTime(),bean.getDate(),bean.getStateType()};
                    db.execSQL(sql, bindArgs);

                    if(db!=null){
                        db.close();
                    }

                    if(dbHelper!=null){
                        dbHelper.close();
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    /*
        根据经纬度查询该Location是否存在
     */
    public static boolean queryLocationExists(Context mContext,LocationBean bean) {
        DBHelper dbHelper = new DBHelper(mContext);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        synchronized (obj) {
            String sql = "select count(*)  from "+SqlIteOperate.LOCATIONRECORD_TABLE_NAME+" where longitude=? and latitude =?";
            Cursor cursor = db.rawQuery(sql, new String[] {bean.getLongitude()+"",bean.getLatitude()+""});
            cursor.moveToFirst();
            int count = cursor.getInt(0);

            if(cursor!=null){
                cursor.close();
            }

            if(db!=null){
                db.close();
            }

            if(dbHelper!=null){
                dbHelper.close();
            }

            return count == 0;
        }
    }

    /*
        删除某天的Location Record
     */
    public static void delete(Context mContext,String date) {
        DBHelper dbHelper = new DBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        synchronized(obj){
            int deleteCount = db.delete(SqlIteOperate.LOCATIONRECORD_TABLE_NAME, "locationdate=?", new String[]{date});
            //Log.e("alen", "删除成功行数："+deleteCount);

            if(db!=null){
                db.close();
            }

            if(dbHelper!=null){
                dbHelper.close();
            }
        }
    }
}
