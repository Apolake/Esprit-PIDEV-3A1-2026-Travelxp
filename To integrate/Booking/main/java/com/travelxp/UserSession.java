package com.travelxp;

import com.travelxp.models.User;

public class UserSession {
    private User user;

    public UserSession(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
