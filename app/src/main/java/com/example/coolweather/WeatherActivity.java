package com.example.coolweather;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.coolweather.gson.Forecast;
import com.example.coolweather.gson.Lifestyle;
import com.example.coolweather.gson.Weather;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

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

    private String mWeatherId;

    private List<String> url=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_weather);
        // 初始化各控件
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        suggestionLayout = (LinearLayout) findViewById(R.id.suggestion_layout);
        aqiText = (TextView) findViewById(R.id.aqi_txt);
        pm25Text = (TextView) findViewById(R.id.pm25_txt);
        // 无缓存时去服务器查询天气
        mWeatherId = getIntent().getStringExtra("weather_id");
        weatherLayout.setVisibility(View.INVISIBLE);
        requestWeather(mWeatherId);
    }


    /**
     * 根据天气id请求城市天气信息。
     */
    public void requestWeather(final String weatherId) {

        url = new ArrayList<>();
        String weatherUrl = "https://free-api.heweather.com/s6/weather/now?location="
                + weatherId + "&key=06761d21d36044c0946c0061794423f6";
        url.add(weatherUrl);
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
                            if (weather != null && "ok".equals(weather.status)) {
                                showWeatherInfo(weather, finalI);
                            } else {
                                Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                                showWeatherInfo(null,1);
                            }
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
                        }
                    });
                }
            });
        }
    }

    /**
     * 处理并展示Weather实体类中的数据。
     */
    private void showWeatherInfo(Weather weather, int num) {

        switch (num) {
            case 0:
                String cityName = weather.basic.cityName;
                String updateTime=weather.upate.updateTime.split(" ")[1];
                String degree = weather.now.temprature + "℃";
                String weatherInfo = weather.now.info;
                titleCity.setText(cityName);
                titleUpdateTime.setText(updateTime);
                degreeText.setText(degree);
                weatherInfoText.setText(weatherInfo);
                break;
            case 1:
                if(weather!=null){
                    aqiText.setText(weather.aqi.aqi);
                    pm25Text.setText(weather.aqi.pm25);
                }else{
                    aqiText.setText("暂无数据");
                    pm25Text.setText("暂无数据");
                }
                break;
            case 2:
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
                for (Lifestyle lifestyle : weather.lifestyleList) {
                    View view = LayoutInflater.from(this).inflate(R.layout.suggestion_item, suggestionLayout, false);
                    TextView dateText = (TextView) view.findViewById(R.id.brf_txt);
                    TextView infoText = (TextView) view.findViewById(R.id.txt);
                    dateText.setText(lifestyle.lifeIndex);
                    infoText.setText(lifestyle.info);
                    suggestionLayout.addView(view);
                }
                weatherLayout.setVisibility(View.VISIBLE);
                url.clear();
                break;
        }
    }
}