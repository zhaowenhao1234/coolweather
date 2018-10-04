package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {
    /**
     * 查询城市名称
     */
    @SerializedName("location")
    public String cityName;
    /**
     * 城市天气id
     */
    @SerializedName("cid")
    public String weatherId;
}
