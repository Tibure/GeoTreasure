package com.example.bonneappligeo;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserScoreRecyclerAdapter extends RecyclerView.Adapter<UserScoreRecyclerAdapter.MyViewHolder>{
    private List<UserScore> userScores;

    public UserScoreRecyclerAdapter(List<UserScore> userScores) { this.userScores = userScores; }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_userscore, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        UserScore userScoreBind = userScores.get(position);
        holder.textViewUsername.setText(userScoreBind.getUsername());
        holder.textViewScore.setText(String.valueOf(userScoreBind.getScore()));
    }

    @Override
    public int getItemCount() {
        return userScores.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView textViewUsername;
        TextView textViewScore;
        public MyViewHolder(@NonNull View itemView){
            super(itemView);
            textViewUsername = itemView.findViewById(R.id.textView_cardUserScore_username);
            textViewScore = itemView.findViewById(R.id.textView_cardUserScore_score);
        }
    }
}
