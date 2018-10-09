package com.example.qingweather;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.qingweather.db.MydatabaseHelper;

public class MainActivity extends AppCompatActivity {

    private MydatabaseHelper dbHelper;
    private SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new MydatabaseHelper(this, "WeatherInfo.db", null, 1);
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("weather", null,
                null, null, null, null, null);
        if(cursor.getCount()>0){
            cursor.close();
            Intent intent=new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
