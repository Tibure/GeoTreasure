package com.example.bonneappligeo;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class UserScore {

    private String username;
    private int score;
    private Date startDate;
    private Date endDate;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public long getTimeDiff(){
        long diffInMillies = endDate.getTime() - startDate.getTime();
        return TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }
}
