package com.example.bonneappligeo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    //LinearLayout getHome_play = findViewById(R.id.linearLayout_main_leaderboards);

    @Override
    protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        LinearLayout home_play = findViewById(R.id.linearLayout_main_play);
        test(home_play);
    }

    private void test(LinearLayout linearLayout) {
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TreasureMapsActivity.class));
            }
        });
    }


}