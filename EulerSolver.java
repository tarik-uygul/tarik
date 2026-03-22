public class EulerSolver {

    // One Euler step
    public static double[] step(double[] x, double h, ODEFunction f) {
        double[] dx = f.compute(x);
        double[] next = new double[x.length];

        for (int i = 0; i < x.length; i++) {
            next[i] = x[i] + h * dx[i];
        }

        return next;
    }
}