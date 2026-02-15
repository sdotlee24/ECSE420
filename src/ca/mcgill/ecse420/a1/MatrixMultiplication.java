package ca.mcgill.ecse420.a1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("ALL")
public class MatrixMultiplication {

    private static int NUMBER_THREADS = 4;
    private static final int MATRIX_SIZE = 2000;

    public static void main(String[] args) {
        // Q1.1,1.2
        validateMatMul(true); // Parallel matmul logic validation
        validateMatMul(false); // Sequential matmul logic validation

        // Q1.3
        System.out.println("\nQ1.3: Execution time measurement");
        measureExecutionTime(MATRIX_SIZE);

        // Q1.4
        System.out.println("\nQ 1.4: Thread count analysis");
        varyThreadCount();

        // Q1.5
        System.out.println("\nQ1.5: Matrix size analysis");
        varyMatrixSize();

        System.out.println("\nDone");
    }

    /**
     * Q1.5: Testing with sizes: 100, 200, 500, 1000, 2000, 3000, 4000
     */
    private static void varyMatrixSize() {
        int[] matrixSizes = {100, 200, 500, 1000, 2000, 3000, 4000};

        NUMBER_THREADS = 16; // 16 threads produced loweest execution time

        // Test each matrix size
        for (int size : matrixSizes) {
            System.out.print("Testing " + size);

            //Generate matrices
            double[][] a = generateRandomMatrix(size, size);
            double[][] b = generateRandomMatrix(size, size);

            // Measure sequential time
            long startTime = System.nanoTime();
            sequentialMultiplyMatrix(a, b);
            long endTime = System.nanoTime();
            double seqTime = (endTime - startTime) / 1_000_000.0;

            // Measure parallel time
            startTime = System.nanoTime();
            parallelMultiplyMatrix(a, b);
            endTime = System.nanoTime();
            double parTime = (endTime - startTime) / 1_000_000.0;

            double speedup = seqTime / parTime;

            System.out.printf("%d | %.2f | %.2f | %.2f\n",
                    size, seqTime, parTime, speedup);
        }
        // Reset to default
        NUMBER_THREADS = 4;
    }

    /**
     * Q1.4: Varies the number of threads and measures execution time
     * Tests with 1, 2, 4, 8, 16 threads on large matrices
     */
    private static void varyThreadCount() {
        int matrixSize = 4000;
        int[] threadCounts = {1, 2, 4, 8, 16};

        System.out.println("Testing with " + matrixSize + "x" + matrixSize + " matrices");
        System.out.println("processors available: " + Runtime.getRuntime().availableProcessors());

        // Generate matrices
        double[][] a = generateRandomMatrix(matrixSize, matrixSize);
        double[][] b = generateRandomMatrix(matrixSize, matrixSize);

        // Measure sequential time once for speedup calculation (baseline)
        System.out.println("Measuring sequential matmul for baseline");
        long startTime = System.nanoTime();
        sequentialMultiplyMatrix(a, b);
        long endTime = System.nanoTime();
        double sequentialTime = (endTime - startTime) / 1_000_000.0;
        System.out.printf("Sequential time: %.2f ms\n", sequentialTime);

        // Test each thread count
        for (int threads : threadCounts) {
            NUMBER_THREADS = threads;

            // Run multiple times and take average for more reliable results
            int runs = 3;
            double totalTime = 0;

            for (int run = 0; run < runs; run++) {
                startTime = System.nanoTime();
                parallelMultiplyMatrix(a, b);
                endTime = System.nanoTime();
                totalTime += (endTime - startTime) / 1_000_000.0;
            }

            double avgTime = totalTime / runs;
            double speedup = sequentialTime / avgTime;
            double efficiency = speedup / threads * 100; // Percentage

            System.out.printf("%d | %.2f | %.2x | %.1f%%\n",
                    threads, avgTime, speedup, efficiency);
        }
        // Reset to default
        NUMBER_THREADS = 4;
    }

    /**
     * Q1.3: Measures and compares execution time for sequential and parallel matrix multiplication
     *
     * @param size the size of the square matrices to multiply (size x size)
     */
    private static void measureExecutionTime(int size) {
        System.out.println("Measuring execution time for " + size + "matrices\n");

        // Generate matrices
        double[][] a = generateRandomMatrix(size, size);
        double[][] b = generateRandomMatrix(size, size);

        // Measure sequential exec time
        long startTime = System.nanoTime();
        double[][] seqResult = sequentialMultiplyMatrix(a, b);
        long endTime = System.nanoTime();
        double sequentialTime = (endTime - startTime) / 1_000_000.0; // Convert to milliseconds

        // Measure parallel exec time
        startTime = System.nanoTime();
        double[][] parResult = parallelMultiplyMatrix(a, b);
        endTime = System.nanoTime();
        double parallelTime = (endTime - startTime) / 1_000_000.0;

        // Calculate speedup
        double speedup = sequentialTime / parallelTime;

        System.out.printf("Sequential execution time: %.2f ms\n", sequentialTime);
        System.out.printf("Parallel execution time:   %.2f ms\n", parallelTime);
        System.out.printf("Speedup:                   %.2fx\n", speedup);

    }

    /**
     * Q1.3: Method execution time for both parallel and sequential matmul
     *
     * @param a           first matrix
     * @param b           second matrix
     * @param useParallel if true, use parallel method; otherwise use sequential
     * @return execution time in milliseconds
     */
    public static double measureTime(double[][] a, double[][] b, boolean useParallel) {
        long startTime = System.nanoTime();

        if (useParallel) {
            parallelMultiplyMatrix(a, b);
        } else {
            sequentialMultiplyMatrix(a, b);
        }

        long endTime = System.nanoTime();
        return (endTime - startTime) / 1_000_000.0; // Convert to milliseconds
    }

    /**
     * Q1.1: Validate sequential matmul by inputting a smaller matrix size
     */
    private static void validateMatMul(boolean isParallel) {
        // validating method with 2x2 matmul's
        double[][] a = {{1, 2}, {3, 4}};
        double[][] b = {{5, 6}, {7, 8}};
        double[][] result;
        if (isParallel) {
            result = parallelMultiplyMatrix(a, b);
        } else {
            result = sequentialMultiplyMatrix(a, b);
        }
        double[][] expected = {{19, 22}, {43, 50}};

        if (matricesEqual(result, expected)) {
            System.out.println("Small matrix test passed");
        }
    }

    /**
     * Returns the result of a sequential matrix multiplication
     * The two matrices are randomly generated
     *
     * @param a is the first matrix
     * @param b is the second matrix
     * @return the result of the multiplication
     */
    public static double[][] sequentialMultiplyMatrix(double[][] a, double[][] b) {

        if (a[0].length != b.length) { // checking if valid matrices for multiplication
            throw new ArithmeticException("Matrix dimensions dont match");
        }
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
     * Returns the result of a concurrent matrix multiplication
     * The two matrices are randomly generated
     *
     * @param a is the first matrix
     * @param b is the second matrix
     * @return the result of the multiplication
     */
    public static double[][] parallelMultiplyMatrix(double[][] a, double[][] b) {

        if (a[0].length != b.length) { // checking if valid matrices for multiplication
            throw new ArithmeticException("Matrix dimensions dont match");
        }
        int row = a.length;
        int col = b[0].length;
        int inter = a[0].length;
        double[][] out = new double[row][col];

        ExecutorService executor = Executors.newFixedThreadPool(NUMBER_THREADS);

        // divide work by rows (each task computes one row)
        for (int i = 0; i < row; i++) {
            executor.submit(new MultiplyMatrixTask(a, b, out, i));
        }

        // shutdown executor and wait for all tasks to complete
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return out;
    }

    private static class MultiplyMatrixTask implements Runnable {
        private double[][] a;
        private double[][] b;
        private double[][] out;
        private int row;


        public MultiplyMatrixTask(double[][] a, double[][] b, double[][] out, int row) {
            this.a = a;
            this.b = b;
            this.out = out;
            this.row = row;
        }
        @Override
        public void run() {
            // compute all columns for this row
            int cols = b[0].length;
            int inter = a[0].length;

            for (int j = 0; j < cols; j++) {
                double sum = 0;
                for (int k = 0; k < inter; k++) {
                    sum += a[row][k] * b[k][j];
                }
                out[row][j] = sum;
            }
        }
    }

    private static boolean matricesEqual(double[][] a, double[][] b) {
        if (a.length != b.length || a[0].length != b[0].length) {
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                if (a[i][j] != b[i][j]) {
                    return false;
                }
            }
        }
        return true;
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
