# ODE Solver Testing Guide

## Quick Start: How to Test the Algorithm

Your system is now ready to validate ODE solvers using either:
1. **Visualizer GUI** - Interactive testing with graphs and point evaluation
2. **ParserTests** - Quick unit tests for the expression parser
3. **SampleAccuracyTestSuite** - Comprehensive convergence analysis

---

## Test 1: Harmonic Oscillator (Simplest)

### Why this test?
- **Exact solution known:** x(t) = cos(t), v(t) = -sin(t)
- **Easy to verify:** Compare numerical results to math
- **Good for debugging:** If this fails, solvers are wrong

### Setup in Visualizer:
```
Solver: RK4 (Runge-Kutta 4 - more accurate than Euler)
Number of variables: 2

Variable names: x, v
Equations: 
  - v
  - -x

Constants: (none, leave empty)

Initial conditions:
  - x(0) = 1.0
  - v(0) = 0.0

Step size: 0.01
Total time: 6.3

Plot: X axis = t, Y axis = x
```

### Expected Results:

**At specific times, evaluate with the evaluation panel:**

| Time | Expected x | Expected v | What it means |
|------|-----------|-----------|---------------|
| 0.0 | 1.0 | 0.0 | Start position |
| 1.5708 (π/2) | ~0.0 | ~-1.0 | Quarter cycle, max velocity |
| 3.1416 (π) | ~-1.0 | ~0.0 | Half cycle, opposite |
| 4.7124 (3π/2) | ~0.0 | ~1.0 | Three quarters, max velocity other direction |
| 6.2832 (2π) | ~1.0 | ~0.0 | Full cycle, back to start |

**Accuracy check:**
- ✅ **RK4**: Should match to within 1e-4 at 6.28
- ⚠️ **Euler**: May have larger errors (~1e-2), depending on step size

### If results don't match:
1. Check step size - make it smaller (0.001 instead of 0.01)
2. Try RK4 instead of Euler
3. Look at graph - should be a perfect circle/ellipse

---

## Test 2: Exponential Growth

### Why this test?
- **Simple differential equation:** dp/dt = r*p
- **Exact solution:** p(t) = p₀ * e^(rt)
- **Easy to calculate by hand**

### Setup in Visualizer:
```
Solver: RK4
Number of variables: 1

Variable names: p

Equations: 
  - r*p

Constants:
  r=0.1

Initial conditions:
  - p(0) = 1.0

Step size: 0.01
Total time: 30.0

Plot: X axis = t, Y axis = p
```

### Expected Results:

**At specific times:**

| Time | Expected p | Formula |
|------|-----------|---------|
| 0 | 1.0 | e^0 = 1 |
| 10 | 2.718 | e^1 ≈ 2.718 |
| 20 | 7.389 | e^2 ≈ 7.389 |
| 30 | 20.086 | e^3 ≈ 20.086 |

**Accuracy check:**
- Should match to within 0.1% for RK4

---

## Test 3: Damped Oscillator

### Why this test?
- **Combines oscillation and decay**
- **Tests friction modeling**
- **Should converge to zero over time**

### Setup in Visualizer:
```
Solver: RK4
Number of variables: 2

Variable names: x, v

Equations:
  - v
  - -0.1*v - x

Constants: (none)

Initial conditions:
  - x(0) = 1.0
  - v(0) = 0.0

Step size: 0.01
Total time: 30.0

Plot: X axis = t, Y axis = x
```

### Expected Results:

**Behavior:**
- Oscillates like harmonic oscillator
- Amplitude decreases exponentially
- By t=20, should be near zero
- By t=30, should be essentially zero

**At specific times:**

| Time | Expected behavior | x value |
|------|------------------|---------|
| 0 | Initial | x = 1.0 |
| 5 | First decay | 0 < x < 1.0 |
| 10 | More decay | x ≈ 0.1-0.3 |
| 20 | Nearly stopped | x < 0.01 |
| 30 | At rest | x ≈ 0.0 |

---

## Test 4: Lotka-Volterra (Predator-Prey)

### Why this test?
- **Models real biological systems**
- **Coupled nonlinear equations**
- **Exhibits periodic cycles**

### Setup in Visualizer:
```
Solver: RK4
Number of variables: 2

Variable names: prey, predator

Equations:
  - 0.25*prey - 0.15*prey*predator
  - 0.1*prey*predator - 0.1*predator

Constants: (none, values already in equations)

Initial conditions:
  - prey(0) = 20.0
  - predator(0) = 2.0

Step size: 0.1
Total time: 50.0

Plot: X axis = prey, Y axis = predator (phase space!)
```

### Expected Results:

**Graph shape:**
- Should form a closed loop (limit cycle)
- Periodic oscillations that never decay
- Phase space plot shows elliptical trajectory

**Pattern:**
1. Few prey, predators increase
2. Many predators kill prey
3. Prey decrease, predators starve
4. Few predators, prey recover
5. Cycle repeats

---

## Running the Complete Test Suite

### Via terminal (automated testing):
```bash
cd /Users/tarikuygul/Documents/gitlab/team_02

# Quick parser tests
javac -d bin src/*.java && java -cp bin ParserTests

# Comprehensive accuracy tests
javac -d bin src/*.java && java -cp bin SampleAccuracyTestSuite

# Show all mock tests available
javac -d bin src/*.java && java -cp bin MockUnitTest
```

### Via Visualizer (interactive testing):
1. Compile: `./build.sh` or run the javac command above
2. Launch: `java -cp bin Visualizer`
3. Set up a test case
4. Click "Run Simulation"
5. Use "Evaluate at Specific Time" panel
6. Compare results to expected values

---

## Interpreting Results

### Green flags (✅ Solver is working):
- RK4 results match exact solutions to 1e-4 or better
- Euler results have error < 0.1% for smooth ODEs
- Graphs show expected behavior (oscillation, decay, growth, etc.)
- Point evaluations cluster around expected values
- System handles edge cases without crashing

### Red flags (❌ Something is wrong):
- Results diverge from expected values
- Graphs show "blowup" (exponential growth when shouldn't)
- Negative population in growth model
- Oscillations grow instead of decay
- Step size doubling changes answer by > 10%

### If something is wrong:
1. **Check step size** - Make it smaller (0.001 instead of 0.01)
2. **Switch solvers** - Try RK4 if using Euler
3. **Verify equations** - Make sure signs are correct
4. **Check initial conditions** - Values must match equations
5. **Check constants** - Make sure a=0.25 not a=2.5

---

## Client Unit Test Checklist

When client sends a test, verify:
- [ ] Variables match problem description
- [ ] Equations match problem description  
- [ ] Initial conditions are correct
- [ ] Step size is small enough (typically 0.01 or smaller)
- [ ] Simulation runs without crashing
- [ ] Results are physically reasonable (no negative populations, etc.)
- [ ] Graph shape matches expected behavior
- [ ] Point evaluations within tolerance

---

## Tips for Best Results

1. **Use RK4 over Euler** - More accurate for same step size
2. **Smaller step size = Better accuracy** - Start with 0.01, go to 0.001 if needed
3. **Check your equations** - Typos are the #1 cause of bad results
4. **Phase space plots** - Plot x vs y (not time) for better insight
5. **Run multiple step sizes** - See convergence behavior
6. **Compare to exact solutions** - When available (harmonic oscillator, exponential, etc.)

---

## Example: Complete Test Session

```
GOAL: Verify Harmonic Oscillator solver

1. Launch Visualizer
2. Choose RK4 solver
3. Enter 2 variables: x, v
4. Equations: v and -x
5. No constants
6. Initial: x=1, v=0
7. Duration: 6.3, step: 0.01
8. Run simulation
9. Graph shows circle - ✅ GOOD
10. Evaluate at t=0: x=1.0, v=0.0 - ✅ MATCH
11. Evaluate at t=3.1416: x≈-1.0, v≈0.0 - ✅ MATCH
12. Evaluate at t=6.2832: x≈1.0, v≈0.0 - ✅ MATCH
13. Console shows final error metrics - CHECK THEM
14. CONCLUSION: Solver passes harmonic oscillator test
```

---

**Your system is ready for production testing! 🎉**
