package com.example.bonneappligeo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.bonneappligeo.scoreManager.ScoreFactory;
import com.example.bonneappligeo.scoreManager.ScoreService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {
    List<UserScore> userScores = new ArrayList<>();
    RecyclerView recyclerView;
    RecyclerView.Adapter recyclerViewAdapter;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ScoreService scoreService = ScoreFactory.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        recyclerView = findViewById(R.id.recyclerView_leaderboard_userscore);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerViewAdapter = new UserScoreRecyclerAdapter(userScores);
        recyclerView.setAdapter(recyclerViewAdapter);

        scoreService.getScores().addOnCompleteListener(new OnCompleteListener<List<UserScore>>() {
            @Override
            public void onComplete(@NonNull Task<List<UserScore>> task) {
                if(task.isSuccessful()){
                    Log.d("130491", "HEYYY");
                    userScores.addAll(task.getResult());
                    recyclerViewAdapter.notifyDataSetChanged();
                }else{
                    Toast.makeText(getApplicationContext(), "une erreur est survenu lors de la recuperation", Toast.LENGTH_SHORT).show();
                }
            }
        });


        setListener();
    }

    private void setListener() {
    }
}