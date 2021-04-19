package com.example.bonneappligeo.scoreManager;

import androidx.annotation.NonNull;

import com.example.bonneappligeo.UserScore;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FirestoreScoreService implements ScoreService {
    FirebaseFirestore firestore_database;
    public FirestoreScoreService(){
        this.firestore_database = FirebaseFirestore.getInstance();
    }

    @Override
    public Task<Void> createScore(UserScore userScore){
        CollectionReference REF_Score = firestore_database.collection("UserScore");

        OnSuccessListener scoreSuccessListener = new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {

            }
        };
        OnFailureListener scoreFailureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                try {
                    throw new Exception(e.getMessage());
                } catch (Exception exception) {
                    exception.printStackTrace();
                }

            }
        };

        return REF_Score.add(userScore).addOnSuccessListener(scoreSuccessListener).addOnFailureListener(scoreFailureListener);
    }

    @Override
    public Task<List<UserScore>> getScores() {
        Continuation<QuerySnapshot, List<UserScore>> continuationUserScore = new Continuation<QuerySnapshot, List<UserScore>>() {
            @Override
            public List<UserScore> then(@NonNull Task<QuerySnapshot> task) throws Exception {
                if(task.isSuccessful()){
                    List<UserScore> userScores = new ArrayList<>();
                    for(QueryDocumentSnapshot document : task.getResult())
                        userScores.add(document.toObject(UserScore.class));
                    return  userScores;
                }else{
                    throw new Exception("Cannot return scores");
                }

            }
        };
        return firestore_database.collection("UserScore").orderBy("score", Query.Direction.DESCENDING)
                .get().continueWith(continuationUserScore);

    }
}
