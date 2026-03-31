import java.util.HashMap;
import java.util.Map;

/**
 * MockUnitTest.java
 * 
 * Provides pre-defined test cases for validating the ODE solver system.
 * These are sample tests you can use in the GUI to verify the algorithm works correctly.
 * 
 * Each test includes:
 *   - Variable names and initial conditions
 *   - Equation strings
 *   - Constants
 *   - Expected behavior description
 *   - Sample test points to verify
 */
public class MockUnitTest {

    // =========================================================================
    // TEST 1: Lotka-Volterra (Predator-Prey Model)
    // =========================================================================
    public static class LotkaVolterraTest {
        public static final String NAME = "Lotka-Volterra (Predator-Prey)";
        
        public static final int NUM_VARIABLES = 2;
        public static final String[] VARIABLE_NAMES = {"prey", "predator"};
        public static final String[] EQUATIONS = {
            "a*prey - b*prey*predator",      // dx/dt = ax - bxy
            "d*prey*predator - g*predator"   // dy/dt = dxy - gy
        };
        
        public static Map<String, Double> getConstants() {
            Map<String, Double> consts = new HashMap<>();
            consts.put("a", 0.25);   // prey growth rate
            consts.put("b", 0.15);   // predation rate
            consts.put("d", 0.10);   // predator efficiency
            consts.put("g", 0.10);   // predator death rate
            return consts;
        }
        
        public static double[] getInitialConditions() {
            return new double[]{20.0, 2.0};  // prey=20, predator=2
        }
        
        public static String getDescription() {
            return "Classic predator-prey model. Prey grow logistically, " +
                   "predators depend on prey for food. Creates oscillating cycles.";
        }
        
        public static String getTestPoints() {
            return "Test at t=0: prey=20, predator=2\n" +
                   "Test at t=10: Should see oscillation\n" +
                   "Test at t=50: System should cycle\n" +
                   "Expected: Periodic oscillations, no steady state";
        }
    }
    
    // =========================================================================
    // TEST 2: Harmonic Oscillator (Simple Pendulum)
    // =========================================================================
    public static class HarmonicOscillatorTest {
        public static final String NAME = "Harmonic Oscillator";
        
        public static final int NUM_VARIABLES = 2;
        public static final String[] VARIABLE_NAMES = {"x", "v"};
        public static final String[] EQUATIONS = {
            "v",           // dx/dt = v
            "-1*x"         // dv/dt = -x (pure oscillation, no damping)
        };
        
        public static Map<String, Double> getConstants() {
            return new HashMap<>();  // no constants needed
        }
        
        public static double[] getInitialConditions() {
            return new double[]{1.0, 0.0};  // x=1, v=0 (displaced, no velocity)
        }
        
        public static String getDescription() {
            return "Simple harmonic motion (like a pendulum with no friction). " +
                   "Object starts at x=1 with zero velocity. " +
                   "Solution: x(t) = cos(t), v(t) = -sin(t)";
        }
        
        public static String getTestPoints() {
            return "Test at t=0: x=1.0, v=0.0\n" +
                   "Test at t=π/2≈1.571: x≈0, v≈-1\n" +
                   "Test at t=π≈3.142: x≈-1, v≈0\n" +
                   "Test at t=2π≈6.283: x≈1, v≈0 (full cycle)\n" +
                   "Expected: Perfect sinusoidal oscillation with period ~6.28";
        }
    }
    
    // =========================================================================
    // TEST 3: Damped Oscillator (Pendulum with Friction)
    // =========================================================================
    public static class DampedOscillatorTest {
        public static final String NAME = "Damped Oscillator";
        
        public static final int NUM_VARIABLES = 2;
        public static final String[] VARIABLE_NAMES = {"x", "v"};
        public static final String[] EQUATIONS = {
            "v",                    // dx/dt = v
            "-c*v - x"             // dv/dt = -cv - x (damping + spring)
        };
        
        public static Map<String, Double> getConstants() {
            Map<String, Double> consts = new HashMap<>();
            consts.put("c", 0.1);  // damping coefficient
            return consts;
        }
        
        public static double[] getInitialConditions() {
            return new double[]{1.0, 0.0};  // x=1, v=0
        }
        
        public static String getDescription() {
            return "Damped harmonic motion. Oscillates like a pendulum but " +
                   "friction gradually reduces the amplitude until it stops.";
        }
        
        public static String getTestPoints() {
            return "Test at t=0: x=1.0, v=0.0\n" +
                   "Test at t=5: x < 1.0 (amplitude decreases)\n" +
                   "Test at t=20: x ≈ 0, v ≈ 0 (nearly stopped)\n" +
                   "Test at t=50: x ≈ 0, v ≈ 0 (at rest)\n" +
                   "Expected: Damped oscillations converging to zero";
        }
    }
    
    // =========================================================================
    // TEST 4: Exponential Growth
    // =========================================================================
    public static class ExponentialGrowthTest {
        public static final String NAME = "Exponential Growth";
        
        public static final int NUM_VARIABLES = 1;
        public static final String[] VARIABLE_NAMES = {"population"};
        public static final String[] EQUATIONS = {
            "r*population"  // dp/dt = r*p (exponential growth)
        };
        
        public static Map<String, Double> getConstants() {
            Map<String, Double> consts = new HashMap<>();
            consts.put("r", 0.1);  // growth rate
            return consts;
        }
        
        public static double[] getInitialConditions() {
            return new double[]{1.0};  // p(0) = 1
        }
        
        public static String getDescription() {
            return "Simple exponential growth model. Population grows " +
                   "at rate proportional to its current size. Solution: p(t) = e^(rt)";
        }
        
        public static String getTestPoints() {
            return "Test at t=0: population=1.0\n" +
                   "Test at t=10: population ≈ e^(0.1*10) = e^1 ≈ 2.718\n" +
                   "Test at t=20: population ≈ e^2 ≈ 7.389\n" +
                   "Test at t=30: population ≈ e^3 ≈ 20.086\n" +
                   "Expected: Continuous exponential growth";
        }
    }
    
    // =========================================================================
    // TEST 5: Logistic Growth (Saturation Model)
    // =========================================================================
    public static class LogisticGrowthTest {
        public static final String NAME = "Logistic Growth";
        
        public static final int NUM_VARIABLES = 1;
        public static final String[] VARIABLE_NAMES = {"N"};
        public static final String[] EQUATIONS = {
            "r*N*(1 - N/K)"  // dN/dt = rN(1 - N/K) (limited growth)
        };
        
        public static Map<String, Double> getConstants() {
            Map<String, Double> consts = new HashMap<>();
            consts.put("r", 0.5);    // intrinsic growth rate
            consts.put("K", 100.0);  // carrying capacity
            return consts;
        }
        
        public static double[] getInitialConditions() {
            return new double[]{10.0};  // N(0) = 10
        }
        
        public static String getDescription() {
            return "Logistic growth model with saturation. Population grows " +
                   "exponentially at first, then slows as it approaches carrying capacity K.";
        }
        
        public static String getTestPoints() {
            return "Test at t=0: N=10.0\n" +
                   "Test at t=5: N > 10 (growing)\n" +
                   "Test at t=10: N growing slower\n" +
                   "Test at t=100: N ≈ 100.0 (approaches K)\n" +
                   "Expected: S-shaped (sigmoid) curve approaching carrying capacity";
        }
    }
    
    // =========================================================================
    // Print all test cases
    // =========================================================================
    public static void printAllTests() {
        System.out.println("\n" + "=".repeat(100));
        System.out.println("MOCK UNIT TESTS FOR ODE SOLVER");
        System.out.println("=".repeat(100));
        
        printTest("TEST 1", LotkaVolterraTest.NAME, 
                  LotkaVolterraTest.VARIABLE_NAMES, LotkaVolterraTest.EQUATIONS,
                  LotkaVolterraTest.getConstants(), LotkaVolterraTest.getInitialConditions(),
                  LotkaVolterraTest.getDescription(), LotkaVolterraTest.getTestPoints());
        
        printTest("TEST 2", HarmonicOscillatorTest.NAME,
                  HarmonicOscillatorTest.VARIABLE_NAMES, HarmonicOscillatorTest.EQUATIONS,
                  HarmonicOscillatorTest.getConstants(), HarmonicOscillatorTest.getInitialConditions(),
                  HarmonicOscillatorTest.getDescription(), HarmonicOscillatorTest.getTestPoints());
        
        printTest("TEST 3", DampedOscillatorTest.NAME,
                  DampedOscillatorTest.VARIABLE_NAMES, DampedOscillatorTest.EQUATIONS,
                  DampedOscillatorTest.getConstants(), DampedOscillatorTest.getInitialConditions(),
                  DampedOscillatorTest.getDescription(), DampedOscillatorTest.getTestPoints());
        
        printTest("TEST 4", ExponentialGrowthTest.NAME,
                  ExponentialGrowthTest.VARIABLE_NAMES, ExponentialGrowthTest.EQUATIONS,
                  ExponentialGrowthTest.getConstants(), ExponentialGrowthTest.getInitialConditions(),
                  ExponentialGrowthTest.getDescription(), ExponentialGrowthTest.getTestPoints());
        
        printTest("TEST 5", LogisticGrowthTest.NAME,
                  LogisticGrowthTest.VARIABLE_NAMES, LogisticGrowthTest.EQUATIONS,
                  LogisticGrowthTest.getConstants(), LogisticGrowthTest.getInitialConditions(),
                  LogisticGrowthTest.getDescription(), LogisticGrowthTest.getTestPoints());
        
        System.out.println("=".repeat(100) + "\n");
    }
    
    private static void printTest(String testNum, String name, String[] varNames, String[] equations,
                                   Map<String, Double> constants, double[] initial,
                                   String description, String testPoints) {
        System.out.println("\n" + testNum + ": " + name);
        System.out.println("-".repeat(100));
        
        System.out.println("DESCRIPTION:");
        System.out.println("  " + description);
        
        System.out.println("\nVARIABLES: " + java.util.Arrays.toString(varNames));
        
        System.out.println("\nEQUATIONS:");
        for (int i = 0; i < equations.length; i++) {
            System.out.println("  d(" + varNames[i] + ")/dt = " + equations[i]);
        }
        
        if (!constants.isEmpty()) {
            System.out.println("\nCONSTANTS:");
            for (Map.Entry<String, Double> e : constants.entrySet()) {
                System.out.printf("  %s = %.4f%n", e.getKey(), e.getValue());
            }
        }
        
        System.out.println("\nINITIAL CONDITIONS:");
        for (int i = 0; i < varNames.length; i++) {
            System.out.printf("  %s(0) = %.4f%n", varNames[i], initial[i]);
        }
        
        System.out.println("\nTEST POINTS TO VERIFY:");
        System.out.println("  " + testPoints.replace("\n", "\n  "));
        
        System.out.println();
    }
    
    public static void main(String[] args) {
        printAllTests();
    }
}
