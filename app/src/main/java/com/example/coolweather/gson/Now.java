package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class Now {
    @SerializedName("tmp")
    public String temprature;

    @SerializedName("cond_txt")
    public String info;
}
