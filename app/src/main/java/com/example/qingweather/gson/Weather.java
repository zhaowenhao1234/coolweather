package com.example.qingweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Weather {
    public String status;

    public Basic basic;

    public Now now;

    @SerializedName("air_now_city")
    public AQI aqi;

    @SerializedName("lifestyle")
    public List<Lifestyle> lifestyleList;

    @SerializedName("update")
    public Upate upate;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}
