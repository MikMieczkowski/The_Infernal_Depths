package com.mikm.rendering.cave;

import com.mikm.Vector2Int;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
//    @Test
//    public void testNaN() {
//        System.out.println(1f/0f);
//        System.out.println(-1f/0f);
//        System.out.println(0f/0f);
//        System.out.println();
//
//        System.out.println(Float.POSITIVE_INFINITY/0f);
//        System.out.println(Float.NEGATIVE_INFINITY/0f);
//        System.out.println();
//
//        System.out.println(Float.POSITIVE_INFINITY/Float.POSITIVE_INFINITY);
//        System.out.println(Float.NEGATIVE_INFINITY/Float.NEGATIVE_INFINITY);
//        System.out.println();
//
//        System.out.println(Float.POSITIVE_INFINITY/Float.NEGATIVE_INFINITY);
//        System.out.println(Float.NEGATIVE_INFINITY/Float.POSITIVE_INFINITY);
//    }


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

    @Test
    public void doesContainsWorkList() {
        Vector2Int vec1 = new Vector2Int(43, 23);
        ArrayList<Vector2Int> arr = new ArrayList<>();
        arr.add(vec1);
        Assertions.assertTrue(arr.contains(new Vector2Int(43, 23)));
    }

    @Test
    public void doesContainsWorkSet() {
        Vector2Int vec1 = new Vector2Int(43, 23);
        Set<Vector2Int> arr = new HashSet<>();
        arr.add(vec1);
        Assertions.assertTrue(arr.contains(new Vector2Int(43, 23)));
    }

    @Test
    public void whatDoesCloneDo() {
        boolean[][] bools = new boolean[5][5];
        boolean[][] boolsClone = bools.clone();
        boolsClone[3][3] = true;
        assertTrue(bools[3][3]);
    }

    @Test
    public void whatDoesCloneDo2() {
        boolean[] bools = new boolean[5];
        boolean[] boolsClone = bools.clone();
        boolsClone[3] = true;
        assertFalse(bools[3]);
    }


}
