package com.ender.communitydayspawner.utils;


import java.util.Random;

public class IvUtils {
    private static final Random random = new Random();
    private static final int MAX_IV = 31;
    private static final int STD_DEV = 6;

    public static int rollIv(int average) {
        // Clamp the average between 0 and 31
        average = Math.max(0, Math.min(31, average));

        // Sample from a Gaussian (mean = 0, std = 1), then scale to our std and shift to the average
        double gaussian = random.nextGaussian(); // mean 0, std 1
        int iv = (int) Math.round(gaussian * STD_DEV + average);

        // Clamp result between 0 and 31
        return Math.max(0, Math.min(MAX_IV, iv));
    }
}