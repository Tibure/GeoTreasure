package com.example.bonneappligeo;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

public class MapService extends JobIntentService {
    private static final String TAG = "MapService";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {

    }
}
