package com.jicengzhili_gm.AMapLocation.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by 西瓜 on 2018/9/10.
 * SQLITE--本地数据库建库
 */

public class DBHelper extends SQLiteOpenHelper {
    public static final String DB_NAME="zhilitong.db";
    public static final int DB_VERSION=1;
    private Context mContext;

    //初始化对象，建库
    public DBHelper(Context context){
        super(context,DB_NAME,null,DB_VERSION);
        this.mContext=context;
    }

    //建表，只在数据库不存在的时候才执行创建表，数据库存在，则不会执行
    @Override
    public void onCreate(SQLiteDatabase db){db.execSQL(SqlIteOperate.CREATE_LOCATIONRECORD_TABLE);}

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
