package com.example.bonneappligeo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    LinearLayout linearLayoutMainLeaderboard, linearLayoutMainPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        initViews();
    }

    private void initViews() {
        linearLayoutMainPlay = findViewById(R.id.linearLayout_main_play);
        linearLayoutMainLeaderboard = findViewById(R.id.linearLayout_main_leaderboards);

        linearLayoutMainLeaderboard.setOnClickListener(this);
        linearLayoutMainPlay.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.linearLayout_main_play:
                startActivity(new Intent(MainActivity.this, TreasureMapsActivity.class));
                break;
            case R.id.linearLayout_main_leaderboards:
                startActivity(new Intent(MainActivity.this, LeaderboardActivity.class));
                break;
        }
    }

}