package com.travelxp.utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    private static final int COST_FACTOR = 12;

    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(COST_FACTOR));
    }

    public static boolean checkPassword(String password, String hashed) {
        return BCrypt.checkpw(password, hashed);
    }
}
