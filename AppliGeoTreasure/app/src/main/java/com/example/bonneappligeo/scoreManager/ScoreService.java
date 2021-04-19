package com.example.bonneappligeo.scoreManager;

import com.example.bonneappligeo.UserScore;
import com.google.android.gms.tasks.Task;
import java.util.List;

public interface ScoreService {
    Task<Void> createScore(UserScore userScore);
    Task<List<UserScore>> getScores();

}
