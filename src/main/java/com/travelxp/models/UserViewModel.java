package com.travelxp.models;

import java.time.LocalDate;

public class UserViewModel {
    private int id;
    private String username;
    private String email;
    private LocalDate birthday;
    private String bio;
    private String profileImage;
    private String role;
    private int xp;
    private int level;
    private String title;

    public UserViewModel(User user, Gamification gamification) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.birthday = user.getBirthday();
        this.bio = user.getBio();
        this.profileImage = user.getProfileImage();
        this.role = user.getRole();
        if (gamification != null) {
            this.xp = gamification.getXp();
            this.level = gamification.getLevel();
            this.title = gamification.getTitle();
        }
    }

    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public LocalDate getBirthday() { return birthday; }
    public String getBio() { return bio; }
    public String getProfileImage() { return profileImage; }
    public String getRole() { return role; }
    public int getXp() { return xp; }
    public int getLevel() { return level; }
    public String getTitle() { return title; }
}
