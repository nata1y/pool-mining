package model;

/**
 * taken https://github.com/rchen8/Algorithms/blob/master/Matrix.java
 */
public class RevenueCalculator {

    public static double[] calculateRevenues(double[][] coefs, double[][] constants){
        double mult[][] = multiply(inverse(coefs), constants);
        double ans[] = new double[constants.length];
        for (int i = 0; i < ans.length; i++){
            ans[i] = mult[i][0];
        }
        return ans;
    }

    private static double determinant(double[][] matrix) {
        if (matrix.length == 1){
            return matrix[0][0];
        }

        if (matrix.length == 2) {
            return matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];
        }

        double det = 0;

        for (int i = 0; i < matrix[0].length; i++) {
            det += Math.pow(-1, i) * matrix[0][i]
                    * determinant(minor(matrix, 0, i));
        }
        return det;
    }

    private static double[][] inverse(double[][] matrix) {
        double[][] inverse = new double[matrix.length][matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                inverse[i][j] = Math.pow(-1, i + j)
                        * determinant(minor(matrix, i, j));
            }
        }

        double det = 1.0 / determinant(matrix);

        for (int i = 0; i < inverse.length; i++) {
            for (int j = 0; j <= i; j++) {
                double temp = inverse[i][j];
                inverse[i][j] = inverse[j][i] * det;
                inverse[j][i] = temp * det;
            }
        }

        return inverse;
    }


    private static double[][] minor(double[][] matrix, int row, int column) {
        double[][] minor = new double[matrix.length - 1][matrix.length - 1];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; i != row && j < matrix[i].length; j++) {
                if (j != column) {
                    minor[i < row ? i : i - 1][j < column ? j : j - 1] = matrix[i][j];
                }
            }
        }
        return minor;
    }

    private static double[][] multiply(double[][] a, double[][] b) {
        double[][] matrix = new double[a.length][b[0].length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < b[0].length; j++) {
                double sum = 0;
                for (int k = 0; k < a[i].length; k++) {
                    sum += a[i][k] * b[k][j];
                }
                matrix[i][j] = sum;
            }
        }
        return matrix;
    }
}
