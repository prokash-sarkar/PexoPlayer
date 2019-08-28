package com.ps.pexoplayer.util;

import java.util.Random;

/**
 * @Author: Prokash Sarkar
 * @Date: 7/7/19
 */
public class RandomUtils {

    /**
     * Generates a random number between a range and excluding digits
     *
     * @param start   position
     * @param end     position
     * @param exclude digits
     * @return a random number
     */
    public static int getRandomWithExclusion(int start, int end, int... exclude) {
        int random = start + new Random().nextInt(end - start + 1 - exclude.length);
        for (int ex : exclude) {
            if (random < ex) {
                break;
            }
            random++;
        }
        return random;
    }

}
