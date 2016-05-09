package com.nacho.padtranslate.util;

/**
 */
public class Distance {

        private static int minOfThree(int a, int b, int c) {
            return Math.min(a, Math.min(b, c));
        }

        private static int calculateCost(char s, char t) {
            return s==t?0:1;
        }

        private static int[][] initializeMatrix(int s, int t) {
            int[][] matrix = new int[s + 1][t + 1];
            for (int i = 0; i <= s; i++) {
                matrix[i][0] = i;
            }

            for (int j = 0; j <= t; j++) {
                matrix[0][j] = j;
            }
            return matrix;
        }

        public static int computeLevenshteinDistance(final String s, final String t) {

            char[] s_arr = s.toCharArray();
            char[] t_arr = t.toCharArray();

            if(s.equals(t)) { return 0; }
            if (s_arr.length == 0) { return t_arr.length;}
            if (t_arr.length == 0) { return s_arr.length;}

            int matrix[][] = initializeMatrix(s_arr.length, t_arr.length);

            for (int i = 0; i < s_arr.length; i++) {
                for (int j = 1; j <= t_arr.length; j++) {
                    matrix[i+1][j] = minOfThree(matrix[i][j] + 1,
                            matrix[i+1][j - 1] + 1,
                            matrix[i][j - 1] + calculateCost(s_arr[i], t_arr[j-1]));
                }
            }

            return matrix[s_arr.length][t_arr.length];
        }
}
