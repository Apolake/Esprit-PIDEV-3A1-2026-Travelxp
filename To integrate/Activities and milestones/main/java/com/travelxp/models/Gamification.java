package com.travelxp.models;

public class Gamification {
    private int userId;
    private int xp;
    private int level;
    private String title;

    public Gamification() {}

    public Gamification(int userId, int xp, int level, String title) {
        this.userId = userId;
        this.xp = xp;
        this.level = level;
        this.title = title;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
