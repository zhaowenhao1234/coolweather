package com.example.qingweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.example.qingweather.db.MydatabaseHelper;
import com.example.qingweather.gson.Weather;
import com.example.qingweather.util.HttpUtil;
import com.example.qingweather.util.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;



public class AutoUpdateService extends Service {
    public MydatabaseHelper dbHelper;
    public SQLiteDatabase db;

    public AutoUpdateService() {

    }

    @Override
    public void onCreate() {
        dbHelper = new MydatabaseHelper(AutoUpdateService.this, "WeatherInfo.db", null, 1);
        db = dbHelper.getReadableDatabase();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateBingpic();
        AlarmManager manager=(AlarmManager)getSystemService(ALARM_SERVICE);
        int anHour=12*60*60*1000;
        long triggerAtTime=SystemClock.elapsedRealtime()+anHour;
        Intent i=new Intent(this,AutoUpdateService.class);
        PendingIntent pi=PendingIntent.getService(this,0,i,0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent, flags, startId);
    }
    /**
     * 更新天气数据
     */
    private void updateWeather(){
        final ContentValues contentValues = new ContentValues();
        List<String> url = new ArrayList<>();
        contentValues.clear();
        url.clear();
        //数据库查询现在当前的天气
        Cursor cursor=db.query("weather",null,null,
                null,null,null,null);
        if(cursor.getCount()!=0){
            cursor.moveToLast();
            final String weatherId=cursor.getString(cursor.getColumnIndex("weatherid"));
            //和风天气API接口
            String nowUrl = "https://free-api.heweather.com/s6/weather/now?location="
                    + weatherId + "&key=06761d21d36044c0946c0061794423f6";
            url.add(nowUrl);
            String aqiUrl = "https://free-api.heweather.com/s6/air/now?location="
                    + weatherId + "&key=06761d21d36044c0946c0061794423f6";
            url.add(aqiUrl);
            String forecastUrl = "https://free-api.heweather.com/s6/weather/forecast?location="
                    + weatherId + "&key=06761d21d36044c0946c0061794423f6";
            url.add(forecastUrl);
            String lifeStyleUrl = "https://free-api.heweather.com/s6/weather/lifestyle?location="
                    + weatherId + "&key=06761d21d36044c0946c0061794423f6";
            url.add(lifeStyleUrl);
            cursor.close();
            for(int i=0;i<4;i++){
                final int finalI=i;
                HttpUtil.sendOkHttpRequest(url.get(i), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseText = response.body().string();
                        Weather weather = Utility.handleWeatherResponse(responseText);
                        switch (finalI) {
                            case 0:
                                contentValues.put("now", responseText);
                                break;
                            case 1:
                                if (weather.status.equals("ok")) {
                                    contentValues.put("aqi", responseText);
                                } else if (weather.status.equals("permission denied")) {
                                    contentValues.put("aqi", "暂无数据");
                                }
                                break;
                            case 2:
                                contentValues.put("forecast", responseText);
                                break;
                            case 3:
                                contentValues.put("lifestyle", responseText);
                                break;
                        }
                        db.update("weather",contentValues,"weatherId = ?",new String[]{weatherId});
                    }
                });
            }

        }
    }
    /**
     * 更新必应每日一图
     */
    private void updateBingpic(){
        String requestBingPic="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingPic=response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
            }
        });
    }
}
