package com.zackehh.javaspaces.util;

public class UserUtils {

    private static String username;

    public static String getCurrentUser(){
        return username;
    }

    public static void setCurrentUser(String user){
        username = user;
        System.out.println("Registered client for user: " + user);
    }

}
