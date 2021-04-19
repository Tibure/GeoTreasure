package com.example.bonneappligeo.scoreManager;

public class ScoreFactory {
    public static ScoreService getInstance(){return (ScoreService) new FirestoreScoreService();}

}
