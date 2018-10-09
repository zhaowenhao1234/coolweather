package com.example.qingweather;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.example.qingweather.db.MydatabaseHelper;
import com.example.qingweather.gson.Forecast;
import com.example.qingweather.gson.Lifestyle;
import com.example.qingweather.gson.Weather;
import com.example.qingweather.service.AutoUpdateService;
import com.example.qingweather.util.HttpUtil;
import com.example.qingweather.util.Utility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import static com.example.qingweather.R.color.colorAccent;


public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private LinearLayout forecastLayout;

    private LinearLayout suggestionLayout;

    private TextView aqiText;

    private TextView pm25Text;

    private ImageView bingPicImg;

    public DrawerLayout drawerLayout;

    private Button nvaButton;

    private String mWeatherId;

    public SwipeRefreshLayout swipeRefresh;

    private List<String> url = null;
    private ContentValues contentValues = null;

    private MydatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private boolean flag;
    private int num = 0;

    private String[] Suggestions={"舒适度指数","穿衣指数","感冒指数" ,"运动指数","旅游指数","紫外线指数","洗车指数","空气污染指数"};

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置状态栏透明
        if(Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        dbHelper = new MydatabaseHelper(this, "WeatherInfo.db", null, 1);
        db = dbHelper.getReadableDatabase();
        // 初始化各控件
        drawerLayout=findViewById(R.id.drawer_layout);
        nvaButton=findViewById(R.id.nva_button);
        swipeRefresh=findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeColors(colorAccent);
        bingPicImg=(ImageView)findViewById(R.id.bing_pic_img);
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        suggestionLayout = (LinearLayout) findViewById(R.id.suggestion_layout);
        aqiText = (TextView) findViewById(R.id.aqi_txt);
        pm25Text = (TextView) findViewById(R.id.pm25_txt);

        mWeatherId = getIntent().getStringExtra("weather_id");
        //获取每日一图
        SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
        String bingPic=prefs.getString("bing_pic",null);
        if(bingPic!=null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else{
            loadBingPic();
        }
        //先查询本地数据库
        flag = requestFromDb(mWeatherId);
        if (flag == false) {
            // 无缓存时去服务器查询天气
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
                db.delete("weather","weatherid = ?",new String[]{mWeatherId});
            }
        });
        nvaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    /**
     * 加载必应每日一图
     */
    public void loadBingPic(){
        String requestBingPic="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic=response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }

    /**
     * 从数据库中查找天气信息
     */
    public boolean requestFromDb(final String weatherId) {
        if (weatherId == null) {
            Cursor cursor = db.query("weather", null,
                    null, null, null, null, null);
            if ((cursor.getCount()) > 0) {
                cursor.moveToLast();
                List<String> infoList = new ArrayList<>();
                String now = cursor.getString(cursor.getColumnIndex("now"));
                infoList.add(now);
                String aqi = cursor.getString(cursor.getColumnIndex("aqi"));
                infoList.add(aqi);
                String forecast = cursor.getString(cursor.getColumnIndex("forecast"));
                infoList.add(forecast);
                String lifestyle = cursor.getString(cursor.getColumnIndex("lifestyle"));
                infoList.add(lifestyle);
                cursor.close();
                for (int i = 0; i < 4; i++) {
                    final Weather weather = Utility.handleWeatherResponse(infoList.get(i));
                    final int finalI = i;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeatherInfo(weather, finalI);
                        }
                    });
                }
                infoList.clear();
                return true;
            } else {
                return false;
            }
        } else {
            Cursor cursor = db.query("weather", null,
                    "weatherid = ?", new String[]{weatherId}, null, null, null);
            if ((cursor.getCount()) > 0) {
                cursor.moveToFirst();
                List<String> infoList = new ArrayList<>();
                String now = cursor.getString(cursor.getColumnIndex("now"));
                infoList.add(now);
                String aqi = cursor.getString(cursor.getColumnIndex("aqi"));
                infoList.add(aqi);
                String forecast = cursor.getString(cursor.getColumnIndex("forecast"));
                infoList.add(forecast);
                String lifestyle = cursor.getString(cursor.getColumnIndex("lifestyle"));
                infoList.add(lifestyle);
                cursor.close();
                for (int i = 0; i < 4; i++) {
                    final Weather weather = Utility.handleWeatherResponse(infoList.get(i));
                    final int finalI = i;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mWeatherId=weather.basic.weatherId;
                            showWeatherInfo(weather, finalI);
                        }
                    });
                }
                infoList.clear();
                return true;
            } else {
                return false;
            }
        }
    }


    /**
     * 根据天气id请求城市天气信息。
     */
    public void requestWeather(final String weatherId) {
        contentValues = new ContentValues();
        url = new ArrayList<>();
        contentValues.clear();
        url.clear();
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

        for (int i = 0; i < 4; i++) {
            final int finalI = i;
            HttpUtil.sendOkHttpRequest(url.get(i), new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String responseText = response.body().string();
                    final Weather weather = Utility.handleWeatherResponse(responseText);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (weather != null) {
                                showWeatherInfo(weather, finalI);
                                num++;
                                //将信息存入数据库
                                switch (finalI) {
                                    case 0:
                                        contentValues.put("weatherid", weatherId);
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
                                //当所有的
                                if (num == 4) {
                                    db.insert("weather", null, contentValues);
                                    num = 0;
                                }
                            }
                            swipeRefresh.setRefreshing(false);
                        }
                    });
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                            swipeRefresh.setRefreshing(false);
                        }
                    });
                }
            });
        }
        loadBingPic();
    }

    /**
     * 处理并展示Weather实体类中的数据。
     */
    private void showWeatherInfo(Weather weather, int num) {

        switch (num) {
            case 0:
                String cityName = weather.basic.cityName;
                String updateTime = weather.upate.updateTime.split(" ")[1];
                String degree = weather.now.temp + "℃";
                String weatherInfo = weather.now.info;
                mWeatherId=weather.basic.weatherId;
                titleCity.setText(cityName);
                titleUpdateTime.setText(updateTime);
                degreeText.setText(degree);
                weatherInfoText.setText(weatherInfo);
                break;
            case 1:
                if (weather!=null&&!weather.status.equals("permission denied")) {
                    aqiText.setText(weather.aqi.aqi);
                    pm25Text.setText(weather.aqi.pm25);
                } else {
                    aqiText.setText("暂无数据");
                    pm25Text.setText("暂无数据");
                }
                break;
            case 2:
                forecastLayout.removeAllViews();
                for (Forecast forecast : weather.forecastList) {
                    View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
                    TextView dateText = (TextView) view.findViewById(R.id.date_txt);
                    TextView infoText = (TextView) view.findViewById(R.id.info_txt);
                    TextView maxText = (TextView) view.findViewById(R.id.max_txt);
                    TextView minText = (TextView) view.findViewById(R.id.min_txt);
                    dateText.setText(forecast.date);
                    infoText.setText(forecast.infoDay);
                    maxText.setText(forecast.tempMax);
                    minText.setText(forecast.tempMin);
                    forecastLayout.addView(view);
                }
                break;
            case 3:
                suggestionLayout.removeAllViews();
                int i=0;
                for (Lifestyle lifestyle : weather.lifestyleList) {
                    View view = LayoutInflater.from(this).inflate(R.layout.suggestion_item, suggestionLayout, false);
                    TextView dateText = (TextView) view.findViewById(R.id.brf_txt);
                    TextView infoText = (TextView) view.findViewById(R.id.txt);
                    dateText.setText(Suggestions[i++]+": "+lifestyle.lifeIndex);
                    infoText.setText("         "+lifestyle.info);
                    suggestionLayout.addView(view);
                }
                weatherLayout.setVisibility(View.VISIBLE);
                break;
        }
        Intent intent=new Intent(WeatherActivity.this,AutoUpdateService.class);
        startService(intent);
    }
}