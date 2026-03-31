import java.util.Map;

// connects the expression parser to the ODE solver interface
// you give it equation strings and variable names, it implements ODEFunction
// so it can be passed directly to EulerSolver.step() or RungeKutta4.step()
//
// example:
//   String[] names     = {"x", "y"};
//   String[] equations = {"a*x - b*x*y", "d*x*y - g*y"};
//   Map<String,Double> constants = Map.of("a",0.25, "b",0.15, "d",0.10, "g",0.10);
//   ODESystemBuilder ode = new ODESystemBuilder(equations, names, constants);
//   double[] next = EulerSolver.step(currentState, dt, ode);
public class ODESystemBuilder implements ODEFunction {

    private final String[] equations;
    private final String[] names;
    private final Map<String, Double> constants;
    private final ExpressionParser parser;

    public ODESystemBuilder(String[] equations, String[] names, Map<String, Double> constants) {
        this.equations = equations;
        this.names     = names;
        this.constants = constants;
        this.parser    = new ExpressionParser();
    }

    // called by EulerSolver and RungeKutta4 on every single step
    // returns the derivatives at the current state
    // e.g. for Lotka-Volterra this returns [dx/dt, dy/dt]
    @Override
    public double[] compute(double[] stateRightNow) {
        double[] derivatives = new double[equations.length];
        for (int i = 0; i < equations.length; i++) {
            derivatives[i] = parser.evaluate(equations[i], names, stateRightNow, constants);
        }
        return derivatives;
    }

    // getters so the GUI can display what system is currently loaded
    public String[] getEquations()            { return equations; }
    public String[] getNames()                { return names;     }
    public Map<String, Double> getConstants() { return constants; }
}
