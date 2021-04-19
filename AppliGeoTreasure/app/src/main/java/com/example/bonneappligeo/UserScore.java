package com.example.bonneappligeo;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class UserScore {

    private String username;
    private int treasuresFound;
    private Date startDate;
    private Date endDate;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getTreasuresFound() {
        return treasuresFound;
    }

    public void setTreasuresFound(int treasuresFound) {
        this.treasuresFound = treasuresFound;
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

    public long getScore(){
        long diffInMillies = endDate.getTime() - startDate.getTime();
        long score = (treasuresFound*1500)/(diffInMillies/100);
        return score;
    }

}
