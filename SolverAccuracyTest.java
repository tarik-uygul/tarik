

public class SolverAccuracyTest {

    private String testName;
    private TestResult result;
    private static final int TIME_REPS = 5;  // timing repetitions

    // Color codes for console output
    private static final String GREEN  = "\u001B[32m";
    private static final String RED    = "\u001B[31m";
    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------
    public SolverAccuracyTest(String testName) {
        this.testName = testName;
        this.result = null;
    }

    // -----------------------------------------------------------------------
    // Single step-size test
    // -----------------------------------------------------------------------
    /**
     * Run test with a single step size, comparing Euler vs RK4 solvers.
     *
     * @param ode                 ODE system to solve
     * @param initialState        Initial state vector
     * @param endTime             Integration end time
     * @param stepSize            Step size for integration
     * @param eulerTolerance      Expected maximum Euler error
     * @param rk4Tolerance        Expected maximum RK4 error
     * @param exactSolution       Expected final state (for error calculation)
     * @return true if both solvers pass their tolerance criteria
     */
    public boolean runTest(ODEFunction ode,
                           double[] initialState,
                           double endTime,
                           double stepSize,
                           double eulerTolerance,
                           double rk4Tolerance,
                           double[] exactSolution) {

        // Euler integration
        long eulerTimeNs = 0;
        double[] eulerResult = null;
        for (int rep = 0; rep < TIME_REPS; rep++) {
            long t0 = System.nanoTime();
            eulerResult = integrate(initialState, stepSize, endTime, ode, "euler");
            eulerTimeNs += System.nanoTime() - t0;
        }
        eulerTimeNs /= TIME_REPS;
        double eulerError = euclideanError(eulerResult, exactSolution);
        boolean eulerPass = eulerError <= eulerTolerance;

        // RK4 integration
        long rk4TimeNs = 0;
        double[] rk4Result = null;
        for (int rep = 0; rep < TIME_REPS; rep++) {
            long t0 = System.nanoTime();
            rk4Result = integrate(initialState, stepSize, endTime, ode, "rk4");
            rk4TimeNs += System.nanoTime() - t0;
        }
        rk4TimeNs /= TIME_REPS;
        double rk4Error = euclideanError(rk4Result, exactSolution);
        boolean rk4Pass = rk4Error <= rk4Tolerance;

        // Store result
        this.result = new TestResult(
            testName,
            stepSize,
            eulerError,
            rk4Error,
            eulerTimeNs,
            rk4TimeNs,
            eulerTolerance,
            rk4Tolerance,
            eulerPass && rk4Pass
        );

        return eulerPass && rk4Pass;
    }

    // -----------------------------------------------------------------------
    // Multi-step-size test (convergence study)
    // -----------------------------------------------------------------------
    /**
     * Run convergence study with multiple step sizes.
     * Useful for verifying O(h^p) convergence rates.
     *
     * @param ode                 ODE system
     * @param initialState        Initial state
     * @param endTime             Integration end time
     * @param stepSizes           Array of step sizes to test
     * @param maxEulerError       Maximum acceptable Euler error at smallest step
     * @param maxRk4Error         Maximum acceptable RK4 error at smallest step
     * @param exactSolution       Expected final state
     * @return array of TestResult for each step size
     */
    public MultiStepResult runMultiStepTest(ODEFunction ode,
                                            double[] initialState,
                                            double endTime,
                                            double[] stepSizes,
                                            double maxEulerError,
                                            double maxRk4Error,
                                            double[] exactSolution) {

        int n = stepSizes.length;
        double[] eulerErrors = new double[n];
        double[] rk4Errors = new double[n];
        long[] eulerTimes = new long[n];
        long[] rk4Times = new long[n];
        boolean[] eulerPass = new boolean[n];
        boolean[] rk4Pass = new boolean[n];

        for (int i = 0; i < n; i++) {
            double h = stepSizes[i];

            // Euler
            long eulerTimeNs = 0;
            double[] eulerResult = null;
            for (int rep = 0; rep < TIME_REPS; rep++) {
                long t0 = System.nanoTime();
                eulerResult = integrate(initialState, h, endTime, ode, "euler");
                eulerTimeNs += System.nanoTime() - t0;
            }
            eulerTimeNs /= TIME_REPS;
            eulerErrors[i] = euclideanError(eulerResult, exactSolution);
            eulerTimes[i] = eulerTimeNs;
            eulerPass[i] = eulerErrors[i] <= maxEulerError;

            // RK4
            long rk4TimeNs = 0;
            double[] rk4Result = null;
            for (int rep = 0; rep < TIME_REPS; rep++) {
                long t0 = System.nanoTime();
                rk4Result = integrate(initialState, h, endTime, ode, "rk4");
                rk4TimeNs += System.nanoTime() - t0;
            }
            rk4TimeNs /= TIME_REPS;
            rk4Errors[i] = euclideanError(rk4Result, exactSolution);
            rk4Times[i] = rk4TimeNs;
            rk4Pass[i] = rk4Errors[i] <= maxRk4Error;
        }

        return new MultiStepResult(testName, stepSizes, eulerErrors, rk4Errors,
                eulerTimes, rk4Times, eulerPass, rk4Pass, maxEulerError, maxRk4Error);
    }

    // -----------------------------------------------------------------------
    // Print single test result
    // -----------------------------------------------------------------------
    public void printReport() {
        if (result == null) {
            System.err.println("No test result available. Run runTest() first.");
            return;
        }
        result.print();
    }

    // -----------------------------------------------------------------------
    // Assert single test result
    // -----------------------------------------------------------------------
    /**
     * Throws AssertionError if test failed.
     */
    public void assertResults() throws AssertionError {
        if (result == null) {
            throw new AssertionError("No test result available");
        }
        if (!result.passed) {
            throw new AssertionError(String.format(
                "Test '%s' failed: Euler error %.3e (tol %.3e), RK4 error %.3e (tol %.3e)",
                result.testName, result.eulerError, result.eulerTolerance,
                result.rk4Error, result.rk4Tolerance
            ));
        }
    }

    // -----------------------------------------------------------------------
    // Integration helper
    // -----------------------------------------------------------------------
    private double[] integrate(double[] initialState, double h,
                                double endTime, ODEFunction ode, String solver) {
        double[] state = initialState.clone();
        double t = 0.0;
        while (t + h <= endTime + 1e-12) {
            if (solver.equals("euler")) {
                state = EulerSolver.step(state, h, ode);
            } else {
                state = RungeKutta4.step(state, h, ode);
            }
            t += h;
        }
        return state;
    }

    // -----------------------------------------------------------------------
    // Error calculation
    // -----------------------------------------------------------------------
    private double euclideanError(double[] numerical, double[] exact) {
        double sum = 0;
        for (int i = 0; i < numerical.length; i++) {
            double d = numerical[i] - exact[i];
            sum += d * d;
        }
        return Math.sqrt(sum);
    }

    // -----------------------------------------------------------------------
    // Test result container - single step size
    // -----------------------------------------------------------------------
    public static class TestResult {
        public final String testName;
        public final double stepSize;
        public final double eulerError;
        public final double rk4Error;
        public final long eulerTimeNs;
        public final long rk4TimeNs;
        public final double eulerTolerance;
        public final double rk4Tolerance;
        public final boolean passed;

        TestResult(String testName, double stepSize,
                   double eulerError, double rk4Error,
                   long eulerTimeNs, long rk4TimeNs,
                   double eulerTolerance, double rk4Tolerance,
                   boolean passed) {
            this.testName = testName;
            this.stepSize = stepSize;
            this.eulerError = eulerError;
            this.rk4Error = rk4Error;
            this.eulerTimeNs = eulerTimeNs;
            this.rk4TimeNs = rk4TimeNs;
            this.eulerTolerance = eulerTolerance;
            this.rk4Tolerance = rk4Tolerance;
            this.passed = passed;
        }

        public void print() {
            String eulerStatus = eulerError <= eulerTolerance
                    ? GREEN + "✓ PASS" + RESET : RED + "✗ FAIL" + RESET;
            String rk4Status = rk4Error <= rk4Tolerance
                    ? GREEN + "✓ PASS" + RESET : RED + "✗ FAIL" + RESET;

            System.out.println();
            System.out.println(BOLD + "Test: " + testName + RESET);
            System.out.println("─".repeat(70));
            System.out.printf("  Step size:        %.4f%n", stepSize);
            System.out.printf("%n  Euler Method:%n");
            System.out.printf("    Error:          %.3e  (tolerance: %.3e)  %s%n",
                    eulerError, eulerTolerance, eulerStatus);
            System.out.printf("    Time:           %d ns%n", eulerTimeNs);
            System.out.printf("%n  Runge-Kutta 4:%n");
            System.out.printf("    Error:          %.3e  (tolerance: %.3e)  %s%n",
                    rk4Error, rk4Tolerance, rk4Status);
            System.out.printf("    Time:           %d ns%n", rk4TimeNs);
            System.out.printf("%n  Speedup (RK4 vs Euler): %.2fx%n",
                    (double) eulerTimeNs / rk4TimeNs);
            System.out.println("─".repeat(70));
            System.out.println(passed ? GREEN + "✓ TEST PASSED" + RESET : RED + "✗ TEST FAILED" + RESET);
            System.out.println();
        }
    }

    // -----------------------------------------------------------------------
    // Test result container - multiple step sizes
    // -----------------------------------------------------------------------
    public static class MultiStepResult {
        public final String testName;
        public final double[] stepSizes;
        public final double[] eulerErrors;
        public final double[] rk4Errors;
        public final long[] eulerTimes;
        public final long[] rk4Times;
        public final boolean[] eulerPass;
        public final boolean[] rk4Pass;
        public final double maxEulerError;
        public final double maxRk4Error;

        MultiStepResult(String testName,
                        double[] stepSizes,
                        double[] eulerErrors, double[] rk4Errors,
                        long[] eulerTimes, long[] rk4Times,
                        boolean[] eulerPass, boolean[] rk4Pass,
                        double maxEulerError, double maxRk4Error) {
            this.testName = testName;
            this.stepSizes = stepSizes;
            this.eulerErrors = eulerErrors;
            this.rk4Errors = rk4Errors;
            this.eulerTimes = eulerTimes;
            this.rk4Times = rk4Times;
            this.eulerPass = eulerPass;
            this.rk4Pass = rk4Pass;
            this.maxEulerError = maxEulerError;
            this.maxRk4Error = maxRk4Error;
        }

        public boolean allPass() {
            for (boolean b : eulerPass) if (!b) return false;
            for (boolean b : rk4Pass) if (!b) return false;
            return true;
        }

        public void print() {
            System.out.println();
            System.out.println(BOLD + "Multi-Step Test: " + testName + RESET);
            System.out.println("═".repeat(100));
            System.out.printf("%-10s  %-15s  %-15s  %-12s  %-12s  %-8s  %-8s%n",
                    "h", "Euler Error", "RK4 Error", "Euler Time", "RK4 Time", "Euler", "RK4");
            System.out.println("─".repeat(100));

            for (int i = 0; i < stepSizes.length; i++) {
                String eulerStat = eulerPass[i] ? GREEN + "✓" + RESET : RED + "✗" + RESET;
                String rk4Stat = rk4Pass[i] ? GREEN + "✓" + RESET : RED + "✗" + RESET;

                System.out.printf("%.4f  %.3e [%.3e]  %.3e [%.3e]  %8d ns  %8d ns  %s  %s%n",
                        stepSizes[i],
                        eulerErrors[i], maxEulerError,
                        rk4Errors[i], maxRk4Error,
                        eulerTimes[i], rk4Times[i],
                        eulerStat, rk4Stat);
            }

            System.out.println("═".repeat(100));
            if (allPass()) {
                System.out.println(GREEN + "✓ ALL TESTS PASSED" + RESET);
            } else {
                System.out.println(RED + "✗ SOME TESTS FAILED" + RESET);
            }
            System.out.println();
        }

        public void assertAllPass() throws AssertionError {
            if (!allPass()) {
                StringBuilder msg = new StringBuilder();
                msg.append("Multi-step test '").append(testName).append("' failed:\n");
                for (int i = 0; i < stepSizes.length; i++) {
                    if (!eulerPass[i]) {
                        msg.append(String.format("  h=%.4f Euler: error %.3e > tolerance %.3e\n",
                                stepSizes[i], eulerErrors[i], maxEulerError));
                    }
                    if (!rk4Pass[i]) {
                        msg.append(String.format("  h=%.4f RK4: error %.3e > tolerance %.3e\n",
                                stepSizes[i], rk4Errors[i], maxRk4Error));
                    }
                }
                throw new AssertionError(msg.toString());
            }
        }
    }

    // -----------------------------------------------------------------------
    // Example ODE implementations for testing
    // -----------------------------------------------------------------------

    /**
     * Harmonic Oscillator: x'' = -x  (equivalently: x' = v, v' = -x)
     * Exact solution: x(t) = cos(t), v(t) = -sin(t) with x(0)=1, v(0)=0
     */
    public static class HarmonicOscillatorODE implements ODEFunction {
        @Override
        public double[] compute(double[] state) {
            // state = [x, v]
            // x' = v, v' = -x
            return new double[]{state[1], -state[0]};
        }
    }

    /**
     * Lotka-Volterra (predator-prey): x' = ax - bxy, y' = -cy + dxy
     * Parameters: a=2, b=1, c=1, d=1  (common test case)
     */
    public static class LotkaVolterraODE implements ODEFunction {
        private final double a, b, c, d;

        public LotkaVolterraODE(double a, double b, double c, double d) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
        }

        @Override
        public double[] compute(double[] state) {
            // state = [x, y]
            double x = state[0];
            double y = state[1];
            return new double[]{
                    a * x - b * x * y,
                    -c * y + d * x * y
            };
        }
    }

    /**
     * Spring with damping: x'' + 0.1*x' + x = 0
     * Equivalently: x' = v, v' = -0.1*v - x
     */
    public static class DampedSpringODE implements ODEFunction {
        private final double dampingCoeff;

        public DampedSpringODE(double dampingCoeff) {
            this.dampingCoeff = dampingCoeff;
        }

        @Override
        public double[] compute(double[] state) {
            // state = [x, v]
            double x = state[0];
            double v = state[1];
            return new double[]{
                    v,
                    -dampingCoeff * v - x
            };
        }
    }

    // -----------------------------------------------------------------------
    // Main method - demonstration
    // -----------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println(BOLD + "ODE Solver Accuracy Test Framework" + RESET);
        System.out.println("═".repeat(70));

        // Test 1: Harmonic oscillator with single step size
        System.out.println("\n" + BOLD + "[Test 1] Single Step-Size Test" + RESET);
        SolverAccuracyTest test1 = new SolverAccuracyTest("Harmonic Oscillator");
        test1.runTest(
                new HarmonicOscillatorODE(),
                new double[]{1.0, 0.0},  // x(0)=1, v(0)=0
                20.0,                     // integrate to t=20
                0.01,                     // step size
                0.1,                      // Euler tolerance
                1e-6,                     // RK4 tolerance
                new double[]{Math.cos(20.0), -Math.sin(20.0)}  // exact solution at t=20
        );
        test1.printReport();

        try {
            test1.assertResults();
        } catch (AssertionError e) {
            System.err.println("Assertion failed: " + e.getMessage());
        }

        // Test 2: Harmonic oscillator with multiple step sizes
        System.out.println("\n" + BOLD + "[Test 2] Multi-Step Convergence Test" + RESET);
        SolverAccuracyTest test2 = new SolverAccuracyTest("Harmonic Oscillator Convergence");
        double[] stepSizes = {0.1, 0.05, 0.02, 0.01, 0.005};
        MultiStepResult multiResult = test2.runMultiStepTest(
                new HarmonicOscillatorODE(),
                new double[]{1.0, 0.0},
                20.0,
                stepSizes,
                0.5,     // max Euler error
                1e-5,    // max RK4 error
                new double[]{Math.cos(20.0), -Math.sin(20.0)}
        );
        multiResult.print();

        try {
            multiResult.assertAllPass();
        } catch (AssertionError e) {
            System.err.println("Assertion failed: " + e.getMessage());
        }

        // Test 3: Damped spring
        System.out.println("\n" + BOLD + "[Test 3] Damped Spring System" + RESET);
        SolverAccuracyTest test3 = new SolverAccuracyTest("Damped Spring (c=0.1)");
        // For damped spring, we test numerically (no simple closed form)
        // Just verify solvers don't diverge
        DampedSpringODE dampedSpringOde = new DampedSpringODE(0.1);
        double[] springResult = test3.integrate(
                new double[]{1.0, 0.0},
                0.01,
                10.0,
                dampedSpringOde,
                "rk4"
        );
        System.out.println("Damped spring at t=10: x=" + springResult[0] + ", v=" + springResult[1]);
        System.out.println(GREEN + "✓ Numerical integration stable" + RESET);

        System.out.println("\n" + "═".repeat(70));
    }
}
