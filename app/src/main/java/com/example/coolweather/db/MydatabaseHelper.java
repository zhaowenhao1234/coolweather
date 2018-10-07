package com.example.coolweather.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MydatabaseHelper extends SQLiteOpenHelper {


    private static final String CREAT_WEATHER="create table weather ("
            +"id integer primary key autoincrement,"
            +"weatherid txt,"
            +"now txt,"+"aqi txt,"+"forecast txt,"+"lifestyle txt)";

    private Context mContext;

    public MydatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        // TODO Auto-generated constructor stub
        mContext=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREAT_WEATHER);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists weather");
        onCreate(db);
    }
}
