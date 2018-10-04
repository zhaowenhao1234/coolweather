package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class Forecast {
    /**
     * 预报时间
     */
    @SerializedName("date")
    public  String date;

    /**
     * 最高温度
     */
    @SerializedName("tem_max")
    public String tempMax;
    /**
     * 最低温度
     */
    @SerializedName("temp_min")
    public String tempMin;
    /**
     * 白天天气情况
     */
    @SerializedName("cond_txt_d")
    public String infoDay;
    /**
     * 夜间天气情况
     */
    @SerializedName("cond_txt_n")
    public String infoNight;
    /**
     * 风向
     */
    @SerializedName("wind_dir")
    public String windDirect;
    /**
     * 风速
     */
    @SerializedName("wind_sc")
    public String windSpeed;
}
