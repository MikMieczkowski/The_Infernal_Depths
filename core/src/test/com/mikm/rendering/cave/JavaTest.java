package com.mikm.rendering.cave;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class JavaTest
{
    /*
    2D ARRAY STUFF:

    1,0| 1,1
    0,0| 0,1
    y is going UP

    array[y][x]

    int[][] array2 = new int[array.length][array[0].length];

    for (y) {
        for (x) {

        }
    }
     */
    @Test
    public void createGoodArray() {
        boolean[][] array = new boolean[3][3];
        array[2] = new boolean[]{true, true, true};
        array[1] = new boolean[]{false, true, false};
        array[0] = new boolean[]{false, false, false};
        assertTrue(array[2][0]);
        boolean[][] redoArray = new boolean[3][3];
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                if (y == 2) {
                    redoArray[y][x] = true;
                }
                if (x == 1 && y == 1) {
                    redoArray[y][x] = true;
                }
            }
        }
        assertTrue(Arrays.deepEquals(array, redoArray));
    }

    //@Test
    //public void arrayListSetDoesntFillInData() {
        //ArrayList<Integer> list = new ArrayList<>();
        // out of bounds - list.set(102, 21);
        // assertNotEquals(list.get(0), 0);
    //}
}
