package ca.mcgill.ecse420.a1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("ALL")
public class MatrixMultiplication {

    private static int NUMBER_THREADS = 4;
    private static final int MATRIX_SIZE = 2000;

    public static void main(String[] args) {
        // Question 1.1 and 1.2: Sequential and parallel
        // Validate both implementations
        System.out.println("Running validation tests\n");
        validateSequentialMultiplication();
        validateParallelMultiplication();

        // Question 1.3: Measure execution time
        System.out.println("\nQuestion 1.3: Execution Time Measurement");
        measureExecutionTime(MATRIX_SIZE);

        // Question 1.4: Vary number of threads
        System.out.println("\nQuestion 1.4: Thread Count Analysis");
        varyThreadCount();

        // Question 1.5: Vary matrix size
        System.out.println("\nQuestion 1.5: Matrix Size Analysis");
        varyMatrixSize();

        System.out.println("\nDone");
    }

    /**
     * Question 1.5: Varies the matrix size and measures execution time for both methods
     * Tests with sizes: 100, 200, 500, 1000, 2000, 3000, 4000
     */
    private static void varyMatrixSize() {
        int[] matrixSizes = {100, 200, 500, 1000, 2000, 3000, 4000};

        // Use optimal thread count from Question 1.4
        int optimalThreads = 16;
        NUMBER_THREADS = optimalThreads;

        System.out.println("Using " + optimalThreads + " threads (optimal from Question 1.4)");
        System.out.println("\nTesting various matrix sizes\n");

        // Print header
        System.out.println("Matrix Size | Sequential Time (ms) | Parallel Time (ms) | Speedup");
        System.out.println("------------|----------------------|--------------------|--------");

        // Test each matrix size
        for (int size : matrixSizes) {
            System.out.print("Testing " + size + "x" + size + "...");

            // Generate matrices
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

            System.out.printf("\r%11d | %20.2f | %18.2f | %7.2fx\n",
                    size, seqTime, parTime, speedup);
        }
        // Reset to default
        NUMBER_THREADS = 4;
    }

    /**
     * Question 1.4: Varies the number of threads and measures execution time
     * Tests with 1, 2, 4, 8, 16 threads on large matrices
     */
    private static void varyThreadCount() {
        int matrixSize = 4000; // Use 4000x4000 or adjust based on system
        int[] threadCounts = {1, 2, 4, 8, 16};

        System.out.println("Testing with " + matrixSize + "x" + matrixSize + " matrices");
        System.out.println("Available processors: " + Runtime.getRuntime().availableProcessors());
        System.out.println("\nGenerating test matrices");

        // Generate matrices once to ensure fair comparison
        double[][] a = generateRandomMatrix(matrixSize, matrixSize);
        double[][] b = generateRandomMatrix(matrixSize, matrixSize);

        // Measure sequential time once for speedup calculation
        System.out.println("Measuring sequential baseline");
        long startTime = System.nanoTime();
        sequentialMultiplyMatrix(a, b);
        long endTime = System.nanoTime();
        double sequentialTime = (endTime - startTime) / 1_000_000.0;
        System.out.printf("Sequential time: %.2f ms\n\n", sequentialTime);

        // Print header
        System.out.println("Thread Count | Execution Time (ms) | Speedup | Efficiency");
        System.out.println("-------------|---------------------|---------|------------");

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

            System.out.printf("%12d | %19.2f | %7.2fx | %9.1f%%\n",
                    threads, avgTime, speedup, efficiency);
        }
        // Reset to default
        NUMBER_THREADS = 4;
    }

    /**
     * Question 1.3: Measures and compares execution time for sequential and parallel matrix multiplication
     * @param size the size of the square matrices to multiply (size x size)
     */
    private static void measureExecutionTime(int size) {
        System.out.println("Measuring execution time for " + size + "x" + size + " matrices\n");

        // Generate random matrices
        double[][] a = generateRandomMatrix(size, size);
        double[][] b = generateRandomMatrix(size, size);

        // Measure sequential execution time
        long startTime = System.nanoTime();
        double[][] seqResult = sequentialMultiplyMatrix(a, b);
        long endTime = System.nanoTime();
        double sequentialTime = (endTime - startTime) / 1_000_000.0; // Convert to milliseconds

        // Measure parallel execution time
        startTime = System.nanoTime();
        double[][] parResult = parallelMultiplyMatrix(a, b);
        endTime = System.nanoTime();
        double parallelTime = (endTime - startTime) / 1_000_000.0; // Convert to milliseconds

        // Calculate speedup
        double speedup = sequentialTime / parallelTime;

        // Display results
        System.out.printf("Sequential execution time: %.2f ms\n", sequentialTime);
        System.out.printf("Parallel execution time:   %.2f ms\n", parallelTime);
        System.out.printf("Speedup:                   %.2fx\n", speedup);

        // Verify correctness
        if (matricesEqual(seqResult, parResult, 1e-9)) {
            System.out.println("\nResults verified: Sequential and parallel produce identical output");
        } else {
            System.out.println("\nWarning: Sequential and parallel results differ");
        }
    }

    /**
     * Question 1.3: Measures execution time for a single matrix multiplication method
     * @param a first matrix
     * @param b second matrix
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
     * Validates sequential multiplication (Question 1.1)
     */
    private static void validateSequentialMultiplication() {
        System.out.println("=== Sequential Validation ===");

        // Test 1: Known result
        double[][] testA = {{1, 2}, {3, 4}};
        double[][] testB = {{5, 6}, {7, 8}};
        double[][] result = sequentialMultiplyMatrix(testA, testB);
        double[][] expected = {{19, 22}, {43, 50}};

        if (matricesEqual(result, expected, 1e-9)) {
            System.out.println("Small matrix test passed");
        }

        // Test 2: Identity matrix
        double[][] A = generateRandomMatrix(3, 3);
        double[][] I = createIdentityMatrix(3);
        double[][] resultAI = sequentialMultiplyMatrix(A, I);

        if (matricesEqual(A, resultAI, 1e-9)) {
            System.out.println("Identity matrix test passed");
        }

        // Test 3: Dimension validation
        try {
            double[][] invalid1 = {{1, 2, 3}};
            double[][] invalid2 = {{1, 2}, {3, 4}};
            sequentialMultiplyMatrix(invalid1, invalid2);
        } catch (ArithmeticException e) {
            System.out.println("Dimension validation test passed");
        }
    }

    /**
     * Validates parallel multiplication (Question 1.2)
     */
    private static void validateParallelMultiplication() {
        System.out.println("\n=== Parallel Validation ===");

        // Test 1: Compare with sequential
        double[][] testA = {{1, 2}, {3, 4}};
        double[][] testB = {{5, 6}, {7, 8}};
        double[][] seqResult = sequentialMultiplyMatrix(testA, testB);
        double[][] parResult = parallelMultiplyMatrix(testA, testB);

        if (matricesEqual(seqResult, parResult, 1e-9)) {
            System.out.println("Small matrix test passed");
        }

        // Test 2: Larger random matrices
        double[][] A = generateRandomMatrix(50, 50);
        double[][] B = generateRandomMatrix(50, 50);
        double[][] seqResult2 = sequentialMultiplyMatrix(A, B);
        double[][] parResult2 = parallelMultiplyMatrix(A, B);

        if (matricesEqual(seqResult2, parResult2, 1e-9)) {
            System.out.println("50x50 matrix test passed");
        }

        // Test 3: Non-square matrices
        double[][] rect1 = generateRandomMatrix(3, 4);
        double[][] rect2 = generateRandomMatrix(4, 2);
        double[][] seqRect = sequentialMultiplyMatrix(rect1, rect2);
        double[][] parRect = parallelMultiplyMatrix(rect1, rect2);

        if (matricesEqual(seqRect, parRect, 1e-9)) {
            System.out.println("Non-square matrix test passed");
        }

        // Test 4: Identity matrix
        double[][] A4 = generateRandomMatrix(10, 10);
        double[][] I4 = createIdentityMatrix(10);
        double[][] parIdentity = parallelMultiplyMatrix(A4, I4);

        if (matricesEqual(A4, parIdentity, 1e-9)) {
            System.out.println("Identity matrix test passed");
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
        private double [][] out;
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

    private static double[][] createIdentityMatrix(int size) {
        double[][] identity = new double[size][size];
        for (int i = 0; i < size; i++) {
            identity[i][i] = 1.0;
        }
        return identity;
    }

    private static boolean matricesEqual(double[][] a, double[][] b, double tolerance) {
        if (a.length != b.length || a[0].length != b[0].length) {
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                if (Math.abs(a[i][j] - b[i][j]) > tolerance) {
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
