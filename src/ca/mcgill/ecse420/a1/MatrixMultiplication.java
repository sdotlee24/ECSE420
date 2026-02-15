package ca.mcgill.ecse420.a1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("ALL")
public class MatrixMultiplication {

    private static final int NUMBER_THREADS = 1;
    private static final int MATRIX_SIZE = 4000;

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
            executor.execute(new MatrixRowTask(a, b, out, i, col, inter));

        }
        executor.shutdown();
        try {
            executor.awaitTermination(2, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            System.out.println("ERROR" + e);
        }
        return out;
    }
    private static class MatrixRowTask implements Runnable {
        private double[][] a;
        private double[][] b;
        private double[][] out;
        private int i;
        private int numCols;
        private int k;

        public MatrixRowTask(double[][] a, double[][] b, double[][] out, int i, int numCols, int k) {
            this.a = a;
            this.b = b;
            this.out = out;
            this.i = i;
            this.numCols = numCols;
            this.k = k;
        }
        @Override
        public void run() {
            for (int j = 0; j < numCols; j++) {
                for (int c = 0; c < k; c++) {
                    out[i][j] += a[i][c] * b[c][j];
                }
            }
        }
    }
    private static void findMatMulRuntimes() {
        double[][] a = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);
        double[][] b = generateRandomMatrix(MATRIX_SIZE, MATRIX_SIZE);

        System.out.println("Measuring parallel...");
        long startTime = System.nanoTime();
        double[][] parResult = parallelMultiplyMatrix(a, b);
        long endTime = System.nanoTime();
        double parallelTime = (endTime - startTime) / 1_000_000.0;

        System.out.println("Parallel (" + NUMBER_THREADS + " threads): " + parallelTime + " ms");
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
