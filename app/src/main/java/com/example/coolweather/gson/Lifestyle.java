package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class Lifestyle {
    /**
     * 生活指数简介
     */
    @SerializedName("brf")
    public String lifeIndex;

    /**
     * 生活指数详细内容
     */
    @SerializedName("txt")
    public String info;
    /**
     * 生活指数类型
     */
    @SerializedName("type")
    public String type;
}
