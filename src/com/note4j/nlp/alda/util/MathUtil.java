package com.note4j.nlp.alda.util;

/**
 * Created by changwei on 15/4/17.
 */
public class MathUtil {

    public static double[][] mMatrix(double[][] a, double[][] b) {
        double[][] multiplyMatrix;
        multiplyMatrix = new double[a.length][b[0].length];
        for (int i = 0; i != a.length; i++) {//rows of a
            for (int j = 0; j != b[0].length; j++) {//columns of b
                for (int k = 0; k != a[0].length; k++) {//columns of a = rows of b
                    multiplyMatrix[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        return multiplyMatrix;
    }

    public static void display(double[][] multiplyMatrix) {
        for (int i = 0; i < multiplyMatrix.length; i++) {
            for (int j = 0; j < multiplyMatrix[0].length; j++) {
                System.out.print(multiplyMatrix[i][j] + " ");
            }
        }
    }
}