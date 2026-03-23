import java.util.ArrayList;
import java.util.List;

public class SolverAccuracyExperiment {

    // Simple Harmonic Oscillator: x'' = -x
    // State vector: [x, v]
    // Derivatives:  [v, -x]
    // Exact solution: x(t) = cos(t), v(t) = -sin(t)
    static ODEFunction harmonicOscillator = state -> new double[]{
        state[1],   // dx/dt = v
        -state[0]   // dv/dt = -x
    };

    // Exact solution at time t (starting from x=1, v=0)
    static double[] exactSolution(double t) {
        return new double[]{Math.cos(t), -Math.sin(t)};
    }

    public static void main(String[] args) {
        double[] initialState = {1.0, 0.0};  // x(0)=1, v(0)=0
        double endTime = 10.0;               // integrate up to t=10

        // Step sizes to test: from large (inaccurate) to small (accurate)
        double[] stepSizes = {1.0, 0.5, 0.1, 0.05, 0.01, 0.005, 0.001, 0.0005, 0.0001};

        System.out.println("h,euler_error,euler_time_ns,rk4_error,rk4_time_ns");

        for (double h : stepSizes) {
            // --- EULER ---
            long startTime = System.nanoTime();
            double[] eulerResult = runSolver("euler", initialState, h, endTime);
            long eulerTime = System.nanoTime() - startTime;

            // --- RK4 ---
            startTime = System.nanoTime();
            double[] rk4Result = runSolver("rk4", initialState, h, endTime);
            long rk4Time = System.nanoTime() - startTime;

            // --- ERROR: Euclidean distance from exact solution at t=endTime ---
            double[] exact = exactSolution(endTime);
            double eulerError = euclideanError(eulerResult, exact);
            double rk4Error   = euclideanError(rk4Result,   exact);

            System.out.printf("%.5f,%.6e,%d,%.6e,%d%n",
                h, eulerError, eulerTime, rk4Error, rk4Time);
        }
    }

    // Runs the chosen solver from t=0 to t=endTime, returns final state
    static double[] runSolver(String solver, double[] initialState, double h, double endTime) {
        double[] state = initialState.clone();
        int steps = (int) Math.round(endTime / h);

        for (int i = 0; i < steps; i++) {
            if (solver.equals("euler")) {
                state = EulerSolver.step(state, h, harmonicOscillator);
            } else {
                state = RungeKutta4.step(state, h, harmonicOscillator);
            }
        }
        return state;
    }

    // ||numerical - exact|| in 2D
    static double euclideanError(double[] numerical, double[] exact) {
        double sum = 0;
        for (int i = 0; i < numerical.length; i++) {
            double diff = numerical[i] - exact[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    } 
}




/* 
## What the Output Looks Like

You'll get a CSV like:
```
h,euler_error,euler_time_ns,rk4_error,rk4_time_ns
1.00000,1.234567e+00,45231,2.345678e-03,89432
0.50000,6.123456e-01,78234,...
...
0.00010,9.876543e-05,4523123,9.123456e-17,8934211
*/