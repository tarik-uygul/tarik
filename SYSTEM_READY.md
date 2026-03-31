# ODE Solver System - Ready for Production

**Status**: ✅ **COMPLETE AND READY FOR CLIENT UNIT TESTS**

**Date**: March 31, 2026  
**System**: ODE Solver with GUI Visualization & Testing Framework

---

## What You Have

### 1. **Visualizer.java** - Interactive GUI Application
- **Input screen**: Choose Euler or RK4 solver, number of variables
- **Setup screen**: Enter variable names, equations (as strings), constants, initial conditions
- **Simulation**: Run solver with specified step size and duration
- **Visualization**: Plot 2D trajectories (any two variables vs each other or vs time)
- **Results display**: Formatted table of all states sampled during simulation
- **Point evaluation**: Query function values at any time within simulation range
- **Console output**: Detailed debugging and result logs

**How to launch:**
```bash
cd /Users/tarikuygul/Documents/gitlab/team_02
javac --module-path /Users/tarikuygul/Downloads/javafx-sdk-25.0.1/lib \
      --add-modules javafx.controls,javafx.fxml -d bin src/*.java
java --module-path /Users/tarikuygul/Downloads/javafx-sdk-25.0.1/lib \
     --add-modules javafx.controls,javafx.fxml -cp bin Visualizer
```

### 2. **Expression Parser** - Mathematical String Evaluator
- **Supports**: +, -, *, /, ^ (power)
- **Functions**: sin(x), cos(x), tan(x), sqrt(x), abs(x), exp(x), log(x)
- **Variables**: Dynamic - any variable names you define
- **Constants**: Define custom constants (a=0.25, b=0.15, etc.)
- **Smart substitution**: Handles variable name length conflicts correctly

**Example:** `0.25*prey - 0.15*prey*predator` evaluates correctly

### 3. **ODE Solvers** - Two Numerical Methods
- **Euler method**: Simple, fast, less accurate
- **Runge-Kutta 4 (RK4)**: More complex, more accurate, recommended

Both can integrate any ODE system provided as string equations.

### 4. **Testing Framework** - SolverAccuracyTest.java
- Run tests with known exact solutions
- Measure error vs exact solution
- Compare Euler vs RK4 performance
- Convergence studies with multiple step sizes
- Automatic timing and reporting

**Example:**
```java
SolverAccuracyTest test = new SolverAccuracyTest("My Test");
boolean passed = test.runTest(
    myODE,           // ODEFunction with equations
    new double[]{1.0}, // initial state
    10.0,            // end time
    0.01,            // step size
    0.1,             // Euler tolerance
    1e-6,            // RK4 tolerance
    new double[]{expectedFinal}
);
test.printReport();
```

### 5. **Mock Unit Tests** - MockUnitTest.java
5 pre-configured test cases ready to use:

| Test | Type | Purpose |
|------|------|---------|
| Harmonic Oscillator | 2D | Verify basic oscillation (exact solution known) |
| Damped Oscillator | 2D | Test friction/decay modeling |
| Lotka-Volterra | 2D | Coupled nonlinear system, predator-prey cycles |
| Exponential Growth | 1D | Simple growth model (exact: e^rt) |
| Logistic Growth | 1D | Saturation/carrying capacity model |

Run tests with:
```bash
java -cp bin MockUnitTest
```

---

## How to Test When Client Sends Unit Tests

### Step 1: Understand the Test
- What variables? (x, y, population, etc.)
- What equations? (with how many constants?)
- Initial conditions?
- What results to verify?

### Step 2: Run in Visualizer
1. Launch Visualizer
2. Choose solver (RK4 recommended)
3. Number of variables from test
4. Enter variable names
5. Enter equations exactly as client specified
6. Enter constants with values
7. Set initial conditions
8. Set step size (typically 0.01, smaller if needed)
9. Set duration to reach end time from test
10. Click "Run Simulation"

### Step 3: Verify Results
- **Graph**: Does shape match expected behavior?
- **Table output**: Are values reasonable?
- **Point evaluation**: Query final state and intermediate points
- **Console log**: Shows all inputs and outputs

### Step 4: Report Results
Print or screenshot:
1. Final state values
2. Graph shape
3. Point evaluations at key times
4. Console output with error metrics
5. Solver used and parameters

---

## Quick Reference: Available Tests

### Run these to verify system works:

```bash
# Test 1: Parser validation
java -cp bin ParserTests
# Expected: ~40 tests, all PASS

# Test 2: Mock unit tests preview
java -cp bin MockUnitTest
# Shows 5 test cases with equations and expected behavior

# Test 3: Comprehensive accuracy suite
java -cp bin SampleAccuracyTestSuite
# Runs multiple test cases with convergence analysis
```

---

## Architecture Overview

```
User Input (GUI)
    ↓
Visualizer.java (Collects parameters)
    ↓
ExpressionParser.java (Parses string equations)
    ↓
ODESystemBuilder.java (Implements ODEFunction)
    ↓
EulerSolver.java or RungeKutta4.java (Integrates)
    ↓
Results (Graph + Table + Console Output)
```

### Key Classes:

1. **ODEFunction interface** - Your ODE must implement this
   ```java
   public interface ODEFunction {
       double[] compute(double[] stateRightNow);
   }
   ```
   Returns derivatives given current state.

2. **EulerSolver** - Single integration step
   ```java
   double[] EulerSolver.step(double[] state, double dt, ODEFunction ode)
   ```

3. **RungeKutta4** - Higher-order integration step
   ```java
   double[] RungeKutta4.step(double[] state, double dt, ODEFunction ode)
   ```

4. **ODESystemBuilder** - Bridges string equations to ODEFunction
   ```java
   new ODESystemBuilder(equations, names, constants);
   ```

---

## Known Limitations & Notes

1. **Accuracy depends on step size**
   - Smaller step = more accurate but slower
   - Start with 0.01, reduce to 0.001 if needed
   - RK4 typically allows larger steps than Euler

2. **Parser limitations**
   - Function arguments must be in parentheses: sin(x), not sinx
   - No implicit multiplication: 2x must be 2*x
   - Spaces are ignored: "a * x" → "a*x"

3. **Variable naming**
   - Can use any names: x, y, prey, predator, N, population, etc.
   - Must match exactly in equations: if variable is "prey", use "prey" not "p"

4. **State storage**
   - Every integration step is stored in memory
   - For very long simulations (>10000 steps), may use significant memory
   - Point evaluation searches through stored states

---

## Success Criteria

Your system passes when:

✅ Parses mathematical expressions correctly  
✅ Integrates ODE systems using Euler and RK4  
✅ Produces correct results for known test cases  
✅ Handles edge cases without crashing  
✅ Visualizes solutions in real-time  
✅ Evaluates solutions at arbitrary points  
✅ Matches client-provided expected values  

---

## File Structure

```
/Users/tarikuygul/Documents/gitlab/team_02/
├── src/
│   ├── Visualizer.java              ← Main GUI application
│   ├── EulerSolver.java             ← Euler method
│   ├── RungeKutta4.java             ← RK4 method
│   ├── ODEFunction.java             ← ODE interface
│   ├── ODESystemBuilder.java        ← Bridges equations to ODE
│   ├── ExpressionParser.java        ← Mathematical expression evaluator
│   ├── SolverAccuracyTest.java      ← Testing framework
│   ├── SampleAccuracyTestSuite.java ← Example tests
│   ├── MockUnitTest.java            ← 5 pre-configured tests
│   ├── ParserTests.java             ← Parser validation
│   └── (other files)
├── bin/                             ← Compiled .class files
├── TESTING_GUIDE.md                 ← How to test (this file)
└── README.md                        ← Project description
```

---

## Next Steps

1. **Verify system works:**
   - Run: `java -cp bin ParserTests` ✓
   - Launch Visualizer, run Harmonic Oscillator test ✓
   - Results should match expected values ✓

2. **Prepare for client tests:**
   - Keep system running and compiled
   - Have Visualizer ready to accept new equations
   - Know how to extract and report results

3. **When client sends test:**
   - Enter equations in Visualizer
   - Run simulation
   - Evaluate at key points
   - Compare to expected values
   - Report pass/fail with metrics

---

## Support

If something goes wrong:

**Parser error:** "Cannot parse 'x' - check your equation"
- → Make sure variable names match exactly
- → Add * for multiplication: 2x → 2*x
- → Use parentheses for functions: sin(x) not sinx

**Simulation diverges:** Results grow to infinity
- → Reduce step size (0.01 → 0.001)
- → Check equations for sign errors
- → Use RK4 instead of Euler

**No graph appears:** Button clicked but nothing happens
- → Check terminal for error messages
- → Make sure all equations filled in
- → Initial conditions must be numbers

**Can't find results:** Where are final values?
- → Check terminal console - full output printed there
- → Use "Evaluate at Specific Time" panel in GUI
- → Scroll down in Visualizer window

---

**🎉 System is complete and production-ready!**

Ready to receive and test client unit tests.
