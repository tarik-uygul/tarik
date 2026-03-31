/**
 * SampleAccuracyTestSuite.java
 *
 * Example test suite demonstrating how to use SolverAccuracyTest framework
 * for unit testing your ODE solvers.
 *
 * This file shows best practices for:
 *   - Creating comprehensive test suites
 *   - Testing multiple ODE systems
 *   - Handling parameterized tests
 *   - Reporting results clearly
 *
 * COMPILE:
 *   javac -d bin src/SampleAccuracyTestSuite.java src/SolverAccuracyTest.java \
 *                 src/ODEFunction.java src/EulerSolver.java src/RungeKutta4.java
 *
 * RUN:
 *   java -cp bin SampleAccuracyTestSuite
 */

public class SampleAccuracyTestSuite {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String BLUE = "\u001B[34m";
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";

    // -----------------------------------------------------------------------
    // Main test suite
    // -----------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println(BOLD + BLUE + "\n" + "═".repeat(70) + RESET);
        System.out.println(BOLD + "ODE Solver Accuracy Test Suite" + RESET);
        System.out.println(BLUE + "═".repeat(70) + RESET + "\n");

        // Suite 1: Basic functionality tests
        testBasicFunctionality();

        // Suite 2: Convergence tests
        testConvergence();

        // Suite 3: Edge cases
        testEdgeCases();

        // Print summary
        printSummary();
    }

    // -----------------------------------------------------------------------
    // SUITE 1: Basic Functionality Tests
    // -----------------------------------------------------------------------
    private static void testBasicFunctionality() {
        System.out.println(BOLD + "SUITE 1: Basic Functionality Tests" + RESET);
        System.out.println("─".repeat(70));

        // Test 1.1: Harmonic oscillator at t=10
        testSingleODE(
            "Harmonic Oscillator (t=10)",
            new SolverAccuracyTest.HarmonicOscillatorODE(),
            new double[]{1.0, 0.0},
            10.0,
            0.01,
            0.3,      // Euler: loose tolerance for h=0.01
            1e-5      // RK4: tight tolerance
        );

        // Test 1.2: Harmonic oscillator at t=20
        testSingleODE(
            "Harmonic Oscillator (t=20)",
            new SolverAccuracyTest.HarmonicOscillatorODE(),
            new double[]{1.0, 0.0},
            20.0,
            0.01,
            0.4,
            1e-5
        );

        // Test 1.3: Damped spring (no exact solution, just stability)
        testDampedSpringStability(0.1, 10.0);

        // Test 1.4: Lotka-Volterra (no exact solution, just stability)
        testLotkaVolterraStability(new double[]{1.0, 1.0}, 20.0);

        System.out.println();
    }

    // -----------------------------------------------------------------------
    // SUITE 2: Convergence Tests
    // -----------------------------------------------------------------------
    private static void testConvergence() {
        System.out.println(BOLD + "SUITE 2: Convergence Tests (O(h^p) analysis)" + RESET);
        System.out.println("─".repeat(70));

        // Test 2.1: Euler convergence (should be O(h))
        testMultiStepConvergence(
            "Euler Convergence (O(h))",
            new SolverAccuracyTest.HarmonicOscillatorODE(),
            new double[]{1.0, 0.0},
            20.0,
            new double[]{0.2, 0.1, 0.05, 0.02, 0.01},
            1.5,      // Euler: loose tolerance (O(h))
            1e-4,     // RK4: strict (O(h^4))
            new double[]{Math.cos(20.0), -Math.sin(20.0)}
        );

        // Test 2.2: RK4 convergence (should be O(h^4))
        testMultiStepConvergence(
            "RK4 Convergence (O(h^4))",
            new SolverAccuracyTest.HarmonicOscillatorODE(),
            new double[]{1.0, 0.0},
            10.0,
            new double[]{0.1, 0.05, 0.02, 0.01},
            1.0,      // Euler: loose
            1e-6,     // RK4: very strict
            new double[]{Math.cos(10.0), -Math.sin(10.0)}
        );

        System.out.println();
    }

    // -----------------------------------------------------------------------
    // SUITE 3: Edge Cases
    // -----------------------------------------------------------------------
    private static void testEdgeCases() {
        System.out.println(BOLD + "SUITE 3: Edge Cases and Boundary Conditions" + RESET);
        System.out.println("─".repeat(70));

        // Test 3.1: Very short time, small step
        testSingleODE(
            "Short Integration (t=1)",
            new SolverAccuracyTest.HarmonicOscillatorODE(),
            new double[]{1.0, 0.0},
            1.0,
            0.001,
            0.01,     // Small error expected
            1e-9
        );

        // Test 3.2: Long integration, coarse step
        testSingleODE(
            "Long Integration (t=50)",
            new SolverAccuracyTest.HarmonicOscillatorODE(),
            new double[]{1.0, 0.0},
            50.0,
            0.05,
            2.5,      // Large error expected
            1e-4
        );

        // Test 3.3: Zero initial velocity (oscillator starting from rest)
        testSingleODE(
            "Harmonic Oscillator (v₀=0)",
            new SolverAccuracyTest.HarmonicOscillatorODE(),
            new double[]{1.0, 0.0},
            10.0,
            0.01,
            0.3,
            1e-5
        );

        System.out.println();
    }

    // -----------------------------------------------------------------------
    // Helper: Single ODE test
    // -----------------------------------------------------------------------
    private static void testSingleODE(String name,
                                       ODEFunction ode,
                                       double[] initialState,
                                       double endTime,
                                       double stepSize,
                                       double eulerTol,
                                       double rk4Tol) {
        totalTests++;

        SolverAccuracyTest test = new SolverAccuracyTest(name);
        test.runTest(
            ode,
            initialState,
            endTime,
            stepSize,
            eulerTol,
            rk4Tol,
            getExactSolution(ode, endTime)
        );

        test.printReport();

        try {
            test.assertResults();
            passedTests++;
            System.out.println();
        } catch (AssertionError e) {
            failedTests++;
            System.err.println(RED + "✗ ASSERTION FAILED" + RESET);
            System.err.println(e.getMessage() + "\n");
        }
    }

    // -----------------------------------------------------------------------
    // Helper: Multi-step convergence test
    // -----------------------------------------------------------------------
    private static void testMultiStepConvergence(String name,
                                                  ODEFunction ode,
                                                  double[] initialState,
                                                  double endTime,
                                                  double[] stepSizes,
                                                  double maxEulerError,
                                                  double maxRk4Error,
                                                  double[] exactSolution) {
        totalTests++;

        SolverAccuracyTest test = new SolverAccuracyTest(name);
        SolverAccuracyTest.MultiStepResult result = test.runMultiStepTest(
            ode,
            initialState,
            endTime,
            stepSizes,
            maxEulerError,
            maxRk4Error,
            exactSolution
        );

        result.print();

        try {
            result.assertAllPass();
            passedTests++;
            System.out.println();
        } catch (AssertionError e) {
            failedTests++;
            System.err.println(RED + "✗ CONVERGENCE TEST FAILED" + RESET);
            System.err.println(e.getMessage() + "\n");
        }
    }

    // -----------------------------------------------------------------------
    // Helper: Damped spring stability test
    // -----------------------------------------------------------------------
    private static void testDampedSpringStability(double damping, double endTime) {
        totalTests++;

        System.out.println("Test: Damped Spring (c=" + damping + ", t=" + endTime + ")");
        System.out.println("─".repeat(70));

        SolverAccuracyTest.DampedSpringODE ode = new SolverAccuracyTest.DampedSpringODE(damping);

        // Integrate with RK4
        double[] state = new double[]{1.0, 0.0};
        double t = 0.0;
        double h = 0.01;

        while (t + h <= endTime + 1e-12) {
            state = RungeKutta4.step(state, h, ode);
            t += h;
        }

        // Check that solution is stable (not diverging)
        double magnitude = Math.sqrt(state[0] * state[0] + state[1] * state[1]);
        boolean stable = magnitude < 1.5;  // Should decay from 1.0

        System.out.printf("  Final state: x=%.6f, v=%.6f, ||state||=%.6f%n",
                state[0], state[1], magnitude);

        if (stable) {
            System.out.println(GREEN + "  ✓ Solution stable" + RESET);
            passedTests++;
        } else {
            System.out.println(RED + "  ✗ Solution diverged" + RESET);
            failedTests++;
        }
        System.out.println();
    }

    // -----------------------------------------------------------------------
    // Helper: Lotka-Volterra stability test
    // -----------------------------------------------------------------------
    private static void testLotkaVolterraStability(double[] initialState, double endTime) {
        totalTests++;

        System.out.println("Test: Lotka-Volterra Predator-Prey");
        System.out.println("─".repeat(70));

        SolverAccuracyTest.LotkaVolterraODE ode = new SolverAccuracyTest.LotkaVolterraODE(2, 1, 1, 1);

        double[] state = initialState.clone();
        double t = 0.0;
        double h = 0.01;

        while (t + h <= endTime + 1e-12) {
            state = RungeKutta4.step(state, h, ode);
            t += h;
        }

        // Populations should remain positive and bounded
        boolean positive = state[0] > 0 && state[1] > 0;
        boolean bounded = state[0] < 10 && state[1] < 10;

        System.out.printf("  Final state: prey=%.6f, predator=%.6f%n", state[0], state[1]);

        if (positive && bounded) {
            System.out.println(GREEN + "  ✓ Solution stable and bounded" + RESET);
            passedTests++;
        } else {
            System.out.println(RED + "  ✗ Solution unstable or diverged" + RESET);
            failedTests++;
        }
        System.out.println();
    }

    // -----------------------------------------------------------------------
    // Helper: Get exact solution at end time
    // -----------------------------------------------------------------------
    private static double[] getExactSolution(ODEFunction ode, double endTime) {
        if (ode instanceof SolverAccuracyTest.HarmonicOscillatorODE) {
            return new double[]{Math.cos(endTime), -Math.sin(endTime)};
        }
        // For other ODEs, return zeros (tests will need specific exact solutions)
        return new double[]{0.0, 0.0};
    }

    // -----------------------------------------------------------------------
    // Print summary statistics
    // -----------------------------------------------------------------------
    private static void printSummary() {
        System.out.println(BLUE + "═".repeat(70) + RESET);
        System.out.println(BOLD + "Test Summary" + RESET);
        System.out.println("─".repeat(70));

        System.out.printf("Total tests:   %d%n", totalTests);
        System.out.printf("Passed:        " + GREEN + "%d" + RESET + "%n", passedTests);
        System.out.printf("Failed:        " + RED + "%d" + RESET + "%n", failedTests);

        System.out.println("─".repeat(70));

        if (failedTests == 0) {
            System.out.println(GREEN + BOLD + "✓ ALL TESTS PASSED" + RESET);
        } else {
            System.out.println(RED + BOLD + "✗ " + failedTests + " TEST(S) FAILED" + RESET);
        }

        System.out.println(BLUE + "═".repeat(70) + RESET + "\n");
    }
}
