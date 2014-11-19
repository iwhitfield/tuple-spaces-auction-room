package com.zackehh.javaspaces;

import com.zackehh.javaspaces.util.SpaceUtils;

public class TestFrame {

    public static void main(String[] args) throws Exception {
        SpaceUtils.getSpace(args.length == 0 ? "localhost" : args[0]);
        System.out.println("Successfully connected!");
    }

}
