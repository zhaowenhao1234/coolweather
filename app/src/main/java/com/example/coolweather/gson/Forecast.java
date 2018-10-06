package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class
Forecast {
    /**
     * 预报时间
     */
    @SerializedName("date")
    public  String date;

    /**
     * 最高温度
     */
    @SerializedName("tmp_max")
    public String tempMax;
    /**
     * 最低温度
     */
    @SerializedName("tmp_min")
    public String tempMin;
    /**
     * 白天天气情况
     */
    @SerializedName("cond_txt_d")
    public String infoDay;
}
