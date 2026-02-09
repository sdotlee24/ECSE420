package ca.mcgill.ecse420.a1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("ALL")
public class MatrixMultiplication {

    private static final int NUMBER_THREADS = 4;
    private static final int MATRIX_SIZE = 2000;

    public static void main(String[] args) {
        findMatMulRuntimes();
    }

    /**
     * Returns the result of a sequential matrix multiplication *assuming valid matrix sizes
     * The two matrices are randomly generated
     *
     * @param a is the first matrix
     * @param b is the second matrix
     * @return the result of the multiplication
     */
    public static double[][] sequentialMultiplyMatrix(double[][] a, double[][] b) {

        int row = a.length;
        int col = b[0].length;
        int inter = a[0].length;
        double[][] out = new double[row][col];

        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                for (int k = 0; k < inter; k++) {
                    out[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        return out;
    }

    /**
     * Returns the result of a concurrent matrix multiplication *assuming valid matrix sizes
     * The two matrices are randomly generated
     *
     * @param a is the first matrix
     * @param b is the second matrix
     * @return the result of the multiplication
     */
    public static double[][] parallelMultiplyMatrix(double[][] a, double[][] b) {

        int row = a.length;
        int col = b[0].length;
        int inter = a[0].length;
        double[][] out = new double[row][col];

        ExecutorService executor = Executors.newFixedThreadPool(NUMBER_THREADS);
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                executor.execute(new MultiplyMatrixTask(a, b, out, i, j, inter));
            }
        }
        executor.shutdown();
        try {
            executor.awaitTermination(2, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            System.out.println("ERROR" + e);
        }
        return out;
    }
    private static class MultiplyMatrixTask implements Runnable {
        private double[][] a;
        private double[][] b;
        private double[][] out;
        private int i;
        private int j;
        private int k;

        public MultiplyMatrixTask(double[][] a, double[][] b, double[][] out, int i, int j, int k) {
            this.a = a;
            this.b = b;
            this.out = out;
            this.i = i;
            this.j = j;
            this.k = k;
        }
        @Override
        public void run() {
            for (int c = 0; c < k; c++) {
                out[i][j] += a[i][c] * b[c][j];
            }
        }
    }
    private static void findMatMulRuntimes() {
        double[][] a = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);
        double[][] b = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);

        long startTime = System.nanoTime();
        sequentialMultiplyMatrix(a, b);
        long endTime = System.nanoTime();
        double sequentialTime = (endTime - startTime) / 1_000_000.0;

        startTime = System.nanoTime();
        parallelMultiplyMatrix(a, b);
        endTime = System.nanoTime();
        double parallelTime = (endTime - startTime) / 1_000_000.0;

        System.out.println("Sequential runtime: " + sequentialTime + " ms, Parallel Time: " + parallelTime + "ms.");
    }
    /**
     * Populates a matrix of given size with randomly generated integers between 0-10.
     *
     * @param numRows number of rows
     * @param numCols number of cols
     * @return matrix
     */
    private static double[][] generateRandomMatrix(int numRows, int numCols) {
        double matrix[][] = new double[numRows][numCols];
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                matrix[row][col] = (double) ((int) (Math.random() * 10.0));
            }
        }
        return matrix;
    }

}
