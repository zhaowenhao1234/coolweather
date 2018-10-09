package com.example.qingweather.gson;

import com.google.gson.annotations.SerializedName;

public class AQI {
    /**
     *空气质量
     */
    @SerializedName("qlty")
    public String aqi;
    /**
     * PM2.5
     */
    @SerializedName("pm25")
    public String pm25;
}
