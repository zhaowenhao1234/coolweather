package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class Now {
    @SerializedName("tmp")
    public String temp;

    @SerializedName("cond_txt")
    public String info;
}
